package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jbpm.services.api.model.DeployedUnit;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.UserDTO;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.aop.producer.StockServiceAspect;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.AutoStockConsumption;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.StockConsumptionRepository;
import org.nh.pharmacy.repository.StockRepository;
import org.nh.pharmacy.repository.search.StockConsumptionSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.mapper.ReceiptToStockConsumptionMapper;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.print.PdfGenerator;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static java.util.Objects.isNull;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.nh.pharmacy.domain.enumeration.Context.Consumption_Approval_Committee;
import static org.nh.pharmacy.util.ElasticSearchUtil.*;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;


/**
 * Service Implementation for managing StockConsumption.
 */
@Service
@Transactional
public class StockConsumptionServiceImpl implements StockConsumptionService {

    private final Logger log = LoggerFactory.getLogger(StockConsumptionServiceImpl.class);

    private final StockConsumptionRepository stockConsumptionRepository;

    private final StockConsumptionSearchRepository stockConsumptionSearchRepository;

    private final StockService stockService;

    private final WorkflowService workflowService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final MessageChannel stockConsumptionChannel;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final StockRepository stockRepository;

    private final RuleExecutorService ruleExecutorService;

    private final GroupService groupService;

    private final ApplicationProperties applicationProperties;

    private final FreemarkerService freemarkerService;

    private final ReceiptToStockConsumptionMapper receiptToStockConsumptionMapper;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Value("${server.port}")
    private String portNo;

    private final SystemAlertService systemAlertService;

    public StockConsumptionServiceImpl(StockConsumptionRepository stockConsumptionRepository, StockConsumptionSearchRepository stockConsumptionSearchRepository,
                                       StockService stockService, WorkflowService workflowService, SequenceGeneratorService sequenceGeneratorService, @Qualifier(Channels.STOCK_OUTPUT)
                                           MessageChannel stockConsumptionChannel, ElasticsearchOperations elasticsearchTemplate, StockRepository stockRepository,
                                       RuleExecutorService ruleExecutorService, GroupService groupService, ApplicationProperties applicationProperties, FreemarkerService freemarkerService,
                                       ReceiptToStockConsumptionMapper receiptToStockConsumptionMapper, PharmacyRedisCacheService pharmacyRedisCacheService, SystemAlertService systemAlertService) {
        this.stockConsumptionRepository = stockConsumptionRepository;
        this.stockConsumptionSearchRepository = stockConsumptionSearchRepository;
        this.stockService = stockService;
        this.workflowService = workflowService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.stockConsumptionChannel = stockConsumptionChannel;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.stockRepository = stockRepository;
        this.ruleExecutorService = ruleExecutorService;
        this.groupService = groupService;
        this.applicationProperties = applicationProperties;
        this.freemarkerService=freemarkerService;
        this.receiptToStockConsumptionMapper = receiptToStockConsumptionMapper;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.systemAlertService = systemAlertService;
    }

    /**
     * Save a stockConsumption.
     *
     * @param stockConsumption the entity to save
     * @return the persisted entity
     */
    @Override
    public StockConsumption save(StockConsumption stockConsumption) throws SequenceGenerateException {
        log.debug("Request to save StockConsumption : {}", stockConsumption);
        if (stockConsumption.getId() == null) {
            stockConsumption.id(stockConsumptionRepository.getId());
            if ((Status.DRAFT).equals(stockConsumption.getDocument().getStatus())) {
                stockConsumption.documentNumber(StringUtils.join(new Object[]{Status.DRAFT, stockConsumption.getId()}, "-"));
            }
            stockConsumption.version(0);
            stockConsumption.getDocument().setId(stockConsumption.getId().toString());
        } else {
            stockConsumptionRepository.updateLatest(stockConsumption.getId());
            if (stockConsumption.getVersion() != null) {
                int version = stockConsumption.getVersion() + 1;
                stockConsumption.version(version);
            } else {
                stockConsumption.version(0);
            }
        }
        if(isNull(stockConsumption.getDocument().getForHSC())) stockConsumption.getDocument().setForHSC(new HealthcareServiceCenterDTO());
        stockConsumption.getDocument().setDocumentNumber(stockConsumption.getDocumentNumber());
        stockConsumption.latest(true);
        generateIdsIfRequiredForLines(stockConsumption);
        saveValidation(stockConsumption);
        StockConsumption result = stockConsumptionRepository.saveAndFlush(stockConsumption);
//        stockConsumptionSearchRepository.save(result);
        return result;
    }

    private void saveAll(List<StockConsumption> stockConsumptions) {
        log.debug("Request to saveAll StockConsumptions : {}", stockConsumptions);
        List<StockConsumption> results = stockConsumptionRepository.saveAll(stockConsumptions);
        stockConsumptionSearchRepository.saveAll(results);
    }

    private void generateIdsIfRequiredForLines(StockConsumption stockConsumption) {
        if (CollectionUtils.isNotEmpty(stockConsumption.getDocument().getLines())) {
            stockConsumption.getDocument().getLines().stream()
                .filter(stockConsumptionLine -> stockConsumptionLine.getId() == null)
                .forEach(stockConsumptionLine -> stockConsumptionLine.setId(stockConsumptionRepository.getId()));
        }
    }

