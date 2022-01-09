package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jbpm.services.api.model.DeployedUnit;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.ItemBatchInfo;
import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.StockCorrection;
import org.nh.pharmacy.domain.dto.CorrectionDocumentLine;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.StockCorrectionRepository;
import org.nh.pharmacy.repository.search.StockCorrectionSearchRepository;
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
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.domain.enumeration.Context.Correction_Approval_Committee;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;

/**
 * Service Implementation for managing StockCorrection.
 */
@Service
@Transactional
public class StockCorrectionServiceImpl implements StockCorrectionService {

    private final Logger log = LoggerFactory.getLogger(StockCorrectionServiceImpl.class);

    private final StockCorrectionRepository stockCorrectionRepository;

    private final StockCorrectionSearchRepository stockCorrectionSearchRepository;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final StockService stockService;

    private final WorkflowService workflowService;

    private final MessageChannel stockCorrectionChannel;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final GroupService groupService;

    private final ApplicationProperties applicationProperties;

    private final ItemBatchInfoService itemBatchInfoService;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    private final RuleExecutorService ruleExecutorService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    public StockCorrectionServiceImpl(StockCorrectionRepository stockCorrectionRepository, StockCorrectionSearchRepository stockCorrectionSearchRepository,
                                      SequenceGeneratorService sequenceGeneratorService, StockService stockService, WorkflowService workflowService, @Qualifier(Channels.STOCK_MOVE_OUTPUT) MessageChannel stockCorrectionChannel,
                                      ElasticsearchOperations elasticsearchTemplate, GroupService groupService, ApplicationProperties applicationProperties, ItemBatchInfoService itemBatchInfoService, RuleExecutorService ruleExecutorService, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.stockCorrectionRepository = stockCorrectionRepository;
        this.stockCorrectionSearchRepository = stockCorrectionSearchRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.stockService = stockService;
        this.workflowService = workflowService;
        this.stockCorrectionChannel = stockCorrectionChannel;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.groupService = groupService;
        this.applicationProperties = applicationProperties;
        this.itemBatchInfoService = itemBatchInfoService;
        this.ruleExecutorService = ruleExecutorService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a stockCorrection.
     *
     * @param stockCorrection the entity to save
     * @return the persisted entity
     */
    @Override
    public StockCorrection save(StockCorrection stockCorrection) {
        log.debug("Request to save StockCorrection : {}", stockCorrection);
        if (stockCorrection.getId() == null) {
            stockCorrection.setId(stockCorrectionRepository.getId());
            stockCorrection.getDocument().setId(stockCorrection.getId());
            stockCorrection.setVersion(0);
            if (Status.DRAFT.equals(stockCorrection.getDocument().getStatus())) {
                stockCorrection.setDocumentNumber("DRAFT-" + stockCorrection.getId());
            }
        } else {
            stockCorrectionRepository.updateLatest(stockCorrection.getId());
            stockCorrection.version(stockCorrection.getVersion() + 1);
        }
        stockCorrection.setLatest(true);
        if (CollectionUtils.isNotEmpty(stockCorrection.getDocument().getLines())) {
            stockCorrection.getDocument().getLines().stream()
                .filter(stockCorrectionLine -> stockCorrectionLine.getId() == null)
                .forEach(stockCorrectionLine -> stockCorrectionLine.setId(stockCorrectionRepository.getId()));

        }
        saveValidation(stockCorrection);
        StockCorrection result = stockCorrectionRepository.save(stockCorrection);
        //stockCorrectionSearchRepository.save(result);
        return result;
    }

    /**
     * Save StockCorrection based on action
     *
     * @param stockCorrection
     * @param action
     * @return
     * @throws Exception
     */
    public StockCorrection save(StockCorrection stockCorrection, String action, Boolean validationRequired) throws Exception {
        log.debug("Request to save StockCorrection {} with action {}", stockCorrection, action);
        StockCorrection result = null;
        switch (action) {
            case "SENDFORAPPROVAL":
                validateStockCorrectionDocument(stockCorrection);
                if (validationRequired != null && validationRequired) {
                    verifyStockReserve(stockCorrection);
                }
                result = sendForApproval(stockCorrection, action);
                break;
            case "APPROVED":
                validateStockCorrectionDocument(stockCorrection);
                if (validationRequired != null && validationRequired) {
                    verifyStockReserve(stockCorrection);
                }
                result = approveStockCorrectionDocument(stockCorrection);
                createItemBatchInfo(result);
                break;
            case "REJECTED":
                //  validateStockCorrectionDocument(stockCorrection);
                result = rejectStockCorrectionDocument(stockCorrection);
                break;
            default:
                if (stockCorrection.getDocument().getStatus() == null) {
                    stockCorrection.getDocument().setStatus(Status.DRAFT);
                    validateDraft(stockCorrection);
                    result = save(stockCorrection);
                } else {
                    validateStockCorrectionDocument(stockCorrection);
                    if (stockCorrection.getDocument().getStatus().equals(Status.WAITING_FOR_APPROVAL)) {
                        refreshReserveStock(stockCorrection);
                    }
                    result = save(stockCorrection);
                }
        }
        index(result);
        return result;
    }

    /**
     * make a itemBatchInfo entry in case of batch correction.
     * @param stockCorrection
     */
    private void createItemBatchInfo(StockCorrection stockCorrection) {
        for (CorrectionDocumentLine line : stockCorrection.getDocument().getLines()) {
            log.debug("createItemBatchInfo: toBatchNumber:{}, fromBatchNumber:{}", line.getToBatchNumber(),line.getFromBatchNumber());
            if (Objects.nonNull(line.getToBatchNumber()) && !line.getToBatchNumber().equals(line.getFromBatchNumber())) {
                itemBatchInfoService.createIfNotExists(new ItemBatchInfo(line.getItem().getId(), line.getToBatchNumber()));
            }
        }
    }

    private StockCorrection sendForApproval(StockCorrection stockCorrection, String action) throws Exception {
        log.debug("Request to Send for Approval for Correction : {}", stockCorrection);
        try {
            stockCorrection.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
            verifyAndGenerateNumber(stockCorrection);
            StockCorrection result = save(stockCorrection);
            reserveStock(result);
            //Get workflow configurations
            Map<String, Object> configurations = retrieveWorkflowConfigurations(result, true);
            //Start workflow if workflow enabled
            if ((Boolean) configurations.get("enableWorkflow")) {
                startWorkflow(result, action, configurations);
            }
            return result;
        } catch (Exception e) {
            stockService.deleteReservedStock(stockCorrection.getId(), stockCorrection.getDocument().getType());
            throw e;
        }
    }

    private void verifyAndGenerateNumber(StockCorrection stockCorrection) {
        boolean generateDocumentNumber = true;
        if (stockCorrection.getId() != null) {
            for (StockCorrection document : stockCorrectionRepository.findAllByIdWithLock(stockCorrection.getId())) {
                if (!document.getDocumentNumber().equals("DRAFT-" + document.getId())) {
                    generateDocumentNumber = Boolean.FALSE;
                    break;
                }
            }
        }
        if (generateDocumentNumber) {
            stockCorrection.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Correction.name(), "NH", stockCorrection));
        }
    }

    private StockCorrection approveStockCorrectionDocument(StockCorrection stockCorrection) throws Exception {
        log.debug("Request to approve Stock Correction Documents {}", stockCorrection);
        StockCorrection result = null;
        //deleteAndReserveStock(stockCorrection);
        refreshReserveStock(stockCorrection);
        stockCorrection.getDocument().setStatus(Status.APPROVED);
        result = save(stockCorrection);
        produce(stockCorrection);
        return result;
    }

    private StockCorrection rejectStockCorrectionDocument(StockCorrection stockCorrection) {
        log.debug("Request to reject Correction Documents {}", stockCorrection);
        stockCorrection.getDocument().setStatus(Status.REJECTED);
        save(stockCorrection);
        stockService.deleteReservedStock(stockCorrection.getId(), stockCorrection.getDocument().getType());
        return stockCorrection;
    }


    private void reserveStock(StockCorrection stockCorrection) throws Exception {
        List<CorrectionDocumentLine> lines = stockCorrection.getDocument().getLines();
        for (CorrectionDocumentLine correctionDocumentLine : lines) {
            if (correctionDocumentLine.getCorrectionQuantity().getValue() > 0) {
                stockService.reserveStock(correctionDocumentLine.getStockId(), correctionDocumentLine.getItem().getId(), correctionDocumentLine.getFromBatchNumber(),
                    stockCorrection.getDocument().getStore().getId(), correctionDocumentLine.getCorrectionQuantity().getValue(), stockCorrection.getId(),
                    stockCorrection.getDocument().getType(), stockCorrection.getDocumentNumber(),
                    correctionDocumentLine.getId(), stockCorrection.getDocument().getApprovedDate() != null ?
                        stockCorrection.getDocument().getApprovedDate() : stockCorrection.getDocument().getCreatedDate(),stockCorrection.getDocument().getCreatedBy().getId());
            }
        }
    }

    /**
     * Get all the stockCorrections.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockCorrection> findAll(Pageable pageable) {
        log.debug("Request to get all StockCorrections");
        Page<StockCorrection> result = stockCorrectionRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockCorrection by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockCorrection findOne(Long id) {
        log.debug("Request to get StockCorrection : {}", id);
        StockCorrection stockCorrection = stockCorrectionRepository.findOne(id);
        return stockCorrection;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public StockCorrection findDetachedOne(Long id) {
        log.debug("Request to get StockIssue : {}", id);
        return stockCorrectionRepository.findOne(id);
    }

    /**
     * Delete the  stockCorrection by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockCorrection : {}", id);
        StockCorrection stockCorrection = stockCorrectionSearchRepository.findByStockCorrectionId(id);
        deleteValidation(stockCorrection);
        stockCorrectionRepository.delete(id);
        stockCorrectionSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockCorrection corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockCorrection> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockCorrections for query {}", query);
        return stockCorrectionSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND)
            .field("documentNumber").field("document.status")
            .field("document.store.name")
            .field("document.approvedBy.displayName"), pageable);
    }

    /**
     * Search for the stockCorrection corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockCorrection> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockCorrections for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.status")
                .field("document.store.name")
                .field("document.approvedBy.displayName")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return ElasticSearchUtil.getPageRecords(searchQuery, StockCorrection.class, elasticsearchTemplate, "stockcorrection");
    }

    /**
     * Get all StockCorrection and publish
     */
    @Override
    public void produce(StockCorrection stockCorrection) {
        log.debug("Stock Correction Id {} has been published", stockCorrection.getId());
        List<StockEntry> stockEntries = getListStockEntry(stockCorrection);
        Map<String, Object> map = new HashedMap();
        map.put("Id", stockCorrection.getId());
        map.put("DocumentNo", stockCorrection.getDocumentNumber());
        map.put("StockEntries", stockEntries);
        stockCorrectionChannel.send(MessageBuilder.withPayload(map).build());

    }

    private List<StockEntry> getListStockEntry(StockCorrection stockCorrection) {
        List<StockEntry> stockEntryList = new ArrayList<>();
        List<CorrectionDocumentLine> correctionDocumentLineList = stockCorrection.getDocument().getLines();
        for (CorrectionDocumentLine correctionDocumentLine : correctionDocumentLineList) {
            StockEntry stockEntry = null;
            stockEntry = setStockEntry(stockCorrection, correctionDocumentLine);
            stockEntryList.add(stockEntry);
        }
        return stockEntryList;
    }

    private StockEntry setStockEntry(StockCorrection stockCorrection, CorrectionDocumentLine correctionDocumentLine) {
        Stock stock = stockService.findOne(correctionDocumentLine.getStockId());
        StockEntry stockEntry = new StockEntry();
        stockEntry.setTransactionLineId(correctionDocumentLine.getId());
        stockEntry.setItemId(correctionDocumentLine.getItem().getId());
        stockEntry.setLocatorId(correctionDocumentLine.getLocator().getId());
        stockEntry.setStoreId(stockCorrection.getDocument().getStore().getId());
        stockEntry.setBatchNo(correctionDocumentLine.getToBatchNumber());
        stockEntry.setMrp(correctionDocumentLine.getToMrp());
        stockEntry.setExpiryDate(correctionDocumentLine.getToExpiryDate());
        stockEntry.setOriginalBatchNo(stock.getOriginalBatchNo());
        stockEntry.setOriginalMRP(stock.getOriginalMRP());
        stockEntry.setOriginalExpiryDate(stock.getOriginalExpiryDate());
        stockEntry.setConsignment(stock.isConsignment());
        stockEntry.setSupplier(stock.getSupplier());
        stockEntry.setOwner(correctionDocumentLine.getOwner());
        stockEntry.setUnitId(stockCorrection.getDocument().getStore().getPartOf().getId());
        stockEntry.setTransactionDate(stockCorrection.getDocument().getCreatedDate());
        stockEntry.setTransactionLineId(correctionDocumentLine.getId());
        stockEntry.setQuantity(correctionDocumentLine.getCorrectionQuantity().getValue());
        stockEntry.setUomId(correctionDocumentLine.getCorrectionQuantity().getUom().getId());
        stockEntry.setCost(stock.getCost());
        stockEntry.setTransactionId(stockCorrection.getId());
        stockEntry.setTransactionNumber(stockCorrection.getDocumentNumber());
        stockEntry.setTransactionType(stockCorrection.getDocument().getType());
        stockEntry.setUserId(stockCorrection.getDocument().getCreatedBy().getId());
        return stockEntry;
    }

    private void deleteAndReserveStock(StockCorrection stockCorrection) throws Exception {
        List<CorrectionDocumentLine> lines = stockCorrection.getDocument().getLines();
        stockService.deleteReservedStock(stockCorrection.getId(), stockCorrection.getDocument().getType());
        for (CorrectionDocumentLine correctionDocumentLine : lines) {
            if (correctionDocumentLine.getCorrectionQuantity().getValue() > 0) {
                stockService.reserveStock(correctionDocumentLine.getStockId(), correctionDocumentLine.getItem().getId(), correctionDocumentLine.getFromBatchNumber(),
                    stockCorrection.getDocument().getStore().getId(), correctionDocumentLine.getCorrectionQuantity().getValue(), stockCorrection.getId(),
                    stockCorrection.getDocument().getType(), stockCorrection.getDocumentNumber(),
                    correctionDocumentLine.getId(), stockCorrection.getDocument().getCreatedDate(),stockCorrection.getDocument().getCreatedBy().getId());
            }
        }
    }

    private void validateDraft(StockCorrection stockCorrection) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockCorrection.getDocument().getStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STORE));
        }
        if (stockCorrection.getDocument().getType() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_DOCUMENT_TYPE));
        }
        if (stockCorrection.getDocument().getCreatedBy() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_NAME));
        }
        if (stockCorrection.getDocument().getCreatedDate() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CREATED_DATE));
        }
        if (stockCorrection.getDocument().getStatus() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STATUS));
        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    private void validateStockCorrectionDocument(StockCorrection stockCorrection) throws FieldValidationException {
        int correctionQuantityCounter = 0;
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockCorrection.getDocument().getLines() != null) {
            for (CorrectionDocumentLine line : stockCorrection.getDocument().getLines()) {

                if (line.getStockId() != null) {
                    Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("document.lines.stockId:" + line.getStockId()
                        + " document.status.raw:" + Status.WAITING_FOR_APPROVAL).defaultOperator(Operator.AND)).build();
                    List<StockCorrection> stockCorrectionList = ElasticSearchUtil.getRecords(query, StockCorrection.class, elasticsearchTemplate, "stockcorrection");
                    Iterator<StockCorrection> stockCorrectionIterator = stockCorrectionList.iterator();
                    while (stockCorrectionIterator.hasNext()) {
                        if (!stockCorrectionIterator.next().getDocumentNumber().equals(stockCorrection.getDocumentNumber())) {
                            Map<String, Object> source = new HashMap<String, Object>();
                            source.put("itemName", line.getItem().getName());
                            source.put("batchNo", line.getFromBatchNumber());
                            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ANOTHER_CORRECTION_IN_PROGRESS, source));
                            break;
                        }
                    }
                } else {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STOCK_ID, source));
                }

                if (line.getItem() == null) {
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ITEM));
                    continue;
                }

                if (line.getCorrectionQuantity() != null) {
                    if (line.getCorrectionQuantity().getUom() == null) {
                        Map<String, Object> source = new HashMap<String, Object>();
                        source.put("itemName", line.getItem().getName());
                        errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UOM, source));
                    }
                    if (line.getCorrectionQuantity().getValue() == null) {
                        Map<String, Object> source = new HashMap<String, Object>();
                        source.put("itemName", line.getItem().getName());
                        errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY, source));
                    }
                    if (line.getCorrectionQuantity().getValue() == 0) correctionQuantityCounter++;
                } else {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY_AND_UOM, source));
                }
                if (line.getFromBatchNumber() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_BATCH, source));
                }
                if (line.getToBatchNumber() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_BATCH, source));
                }
                if (line.getFromMrp() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_MRP, source));
                }
                if (line.getToMrp() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_MRP, source));
                }
                if (line.getFromExpiryDate() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_EXPIRY_DATE, source));
                }
                if (line.getToExpiryDate() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_EXPIRY_DATE, source));
                }
            }
            if (correctionQuantityCounter > 0) {
                Map<String, Object> source = new HashMap<String, Object>();
                source.put("document", stockCorrection.getDocumentNumber());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ALL_CORRECTION_QUANTITY_MUST_BE_POSITIVE));
            }
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockCorrection.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CORRECTION_DOCUMENT_LINES));
        }
        Boolean throwNewException = false;
        try {
            validateDraft(stockCorrection);
            if (!errorMessages.isEmpty()) {
                throwNewException = true;
            }
        } catch (FieldValidationException exception) {
            exception.getErrorMessages().addAll(errorMessages);
            throw exception;
        }
        if (throwNewException) {
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }

    /**
     * Search for the Stock Correction to get status count corresponding to the query.
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
                .field("documentNumber").field("document.status")
                .field("document.store.name")
                .field("document.approvedBy.displayName")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "stockcorrection");
        Terms terms = aggregations.get("status_count");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    /**
     * Execute workflow
     *
     * @param stockCorrection the entity to save
     * @param transition      to be performed
     * @param taskId          task Id
     * @return stockCorrection object
     * @throws Exception
     */
    @Override
    @Transactional
    public StockCorrection executeWorkflow(StockCorrection stockCorrection, String transition, Long taskId) throws Exception {
        StockCorrection result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Approved":
                action = "APPROVED";
                //validateDocumentApprover(stockCorrection);
                validateStockCorrectionDocument(stockCorrection);
                result = approveStockCorrectionDocument(stockCorrection);
                break;
            case "Rejected":
                action = "REJECTED";
                // validateStockCorrectionDocument(stockCorrection);
                result = rejectStockCorrectionDocument(stockCorrection);
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }
        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("action_out", action);
        workflowService.completeUserTask(taskId, userId, results);
        index(result);
        return result;
    }

    public void validateDocumentApprover(StockCorrection stockCorrection) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockCorrection, "stock_correction_document_approver_validation");

    }

    /**
     * Start workflow
     *
     * @param stockCorrection
     * @param action
     * @Param configurations
     */
    public void startWorkflow(StockCorrection stockCorrection, String action, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", stockCorrection.getId());
            content.put("document_type", stockCorrection.getDocument().getType());
            content.put("group_id", configurations.get("groupIds"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_number", stockCorrection.getDocumentNumber());
            params.put("created_date", stockCorrection.getDocument().getCreatedDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("from_store", stockCorrection.getDocument().getStore().getName());
            params.put("unit_id", stockCorrection.getDocument().getStore().getPartOf().getId());
            params.put("content", content);
            //Set result
            results.put("action_out", action);
            //Start the process
            Long processInstanceId = workflowService.startProcess(deployedUnit, (String) configurations.get("processId"), params);
            //Complete the document creation task
            workflowService.completeUserTaskForProcessInstance(processInstanceId, userId, results);
        }
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
        StockCorrection stockCorrection = stockCorrectionRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(stockCorrection, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) : workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,stockCorrection.getDocument().getCreatedBy().getLogin());
            if ((Boolean) taskDetails.get("isGroupTask")) {
                List<String> workflowGroupIdList = (List<String>) taskDetails.get("groupIdList");
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
     * @param stockCorrection
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(StockCorrection stockCorrection, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_stockcorrection_enable_workflow", stockCorrection.getDocument().getStore().getId(), stockCorrection.getDocument().getStore().getPartOf().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_stockcorrection_workflow_definition", stockCorrection.getDocument().getStore().getId(), stockCorrection.getDocument().getStore().getPartOf().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(Correction_Approval_Committee, stockCorrection.getDocument().getStore().getPartOf().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", stockCorrection.getDocument().getStore().getId(), stockCorrection.getDocument().getStore().getPartOf().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    @Override
    public void index(StockCorrection stockCorrection) {
        stockCorrectionSearchRepository.save(stockCorrection);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Stock Correction");
        stockCorrectionSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockCorrection latest=true");
        List<StockCorrection> data = stockCorrectionRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockCorrectionSearchRepository.saveAll(data);
        }
    }


    @Override
    public void reIndex(Long id) {
        StockCorrection stockCorrection = stockCorrectionRepository.findOne(id);
        if (stockCorrection == null) {
            if (stockCorrectionSearchRepository.existsById(id))
                stockCorrectionSearchRepository.deleteById(id);
        } else {
            stockCorrectionSearchRepository.save(stockCorrection);
        }

    }

    private void refreshReserveStock(StockCorrection stockCorrection) throws Exception {
        List<ReserveStock> reserveStocks = stockService.findReserveStockByTransactionNo(stockCorrection.getDocumentNumber());
        List<Long> deleteReserveStock = new ArrayList<>();
        List<CorrectionDocumentLine> refreshReserveStock = new ArrayList<>();
        List<CorrectionDocumentLine> addReserveStock = new ArrayList<>();
        List<CorrectionDocumentLine> tempDocumentLineList = new ArrayList<>(stockCorrection.getDocument().getLines());
        List<ReserveStock> tempReserveStockList = new ArrayList<>(reserveStocks);

        reserveStocks.forEach(reserveStock -> stockCorrection.getDocument().getLines().stream().filter(line -> reserveStock.getTransactionLineId().equals(line.getId())).forEachOrdered(line -> {
            tempReserveStockList.remove(reserveStock);
            tempDocumentLineList.remove(line);
            if (line.getCorrectionQuantity().getValue() == 0) {
                deleteReserveStock.add(reserveStock.getId());
            } else {
                refreshReserveStock.add(line);
            }
        }));

        tempReserveStockList.stream().map(ReserveStock::getId).forEachOrdered(deleteReserveStock::add);
        tempDocumentLineList.stream().filter(line -> line.getCorrectionQuantity().getValue() > 0).forEachOrdered(line -> {
            if (line.getId() == null) {
                line.setId(stockCorrectionRepository.getId());
            }
            addReserveStock.add(line);
        });

        reserveStock(stockCorrection, addReserveStock);
        deleteAndReserveStock(stockCorrection, refreshReserveStock);
        stockService.deleteReservedStocks(deleteReserveStock);
    }

    private void deleteAndReserveStock(StockCorrection stockCorrection, List<CorrectionDocumentLine> lines) {
        for (CorrectionDocumentLine correctionDocumentLine : lines) {
            try {
                stockService.deleteAndReserveStock(correctionDocumentLine.getStockId(), correctionDocumentLine.getItem().getId(), correctionDocumentLine.getFromBatchNumber(),
                    stockCorrection.getDocument().getStore().getId(), correctionDocumentLine.getCorrectionQuantity().getValue(), stockCorrection.getId(),
                    stockCorrection.getDocument().getType(), stockCorrection.getDocumentNumber(),
                    correctionDocumentLine.getId(), stockCorrection.getDocument().getApprovedDate() != null ?
                        stockCorrection.getDocument().getApprovedDate() : stockCorrection.getDocument().getCreatedDate(),stockCorrection.getDocument().getCreatedBy().getId());
            } catch (StockException e) {
                e.setItemName(correctionDocumentLine.getItem().getName());
                e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                throw e;
            }
        }
    }

    private void reserveStock(StockCorrection stockCorrection, List<CorrectionDocumentLine> lines) throws Exception {
        for (CorrectionDocumentLine correctionDocumentLine : lines) {
            if (correctionDocumentLine.getCorrectionQuantity().getValue() > 0) {
                try {
                    stockService.reserveStock(correctionDocumentLine.getStockId(), correctionDocumentLine.getItem().getId(), correctionDocumentLine.getFromBatchNumber(),
                        stockCorrection.getDocument().getStore().getId(), correctionDocumentLine.getCorrectionQuantity().getValue(), stockCorrection.getId(),
                        stockCorrection.getDocument().getType(), stockCorrection.getDocumentNumber(),
                        correctionDocumentLine.getId(), stockCorrection.getDocument().getApprovedDate() != null ?
                            stockCorrection.getDocument().getApprovedDate() : stockCorrection.getDocument().getCreatedDate(),stockCorrection.getDocument().getCreatedBy().getId());
                } catch (StockException e) {
                    e.setItemName(correctionDocumentLine.getItem().getName());
                    e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                    throw e;
                }
            }
        }
    }

    @Override
    public void verifyStockReserve(StockCorrection stockCorrection) {
        if (stockCorrection.getDocumentNumber() != null) {
            List<ReserveStock> reserveStocks = stockService.findReserveStockByTransactionNo(stockCorrection.getDocumentNumber());

            if (!reserveStocks.isEmpty()) {
                List<CorrectionDocumentLine> correctionDocumentLines = new ArrayList<>();
                stockCorrection.getDocument().getLines().forEach(correctionDocumentLine -> {
                    ReserveStock reserveStock = reserveStocks.stream().filter(reserveStock1 -> reserveStock1.getTransactionLineId().equals(correctionDocumentLine.getId()))
                        .findAny().orElse(null);
                    if (reserveStock == null) {
                        correctionDocumentLines.add(correctionDocumentLine);
                    }
                });
                checkForReservation(correctionDocumentLines);
            } else {
                checkForReservation(stockCorrection.getDocument().getLines());
            }
        } else {
            checkForReservation(stockCorrection.getDocument().getLines());
        }

    }

    private void checkForReservation(List<CorrectionDocumentLine> correctionDocumentLines) {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        correctionDocumentLines.forEach(
            correctionDocumentLine -> {
                Float totalReserveQuantity = stockService.isItemReserved(correctionDocumentLine.getStockId());
                if (totalReserveQuantity > 0) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", correctionDocumentLine.getItem().getName());
                    source.put("quantity", totalReserveQuantity);
                    source.put("batchNo", correctionDocumentLine.getFromBatchNumber());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.SELECTED_ITEMS_ARE_RESERVED, source));
                }
            }
        );
        if (!errorMessages.isEmpty()) {
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }

    private void saveValidation(StockCorrection stockCorrectionRef) {
        StockCorrection stockCorrection = stockCorrectionSearchRepository.findByStockCorrectionDocumentNumber(stockCorrectionRef.getDocumentNumber());
        if (null == stockCorrection) {
            stockCorrection = stockCorrectionRef;
        }
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(stockCorrection.getDocument().getStore().getPartOf().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with logged in unit");
        }
    }

    private void deleteValidation(StockCorrection stockCorrection) {
        log.debug("validate before delete Stock Correction : {}", stockCorrection);
        if (stockCorrection.getDocument().getStatus() != Status.DRAFT) {
            throw new CustomParameterizedException("10088", "Can't delete document ,Only Draft Status document can be deleted");
        }
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<StockCorrection> search = this.search("documentNumber.raw:" + documentNumber, PageRequest.of(0, 1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        StockCorrection stockCorrection = search.iterator().next();
        Map<String, Object> configurations = retrieveWorkflowConfigurations(stockCorrection, true);
        //Start workflow if workflow enabled
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(stockCorrection, "SENDFORAPPROVAL", configurations);
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
