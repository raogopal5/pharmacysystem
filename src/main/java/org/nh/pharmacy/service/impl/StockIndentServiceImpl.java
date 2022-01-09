package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jbpm.services.api.model.DeployedUnit;
import org.nh.common.dto.ItemCategoryDTO;
import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.UOMDTO;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.Consumption;
import org.nh.pharmacy.domain.dto.IndentDocumentLine;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.Priority;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.StockIndentRepository;
import org.nh.pharmacy.repository.search.ItemSearchRepository;
import org.nh.pharmacy.repository.search.ItemStoreStockViewSearchRepository;
import org.nh.pharmacy.repository.search.StockIndentSearchRepository;
import org.nh.pharmacy.repository.search.UOMSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.mapper.IndentCopyMapper;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.print.PdfGenerator;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.nh.pharmacy.domain.enumeration.Context.Indent_Approval_Committee;
import static org.nh.pharmacy.util.ConfigurationUtil.getCommaSeparatedGroupCodes;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;

/**
 * Service Implementation for managing StockIndent.
 */
@Service
@Transactional
public class StockIndentServiceImpl implements StockIndentService {

    private final Logger log = LoggerFactory.getLogger(StockIndentServiceImpl.class);

    private final StockIndentRepository stockIndentRepository;

    private final StockIndentSearchRepository stockIndentSearchRepository;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final RuleExecutorService ruleExecutorService;

    private final WorkflowService workflowService;

    private final IndentCopyMapper indentCopyMapper;

    private final ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository;

    private final ItemSearchRepository itemSearchRepository;

    private final UOMSearchRepository uomSearchRepository;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ElasticSearchQueryService elasticSearchQueryService;

    private final OrganizationService organizationService;

    private final RestHighLevelClient restHighLevelClient;

    private final GroupService groupService;

    private final StockService stockService;

    private final ApplicationProperties applicationProperties;