    /**
     * Save a stockConsumption.
     *
     * @param stockConsumption the entity to save
     * @return the persisted entity
     * @Param action the activity needs to be performed
     */
    @PublishStockTransaction
    public StockConsumption save(StockConsumption stockConsumption, String action) throws Exception {
        StockConsumption result;
        switch (action) {
            case "SENDFORAPPROVAL":
                validateSendForApproval(stockConsumption);
                validateConsumptionDocument(stockConsumption);
                if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
                    result = sendForApprovalForReversal(stockConsumption, action);
                } else {
                    result = sendForApproval(stockConsumption, action);
                }
                break;
            case "APPROVED":
                validatesStockConsumption(stockConsumption);
                validateConsumptionDocument(stockConsumption);
                if (stockConsumption.getId() == null) {
                    assignValuesForDirectApprove(stockConsumption);
                }
                if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
                    result = approveReversalConsumptionDocument(stockConsumption);
                } else {
                    result = approveConsumptionDocument(stockConsumption);
                }
                break;
            case "REJECTED":
                //validatesStockConsumption(stockConsumption);
                //validateConsumptionDocument(stockConsumption);
                result = rejectConsumptionDocument(stockConsumption);
                break;
            default:
                if (stockConsumption.getDocument().getStatus() == null) {
                    stockConsumption.getDocument().setStatus(Status.DRAFT);
                    validateDraft(stockConsumption);
                    result = save(stockConsumption);
                } else {
                    validateConsumptionDocument(stockConsumption);
                    if (stockConsumption.getDocument().getStatus().equals(Status.WAITING_FOR_APPROVAL)) {
                        generateIdsIfRequiredForLines(stockConsumption);
                        if(TransactionType.Stock_Consumption.equals(stockConsumption.getDocument().getDocumentType()))
                            refreshReserveStock(stockConsumption);
                    }
                    result = save(stockConsumption);
                }
                break;
        }
        return result;
    }

    private void assignValuesForDirectApprove(StockConsumption stockConsumption) throws SequenceGenerateException {
        stockConsumption.getDocument().setDraft(false);
        if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
            stockConsumption.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Reversal_Consumption.name(), "NH", stockConsumption));
        } else {
            stockConsumption.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Consumption.name(), "NH", stockConsumption));
        }
        stockConsumption.id(stockConsumptionRepository.getId());
        stockConsumption.getDocument().setId(stockConsumption.getId().toString());
    }

    /**
     * @param stockConsumption
     * @param action
     * @return stockConsumption
     * @throws Exception
     */
    @Transactional
    public StockConsumption sendForApproval(StockConsumption stockConsumption, String action) throws Exception {
        log.debug("Request to Send for Approval for Consumption : {}", stockConsumption);
        StockConsumption result;
        stockConsumption.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
        if (stockConsumption.getDocument().isDraft()) {
            stockConsumption.getDocument().setDraft(false);
            stockConsumption.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Consumption.name(), "NH", stockConsumption));
            result = save(stockConsumption);
            try {
                reserveStock(result, result.getDocument().getLines());
            } catch (Exception exception) {
                stockService.deleteReservedStock(result.getId(), result.getDocument().getDocumentType());
                throw exception;
            }
            //Get workflow configurations
            Map<String, Object> configurations = retrieveWorkflowConfigurations(stockConsumption, true);
            //Start workflow if workflow enabled
            if ((Boolean) configurations.get("enableWorkflow")) {
                startWorkflow(result, action, configurations);
            }
        } else {
            //In case of modification
            result = save(stockConsumption);
            refreshReserveStock(stockConsumption);
        }
        return result;
    }

    private void reserveStock(StockConsumption stockConsumption, List<ConsumptionDocumentLine> lines) throws Exception {
        //List<ConsumptionDocumentLine> lines = stockConsumption.getDocument().getLines();
        for (ConsumptionDocumentLine consumptionDocumentLine : lines) {
            if (consumptionDocumentLine.getQuantity().getValue() > 0) {
                try {
                    stockService.reserveStock(consumptionDocumentLine.getStockId(), consumptionDocumentLine.getItem().getId(), consumptionDocumentLine.getBatchNumber(),
                        stockConsumption.getDocument().getConsumptionStore().getId(), consumptionDocumentLine.getQuantity().getValue(), stockConsumption.getId(),
                        stockConsumption.getDocument().getDocumentType(), stockConsumption.getDocumentNumber(),
                        consumptionDocumentLine.getId(), stockConsumption.getDocument().getApprovedDate() != null ?
                            stockConsumption.getDocument().getApprovedDate() : stockConsumption.getDocument().getCreatedDate(),stockConsumption.getDocument().getConsumedBy().getId());
                } catch (StockException e) {
                    e.setItemName(consumptionDocumentLine.getItem().getName());
                    e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                    throw e;
                }
            }
        }
    }


    private void deleteAndReserveStock(StockConsumption stockConsumption, List<ConsumptionDocumentLine> lines) throws Exception {
        for (ConsumptionDocumentLine consumptionDocumentLine : lines) {
            try {
                stockService.deleteAndReserveStock(consumptionDocumentLine.getStockId(), consumptionDocumentLine.getItem().getId(), consumptionDocumentLine.getBatchNumber(),
                    stockConsumption.getDocument().getConsumptionStore().getId(), consumptionDocumentLine.getQuantity().getValue(), stockConsumption.getId(),
                    stockConsumption.getDocument().getDocumentType(), stockConsumption.getDocumentNumber(),
                    consumptionDocumentLine.getId(), stockConsumption.getDocument().getApprovedDate() != null ?
                        stockConsumption.getDocument().getApprovedDate() : stockConsumption.getDocument().getCreatedDate(),stockConsumption.getDocument().getCreatedBy().getId());
            } catch (StockException e) {
                e.setItemName(consumptionDocumentLine.getItem().getName());
                e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                throw e;
            }
        }
    }

    private void refreshReserveStock(StockConsumption stockConsumption) throws Exception {
        List<ReserveStock> reserveStocks = stockService.findReserveStockByTransactionNo(stockConsumption.getDocumentNumber());
        List<Long> deleteReserveStock = new ArrayList<>();
        List<ConsumptionDocumentLine> refreshReserveStock = new ArrayList<>();
        List<ConsumptionDocumentLine> addReserveStock = new ArrayList<>();
        List<ConsumptionDocumentLine> tempDocumentLineList = new ArrayList<>(stockConsumption.getDocument().getLines());
        List<ReserveStock> tempReserveStockList = new ArrayList<>(reserveStocks);

        reserveStocks.forEach(reserveStock -> stockConsumption.getDocument().getLines().stream().filter(line -> reserveStock.getTransactionLineId().equals(line.getId())).forEachOrdered(line -> {
            tempReserveStockList.remove(reserveStock);
            tempDocumentLineList.remove(line);
            if (line.getQuantity().getValue() == 0) {
                deleteReserveStock.add(reserveStock.getId());
            } else {
                refreshReserveStock.add(line);
            }
        }));

        tempReserveStockList.stream().map(ReserveStock::getId).forEachOrdered(deleteReserveStock::add);
        tempDocumentLineList.stream().filter(line -> line.getQuantity().getValue() > 0).forEachOrdered(line -> {
            if (line.getId() == null) {
                line.setId(stockConsumptionRepository.getId());
            }
            addReserveStock.add(line);
        });

        reserveStock(stockConsumption, addReserveStock);
        deleteAndReserveStock(stockConsumption, refreshReserveStock);
        stockService.deleteReservedStocks(deleteReserveStock);
    }

    /**
     * Get stockConsumption and publish
     */
    @Override
    public void produce(StockConsumption stockConsumption) {
        log.debug("Stock Consumption Id {} has been published", stockConsumption.getId());
        Map<String, Object> stockConsumptionMap = new HashedMap();
        stockConsumptionMap.put("Id", stockConsumption.getId());
        stockConsumptionMap.put("DocumentNo", stockConsumption.getDocumentNumber());
        //stockConsumptionChannel.send(MessageBuilder.withPayload(stockConsumptionMap).build());
        StockServiceAspect.threadLocal.get().put(Channels.STOCK_OUTPUT, stockConsumptionMap);
    }

    /**
     * @param stockConsumption
     * @return stockConsumption
     * @throws Exception
     */
    @Transactional
    public StockConsumption approveConsumptionDocument(StockConsumption stockConsumption) throws Exception {
        log.debug("Request to approve Consumption Documents {}", stockConsumption);
        generateIdsIfRequiredForLines(stockConsumption);
        refreshReserveStock(stockConsumption);
        stockConsumption.getDocument().setStatus(Status.APPROVED);
        save(stockConsumption);
        produce(stockConsumption);
        return stockConsumption;

    }

    /**
     * @param stockConsumption
     * @return stockConsumption
     */
    @Transactional
    public StockConsumption rejectConsumptionDocument(StockConsumption stockConsumption) throws Exception {
        log.debug("Request to reject Consumption Documents {}", stockConsumption);
        stockConsumption.getDocument().setStatus(Status.REJECTED);
        save(stockConsumption);
        if(TransactionType.Stock_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
            stockService.deleteReservedStock(stockConsumption.getId(), stockConsumption.getDocument().getDocumentType());
        }
        return stockConsumption;
    }

    /**
     * Get all the stockConsumptions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockConsumption> findAll(Pageable pageable) {
        log.debug("Request to get all StockConsumptions");
        Page<StockConsumption> result = stockConsumptionRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockConsumption by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockConsumption findOne(Long id) {
        log.debug("Request to get StockConsumption : {}", id);
        StockConsumption stockConsumption = stockConsumptionRepository.findOne(id);
        if (stockConsumption != null)
            if (stockConsumption.getDocument().getStatus() == Status.DRAFT || stockConsumption.getDocument().getStatus() == Status.WAITING_FOR_APPROVAL) {
                for (ConsumptionDocumentLine consumptionDocumentLine : stockConsumption.getDocument().getLines()) {
                    consumptionDocumentLine.setCurrentStock(new Quantity(stockRepository.findById(consumptionDocumentLine.getStockId()).get().getQuantity(), consumptionDocumentLine.getItem().getTrackUOM()));
                }
            }
        return stockConsumption;
    }

    /**
     * Get one stockConsumption by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockConsumption findOne(Long id, Integer version) {

        log.debug("Request to get StockConsumption : {}", id, version);
        StockConsumption stockConsumption = stockConsumptionRepository.findById(new DocumentId(id, version)).get();
        return stockConsumption;
    }

    /**
     * Delete the  stockConsumption by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) throws BusinessRuleViolationException {
        log.debug("Request to delete StockConsumption : {}", id);
        validateDelete(stockConsumptionRepository.findOne(id));
        stockConsumptionRepository.delete(id);
        stockConsumptionSearchRepository.deleteById(id);
    }

    /**
     * Delete the stockConsumption by id,version.
     *
     * @param id,version the id of the entity
     */
    @Override
    public void delete(Long id, Integer version) {
        log.debug("Request to delete StockConsumption : {}", id, version);
        stockConsumptionRepository.deleteById(new DocumentId(id, version));
        stockConsumptionSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockConsumption corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockConsumption> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockConsumptions for query {}", query);
        return stockConsumptionSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.status").field("document.forHSC.name")
            .field("document.forDepartment.name").field("document.forPatient.fullName")
            .field("document.consumptionStore.name")
            .defaultOperator(Operator.AND), pageable);
    }

    /**
     * Search for the stockConsumption corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockConsumption> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockConsumptions for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.status").field("document.forHSC.name")
                .field("document.forDepartment.name").field("document.forPatient.fullName")
                .field("document.consumptionStore.name")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return getPageRecords(searchQuery, StockConsumption.class, elasticsearchTemplate, "stockconsumption");
    }

    /**
     * Get the stockConsumption status count corresponding to the query.
     *
     * @param query the query of the search
     * @return status count
     */
    @Override
    public Map<String, Long> getStatusCount(String query) {
        Map<String, Long> statusCount = new HashMap<>();
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.status").field("document.forHSC.name")
                .field("document.forDepartment.name").field("document.forPatient.fullName")
                .field("document.consumptionStore.name")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = getAggregations(searchQuery, elasticsearchTemplate, "stockconsumption");
        Terms aggregationTerms = aggregations.get("status_count");
        for (Terms.Bucket bucket : aggregationTerms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    /**
     * Execute workflow
     *
     * @param stockConsumption the entity to save
     * @param transition       to be performed
     * @param taskId           task Id
     * @return stockConsumption object
     * @throws Exception
     */
    @Override
    @Transactional
    @PublishStockTransaction
    public StockConsumption executeWorkflow(StockConsumption stockConsumption, String transition, Long taskId) throws Exception {
        StockConsumption result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Send for Approval":
                action = "SENDFORAPPROVAL";
                if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
                    result = sendForApprovalForReversal(stockConsumption, action);
                } else {
                    result = sendForApproval(stockConsumption, action);
                }
                break;
            case "Approved":
                action = "APPROVED";
                //validateDocumentApprover(stockConsumption);
                if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
                    result = approveReversalConsumptionDocument(stockConsumption);
                } else {
                    result = approveConsumptionDocument(stockConsumption);
                }
                break;
            case "Rejected":
                action = "REJECTED";
                result = rejectConsumptionDocument(stockConsumption);
                break;
            case "Modify":
                action = "DRAFT";
                stockConsumption.getDocument().setStatus(Status.DRAFT);
                result = save(stockConsumption);
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }
        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("action_out", action);
        workflowService.completeUserTask(taskId, userId, results);
        return result;
    }

    /**
     * Start workflow
     *
     * @param stockConsumption
     * @param configurations
     * @param action
     * @Param processId
     */
    public void startWorkflow(StockConsumption stockConsumption, String action, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", stockConsumption.getId());
            content.put("document_type", stockConsumption.getDocument().getDocumentType());
            content.put("group_id", configurations.get("groupIds"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_number", stockConsumption.getDocumentNumber());
            params.put("consume_date", stockConsumption.getDocument().getConsumedDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("from_store", stockConsumption.getDocument().getConsumptionStore().getName());
            params.put("unit_id", String.valueOf(stockConsumption.getDocument().getConsumptionUnit().getId()));
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
        StockConsumption stockConsumption = stockConsumptionSearchRepository.findByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(stockConsumption, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) : workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,stockConsumption.getDocument().getCreatedBy().getLogin());
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
     * @param stockConsumption
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(StockConsumption stockConsumption, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_stockconsumption_enable_workflow", stockConsumption.getDocument().getConsumptionStore().getId(), stockConsumption.getDocument().getConsumptionUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_stockconsumption_workflow_definition", stockConsumption.getDocument().getConsumptionStore().getId(), stockConsumption.getDocument().getConsumptionUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(Consumption_Approval_Committee, stockConsumption.getDocument().getConsumptionUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", stockConsumption.getDocument().getConsumptionStore().getId(), stockConsumption.getDocument().getConsumptionUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Stock Consumption");
        stockConsumptionSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockConsumption latest=true");
        List<StockConsumption> data = stockConsumptionRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockConsumptionSearchRepository.saveAll(data);
        }
        stockConsumptionSearchRepository.refresh();
    }

    private void validateDraft(StockConsumption stockConsumption) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockConsumption.getDocument().getConsumptionStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CONSUMPTION_STORE));
        }
        if (stockConsumption.getDocument().getConsumptionUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CONSUMPTION_UNIT));
        }
        if (stockConsumption.getDocument().getDocumentType() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_DOCUMENT_TYPE));
        }
        if (stockConsumption.getDocument().getConsumedBy() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CONSUMEDBY));
        }
        if (stockConsumption.getDocument().getCreatedDate() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CREATED_DATE));
        }
        if (stockConsumption.getDocument().getStatus() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STATUS));
        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    private void validateConsumptionDocument(StockConsumption stockConsumption) throws FieldValidationException {
        int zeroConsumedQuantityCounter = 0;
        List<ErrorMessage> errorMessages = new ArrayList<>();
        /*if (stockConsumption.getDocument().getForHSC() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FOR_HSC));
        }
        if (stockConsumption.getDocument().getForDepartment() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FOR_DEPARTMENT));
        }*/
        if (stockConsumption.getDocument().getLines() != null) {
            for (ConsumptionDocumentLine line : stockConsumption.getDocument().getLines()) {
                if (line.getItem() != null) {
                    if (!line.isGeneric()) {
                        if (line.getItem().getId() == null) {
                            Map<String, Object> source = new HashMap<String, Object>();
                            source.put("itemName", line.getItem().getName());
                            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ITEM_ID_FOR_NOT_GENERIC_ITEM, source));
                            continue;
                        }
                    } else {
                        if (line.getItem().getName() == null) {
                            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ITEM_NAME_FOR_GENERIC_ITEM));
                            continue;
                        }
                    }
                }

                if (line.getItem() == null) {
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ITEM));
                    continue;
                }

                if (line.getQuantity() != null) {
                    if (line.getQuantity().getUom() == null) {
                        Map<String, Object> source = new HashMap<String, Object>();
                        source.put("itemName", line.getItem().getName());
                        errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UOM, source));
                    }
                    if (line.getQuantity().getValue() == null) {
                        Map<String, Object> source = new HashMap<String, Object>();
                        source.put("itemName", line.getItem().getName());
                        errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY, source));
                    }
                    if (line.getQuantity().getValue() == 0) zeroConsumedQuantityCounter++;
                } else {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY_AND_UOM, source));
                }
            }
            if (zeroConsumedQuantityCounter > 0) {
                Map<String, Object> source = new HashMap<String, Object>();
                source.put("document", stockConsumption.getDocumentNumber());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ALL_CONSUMED_QUANTITY_MUST_BE_POSITIVE));
            }
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockConsumption.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CONSUMPTION_DOCUMENT_LINES));
        }
        try {
            validateDraft(stockConsumption);
            if (!errorMessages.isEmpty()) {
                throw new FieldValidationException(errorMessages, "Validation exception");
            }
        } catch (FieldValidationException exception) {
            exception.getErrorMessages().addAll(errorMessages);
            throw exception;
        }
    }

    /**
     * Validate entity
     *
     * @param stockConsumption
     * @throws BusinessRuleViolationException
     */
    public void validatesStockConsumption(StockConsumption stockConsumption) throws BusinessRuleViolationException {
        //ruleExecutorService.executeByGroup(stockConsumption, "stock_consumption_rules");
    }


    public void validateSendForApproval(StockConsumption stockConsumption) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockConsumption, "stock_consumption_send_for_approval_validation");
    }

    public void validateDocumentApprover(StockConsumption stockConsumption) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockConsumption, "stock_consumption_document_approver_validation");
    }

    public void validateDelete(StockConsumption stockConsumption) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockConsumption, "stock_consumption_delete_validation");
    }

    private void saveValidation(StockConsumption stockConsumption) {
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(stockConsumption.getDocument().getConsumptionUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with logged in unit");
        }
    }

    @Override
    public void index(StockConsumption stockConsumption) {
        stockConsumptionSearchRepository.save(stockConsumption);
    }
    public byte[] getStockConsumptionPDF(StockConsumption stockConsumption) throws Exception {
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;
        Map<String, Object> outData = this.getStockConsumptionHTML(stockConsumption);
        String htmlData = outData.get("html").toString();
        contentInBytes = PdfGenerator.createPDF(htmlData);

        return contentInBytes;
    }

    @Override
    public Map<String, Object> getStockConsumptionHTML(StockConsumption stockConsumption) throws Exception {
        log.debug("issueIssuer: {}", stockConsumption);

        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "stockConsumption.ftl"; // Fixed template
        Map<String, Object> stockConsumptionData = populateDirectTransferData(stockConsumption);
        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, stockConsumptionData);
        printFile.put("html", html);
        return printFile;
    }

    @Override
    @PublishStockTransaction
    public StockConsumption stockAutoConsumption(Map<String, Object> params) throws Exception {
        log.debug("stock auto consumption service: {}", params);
        String documentNumber = String.valueOf(params.get("documentNumber"));
        StockReceipt stockReceipt = getRecord("stockreceipt", "documentNumber.raw:"+ documentNumber, elasticsearchTemplate, StockReceipt.class);
        log.debug("stock receipt for consumption: {}", stockReceipt);

        TransactionType transactionType = stockReceipt.getDocument().getSourceType();
        if (TransactionType.Stock_Issue.name().equals(transactionType.getTransactionType())
            || TransactionType.Stock_Direct_Transfer.name().equals(transactionType.getTransactionType())
            || TransactionType.Inter_Unit_Stock_Issue.name().equals(transactionType.getTransactionType())) {

            HealthcareServiceCenterDTO indentStore = stockReceipt.getDocument().getIndentStore();
            BoolQueryBuilder boolQueryBuilder = boolQuery();
            boolQueryBuilder.must(matchQuery("id", indentStore.getId()));
            BoolQueryBuilder capBoolQueryBuilder = boolQuery();
            capBoolQueryBuilder.must(matchQuery("capabilities.valueSet.code", "HSC_CAPABILITY"));
            capBoolQueryBuilder.must(matchQuery("capabilities.code", "auto_consumption"));
            boolQueryBuilder.must(capBoolQueryBuilder);

            Query searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).build();
            List<HealthcareServiceCenterDTO> iStore = getRecords(searchQuery, HealthcareServiceCenterDTO.class, elasticsearchTemplate, "healthcareservicecenter");
            StockConsumption stockConsumption = receiptToStockConsumptionMapper.convertReceiptToStockConsumption(stockReceipt);
            log.debug("stock consumption after conversion: {}", stockConsumption);
            List<ConsumptionDocumentLine> lines = stockConsumption.getDocument().getLines();

            stockConsumption.getDocument().setLines(lines.stream().map(line -> {
                Item item = queryForObject("item",new CriteriaQuery(new Criteria("id").is(line.getItem().getId())), elasticsearchTemplate, Item.class);
                log.debug("auto consumption: {}, item: {}", item.getAutoStockConsumption(), item);

                BoolQueryBuilder itemStoreQuery = boolQuery().must(matchQuery("item.id", line.getItem().getId()))
                    .must(matchQuery("store.id", stockConsumption.getDocument().getConsumptionStore().getId()));
                Query itemStoreSearchQuery = new NativeSearchQueryBuilder().withQuery(itemStoreQuery).build();
                List<ItemStoreAutoConsumption> itemStoreAutoConsumptionList = getRecords(itemStoreSearchQuery, ItemStoreAutoConsumption.class, elasticsearchTemplate, "itemstoreautoconsumption");
                AutoStockConsumption itemStoreAutoStockConsumption = (isNull(itemStoreAutoConsumptionList) || itemStoreAutoConsumptionList.isEmpty()) ? AutoStockConsumption.None : itemStoreAutoConsumptionList.get(0).getAutoConsumption();

                if (AutoStockConsumption.Disabled.name().equals(item.getAutoStockConsumption().name()) || AutoStockConsumption.Disabled.name().equals(itemStoreAutoStockConsumption.name())) {
                    return null;
                }

                if (!iStore.isEmpty()) {
                    Stock stock = stockService.getStockByUniqueIdAndStoreId(line.getSku(), stockConsumption.getDocument().getConsumptionStore().getId());
                    if (isNull(stock)) {
                        String sku = StockServiceImpl.constructSKU(stockConsumption.getDocument().getConsumptionUnit().getCode(), line.getItem().getId(), line.getBatchNumber(), line.getExpiryDate(), line.getCost(), line.getMrp(), line.getConsignment());
                        stock = stockService.getStockByUniqueIdAndStoreId(sku, stockConsumption.getDocument().getConsumptionStore().getId());
                    }

                    line.setStockId(stock.getId());
                    line.setCurrentStock(new Quantity(stockService.getAvailableStock(line.getItem().getId(), line.getBatchNumber(),
                        stockConsumption.getDocument().getConsumptionStore().getId()), line.getItem().getTrackUOM()));
                    return line;

                }

                if (AutoStockConsumption.Enabled.name().equals(itemStoreAutoStockConsumption.name())) {
                    Stock stock = stockService.getStockByUniqueIdAndStoreId(line.getSku(), stockConsumption.getDocument().getConsumptionStore().getId());
                    if (isNull(stock)) {
                        String sku = StockServiceImpl.constructSKU(stockConsumption.getDocument().getConsumptionUnit().getCode(), line.getItem().getId(), line.getBatchNumber(), line.getExpiryDate(), line.getCost(), line.getMrp(), line.getConsignment());
                        stock = stockService.getStockByUniqueIdAndStoreId(sku, stockConsumption.getDocument().getConsumptionStore().getId());
                    }

                    line.setStockId(stock.getId());
                    line.setCurrentStock(new Quantity(stockService.getAvailableStock(line.getItem().getId(), line.getBatchNumber(),
                        stockConsumption.getDocument().getConsumptionStore().getId()), line.getItem().getTrackUOM()));
                    return line;

                }
                return null;
            }).filter(line -> line != null).collect(Collectors.toList()));
            if (isNull(stockConsumption.getDocument().getLines()) || stockConsumption.getDocument().getLines().isEmpty()) {
                log.info("none of the items are enabled for autoconsumption of receipt with documentNumber: {}", documentNumber);
                return null;
            }
            stockConsumption.getDocument().setDocumentType(TransactionType.Stock_Consumption);
            SourceDocument sourceDocument = new SourceDocument();
            List<SourceDocument> sourceDocumentList = new ArrayList<>();
            sourceDocument.setDocumentNumber(stockReceipt.getDocumentNumber());
            sourceDocument.setId(stockReceipt.getId());
            sourceDocument.setType(stockReceipt.getDocument().getDocumentType());
            sourceDocumentList.add(sourceDocument);
            stockConsumption.getDocument().setSourceDocument(sourceDocumentList);
            stockConsumption.getDocument().setStatus(Status.APPROVED);
            stockConsumption.getDocument().setCreatedDate(LocalDateTime.now());
            BoolQueryBuilder userQueryBuilder = boolQuery();
            userQueryBuilder.must(matchQuery("login.raw", "admin"));
            Query userQuery = new NativeSearchQueryBuilder().withQuery(userQueryBuilder).build();
            List<UserDTO> userDTOS = getRecords(userQuery, UserDTO.class, elasticsearchTemplate, "user");
            stockConsumption.getDocument().setApprovedBy(userDTOS.get(0));
            stockConsumption.getDocument().setApprovedDate(LocalDateTime.now());
            log.debug("stock auto consumption before save: {}", stockConsumption);
            StockConsumption result = save(stockConsumption, (Status.APPROVED).name());
            index(result);
            log.debug("stock consumption after save: {}", result);
            return result;
        } else {
            log.info("receipt not is not one of issue, direct transfer, inter unit stock issue: documentNumber {}", documentNumber);
        }
        return null;
    }

    private Map<String, Object> populateDirectTransferData(StockConsumption stockConsumption) {
        Map<String, Object> stockConsumptionData = new HashMap<>();
        stockConsumptionData.put("consumptionNo", stockConsumption.getDocumentNumber());
        stockConsumptionData.put("consumptionUnit", stockConsumption.getDocument().getConsumptionUnit().getName());
        stockConsumptionData.put("issueStore", stockConsumption.getDocument().getConsumptionStore().getName());
        stockConsumptionData.put("consumptionStore", stockConsumption.getDocument().getForHSC() != null?stockConsumption.getDocument().getForHSC().getName() : null);
        stockConsumptionData.put("consumptionDate", stockConsumption.getDocument().getConsumedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        stockConsumptionData.put("forDept", stockConsumption.getDocument().getForDepartment() != null ? stockConsumption.getDocument().getForDepartment().getName() : "-");
        stockConsumptionData.put("forPatient",stockConsumption.getDocument().getForPatient()!=null? stockConsumption.getDocument().getForPatient().getDisplayName():"-");
        stockConsumptionData.put("forPerson", stockConsumption.getDocument().getConsumedBy() != null ? stockConsumption.getDocument().getConsumedBy().getDisplayName() : "-");
        stockConsumptionData.put("status", stockConsumption.getDocument().getStatus().toString().replaceAll("_", " "));
        stockConsumptionData.put("issueLines", stockConsumption.getDocument().getLines());
        stockConsumptionData.put("createdBy", stockConsumption.getDocument().getCreatedBy() != null ? stockConsumption.getDocument().getConsumedBy().getDisplayName() : "-");
        stockConsumptionData.put("createdOn", stockConsumption.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockConsumptionData.put("approvedBy", stockConsumption.getDocument().getApprovedBy() != null ? stockConsumption.getDocument().getApprovedBy().getDisplayName() : "-");
        stockConsumptionData.put("approvedOn", stockConsumption.getDocument().getApprovedDate() != null ? stockConsumption.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")) : "-");
        stockConsumptionData.put("publishedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockConsumptionData.put("datetimeformatter", DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        return stockConsumptionData;
    }

    /**
     * Consumption Reversal
     */
    /**
     * @param stockConsumption
     * @param action
     * @return stockConsumption
     * @throws Exception
     */
    @Transactional
    public StockConsumption sendForApprovalForReversal(StockConsumption stockConsumption, String action) throws Exception {
        log.debug("Request to Send for Approval for reversal Consumption : {}", stockConsumption);
        StockConsumption result;
        stockConsumption.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
        if (stockConsumption.getDocument().isDraft()) {
            stockConsumption.getDocument().setDraft(false);
            stockConsumption.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Reversal_Consumption.name(), "NH", stockConsumption));
            result = save(stockConsumption);
            //Get workflow configurations
            Map<String, Object> configurations = retrieveWorkflowConfigurations(stockConsumption, true);
            //Start workflow if workflow enabled
            if ((Boolean) configurations.get("enableWorkflow")) {
                startWorkflow(result, action, configurations);
            }
        } else {
            //In case of modification
            result = save(stockConsumption);
        }
        return result;
    }

    /**
     * @param stockConsumptionReversal
     * @return stockConsumption
     * @throws Exception
     */
    @Transactional
    public StockConsumption approveReversalConsumptionDocument(StockConsumption stockConsumptionReversal) throws Exception {
        log.debug("Request to approve Reversal Consumption Documents {}", stockConsumptionReversal);
        StockConsumption exitingReversal = stockConsumptionSearchRepository.findById(stockConsumptionReversal.getId()).get();
        if (Status.APPROVED.equals(exitingReversal.getDocument().getStatus())) {
            throw new CustomParameterizedException("10222", "Consumption Reversal Document (" + stockConsumptionReversal.getDocumentNumber() + ") is already approved");
        }
        stockConsumptionReversal.getDocument().setStatus(Status.APPROVED);
        stockConsumptionReversal = save(stockConsumptionReversal);
        stockReversal(stockConsumptionReversal);
        updateReversalQuantityInConsumptionDocument(stockConsumptionReversal);
        return stockConsumptionReversal;
    }

    /**
     * Update reversal quantity in original consumption document line by line.
     * @param stockConsumptionReversal
     */
    private void updateReversalQuantityInConsumptionDocument(StockConsumption stockConsumptionReversal) {
        List<StockConsumption> consumptionList = new ArrayList<>();
        for (ConsumptionDocumentLine reversalLine : stockConsumptionReversal.getDocument().getLines()) {
            StockConsumption consumption = stockConsumptionRepository.findByDocumentNumberWithLock(reversalLine.getDocumentNumber());
            ConsumptionDocumentLine consumptionLine = consumption.getDocument().getLines().stream().filter(line ->
                line.getId().equals(reversalLine.getSourceDocument().get(0).getLineId())).findFirst().get();
            if (isNull(consumptionLine.getPreviousReturnedQuantity())) {
                consumptionLine.setPreviousReturnedQuantity(reversalLine.getQuantity().getValue());
            } else {
                consumptionLine.setPreviousReturnedQuantity(consumptionLine.getPreviousReturnedQuantity() + reversalLine.getQuantity().getValue());
            }
            if(consumptionLine.getQuantity().getValue() < consumptionLine.getPreviousReturnedQuantity()) {
                List<ErrorMessage> errorMessages = new ArrayList<>();
                Map<String, Object> source = new HashMap<>();
                source.put("itemName", consumptionLine.getItem().getName());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.SUM_OF_REVERSAL_QUANTITY_MORE_THAN_CONSUMPTION_QUANTITY, source));
                throw new FieldValidationException(errorMessages, "Validation exception");
            }
            consumptionList.add(consumption);
        }
        if(CollectionUtils.isNotEmpty(consumptionList)){
            this.saveAll(consumptionList);
        }
    }

    private List<StockEntry> stockReversal(StockConsumption stockConsumption) throws Exception {
        ConsumptionDocument document = stockConsumption.getDocument();
        List<StockEntry> stockEntries = new ArrayList<StockEntry>();
        if (CollectionUtils.isNotEmpty(document.getLines())) {
            document.getLines().forEach(line -> {
                if (line.getQuantity().getValue() > 0) {
                    StockEntry stockEntry = new StockEntry();
                    stockEntry.setStoreId(document.getForHSC() != null ? document.getForHSC().getId() : null);
                    stockEntry.setTransactionDate(document.getModifiedDate());
                    stockEntry.setTransactionId(stockConsumption.getId());
                    stockEntry.setTransactionNumber(stockConsumption.getDocumentNumber());
                    stockEntry.setTransactionType(document.getDocumentType());
                    stockEntry.setUnitId(document.getConsumptionUnit().getId());
                    stockEntry.setAvailableQuantity(line.getQuantity().getValue());
                    stockEntry.setBarCode(line.getBarCode());
                    stockEntry.setBatchNo(line.getBatchNumber());
                    stockEntry.setConsignment(line.getConsignment());
                    stockEntry.setCost(line.getCost());
                    stockEntry.setExpiryDate(line.getExpiryDate());
                    stockEntry.setItemId(line.getItem().getId());
                    stockEntry.setMrp(line.getMrp());
                    stockEntry.setOwner(line.getOwner());
                    stockEntry.setQuantity(line.getQuantity().getValue());
                    stockEntry.setSku(line.getSku());
                    stockEntry.setTransactionLineId(line.getId());
                    stockEntry.setSupplier(line.getSupplier());
                    stockEntry.uomId(line.getQuantity().getUom().getId());
                    stockEntry.setStockId(line.getStockId());
                    stockEntry.setLocatorId(line.getLocator().getId());
                    stockEntry.setUserId(stockConsumption.getDocument().getCreatedBy().getId());
                    stockEntries.add(stockEntry);
                }
            });
            stockService.stockIn(stockEntries);
        }
        return stockEntries;
    }

    @Override
    public List<ConsumptionDocumentLine> getConsumptionReversalItems(Long itemId, Long consumptionHscId, Long forHscId, Long departmentId, String mrn) throws Exception {
        List<ConsumptionDocumentLine> lines = new ArrayList<>();
        Page<StockConsumption> stockConsumptions = this.fetchStockConsumptions(itemId, consumptionHscId, forHscId, departmentId, mrn);
        log.debug("StockConsumption Query Result count:{}", stockConsumptions.getTotalElements());
        if(!stockConsumptions.isEmpty()){
            filterConsumptionLines(itemId, stockConsumptions.getContent(), lines);
        }
        return lines;
    }

    /**
     * query last 30 days consumption stocks
     * @param itemId
     * @param consumptionHscId
     * @param forHscId
     * @param departmentId
     * @param mrn
     * @return
     * @throws Exception
     */
    private Page<StockConsumption> fetchStockConsumptions(Long itemId, Long consumptionHscId, Long forHscId, Long departmentId, String mrn) throws Exception {
        StringBuilder queryBuilder = new StringBuilder(" document.lines.item.id: ").append(itemId).append(" AND document.documentType:Stock_Consumption ");
        queryBuilder.append(" AND document.consumptionStore.id: ").append(consumptionHscId);
        if (null != forHscId) queryBuilder.append(" AND document.forHSC.id: ").append(forHscId);
        if (null != departmentId) queryBuilder.append(" AND document.forDepartment.id: ").append(departmentId);
        if (null != mrn) {
            queryBuilder.append(" AND (document.forPatient.mrn.raw: ").append(mrn).append(" OR document.forPatient.tempNumber.raw: ").append(mrn).append(" )");
        }
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(30);
        queryBuilder.append(" AND document.approvedDate:[" + fromDate.toString() + " TO " + toDate.toString() + "]");

        return search(queryBuilder.toString(), PageRequest.of(0, 500, Sort.Direction.DESC, "document.consumedDate"));
    }

    private void filterConsumptionLines(final Long itemId, List<StockConsumption> stockConsumptions, List<ConsumptionDocumentLine> lines) {
        for (StockConsumption stockConsumption : stockConsumptions) {
            for (ConsumptionDocumentLine consumptionLine : stockConsumption.getDocument().getLines()) {
                if (itemId.equals(consumptionLine.getItem().getId())) {
                    consumptionLine.setDocumentNumber(stockConsumption.getDocumentNumber());
                    consumptionLine.setConsumedQuantity(consumptionLine.getQuantity().getValue().floatValue());
                    consumptionLine.getQuantity().setValue(0f);
                    consumptionLine.setConsumptionDate(stockConsumption.getDocument().getConsumedDate());
                    lines.add(consumptionLine);
                }
            }
        }
    }

    @PublishStockTransaction
    @Override
    public void consumeExternalStockConsumption(StockConsumption stockConsumption) throws Exception{
        log.debug("Request to Consume BloodBag StockConsumption: {}", stockConsumption);
            StockConsumption result = createExternalStockConsumptionOrReversal(stockConsumption);
            index(result);
    }

    /**
     * This method will get called for hinai data population
     * @param stockConsumption
     * @return stockConsumption
     * @throws Exception
     */
    private  StockConsumption createExternalStockConsumptionOrReversal(StockConsumption stockConsumption) throws Exception {
        log.debug("Request to create stock consumption document");
        validatesStockConsumption(stockConsumption);
        validateConsumptionDocument(stockConsumption);
        if (stockConsumption.getId() == null) {
            assignValuesForDirectApprove(stockConsumption);
        }
        log.debug("Request to approve {} documentNumber {}", stockConsumption.getDocument().getDocumentType(), stockConsumption.getDocumentNumber());
        if (TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())) {
            stockConsumption.getDocument().setStatus(Status.APPROVED);
            stockConsumption = save(stockConsumption);
            stockReversal(stockConsumption);
            updateReversalQuantityInConsumptionDocument(stockConsumption);
        } else {
            stockConsumption = approveConsumptionDocument(stockConsumption);
        }
        return stockConsumption;
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
