package org.nh.pharmacy.service.impl;

import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jbpm.services.api.model.DeployedUnit;
import org.kie.api.task.model.Task;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.aop.producer.StockServiceAspect;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.InventoryAdjustment;
import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.StockAuditPlan;
import org.nh.pharmacy.domain.dto.InventoryAdjustmentDocumentLine;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.InventoryAdjustmentRepository;
import org.nh.pharmacy.repository.search.InventoryAdjustmentSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.common.util.BigDecimalUtil.multiply;
import static org.nh.pharmacy.domain.enumeration.AdjustmentType.NEGATIVE_ADJUSTMENT;
import static org.nh.pharmacy.domain.enumeration.AdjustmentType.POSITIVE_ADJUSTMENT;
import static org.nh.pharmacy.domain.enumeration.Context.Adjustment_Level_One_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.Context.Adjustment_Level_Two_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.Status.*;

/**
 * Service Implementation for managing InventoryAdjustment.
 */
@Service
@Transactional
public class InventoryAdjustmentServiceImpl implements InventoryAdjustmentService {

    private final Logger log = LoggerFactory.getLogger(InventoryAdjustmentServiceImpl.class);

    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;

    private final InventoryAdjustmentSearchRepository inventoryAdjustmentSearchRepository;

    private final StockService stockService;

    private final WorkflowService workflowService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final MessageChannel inventoryAdjustmentChannel;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final GroupService groupService;

    private final ApplicationProperties applicationProperties;