    private final FreemarkerService freemarkerService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Value("${server.port}")
    private String portNo;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    public StockIndentServiceImpl(StockIndentRepository stockIndentRepository, StockIndentSearchRepository stockIndentSearchRepository, SequenceGeneratorService sequenceGeneratorService, RuleExecutorService ruleExecutorService, WorkflowService workflowService, IndentCopyMapper indentCopyMapper, ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository, ItemSearchRepository itemSearchRepository, UOMSearchRepository uomSearchRepository, ElasticsearchOperations elasticsearchTemplate,
                                  ElasticSearchQueryService elasticSearchQueryService, OrganizationService organizationService, RestHighLevelClient restHighLevelClient, GroupService groupService, StockService stockService,
                                  ApplicationProperties applicationProperties, FreemarkerService freemarkerService, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.stockIndentRepository = stockIndentRepository;
        this.stockIndentSearchRepository = stockIndentSearchRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.ruleExecutorService = ruleExecutorService;
        this.workflowService = workflowService;
        this.indentCopyMapper = indentCopyMapper;
        this.itemStoreStockViewSearchRepository = itemStoreStockViewSearchRepository;
        this.itemSearchRepository = itemSearchRepository;
        this.uomSearchRepository = uomSearchRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.elasticSearchQueryService = elasticSearchQueryService;
        this.organizationService = organizationService;
        this.restHighLevelClient = restHighLevelClient;
        this.groupService = groupService;
        this.stockService = stockService;
        this.applicationProperties = applicationProperties;
        this.freemarkerService = freemarkerService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a stockIndent.
     *
     * @param stockIndent the entity to save
     * @return the persisted entity
     */
    @Override
    public StockIndent save(StockIndent stockIndent) {
        log.debug("Request to save StockIndent : {}", stockIndent);
        saveValidation(stockIndent);
        if (stockIndent.getId() == null) {
            stockIndent.id(stockIndentRepository.getId());
            stockIndent.getDocument().setId(stockIndent.getId().toString());
            stockIndent.version(0);
            if (Status.DRAFT.equals(stockIndent.getDocument().getStatus())) {
                stockIndent.setDocumentNumber("DRAFT-" + stockIndent.getId());
            }
        } else {
            stockIndentRepository.updateLatest(stockIndent.getId());
            int version = stockIndent.getVersion() + 1;
            stockIndent.version(version);
        }
        stockIndent.getDocument().setDocumentNumber(stockIndent.getDocumentNumber());
        stockIndent.setLatest(true);
        generateIdsIfRequiredForLines(stockIndent);
        StockIndent result = stockIndentRepository.save(stockIndent);
        //stockIndentSearchRepository.save(result);
        return result;
    }

    private void generateIdsIfRequiredForLines(StockIndent stockIndent) {
        if (CollectionUtils.isNotEmpty(stockIndent.getDocument().getLines())) {
            Set<Long> itemIds = new HashSet<>();
            for (IndentDocumentLine indentDocumentLine : stockIndent.getDocument().getLines()) {
                if (null == indentDocumentLine.getId() || itemIds.contains(indentDocumentLine.getId())) {
                    indentDocumentLine.setId(stockIndentRepository.getId());
                }
                itemIds.add(indentDocumentLine.getId());
            }
        }
    }

    /**
     * Save stockIndent document. Called from all services for which indent is source document
     *
     * @param stockIndent
     * @return
     */
    @Override
    public StockIndent updateSourceDocumentOnDestinationModification(StockIndent stockIndent) {
        stockIndentRepository.updateLatest(stockIndent.getId());
        int version = stockIndent.getVersion() + 1;
        stockIndent.version(version);
        return stockIndentRepository.save(stockIndent);
    }

    /**
     * Save a stockIndent.
     *
     * @param stockIndent the entity to save
     * @param action      to be performed
     * @return the persisted entity
     */
    @Override
    public StockIndent save(StockIndent stockIndent, String action) throws SequenceGenerateException, BusinessRuleViolationException, FieldValidationException {
        log.debug("Request to save StockIndent with action : {}", action);
        StockIndent result;
        switch (action) {
            case "SENDFORAPPROVAL":
                log.debug("Request to send approval StockIndent : {}", stockIndent);
                validateSendForApproval(stockIndent);
                validateIndentDocument(stockIndent);
                assignValidityDate(stockIndent);
                stockIndent.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
                if (stockIndent.getDocument().isDraft()) {
                    stockIndent.getDocument().setDraft(false);
                    stockIndent.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Indent.name(), "NH", stockIndent));
                    result = save(stockIndent);
                    //Get workflow configurations
                    Map<String, Object> configurations = retrieveWorkflowConfigurations(stockIndent, true);
                    //Start workflow if workflow enabled
                    if ((Boolean) configurations.get("enableWorkflow")) {
                        startWorkflow(result, action, configurations);
                    }
                    return result;
                }
                break;
            case "APPROVED":
                log.debug("Request to approve StockIndent : {}", stockIndent);
                //validateDocumentApprover(stockIndent);
                validateIndentDocument(stockIndent);
                validateStockIndent(stockIndent);
                stockIndent.getDocument().setStatus(Status.APPROVED);
                break;
            case "REJECTED":
                log.debug("Request to reject StockIndent : {}", stockIndent);
//                validateIndentDocument(stockIndent);
//                validateStockIndent(stockIndent);
                stockIndent.getDocument().setStatus(Status.REJECTED);
                break;
            default:
                log.debug("Request to save as draft StockIndent : {}", stockIndent);
                if (stockIndent.getDocument().getStatus() == null) {
                    stockIndent.getDocument().setStatus(Status.DRAFT);
                    validateDraft(stockIndent);
                } else {
                    validateIndentDocument(stockIndent);
                }
                break;
        }
        return save(stockIndent);
    }

    /**
     * Get all the stockIndents.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockIndent> findAll(Pageable pageable) {
        log.debug("Request to get all StockIndents");
        Page<StockIndent> result = stockIndentRepository.findAll(pageable);
        return result;
    }

    /**
     * Validate entity
     *
     * @param stockIndent
     * @throws BusinessRuleViolationException
     */
    public void validateStockIndent(StockIndent stockIndent) throws BusinessRuleViolationException {
        //ruleExecutorService.executeByGroup(stockIndent, "stock_indent_rules");
    }


    public void validateSendForApproval(StockIndent stockIndent) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockIndent, "send_for_approval_validation");
    }

    public void validateDocumentApprover(StockIndent stockIndent) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockIndent, "stock_indent_document_approver_validation");
    }

    public void validateDelete(StockIndent stockIndent) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockIndent, "delete_validation");
    }

    /**
     * Get one stockIndent by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockIndent findOne(Long id) {
        log.debug("Request to get StockIndent : {}", id);
        StockIndent stockIndent = stockIndentRepository.findOne(id);
        if (stockIndent != null)
            if (stockIndent.getDocument().getStatus() == Status.DRAFT) {
                for (IndentDocumentLine indentDocumentLine : stockIndent.getDocument().getLines()) {
                    Page<ItemStoreStockView> itemStoreStockViews = itemStoreStockViewSearchRepository.search(queryStringQuery("itemId:" + indentDocumentLine.getItem().getId() + " store.id:" + stockIndent.getDocument().getIndentStore().getId()).defaultOperator(Operator.AND), PageRequest.of(0,1));
                    if (itemStoreStockViews.iterator().hasNext()) {
                        ItemStoreStockView itemStoreStockView = itemStoreStockViews.iterator().next();
                        populateAvailableStockAndQuantity(itemStoreStockView, indentDocumentLine);
                    }
                }
            }
        return stockIndent;
    }

    /**
     * Get one stockIndent by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockIndent findOne(Long id, Integer version) {
        log.debug("Request to get stockIndent : {}", id, version);
        StockIndent stockIndent = stockIndentRepository.findById(new DocumentId(id, version)).get();
        return stockIndent;
    }

    /**
     * Get one stockIndent by id.
     *
     * @param Id the id of the entity
     * @return the entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public StockIndent findDetachedOne(Long Id) {
        log.debug("Request to get StockIndent : {}", Id);
        return stockIndentRepository.findOne(Id);
    }

    /**
     * Execute workflow
     *
     * @param stockIndent the entity to save
     * @param transition  to be performed
     * @param taskId      task Id
     * @return stockIndent object
     * @throws Exception
     */
    @Override
    @Transactional
    public StockIndent executeWorkflow(StockIndent stockIndent, String transition, Long taskId) throws Exception {
        StockIndent result;
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
            case "Modify":
                action = "DRAFT";
                stockIndent.getDocument().setStatus(Status.DRAFT);
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }
        //Save stock indent
        result = save(stockIndent, action);

        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("action_out", action);
        workflowService.completeUserTask(taskId, userId, results);
        return result;
    }

    /**
     * Start workflow
     *
     * @param stockIndent
     * @param action
     * @param configurations
     */
    public void startWorkflow(StockIndent stockIndent, String action, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", stockIndent.getId());
            content.put("document_type", stockIndent.getDocument().getDocumentType());
            content.put("group_id", configurations.get("groupIds"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_number", stockIndent.getDocumentNumber());
            params.put("indent_date", String.valueOf(stockIndent.getDocument().getIndentDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm"))));
            params.put("from_store", stockIndent.getDocument().getIndentStore().getName());
            params.put("unit_id", String.valueOf(stockIndent.getDocument().getIndentUnit().getId()));
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
        StockIndent stockIndent = stockIndentRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(stockIndent, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) :workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,stockIndent.getDocument().getCreatedBy().getLogin());
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
     * @param stockIndent
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(StockIndent stockIndent, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_stockindent_enable_workflow", stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_stockindent_workflow_definition", stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(Indent_Approval_Committee, stockIndent.getDocument().getIndentUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    @Override
    public StockIndent copyStockIndent(Long id, String docNum) {
        String query = id != null ? "id:" + id : "documentNumber:" + docNum;
        StockIndent copyStockIndent = null;
        Page<StockIndent> page = stockIndentSearchRepository.search(queryStringQuery(query), PageRequest.of(0,1));
        if (page.iterator().hasNext()) {
            copyStockIndent = indentCopyMapper.copyStockIndent(page.iterator().next());
        }
        return copyStockIndent;
    }

    @Override
    public Map<String, Object> importStockIndentDocumentLine(MultipartFile file, Long storeId, Long indentStoreId) throws IOException {
        log.debug("Request to import StockIndentDocumentLine ");
        CSVParser csvFileParser = null;
        BufferedReader bufferedReader = null;
        Map<String, Object> result = new HashMap<>();
        Map<String, IndentDocumentLine> acceptedItemMap = new LinkedHashMap<>();
        List<Map<String, Object>> rejectedItemList = new ArrayList<>();
        //Map rejectedItemMap = new HashMap();
        if (!file.isEmpty()) {
            //Create the CSVFormat object with the header mapping
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
            InputStream inputStream = new BufferedInputStream(file.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //initialize CSVParser object
            csvFileParser = new CSVParser(bufferedReader, csvFileFormat);

            //Get a list of CSV file records
            List csvRecords = csvFileParser.getRecords();

            //Read the CSV file records starting from the second record to skip the header
            for (int i = 0; i < csvRecords.size(); i++) {
                Map rejectedItemMap = new LinkedHashMap();
                CSVRecord record = (CSVRecord) csvRecords.get(i);
                Boolean isNumber = true;
                Boolean isValidCSV = true;
                try {
                    Float.valueOf(record.get(3));
                    isNumber = true;
                } catch (ArrayIndexOutOfBoundsException exc) {
                    mapRejectedItem(PharmacyErrorCodes.INVALID_LINE, record.get(1), record.get(0), i, rejectedItemMap);
                    isValidCSV = false;
                } catch (NumberFormatException e) {
                    isNumber = false;
                }
                if (isValidCSV) {
                    if (isNumber) {
                        if (Float.valueOf(record.get(3)) > 0) {
                            if (acceptedItemMap.get(record.get(0)) == null) {
                                if (!record.get(0).isEmpty()) {
                                    Page<ItemStoreStockView> itemStoreStockViews = itemStoreStockViewSearchRepository.search(queryStringQuery("code.raw:\"" + record.get(0) + "\" store.id:" + storeId)
                                        .defaultOperator(Operator.AND), PageRequest.of(0,1));
                                    if (itemStoreStockViews.iterator().hasNext()) {
                                        Page<ItemStoreStockView> indentItemStoreStockViews = itemStoreStockViewSearchRepository.search(queryStringQuery("code.raw:\"" + record.get(0) + "\" store.id:" + indentStoreId)
                                            .defaultOperator(Operator.AND), PageRequest.of(0,1));
                                        ItemStoreStockView indentItemStoreStockView = indentItemStoreStockViews.iterator().hasNext() ? indentItemStoreStockViews.iterator().next() : null;
                                        ItemStoreStockView itemStoreStockView = itemStoreStockViews.iterator().next();
                                        if (!record.get(2).isEmpty()) {
                                            Page<UOM> UOMPage = uomSearchRepository.search(queryStringQuery("name.raw:\"" + record.get(2) + "\""), PageRequest.of(0,1));
                                            Page<Item> itemPage = itemSearchRepository.search(queryStringQuery("code.raw:\""+record.get(0)+"\""),PageRequest.of(0,1));
                                            Item item=null;
                                            if(itemPage.iterator().hasNext()){
                                                item=itemPage.iterator().next();
                                            }
                                            if (UOMPage.iterator().hasNext() && item!=null && item.getTrackUOM().getId().equals(mapUOM(record.get(2)).getId())) {
                                                if (UOMPage.iterator().hasNext()) {
                                                    IndentDocumentLine line = mapIndentDocumentLine(record.get(2), Float.valueOf(record.get(3)), itemStoreStockView, indentItemStoreStockView);
                                                    acceptedItemMap.put(record.get(0), line);
                                                } else {
                                                    //rejectedItemMap.put(record.get(0), "UOM is not valid");
                                                    mapRejectedItem(PharmacyErrorCodes.INVALID_UOM, record.get(1), record.get(0), i, rejectedItemMap);
                                                }
                                            } else {
                                                mapRejectedItem(PharmacyErrorCodes.UOM_NOT_SUPPORTED, record.get(1), record.get(0), i, rejectedItemMap);
                                            }
                                        } else {
                                            mapRejectedItem(PharmacyErrorCodes.INVALID_UOM, record.get(1), record.get(0), i, rejectedItemMap);

                                        }
                                    } else {
                                        mapRejectedItem(PharmacyErrorCodes.UNAVAILABLE_ITEM, record.get(1), record.get(0), i, rejectedItemMap);
                                    }
                                } else {
                                    //rejectedItemMap.put(record.get(0), "Item Not Found");
                                    mapRejectedItem(PharmacyErrorCodes.UNAVAILABLE_ITEM, record.get(1), record.get(0), i, rejectedItemMap);
                                }
                            } else {
                                IndentDocumentLine line = acceptedItemMap.get(record.get(0));
                                Float quantityValue = line.getQuantity().getValue() + Float.valueOf(record.get(3));
                                line.getQuantity().setValue(quantityValue);
                                acceptedItemMap.put(record.get(0), line);
                                //rejectedItemMap.put(record.get(0), "Found Duplicate Records hence quantity is sumed up");
                                mapRejectedItem(PharmacyErrorCodes.ADDING_DUPLICATE_RECORDS, record.get(1), record.get(0), i, rejectedItemMap);
                            }
                        } else {
                            mapRejectedItem(PharmacyErrorCodes.ZERO_INDENT_VALUE, record.get(1), record.get(0), i, rejectedItemMap);
                        }
                    } else {
                        //rejectedItemMap.put(record.get(0), "Quantity field is not numeric");
                        mapRejectedItem(PharmacyErrorCodes.NOT_NUMERIC_VALUE, record.get(1), record.get(0), i, rejectedItemMap);
                    }
                }
                if (!rejectedItemMap.isEmpty()) {
                    rejectedItemList.add(rejectedItemMap);
                }
            }
        }
        result.put("INDENT_DOC_LINE", acceptedItemMap.values());
        result.put("WARNING", rejectedItemList);
        return result;
    }

    private void mapRejectedItem(String errorCode, String itemName, String itemCode, int lineNo, Map<String, Object> rejectedItem) {
        rejectedItem.put("errorCode", errorCode);
        rejectedItem.put("itemName", itemName);
        rejectedItem.put("itemCode", itemCode);
        rejectedItem.put("lineNo", (lineNo + 2));
    }

    private IndentDocumentLine mapIndentDocumentLine(String primaryUOM, Float quantity, ItemStoreStockView itemStoreStockView, ItemStoreStockView indentItemStoreStockView) {
        IndentDocumentLine line = new IndentDocumentLine();
        line.setItem(mapItem(itemStoreStockView));
        line.setQuantity(mapQuantity(primaryUOM, quantity));
        Float availableQty = 0f;
        if (indentItemStoreStockView != null) {
            Page<Stock> stocks = stockService.getBatchDetails(Long.valueOf(indentItemStoreStockView.getStore().get("id").toString()), itemStoreStockView.getCode(), null, "-", false,null);
            for (Stock stock : stocks.getContent()) {
                availableQty += stock.getQuantity();
            }
            //line.setAvailableStock(indentItemStoreStockView.getAvailableStock());
            line.setConsumedQuantity(mapConsumption(indentItemStoreStockView));
            line.setTransitQuantity(indentItemStoreStockView.getTransitQty());
        } else {
            //line.setAvailableStock(Float.valueOf(0));
            line.setConsumedQuantity(mapConsumption(itemStoreStockView));
            line.getConsumedQuantity().setConsumedInCurrentMonth(Float.valueOf(0));
            line.getConsumedQuantity().setConsumedInLastMonth(Float.valueOf(0));
            line.setTransitQuantity(Float.valueOf(0));
        }
        line.setAvailableStock(availableQty);
        return line;
    }

    private Quantity mapQuantity(String uomName, Float quantityValue) {
        Quantity quantity = new Quantity();
        quantity.setUom(mapUOM(uomName));
        quantity.setValue(quantityValue);
        return quantity;
    }

    private UOMDTO mapUOM(String uomName) {
        Page<UOM> UOMPage = uomSearchRepository.search(queryStringQuery("name.raw:\"" + uomName + "\""), PageRequest.of(0,1));
        UOM uom = UOMPage.iterator().next();
        return mapUOM(uom);
    }

    private Consumption mapConsumption(ItemStoreStockView itemStoreStockView) {
        Consumption consumption = new Consumption();
        consumption.setConsumedInLastMonth(itemStoreStockView.getConsumedQtyLastMonth());
        consumption.setConsumedInCurrentMonth(itemStoreStockView.getConsumedQtyCurrMonth());
        return consumption;
    }

    private ItemDTO mapItem(ItemStoreStockView itemStoreStockView) {
        Page<Item> page = itemSearchRepository.search(queryStringQuery("id:" + itemStoreStockView.getItemId()), PageRequest.of(0,1));
        Item item = page.iterator().next();
        ItemDTO tempItem = new ItemDTO();
        tempItem.setId(itemStoreStockView.getItemId());
        tempItem.setCode(itemStoreStockView.getCode());
        tempItem.setName(itemStoreStockView.getName());
        tempItem.setCategory(mapItemCategory(item.getCategory()));
        tempItem.setPurchaseUOM(mapUOM(item.getPurchaseUOM()));
        tempItem.setSaleUOM(mapUOM(item.getSaleUOM()));
        tempItem.setTrackUOM(mapUOM(item.getTrackUOM()));
        return tempItem;
    }

    private UOMDTO mapUOM(UOM uom) {
        if (uom == null) return null;
        UOMDTO uomDTO = new UOMDTO();
        uomDTO.setId(uom.getId());
        uomDTO.setCode(uom.getCode());
        uomDTO.setName(uom.getName());
        //TODO uncomment & fix, if its required
        //uomDTO.setUomType(uom.getUomType());
        //uomDTO.setBaseUOM(uom.getBaseUOM());
        return uomDTO;
    }

    private ItemCategoryDTO mapItemCategory(ItemCategory itemCategory) {
        if (itemCategory == null) return null;
        ItemCategoryDTO itemCategoryDTO = new ItemCategoryDTO();
        itemCategoryDTO.setId(itemCategory.getId());
        itemCategoryDTO.setCode(itemCategory.getCode());
        itemCategoryDTO.setActive(itemCategory.isActive());
        itemCategoryDTO.setGroup(itemCategory.isGroup());
        itemCategoryDTO.setDescription(itemCategory.getDescription());
        return itemCategoryDTO;
    }

    /**
     * Delete the  stockIndent by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) throws BusinessRuleViolationException {
        log.debug("Request to delete StockIndent : {}", id);
        validateDelete(stockIndentRepository.findOne(id));
        stockIndentRepository.delete(id);
        stockIndentSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockIndent corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockIndent> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockIndents for query {}", query);
        return stockIndentSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.status")
            .field("document.issueUnit.name").field("document.issueStore.name")
            .field("document.indentStore.name")
            .field("document.indentUnit.name")
            .defaultOperator(Operator.AND), pageable);
    }

    /**
     * Search for the stockIndent corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockIndent> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockIndents for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.status")
                .field("document.issueUnit.name").field("document.issueStore.name")
                .field("document.indentStore.name")
                .field("document.indentUnit.name")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return ElasticSearchUtil.getPageRecords(searchQuery, StockIndent.class, elasticsearchTemplate, "stockindent");
    }

    /**
     * Get the stockIndent status count corresponding to the query.
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
                .field("documentNumber").field("document.status")
                .field("document.issueUnit.name").field("document.issueStore.name")
                .field("document.indentStore.name")
                .field("document.indentUnit.name")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate,"stockindent");
        Terms aggregation1 = aggregations.get("status_count");
        for (Terms.Bucket bucket : aggregation1.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Feature");
        stockIndentSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockIndent latest=true");
        List<StockIndent> data = stockIndentRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockIndentSearchRepository.saveAll(data);
        }
    }

    private void validateDraft(StockIndent stockIndent) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockIndent.getDocument().getIssueStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_STORE));
        }
        if (stockIndent.getDocument().getIndentStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_STORE));
        }
        if (stockIndent.getDocument().getIssueUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_UNIT));
        }
        if (stockIndent.getDocument().getIndentUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_UNIT));
        }
        if (stockIndent.getDocument().getDocumentType() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_DOCUMENT_TYPE));
        }
        if (stockIndent.getDocument().getIndenterName() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_INDENTER_NAME));
        }
        if (stockIndent.getDocument().getCreatedDate() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CREATED_DATE));
        }
        if (stockIndent.getDocument().getStatus() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STATUS));
        }
        if (errorMessages.isEmpty()) {

            Boolean isIndentValid = true;
            Boolean isIndentReversalPending = true;

            String restrictionHour = retrieveStockIndentConfiguration(stockIndent, TransactionType.Inter_Unit_Stock_Indent.equals(stockIndent.getDocument().getDocumentType()) ? "athma_stockindent_restriction_interbranch_openreceipt_duration_hours" : "athma_stockindent_restriction_local_openreceipt_duration_hours");
            if (restrictionHour != null && stockIndent.getId() == null) {// Validate only for new indent
                Query issueQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryStringQuery("document.issueStore.id:" + stockIndent.getDocument().getIssueStore().getId()
                        + " document.indentStore.id:" + stockIndent.getDocument().getIndentStore().getId() + " document.status.raw:" + Status.APPROVED).defaultOperator(Operator.AND))
                    .addAggregation(AggregationBuilders.min("approved_date").field("document.approvedDate")).build();

                List<StockIssue> stockIssues = ElasticSearchUtil.getRecords(issueQuery, StockIssue.class, elasticsearchTemplate, "stockissue");
                if (!stockIssues.isEmpty()) {
                    StockIssue stockIssue = stockIssues.iterator().next();
                    LocalDateTime approvedDate = stockIssue.getDocument().getApprovedDate();
                    if (approvedDate != null) {
                        LocalDateTime validPeriod = approvedDate
                            .plusHours(Long.parseLong((String) restrictionHour));
                        if (validPeriod.isBefore(LocalDateTime.now())) {
                            log.error("validateDraft() Stock stockIssue:{} pending issue.", stockIssue.getDocumentNumber());
                            isIndentValid = false;
                        }
                    }
                }
            }

            restrictionHour = retrieveStockIndentConfiguration(stockIndent, "athma_stockindent_restriction_local_openreversal_duration_hours");
            if (restrictionHour != null && stockIndent.getId() == null) {// Validate only for new indent

                Query reversalQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryStringQuery("document.issueStore.id:" + stockIndent.getDocument().getIssueStore().getId()
                        + " document.indentStore.id:" + stockIndent.getDocument().getIndentStore().getId() + " document.status.raw:" + Status.APPROVED).defaultOperator(Operator.AND))
                    .addAggregation(AggregationBuilders.min("approved_date").field("document.approvedDate")).build();

                List<StockReversal> stockReversal = ElasticSearchUtil.getRecords(reversalQuery, StockReversal.class, elasticsearchTemplate, "stockreversal");
                if (!stockReversal.isEmpty()) {
                    StockReversal stockRevrsal= stockReversal.iterator().next();
                    LocalDateTime approvedDate = stockRevrsal.getDocument().getApprovedDate();
                    if (approvedDate != null) {
                        LocalDateTime validPeriod = approvedDate
                            .plusHours(Long.parseLong((String) restrictionHour));
                        if (validPeriod.isBefore(LocalDateTime.now())) {
                            log.error("validateDraft() Stock reversal:{} pending issue.", stockRevrsal.getDocumentNumber());
                            isIndentReversalPending = false;
                        }
                    }
                }
            }
            if (!isIndentValid) {
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.INDENT_NOT_ALLOWED));
            }
            if (!isIndentReversalPending) {
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.INDENT_NOT_ALLOWED_FOR_OPEN_REVERSAL));
            }

        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    private String retrieveStockIndentConfiguration(StockIndent stockIndent, String key) {
        String configurationValue = null;
        try {
            configurationValue = ConfigurationUtil.getConfigurationData(key, stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
        } catch (Exception e) {
            log.error("Configuration value is not set for " + key);
        }
        return configurationValue;
    }

    private void validateIndentDocument(StockIndent stockIndent) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        int correctionQuantityCounter = 0;
        if (stockIndent.getDocument().getLines() != null) {
            for (IndentDocumentLine line : stockIndent.getDocument().getLines()) {
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
                    if (line.getQuantity().getValue() == 0) correctionQuantityCounter++;
                } else {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY_AND_UOM, source));
                }
            }
            if (correctionQuantityCounter > 0) {
                Map<String, Object> source = new HashMap<String, Object>();
                source.put("document", stockIndent.getDocumentNumber());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ALL_INDENT_QUANTITY_MUST_BE_POSITIVE));
            }
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockIndent.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_INDENT_DOCUMENT_LINES));
        }
        try {
            validateDraft(stockIndent);
            if (!errorMessages.isEmpty()) {
                throw new FieldValidationException(errorMessages, "Validation exception");
            }
        } catch (FieldValidationException exception) {
            exception.getErrorMessages().addAll(errorMessages);
            throw exception;
        }
    }

    public void assignValidityDate(StockIndent stockIndent) {
        String validityDays = retrieveStockIndentConfiguration(stockIndent, "athma_stockindent_autoclose_local_period_days");
        if (validityDays != null && validityDays.trim().matches("\\d+")) {
            stockIndent.getDocument().setIndentValidDate(stockIndent.getDocument().getIndentDate().plusDays(Long.valueOf(validityDays)).withHour(23).withMinute(59).withSecond(59).withNano(0));
        }
    }

    public IndentDocumentLine populateAvailableStockAndQuantity(ItemStoreStockView itemStoreStockView, IndentDocumentLine indentDocumentLine) {
        indentDocumentLine.setAvailableStock(itemStoreStockView.getAvailableStock());
        indentDocumentLine.setConsumedQuantity(mapConsumption(itemStoreStockView));
        return indentDocumentLine;
    }


    @Override
    public void doCloseBySystem() {

        LocalDateTime currentDate = LocalDate.now().atStartOfDay();

        String processId = null;

        QueryBuilder orClauseTermQuery = termsQuery("document.status.raw", Status.APPROVED, Status.PARTIALLY_PROCESSED
            , Status.PARTIALLY_RECEIVED, Status.PARTIALLY_ISSUED, Status.REVERSAL_PENDING);

        RangeQueryBuilder dateRangeQuery = rangeQuery("document.indentValidDate").lt(currentDate.toString());

        QueryBuilder queryForRangeAndOr = boolQuery().should(orClauseTermQuery).must(dateRangeQuery);

        QueryBuilder querySearch = QueryBuilders.constantScoreQuery(queryForRangeAndOr);

        Iterator<StockIndent> stockIndentIterable = stockIndentSearchRepository.search(querySearch).iterator();

        while (stockIndentIterable.hasNext()) {
            StockIndent stockIndent = stockIndentIterable.next();
            stockIndent.getDocument().setStatus(Status.CLOSED);
            stockIndent.getDocument().setModifiedDate(LocalDateTime.now());
            stockIndent = save(stockIndent);
            index(stockIndent);
            try {
                boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_stockindent_enable_workflow", stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
                if (isWorkflowEnabled) {
                    processId = ConfigurationUtil.getConfigurationData("athma_stockindent_workflow_definition", stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
                }
            } catch (Exception e) {
                //Do nothing
            }
            if (processId != null) {
                workflowService.abortActiveProcessInstance(processId, "document_number", stockIndent.getDocumentNumber(), "admin");
            }
        }
    }

    @Override
    public void autoRejectBySystem() {
        Query query = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("document.status.raw:" + Status.WAITING_FOR_APPROVAL))
            .withPageable(PageRequest.of(0, 10000))
            .build();

        LocalDateTime currentDate = LocalDate.now().atStartOfDay();
        Iterator<StockIndent> stockIndentIterator = ElasticSearchUtil.getRecords(query, StockIndent.class, elasticsearchTemplate, "stockindent").listIterator();

        while (stockIndentIterator.hasNext()) {
            StockIndent stockIndent = stockIndentIterator.next();
            String daysForRejection = null;
            try {
                daysForRejection = ConfigurationUtil.getConfigurationData("athma_stockindent_autorejection_duration_days", stockIndent.getDocument().getIndentStore().getId(), stockIndent.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            } catch (Exception e) {
            }
            LocalDateTime modifiedDate = stockIndent.getDocument().getModifiedDate();
            if (daysForRejection != null && modifiedDate != null) {
                if (modifiedDate.isBefore(currentDate.now().minusDays(Long.parseLong(daysForRejection)))) {
                    stockIndent.getDocument().setStatus(Status.REJECTED);
                    stockIndent = save(stockIndent);
                    index(stockIndent);
                }
            }
        }
    }

    @Override
    public void index(StockIndent stockIndent) {
        stockIndentSearchRepository.save(stockIndent);
    }

    @Override
    public Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException {
        log.debug("Gell all documents which is related to given stockindent document:-" + documentNumber);

        Set<RelatedDocument> relatedDocumentList = new LinkedHashSet<>();
        Map<String, Set<RelatedDocument>> finalList = new LinkedHashMap<>();

        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest issueQueryReq= new SearchRequest("stockissue");
        SearchSourceBuilder issueSourceQueryReq = new SearchSourceBuilder().size(100)
            .query(QueryBuilders.queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + documentNumber))
            .sort(SortBuilders.fieldSort("document.createdDate").order(SortOrder.DESC));
        issueQueryReq.source(issueSourceQueryReq);

        SearchRequest stockreceiptQueryReq= new SearchRequest("stockreceipt");
        SearchSourceBuilder stockreceiptSourceQueryReq = new SearchSourceBuilder().size(100)
            .query(QueryBuilders.queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + documentNumber))
            .sort(SortBuilders.fieldSort("document.createdDate").order(SortOrder.DESC));
        stockreceiptQueryReq.source(stockreceiptSourceQueryReq);

        SearchRequest stockreversalQueryReq= new SearchRequest("stockreversal");
        SearchSourceBuilder stockreversalSourceQueryReq = new SearchSourceBuilder().size(100)
            .query(QueryBuilders.queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + documentNumber))
            .sort(SortBuilders.fieldSort("document.createdDate").order(SortOrder.DESC));
        stockreversalQueryReq.source(stockreversalSourceQueryReq);

        request.add(issueQueryReq);request.add(stockreceiptQueryReq);request.add(stockreversalQueryReq);
        MultiSearchResponse mSearchResponse = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);
        MultiSearchResponse.Item[] items = mSearchResponse.getResponses();

        for (MultiSearchResponse.Item item : items) {
            SearchHit[] hits = item.getResponse().getHits().getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> sourceDocument = (Map<String, Object>) source.get("document");
                TransactionType txnType = TransactionType.findByTransactionType(sourceDocument.get("documentType").toString());
                String dispDocumentType = txnType.getTransactionTypeDisplay();
                if (null != finalList && null != finalList.get(dispDocumentType))
                    relatedDocumentList = finalList.get(dispDocumentType);
                else
                    relatedDocumentList = new LinkedHashSet<>();

                RelatedDocument relDoc = new RelatedDocument();
                relDoc.setId(sourceDocument.get("id").toString());
                relDoc.setDocumentType(TransactionType.valueOf(sourceDocument.get("documentType").toString()));
                relDoc.setDocumentNumber(sourceDocument.get("documentNumber").toString());
                relDoc.setStatus(Status.valueOf(sourceDocument.get("status").toString()));
                relDoc.setCreatedDate(LocalDateTime.parse(sourceDocument.get("createdDate").toString()));
                relatedDocumentList.add(relDoc);
                finalList.put(dispDocumentType, relatedDocumentList);
            }
        }
        return finalList;
    }

    private void saveValidation(StockIndent stockIndent) {
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(stockIndent.getDocument().getIndentUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with logged in unit");
        }
    }

    /**
     * creates stock-indent html to print
     * @param indentId
     * @param documentNumber
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getStockIndentHTMLByIndentId(Long indentId, String documentNumber) throws Exception {
        log.debug("indentId: {}, indentNumber: {}", indentId, documentNumber);
        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "stock-indent.ftl"; // Fixed template
        StockIndent stockIndent = getStockIndent(indentId,documentNumber);
        String fileName = stockIndent.getDocument().getDocumentNumber();
        printFile.put("fileName", fileName);
        Map<String, Object> indentData = populateIndentData(stockIndent);
        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, indentData);
        printFile.put("html", html);
        byte[] contentInBytes = html.getBytes();
        printFile.put("content", contentInBytes);
      if (!(Status.DRAFT.equals(stockIndent.getDocument().getStatus()) || Status.PENDING_APPROVAL.equals(stockIndent.getDocument().getStatus()))){
            createHTMLFile(html, fileName);
        }
        return printFile;
    }

    /**
     * returns stock indent based on id or indent number
     * @param indentId
     * @param documentNumber
     * @return
     * @throws Exception
     */
    private StockIndent getStockIndent(Long indentId, String documentNumber) throws Exception{
        CriteriaQuery query = null;

        if(indentId != null){
            query = new CriteriaQuery(new Criteria("id").is(indentId));
        } else if(documentNumber != null) {
            query = new CriteriaQuery(new Criteria("documentNumber.raw").is(documentNumber));
        }

        if(query == null) {
            throw new Exception("Atleast Id or Indent Number should be provided");
        }
        return ElasticSearchUtil.queryForObject("stockindent",query, elasticsearchTemplate, StockIndent.class);
    }

    /**
     *
     * @param stockIndent
     * @return
     */
    private Map<String, Object> populateIndentData(StockIndent stockIndent) {
        org.nh.pharmacy.domain.User user = ElasticSearchUtil.getRecord("user","login.raw:"+SecurityUtils.getCurrentUserLogin().get(), elasticsearchTemplate, org.nh.pharmacy.domain.User.class);
        Map<String, Object> stockIndentData = new HashMap<>();
        stockIndentData.put("indentNumber", stockIndent.getDocumentNumber());
        stockIndentData.put("indentDate", stockIndent.getDocument().getIndentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockIndentData.put("indentUnit", stockIndent.getDocument().getIndentUnit().getName());
        stockIndentData.put("indentStore", stockIndent.getDocument().getIndentStore().getName());
        stockIndentData.put("issueUnit", stockIndent.getDocument().getIssueUnit().getName());
        stockIndentData.put("issueStore", stockIndent.getDocument().getIssueStore().getName());
        stockIndentData.put("status", stockIndent.getDocument().getStatus().getStatusDisplay());
        stockIndentData.put("priority", stockIndent.getDocument().getPriority().equals(Priority.ROUTINE)? "Normal":"Urgent");
        stockIndentData.put("lineItems", stockIndent.getDocument().getLines());
        stockIndentData.put("createdBy", stockIndent.getDocument().getIndenterName()!=null?stockIndent.getDocument().getIndenterName().getDisplayName(): "-");
        stockIndentData.put("createdOn", stockIndent.getDocument().getCreatedDate()!=null?stockIndent.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")):"-");
        stockIndentData.put("approvedBy",stockIndent.getDocument().getApprovedBy()!=null?stockIndent.getDocument().getApprovedBy().getDisplayName():"-");
        stockIndentData.put("approvedOn",stockIndent.getDocument().getApprovedDate()!=null?stockIndent.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")):"-");
        stockIndentData.put("publishedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockIndentData.put("publishedBy", user.getDisplayName());
        return stockIndentData;
    }

    /**
     * Create html file
     *
     * @param html
     * @param fileName
     */
    private void createHTMLFile(String html, String fileName) {
        FileOutputStream fop = null;
        File file = null;
        try {
            file = new File(applicationProperties.getAthmaBucket().getPrintSaveFile().concat(File.separator).concat(fileName + ".html"));
            fop = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = html.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] getStockIndentPdfByIndentId(Long indentId, String documentNumber, String original) throws Exception {
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;
            Map<String, Object> outData = this.getStockIndentHTMLByIndentId(indentId, documentNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPDF(htmlData);
        return contentInBytes;
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<StockIndent> search = this.search("documentNumber.raw:" + documentNumber, PageRequest.of(0,1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        StockIndent stockIndent = search.iterator().next();
        Map<String, Object> configurations = retrieveWorkflowConfigurations(stockIndent, true);
        //Start workflow if workflow enabled
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(stockIndent, "SENDFORAPPROVAL", configurations);
        }
    }

    private String getGroupData(Context context, Long unitId) {
        String cacheKey = "PHR: context:"+context.name()+" AND active:true AND partOf.id:"+unitId+" !_exists_:partOf";
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return pharmacyRedisCacheService.getCommaSeparatedGroupCodes(context,unitId,elasticsearchTemplate,cacheKey);
        }else {
            return getCommaSeparatedGroupCodes(context, unitId, elasticsearchTemplate);
        }
    }

}