    private final RuleExecutorService ruleExecutorService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    public InventoryAdjustmentServiceImpl(InventoryAdjustmentRepository inventoryAdjustmentRepository, InventoryAdjustmentSearchRepository inventoryAdjustmentSearchRepository,
                                          StockService stockService, WorkflowService workflowService, SequenceGeneratorService sequenceGeneratorService,
                                          @Qualifier(Channels.STOCK_OUTPUT) MessageChannel inventoryAdjustmentChannel,
                                          ElasticsearchOperations elasticsearchTemplate, GroupService groupService, ApplicationProperties applicationProperties, RuleExecutorService ruleExecutorService, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
        this.inventoryAdjustmentSearchRepository = inventoryAdjustmentSearchRepository;
        this.stockService = stockService;
        this.workflowService = workflowService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.inventoryAdjustmentChannel = inventoryAdjustmentChannel;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.groupService = groupService;
        this.applicationProperties = applicationProperties;
        this.ruleExecutorService = ruleExecutorService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a inventoryAdjustment.
     *
     * @param inventoryAdjustment the entity to save
     * @return the persisted entity
     */
    @Override
    public InventoryAdjustment save(InventoryAdjustment inventoryAdjustment) {
        log.debug("Request to save InventoryAdjustment : {}", inventoryAdjustment);
        if (inventoryAdjustment.getId() == null) {
            inventoryAdjustment.setId(inventoryAdjustmentRepository.getId());
            inventoryAdjustment.getDocument().setId(inventoryAdjustment.getId());
            inventoryAdjustment.version(0);
            if (DRAFT.equals(inventoryAdjustment.getDocument().getStatus())) {
                inventoryAdjustment.setDocumentNumber("DRAFT-" + inventoryAdjustment.getId());
            }
        } else {
            inventoryAdjustmentRepository.updateLatest(inventoryAdjustment.getId());
            int version = inventoryAdjustment.getVersion() + 1;
            inventoryAdjustment.version(version);
        }
        inventoryAdjustment.setLatest(true);
        if (isNotEmpty(inventoryAdjustment.getDocument().getLines())) {
            for (InventoryAdjustmentDocumentLine inventoryAdjustmentLine : inventoryAdjustment.getDocument().getLines()) {
                if (inventoryAdjustmentLine.getId() == null) {
                    inventoryAdjustmentLine.setId(inventoryAdjustmentRepository.getId());
                }
                inventoryAdjustmentLine.setAdjustValue(multiply(inventoryAdjustmentLine.getAdjustQuantity().getValue(), inventoryAdjustmentLine.getCost()));
            }
        }
        inventoryAdjustment.getDocument().setModifiedDate(inventoryAdjustment.getDocument().getModifiedDate() == null ? LocalDateTime.now() : inventoryAdjustment.getDocument().getModifiedDate());
        saveValidation(inventoryAdjustment);
        InventoryAdjustment result = inventoryAdjustmentRepository.saveAndFlush(inventoryAdjustment);
        inventoryAdjustmentSearchRepository.save(result);
        return result;
    }

    @Override
    @Transactional
    @PublishStockTransaction
    public InventoryAdjustment save(InventoryAdjustment inventoryAdjustment, String action) throws Exception {
        log.debug("Request to save Inventory Adjustment {} with action {}", inventoryAdjustment, action);
        InventoryAdjustment result;
        switch (action) {
            case "SENDFORAPPROVAL":
                validateInventoryAdjustmentDocument(inventoryAdjustment);
                result = sendForApproval(inventoryAdjustment, action);
                break;
            case "APPROVED":
                log.debug("Executing workflow for transition : Approved and for L2 approval");
                //validateDocumentApprover(inventoryAdjustment);
                validateInventoryAdjustmentDocument(inventoryAdjustment);
                result = approveInventoryAdjustmentDocument(inventoryAdjustment);
                break;
            case "REJECTED":
                validateInventoryAdjustmentDocument(inventoryAdjustment);
                result = rejectInventoryAdjustmentDocument(inventoryAdjustment);
                break;
            default:
                if (inventoryAdjustment.getDocument().getStatus() == null) {
                    inventoryAdjustment.getDocument().setStatus(DRAFT);
                    validateDraft(inventoryAdjustment);
                    result = save(inventoryAdjustment);
                } else {
                    validateInventoryAdjustmentDocument(inventoryAdjustment);
                    if (inventoryAdjustment.getDocument().getStatus().equals(Status.WAITING_FOR_APPROVAL)) {
                        generateIdsIfRequiredForLines(inventoryAdjustment);
                        refreshReserveStock(inventoryAdjustment);
                    }
                    result = save(inventoryAdjustment);
                }
        }
        return result;
    }

    public InventoryAdjustment rejectInventoryAdjustmentDocument(InventoryAdjustment inventoryAdjustment) {
        log.debug("Request to reject Inventory Adjustment Documents {}", inventoryAdjustment);
        inventoryAdjustment.getDocument().setStatus(REJECTED);
        inventoryAdjustment = save(inventoryAdjustment);
        stockService.deleteReservedStock(inventoryAdjustment.getId(), inventoryAdjustment.getDocument().getDocumentType());
        return inventoryAdjustment;
    }

    @Override
    @Transactional
    public InventoryAdjustment approveInventoryAdjustmentDocument(InventoryAdjustment inventoryAdjustment) throws Exception {
        return doAdjustment(inventoryAdjustment);
    }

    public InventoryAdjustment doAdjustment(InventoryAdjustment inventoryAdjustment) throws Exception {
        refreshReserveStock(inventoryAdjustment);
        inventoryAdjustment.getDocument().setStatus(APPROVED);
        inventoryAdjustment = save(inventoryAdjustment);
        doPositiveAdjustment(inventoryAdjustment);
        doNegativeAdjustment(inventoryAdjustment);
        return inventoryAdjustment;
    }

    @Override
    @Transactional
    public InventoryAdjustment sendForApproval(InventoryAdjustment inventoryAdjustment, String action) throws Exception {
        log.debug("Request for Send for approval {}", inventoryAdjustment);
        inventoryAdjustment.getDocument().setStatus(WAITING_FOR_APPROVAL);
        inventoryAdjustment.documentNumber(sequenceGeneratorService.generateSequence(inventoryAdjustment.getDocument().getDocumentType().name(), "NH", inventoryAdjustment));
        InventoryAdjustment result = save(inventoryAdjustment);
        try {
            List<InventoryAdjustmentDocumentLine> documentLines = inventoryAdjustment.getDocument().getLines();
            for (InventoryAdjustmentDocumentLine line : documentLines) {
                if (NEGATIVE_ADJUSTMENT.equals(line.getAdjustmentType())) {
                    stockService.reserveStock(line.getStockId(), line.getItem().getId(), line.getBatchNumber(), inventoryAdjustment.getDocument().getStore().getId(),
                        line.getAdjustQuantity().getValue(), inventoryAdjustment.getId(), inventoryAdjustment.getDocument().getDocumentType(),
                        inventoryAdjustment.getDocumentNumber(), line.getId(), inventoryAdjustment.getDocument().getApprovedDate() != null ?
                            inventoryAdjustment.getDocument().getApprovedDate() : inventoryAdjustment.getDocument().getDocumentDate(),inventoryAdjustment.getDocument().getCreatedBy().getId());
                }
            }
        } catch (Exception ex) {
            stockService.deleteReservedStock(inventoryAdjustment.getId(), inventoryAdjustment.getDocument().getDocumentType());
            throw ex;
        }
        //Get workflow configurations
        Map<String, Object> configurations = retrieveWorkflowConfigurations(result, true);
        //Start workflow if workflow enabled
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(result, action, configurations);
        }
        return result;
    }

    /**
     * Get all the inventoryAdjustments.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAdjustment> findAll(Pageable pageable) {
        log.debug("Request to get all InventoryAdjustments");
        Page<InventoryAdjustment> result = inventoryAdjustmentRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one inventoryAdjustment by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public InventoryAdjustment findOne(Long id) {
        log.debug("Request to get InventoryAdjustment : {}", id);
        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentRepository.findOne(id);
        return inventoryAdjustment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public InventoryAdjustment findDetachedOne(Long id) {
        log.debug("Request to get InventoryAdjustment : {}", id);
        return inventoryAdjustmentRepository.findOne(id);
    }

    /**
     * Delete the  inventoryAdjustment by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete InventoryAdjustment : {}", id);
        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentSearchRepository.findByAdjustmentId(id);
        deleteValidation(inventoryAdjustment);
        inventoryAdjustmentRepository.delete(id);
        inventoryAdjustmentSearchRepository.deleteById(id);
    }

    /**
     * Search for the inventoryAdjustment corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAdjustment> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of InventoryAdjustments for query {}", query);
        return inventoryAdjustmentSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.store.name").field("document.status")
            .defaultOperator(Operator.AND), pageable);
    }

    /**
     * @param query
     * @param pageable
     * @param includeFields
     * @param excludeFields
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAdjustment> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of InventoryAdjustments for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.store.name").field("document.status")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return ElasticSearchUtil.getPageRecords(searchQuery, InventoryAdjustment.class, elasticsearchTemplate, "inventoryadjustment");
    }

    private InventoryAdjustment doPositiveAdjustment(InventoryAdjustment inventoryAdjustment) {
        log.debug("Request to process Positive Adjustment lines : {}", inventoryAdjustment.getDocumentNumber());
        List<InventoryAdjustmentDocumentLine> lines = inventoryAdjustment.getDocument().getLines();
        List<StockEntry> stockEntries = new ArrayList<>();
        for (InventoryAdjustmentDocumentLine line : lines) {
            if (POSITIVE_ADJUSTMENT.equals(line.getAdjustmentType())) {
                StockEntry stockEntry = setStockEntryBase(inventoryAdjustment);
                stockEntry = setStockEntryLine(line, stockEntry);
                stockEntry.setUserId(inventoryAdjustment.getDocument().getCreatedBy().getId());
                stockEntries.add(stockEntry);
            }
        }
        if (!stockEntries.isEmpty())
            stockService.stockIn(stockEntries);
        return inventoryAdjustment;
    }

    private StockEntry setStockEntryLine(InventoryAdjustmentDocumentLine inventoryAdjustmentDocumentLine, StockEntry stockEntry) {
        stockEntry.setAvailableQuantity(inventoryAdjustmentDocumentLine.getStockQuantity().getValue());
        stockEntry.setBarCode(inventoryAdjustmentDocumentLine.getBarcode());
        stockEntry.setBatchNo(inventoryAdjustmentDocumentLine.getBatchNumber());
        stockEntry.setConsignment(inventoryAdjustmentDocumentLine.getConsignment());
        stockEntry.setCost(inventoryAdjustmentDocumentLine.getCost());
        stockEntry.setExpiryDate(inventoryAdjustmentDocumentLine.getExpiryDate());
        stockEntry.setItemId(inventoryAdjustmentDocumentLine.getItem().getId());
        stockEntry.setLocatorId(inventoryAdjustmentDocumentLine.getLocator().getId());
        stockEntry.setMrp(inventoryAdjustmentDocumentLine.getMrp());
        stockEntry.setOwner(inventoryAdjustmentDocumentLine.getOwner());
        stockEntry.setQuantity(inventoryAdjustmentDocumentLine.getAdjustQuantity().getValue());
        stockEntry.setSku(inventoryAdjustmentDocumentLine.getSku());
        stockEntry.setTransactionLineId(inventoryAdjustmentDocumentLine.getId());
        stockEntry.setUomId(inventoryAdjustmentDocumentLine.getAdjustQuantity().getUom().getId());
        stockEntry.setStockId(inventoryAdjustmentDocumentLine.getStockId());
        return stockEntry;
    }

    private StockEntry setStockEntryBase(InventoryAdjustment inventoryAdjustment) {
        StockEntry stockEntry = new StockEntry();
        stockEntry.setStoreId(inventoryAdjustment.getDocument().getStore().getId());
        stockEntry.setTransactionDate(inventoryAdjustment.getDocument().getApprovedDate() != null ?
            inventoryAdjustment.getDocument().getApprovedDate() : inventoryAdjustment.getDocument().getDocumentDate());
        stockEntry.setTransactionId(inventoryAdjustment.getId());
        stockEntry.setTransactionNumber(inventoryAdjustment.getDocumentNumber());
        stockEntry.setTransactionRefNo(inventoryAdjustment.getDocument().getReferenceDocumentNumber());
        stockEntry.setTransactionType(inventoryAdjustment.getDocument().getDocumentType());
        stockEntry.setUnitId(inventoryAdjustment.getDocument().getUnit().getId());
        return stockEntry;
    }

    private void doNegativeAdjustment(InventoryAdjustment inventoryAdjustment) throws Exception {
        log.debug("Request to process Negative Adjustment lines : {}", inventoryAdjustment.getDocumentNumber());
        produce(inventoryAdjustment);
    }

    public InventoryAdjustment doStockCorrection(InventoryAdjustment inventoryAdjustment) throws Exception {
        log.debug("Request to process Stock Correction : {}", inventoryAdjustment.getDocumentNumber());
        save(inventoryAdjustment);
        save(inventoryAdjustment, "SENDFORAPPROVAL");
        InventoryAdjustment result = save(inventoryAdjustment, "APPROVED");
        return result;

    }

    private void refreshReserveStock(InventoryAdjustment inventoryAdjustment) throws Exception {
        List<ReserveStock> reserveStocks = stockService.findReserveStockByTransactionNo(inventoryAdjustment.getDocumentNumber());

        List<Long> deleteReserveStock = new ArrayList<>();
        List<InventoryAdjustmentDocumentLine> addReserveStock = new ArrayList<>();
        List<InventoryAdjustmentDocumentLine> refreshReserveStock = new ArrayList<>();
        List<InventoryAdjustmentDocumentLine> tempDocumentLineList = new ArrayList<>(inventoryAdjustment.getDocument().getLines());
        List<ReserveStock> tempReserveStockList = new ArrayList<>(reserveStocks);

        reserveStocks.forEach(reserveStock -> inventoryAdjustment.getDocument().getLines().stream().filter(line -> reserveStock.getTransactionLineId().equals(line.getId())).forEachOrdered(line -> {
            tempReserveStockList.remove(reserveStock);
            tempDocumentLineList.remove(line);
            if (NEGATIVE_ADJUSTMENT.equals(line.getAdjustmentType())) {
                refreshReserveStock.add(line);
            } else {
                if (POSITIVE_ADJUSTMENT.equals(line.getAdjustmentType())) {
                    deleteReserveStock.add(reserveStock.getId());
                }
            }
        }));

        tempReserveStockList.stream().map(ReserveStock::getId).forEachOrdered(deleteReserveStock::add);
        tempDocumentLineList.stream().filter(line -> NEGATIVE_ADJUSTMENT.equals(line.getAdjustmentType())).forEachOrdered(line -> {
            if (line.getId() == null) {
                line.setId(inventoryAdjustmentRepository.getId());
            }
            addReserveStock.add(line);
        });

        reserveStock(inventoryAdjustment, addReserveStock);
        deleteAndReserveStock(inventoryAdjustment, refreshReserveStock);
        stockService.deleteReservedStocks(deleteReserveStock);
    }


    private void reserveStock(InventoryAdjustment inventoryAdjustment, List<InventoryAdjustmentDocumentLine> lines) throws Exception {
        for (InventoryAdjustmentDocumentLine line : lines) {
            try {
                stockService.reserveStock(line.getStockId(), line.getItem().getId(), line.getBatchNumber(), inventoryAdjustment.getDocument().getStore().getId(),
                    line.getAdjustQuantity().getValue(), inventoryAdjustment.getId(), inventoryAdjustment.getDocument().getDocumentType(),
                    inventoryAdjustment.getDocumentNumber(), line.getId(),
                    inventoryAdjustment.getDocument().getApprovedDate() == null ? inventoryAdjustment.getDocument().getDocumentDate() : inventoryAdjustment.getDocument().getApprovedDate(),inventoryAdjustment.getDocument().getCreatedBy().getId());
            } catch (StockException e) {
                e.setItemName(line.getItem().getName());
                e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                throw e;
            }
        }
    }

    private void deleteAndReserveStock(InventoryAdjustment inventoryAdjustment, List<InventoryAdjustmentDocumentLine> lines) throws Exception {
        for (InventoryAdjustmentDocumentLine line : lines) {
            try {
                stockService.deleteAndReserveStock(line.getStockId(), line.getItem().getId(), line.getBatchNumber(), inventoryAdjustment.getDocument().getStore().getId(),
                    line.getAdjustQuantity().getValue(), inventoryAdjustment.getId(), inventoryAdjustment.getDocument().getDocumentType(),
                    inventoryAdjustment.getDocumentNumber(), line.getId(), inventoryAdjustment.getDocument().getApprovedDate() == null ? inventoryAdjustment.getDocument().getDocumentDate() : inventoryAdjustment.getDocument().getApprovedDate(),inventoryAdjustment.getDocument().getCreatedBy().getId());
            } catch (StockException e) {
                e.setItemName(line.getItem().getName());
                e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                throw e;
            }
        }
    }

    /**
     * Get all negative inventory adjustments and publish
     */
    @Override
    public void produce(InventoryAdjustment inventoryAdjustment) {
        if (inventoryAdjustment.getDocument().getLines().stream().anyMatch(line -> NEGATIVE_ADJUSTMENT.equals(line.getAdjustmentType()))) {
            log.debug("Inventory Adjustment Id {} has been published", inventoryAdjustment.getId());
            Map<String, Object> inventoryAdjustmentMap = new HashedMap();
            inventoryAdjustmentMap.put("Id", inventoryAdjustment.getId());
            inventoryAdjustmentMap.put("DocumentNo", inventoryAdjustment.getDocumentNumber());
            //inventoryAdjustmentChannel.send(MessageBuilder.withPayload(inventoryAdjustmentMap).build());
            StockServiceAspect.threadLocal.get().put(Channels.STOCK_OUTPUT, inventoryAdjustmentMap);
        }
    }

    private void validateDraft(InventoryAdjustment inventoryAdjustment) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (inventoryAdjustment.getDocument().getStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STORE));
        }
        if (inventoryAdjustment.getDocument().getUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UNIT));
        }
        if (inventoryAdjustment.getDocument().getDocumentType() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_DOCUMENT_TYPE));
        }
        if (inventoryAdjustment.getDocument().getCreatedBy() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_NAME));
        }
        if (inventoryAdjustment.getDocument().getCreatedDate() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CREATED_DATE));
        }
        if (inventoryAdjustment.getDocument().getStatus() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STATUS));
        }
        if (inventoryAdjustment.getDocument().getLines() == null
            || inventoryAdjustment.getDocument().getLines().isEmpty()) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ADJUSTMENT_DOCUMENT_LINES));
        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    private void validateInventoryAdjustmentDocument(InventoryAdjustment inventoryAdjustment) throws FieldValidationException {
        int adjustQuantityCounter = 0;
        validateDraft(inventoryAdjustment);
        List<ErrorMessage> errorMessages = new ArrayList<>();
        for (InventoryAdjustmentDocumentLine line : inventoryAdjustment.getDocument().getLines()) {
            if (line.getItem() == null) {
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ITEM));
                continue;
            }
            if (line.getAdjustQuantity() != null) {
                if (line.getAdjustQuantity().getUom() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UOM, source));
                }
                if (line.getAdjustQuantity().getValue() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY, source));
                }
                if (line.getAdjustQuantity().getValue() == 0) adjustQuantityCounter++;
            } else {
                Map<String, Object> source = new HashMap<String, Object>();
                source.put("itemName", line.getItem().getName());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY_AND_UOM, source));
            }
        }
        if (adjustQuantityCounter > 0) {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", inventoryAdjustment.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ALL_ADJUST_QUANTITY_MUST_BE_POSITIVE));
        }
        if (!errorMessages.isEmpty()) {
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }

    /**
     * Search for the Inventory Adjustment to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    @Override
    public Map<String, Long> getStatusCount(String query) {
        Map<String, Long> statusCount = new HashMap<>();
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.store.name").field("document.status")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw"))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "inventoryadjustment");
        Terms terms = aggregations.get("status_count");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Inventory Adjustment");
        inventoryAdjustmentSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on inventoryAdjustment latest=true");
        List<InventoryAdjustment> data = inventoryAdjustmentRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            inventoryAdjustmentSearchRepository.saveAll(data);
        }
    }

    /**
     * Start workflow
     *
     * @param inventoryAdjustment
     * @param action
     * @Param configurations
     */
    public void startWorkflow(InventoryAdjustment inventoryAdjustment, String action, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", inventoryAdjustment.getId());
            content.put("document_type", inventoryAdjustment.getDocument().getDocumentType());
            content.put("level_one_approval_group", configurations.get("approvalGroupIdsLevelOne"));
            content.put("level_two_approval_group", configurations.get("approvalGroupIdsLevelTwo"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", new StringBuilder((String) configurations.get("approvalGroupIdsLevelOne")).append(",").append(configurations.get("approvalGroupIdsLevelTwo")));
            params.put("level_one_approval_group", configurations.get("approvalGroupIdsLevelOne"));
            params.put("level_two_approval_group", configurations.get("approvalGroupIdsLevelTwo"));
            params.put("document_number", inventoryAdjustment.getDocumentNumber());
            params.put("document_date", inventoryAdjustment.getDocument().getDocumentDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("store", inventoryAdjustment.getDocument().getStore().getName());
            params.put("unit_id", String.valueOf(inventoryAdjustment.getDocument().getUnit().getId()));
            params.put("content", content);
            //Start the process
            Long processInstanceId = workflowService.startProcess(deployedUnit, (String) configurations.get("processId"), params);
            //Set result
            results.put("action_out", action);
            //Complete the document creation task
            workflowService.completeUserTaskForProcessInstance(processInstanceId, userId, results);
        }
    }

    /**
     * Execute workflow
     *
     * @param inventoryAdjustment the entity to save
     * @param transition          to be performed
     * @param taskId              task Id
     * @return inventoryAdjustment object
     * @throws Exception
     */
    @Override
    @Transactional
    @PublishStockTransaction
    public InventoryAdjustment executeWorkflow(InventoryAdjustment inventoryAdjustment, String transition, Long taskId) throws Exception {
        InventoryAdjustment result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Send for Approval":
                action = "SENDFORAPPROVAL";
                break;
            case "Approved":
                action = "APPROVED";
                break;
            case "Rejected":
                action = "REJECTED";
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }
        //Save inventory adjustment
        if ("Approved".equals(transition) && "AdjustmentApprovalLevelOne".equals(workflowService.getUserTaskService().getTask(taskId).getFormName())) {
            log.debug("Executing workflow for transition : Approved and for L1 approval");
            //validateDocumentApprover(inventoryAdjustment);
            validateInventoryAdjustmentDocument(inventoryAdjustment);
            refreshReserveStock(inventoryAdjustment);
            inventoryAdjustment.getDocument().setStatus(WAITING_FOR_APPROVAL);
            inventoryAdjustment.getDocument().setApprovedDate(null);
            inventoryAdjustment.getDocument().setApprovedBy(null);
            result = save(inventoryAdjustment);
        } else {
            result = save(inventoryAdjustment, action);
        }
        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("action_out", action);
        workflowService.completeUserTask(taskId, userId, results);
        return result;
    }

    public void validateDocumentApprover(InventoryAdjustment inventoryAdjustment) throws BusinessRuleViolationException {
        log.debug("Request to validate inventoryAdjustment document creator and approver are not same");
        ruleExecutorService.executeByGroup(inventoryAdjustment, "inventory_adjustment_document_approver_validation");
    }

    /**
     * Get task constraints
     *
     * @param documentNumber
     * @param userId
     * @param taskId
     * @return taskId, constraints
     */
    @Override
    public Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId) {
        Map<String, Object> configurations, taskDetails;
        List<String> workflowGroupIdList;
        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(inventoryAdjustment, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) : workflowService.getTaskConstraintsByProcessIdByVariable(processId, "document_number", documentNumber, userId);
            if ((Boolean) taskDetails.get("isGroupTask")) {
                Task taskDesc = workflowService.getUserTaskService().getTask((Long) taskDetails.get("taskId"));
                workflowGroupIdList = Splitter.on(",").splitToList(workflowService.getVariableValue(taskDesc.getTaskData().getProcessInstanceId(), "AdjustmentApprovalLevelOne".equals(taskDesc.getFormName()) ? "level_one_approval_group" : "level_two_approval_group"));
                List<String> userGroupIdList = groupService.groupsForUser(userId);
                if (disjoint(workflowGroupIdList, userGroupIdList)) {
                    taskDetails.put("taskId", null);
                    taskDetails.put("isGroupTask", false);
                }
            }
            taskDetails.remove("groupIdList");
        } else {
            taskDetails = new HashMap<String, Object>() {{
                put("taskId", null);
                put("constraints", new HashSet<String>());
                put("isGroupTask", false);
            }};
        }
        return taskDetails;
    }

    /**
     * Get workflow configurations
     *
     * @param inventoryAdjustment
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(InventoryAdjustment inventoryAdjustment, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_inventoryadjustment_enable_workflow", inventoryAdjustment.getDocument().getStore().getId(), inventoryAdjustment.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_inventoryadjustment_workflow_definition", inventoryAdjustment.getDocument().getStore().getId(), inventoryAdjustment.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("approvalGroupIdsLevelOne", getGroupData(Adjustment_Level_One_Approval_Committee, inventoryAdjustment.getDocument().getUnit().getId()));
            configurations.put("approvalGroupIdsLevelTwo", getGroupData(Adjustment_Level_Two_Approval_Committee, inventoryAdjustment.getDocument().getUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", inventoryAdjustment.getDocument().getStore().getId(), inventoryAdjustment.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    /**
     * Do index for Inventory Adjustment
     *
     * @param inventoryAdjustment
     */
    @Override
    public void index(InventoryAdjustment inventoryAdjustment) {
        inventoryAdjustmentSearchRepository.save(inventoryAdjustment);
    }

    /**
     * Reindex adjustment elasticsearch for given id
     *
     * @param id
     */
    @Override
    public void reIndex(Long id) {
        if (id != null) {
            InventoryAdjustment inventoryAdjustment = inventoryAdjustmentRepository.findOne(id);
            if (inventoryAdjustment == null) {
                if (inventoryAdjustmentSearchRepository.existsById(id)) {
                    inventoryAdjustmentSearchRepository.deleteById(id);
                }

            } else {
                inventoryAdjustmentSearchRepository.save(inventoryAdjustment);
            }
        }

    }

    /**
     * Reverse adjustment elasticsearch data for audit
     *
     * @param auditDocumentNumber
     */
    @Override
    public void reverseAdjustmentDataForAudit(String auditDocumentNumber) {
        if (auditDocumentNumber != null) {
            QueryBuilder queryBuilder = boolQuery().must(queryStringQuery(new StringBuilder("document.referenceDocumentNumber.raw:").append(auditDocumentNumber).toString()));
            Iterator adjustmentIterator = inventoryAdjustmentSearchRepository.search(queryBuilder).iterator();
            if (adjustmentIterator.hasNext()) {
                InventoryAdjustment adjustment = (InventoryAdjustment) adjustmentIterator.next();
                inventoryAdjustmentSearchRepository.deleteById(adjustment.getId());
                stockService.deleteReservedStock(adjustment.getId(), adjustment.getDocument().getDocumentType());
            }
        }
    }

    @Override
    public Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) {
        log.debug("Get all documents which is related to given stockIssue document:-" + documentNumber);

        Set<RelatedDocument> relatedDocumentList = new LinkedHashSet<>();
        Map<String, Set<RelatedDocument>> finalList = new LinkedHashMap<>();
        finalList.put(TransactionType.Stock_Audit_Plan.getTransactionTypeDisplay(), new LinkedHashSet<>());
        Iterable<InventoryAdjustment> inventoryAdjustments = inventoryAdjustmentSearchRepository.search(queryStringQuery("documentNumber.raw:" + documentNumber));
        inventoryAdjustments.forEach(inventoryAdjustment -> {
            Query queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(queryStringQuery("documentNumber.raw:" + inventoryAdjustment.getDocument().getReferenceDocumentNumber())).build();
            List<StockAudit> stockAudits = ElasticSearchUtil.getRecords(queryBuilder, StockAudit.class, elasticsearchTemplate, "stockaudit");
            stockAudits.forEach(stockAudit -> {
                RelatedDocument relDoc = new RelatedDocument();
                relDoc.setId(stockAudit.getId().toString());
                relDoc.setDocumentType(TransactionType.Stock_Audit);
                relDoc.setDocumentNumber(stockAudit.getDocumentNumber());
                relDoc.setStatus(valueOf(stockAudit.getDocument().getStatus().toString()));
                relDoc.setCreatedDate(LocalDateTime.parse(stockAudit.getDocument().getCreatedDate().toString()));
                relatedDocumentList.add(relDoc);
                finalList.put(TransactionType.Stock_Audit.getTransactionTypeDisplay(), relatedDocumentList);
                if (stockAudit.getDocument().getReferenceDocumentNumber() != null) {
                    populateAuditPlanRelatedDocument(stockAudit.getDocument().getReferenceDocumentNumber(), finalList);
                }
            });
        });
        return finalList;
    }

    private void populateAuditPlanRelatedDocument(String documentNumber, Map<String, Set<RelatedDocument>> finalList) {
        Set<RelatedDocument> relatedDocumentList = finalList.get(TransactionType.Stock_Audit_Plan.getTransactionTypeDisplay());
        Query queryBuilder = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("documentNumber:" + documentNumber)).build();
        List<StockAuditPlan> stockAuditPlans = ElasticSearchUtil.getRecords(queryBuilder, StockAuditPlan.class, elasticsearchTemplate, "stockauditplan");
        stockAuditPlans.forEach(stockAuditPlan -> {
            RelatedDocument relDoc = new RelatedDocument();
            relDoc.setId(stockAuditPlan.getId().toString());
            relDoc.setDocumentType(TransactionType.Stock_Audit_Plan);
            relDoc.setDocumentNumber(stockAuditPlan.getDocumentNumber());
            relDoc.setStatus(valueOf(stockAuditPlan.getDocument().getStatus().toString()));
            relDoc.setCreatedDate(LocalDateTime.parse(stockAuditPlan.getDocument().getCreatedDate().toString()));
            relatedDocumentList.add(relDoc);
            finalList.put(TransactionType.Stock_Audit_Plan.getTransactionTypeDisplay(), relatedDocumentList);
        });
    }

    /**
     * Get adjustment list in csv file
     *
     * @param file
     * @param query
     * @param pageable
     * @throws IOException
     */
    @Override
    public void generateInventoryAdjustmentList(File file, String query, Pageable pageable) throws IOException {
        Iterator<InventoryAdjustment> inventoryAdjustmentIterator = search(query, pageable).iterator();
        FileWriter inventoryAdjustmentFileWriter = new FileWriter(file);
        String status = Arrays.stream(query.split(" ")).filter(param -> "document.status.raw".equals(param.split(":")[0])).findFirst().map(param -> param.split(":")[1]).orElse(null);
        boolean statusFlag = DRAFT.name().equals(status) || WAITING_FOR_APPROVAL.name().equals(status);
        final String[] adjustmentFileHeader = statusFlag ? new String[]{"Adjustment No", "Creation Date", "Adjustment Store", "Store Contact", "Status"} : new String[]{"Adjustment No", "Approval Date", "Adjustment Store", "Store Contact", "Status"};
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(inventoryAdjustmentFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(adjustmentFileHeader);
            DateTimeFormatter formatter = ofPattern("yyyy/MM/dd HH:mm");
            while (inventoryAdjustmentIterator.hasNext()) {
                InventoryAdjustment inventoryAdjustment = inventoryAdjustmentIterator.next();
                List inventoryAdjustmentData = new ArrayList();
                inventoryAdjustmentData.add(inventoryAdjustment.getDocumentNumber());
                inventoryAdjustmentData.add(statusFlag ? (inventoryAdjustment.getDocument().getCreatedDate() != null ? inventoryAdjustment.getDocument().getCreatedDate().format(formatter) : null) : inventoryAdjustment.getDocument().getApprovedDate() != null ? inventoryAdjustment.getDocument().getApprovedDate().format(formatter) : null);
                inventoryAdjustmentData.add(inventoryAdjustment.getDocument().getStore().getName());
                inventoryAdjustmentData.add(inventoryAdjustment.getDocument().getStoreContact() != null ? inventoryAdjustment.getDocument().getStoreContact().getDisplayName() : " ");
                inventoryAdjustmentData.add(inventoryAdjustment.getDocument().getStatus().getStatusDisplay());
                csvFilePrinter.printRecord(inventoryAdjustmentData);
            }
        }
    }

    private void saveValidation(InventoryAdjustment inventoryAdjustmentRef) {
        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentSearchRepository.findByAdjustmentDocumentNumber(inventoryAdjustmentRef.getDocumentNumber());
        if (null == inventoryAdjustment) {
            inventoryAdjustment = inventoryAdjustmentRef;
        }
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(inventoryAdjustment.getDocument().getUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with logged in unit");
        }
    }

    private void deleteValidation(InventoryAdjustment inventoryAdjustment) {
        log.debug("validate before delete InventoryAdjustment : {}", inventoryAdjustment);
        if (inventoryAdjustment.getDocument().getStatus() != Status.DRAFT) {
            throw new CustomParameterizedException("10088", "Can't delete document ,Only Draft Status document can be deleted");
        }
    }

    private void generateIdsIfRequiredForLines(InventoryAdjustment inventoryAdjustment) {
        if (CollectionUtils.isNotEmpty(inventoryAdjustment.getDocument().getLines())) {
            inventoryAdjustment.getDocument().getLines().stream()
                .filter(inventoryAdjustmentLine -> inventoryAdjustmentLine.getId() == null)
                .forEach(inventoryAdjustmentLine -> inventoryAdjustmentLine.setId(inventoryAdjustmentRepository.getId()));
        }
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<InventoryAdjustment> search = this.search("documentNumber.raw:" + documentNumber, PageRequest.of(0, 1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        InventoryAdjustment inventoryAdjustment = search.iterator().next();
        Map<String, Object> configurations = retrieveWorkflowConfigurations(inventoryAdjustment, true);
        //Start workflow if workflow enabled
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(inventoryAdjustment, "SENDFORAPPROVAL", configurations);
        }
    }

    private String getGroupData(Context context, Long unitId) {
        String cacheKey = "PHR: context:"+context.name()+" AND active:true AND partOf.id:"+unitId+" !_exists_:partOf";
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return pharmacyRedisCacheService.getCommaSeparatedGroupCodes(context,unitId,elasticsearchTemplate,cacheKey);
        }else {
            return ConfigurationUtil.getCommaSeparatedGroupCodes(context, unitId, elasticsearchTemplate);
        }
    }
}
