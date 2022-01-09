package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jbpm.services.api.model.DeployedUnit;
import org.nh.billing.domain.dto.ItemTaxMapping;
import org.nh.common.dto.ItemCategoryDTO;
import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.UOMDTO;
import org.nh.common.dto.UserDTO;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.aop.producer.StockServiceAspect;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.Priority;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.ItemRepository;
import org.nh.pharmacy.repository.LocatorRepository;
import org.nh.pharmacy.repository.StockIssueRepository;
import org.nh.pharmacy.repository.UOMRepository;
import org.nh.pharmacy.repository.search.StockIssueSearchRepository;
import org.nh.pharmacy.repository.search.StockReversalSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.mapper.IssueMapper;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.print.PdfGenerator;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static java.util.Objects.nonNull;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.domain.enumeration.Context.DirectTransfer_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.Context.Issue_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.TransactionType.*;
import static org.nh.pharmacy.util.ConfigurationUtil.getCommaSeparatedGroupCodes;
import static org.nh.pharmacy.util.ElasticSearchUtil.getRecord;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;

/**
 * Service Implementation for managing StockIssue.
 */
@Service
@Transactional
public class StockIssueServiceImpl implements StockIssueService {

    private final Logger log = LoggerFactory.getLogger(StockIssueServiceImpl.class);

    private final StockIssueRepository stockIssueRepository;

    private final StockIssueSearchRepository stockIssueSearchRepository;

    private final StockService stockService;

    private final MessageChannel stockIssueChannel;

    private final MessageChannel stockTransitChannel;

    private final StockIndentService stockIndentService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final StockReversalSearchRepository stockReversalSearchRepository;

    private final IssueMapper issueMapper;

    private final WorkflowService workflowService;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final UOMRepository uomRepository;

    private final RestHighLevelClient restHighLevelClient;

    private final LocatorRepository locatorRepository;

    private final RuleExecutorService ruleExecutorService;

    private final ItemRepository itemRepository;

    private final ElasticSearchQueryService elasticSearchQueryService;

    private final OrganizationService organizationService;

    private final GroupService groupService;

    private final ApplicationProperties applicationProperties;

    private final FreemarkerService freemarkerService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Value("${server.port}")
    private String portNo;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    public StockIssueServiceImpl(StockIssueRepository stockIssueRepository, StockIssueSearchRepository stockIssueSearchRepository,
                                 StockService stockService, @Qualifier(Channels.STOCK_OUTPUT) MessageChannel stockIssueChannel, @Qualifier(Channels.MOVE_TO_TRANSIT_STOCK_OUTPUT) MessageChannel stockTransitChannel,
                                 StockIndentService stockIndentService, SequenceGeneratorService sequenceGeneratorService,
                                 StockReversalSearchRepository stockReversalSearchRepository, IssueMapper issueMapper, WorkflowService workflowService, ElasticsearchOperations elasticsearchTemplate, UOMRepository uomRepository, RestHighLevelClient restHighLevelClient, LocatorRepository locatorRepository,
                                 RuleExecutorService ruleExecutorService, ItemRepository itemRepository, ElasticSearchQueryService elasticSearchQueryService, OrganizationService organizationService, GroupService groupService, ApplicationProperties applicationProperties,
                                 FreemarkerService freemarkerService, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.stockIssueRepository = stockIssueRepository;
        this.stockIssueSearchRepository = stockIssueSearchRepository;
        this.stockService = stockService;
        this.stockIssueChannel = stockIssueChannel;
        this.stockIndentService = stockIndentService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.issueMapper = issueMapper;
        this.stockReversalSearchRepository = stockReversalSearchRepository;
        this.workflowService = workflowService;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.uomRepository = uomRepository;
        this.restHighLevelClient = restHighLevelClient;
        this.locatorRepository = locatorRepository;
        this.ruleExecutorService = ruleExecutorService;
        this.itemRepository = itemRepository;
        this.elasticSearchQueryService = elasticSearchQueryService;
        this.organizationService = organizationService;
        this.groupService = groupService;
        this.applicationProperties = applicationProperties;
        this.freemarkerService = freemarkerService;
        this.stockTransitChannel = stockTransitChannel;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a stockIssue.
     *
     * @param stockIssue the entity to save
     * @return the persisted entity
     */
    @Override
    public StockIssue save(StockIssue stockIssue) throws SequenceGenerateException {
        log.debug("Request to save StockIssue : {}", stockIssue);
        saveValidation(stockIssue);
        if (stockIssue.getId() == null) {
            stockIssue.setId(stockIssueRepository.getId());
            stockIssue.getDocument().setId(stockIssue.getId().toString());
            stockIssue.version(0);
            if (Status.DRAFT.equals(stockIssue.getDocument().getStatus())) {
                stockIssue.setDocumentNumber("DRAFT-" + stockIssue.getId());
            }
        } else {
            stockIssueRepository.updateLatest(stockIssue.getId());
            int version = stockIssue.getVersion() + 1;
            stockIssue.version(version);
        }
        stockIssue.getDocument().setDocumentNumber(stockIssue.getDocumentNumber());
        stockIssue.setLatest(true);
        generateIdsIfRequiredForLines(stockIssue);
        StockIssue result = stockIssueRepository.save(stockIssue);
//        stockIssueSearchRepository.save(result);
        return result;
    }

    @Override
    public StockIssue updateSourceDocumentOnDestinationModification(StockIssue stockIssue) {
        stockIssueRepository.updateLatest(stockIssue.getId());
        int version = stockIssue.getVersion() + 1;
        stockIssue.version(version);
        return stockIssueRepository.save(stockIssue);
    }

    @PublishStockTransaction
    public StockIssue save(StockIssue stockIssue, String action) throws Exception {
        log.debug("Request to save StockIssues {} with action {}", stockIssue, action);
        StockIssue result = null;
        switch (action) {
            case "SENDFORAPPROVAL":
                validateSendForApproval(stockIssue);
                validateIssueDocument(stockIssue);
                validateSourceQuantity(stockIssue);
                result = sendForApproval(stockIssue, action);
                break;
            case "APPROVED":
                validateStockIssue(stockIssue);
                validateIssueDocument(stockIssue);
                validateSourceQuantity(stockIssue);
                if (stockIssue.getId() == null) {
                    assignValuesForDirectApprove(stockIssue, action);
                }
                result = approveIssueDocument(stockIssue);
                assignTransitStoreToMap(result);
                break;
            case "REJECTED":
                //validateStockIssue(stockIssue);
                //validateIssueDocument(stockIssue);
                result = rejectIssueDocument(stockIssue);
                /*if (result.getDocument().getDocumentType().equals(Stock_Issue) || result.getDocument().getDocumentType().equals(Inter_Unit_Stock_Issue)) {
                    SourceDocument sourceDocument = stockIssue.getDocument().getLines().stream().findFirst().get().getSourceDocument().stream().filter(srcDocument -> Stock_Indent.equals(srcDocument.getType()) || Inter_Unit_Stock_Indent.equals(srcDocument.getType())).findFirst().get();
                    updateSourceDocumentConversionCompleted(Boolean.FALSE, sourceDocument.getId());
                }*/
                break;
            default:
                if (stockIssue.getDocument().getStatus() == null) {
                    stockIssue.getDocument().setStatus(Status.DRAFT);
                    validateDraft(stockIssue);
                    result = save(stockIssue);
                } else {
                    validateIssueDocument(stockIssue);
                    if (stockIssue.getDocument().getStatus().equals(Status.WAITING_FOR_APPROVAL)) {
                        generateIdsIfRequiredForLines(stockIssue);
                        refreshReserveStock(stockIssue);
                    }
                    result = save(stockIssue);
                }
                break;
        }
        index(result);
        return result;
    }

    private void assignValuesForDirectApprove(StockIssue stockIssue, String action) throws SequenceGenerateException {
        stockIssue.getDocument().setDraft(false);
        stockIssue.documentNumber(sequenceGeneratorService.generateSequence(stockIssue.getDocument().getDocumentType().name(), "NH", stockIssue));
        stockIssue.setId(stockIssueRepository.getId());
        stockIssue.getDocument().setId(stockIssue.getId().toString());
    }

    private void assignTransitStoreToMap(StockIssue stockIssue) {
        Map<String, Object> map = (Map) StockServiceAspect.threadLocal.get().get(Channels.ITEM_STORE_STOCK_OUTPUT);
        if (map == null) map = new HashMap<>();
        Set<Long> itemIds = new HashSet<>();
        stockIssue.getDocument().getLines().forEach(issueDocumentLine -> itemIds.add(issueDocumentLine.getItem().getId()));
        map.put("transitStoreType", stockIssue.getDocument().getDocumentType());
        map.put("transitStore", stockIssue.getDocument().getIndentStore().getId());
        map.put("itemIds", itemIds);
        StockServiceAspect.threadLocal.get().put(Channels.ITEM_STORE_STOCK_OUTPUT, map);
    }

    /**
     * Get all the stockIssues.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockIssue> findAll(Pageable pageable) {
        log.debug("Request to get all StockIssues");
        Page<StockIssue> result = stockIssueRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockIssue by documentId.
     *
     * @param documentId the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockIssue findOne(DocumentId documentId) {
        log.debug("Request to get StockIndent : {}", documentId);
        StockIssue stockIssue = stockIssueRepository.findById(documentId).get();
        return stockIssue;
    }

    /**
     * Get one stockIssue by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockIssue findOne(Long id) {
        log.debug("Request to get StockIssue : {}", id);
        StockIssue stockIssue = stockIssueRepository.findOne(id);
        if (stockIssue != null)
            if (stockIssue.getDocument().getStatus() == Status.DRAFT) {
                for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
                    Page<Stock> stockPage = stockService.getBatchDetails(stockIssue.getDocument().getIssueStore().getId(), issueDocumentLine.getItem().getCode(), null, "-", false, null);
                    float quantity = 0.0f;
                    for (Stock stock : stockPage) {
                        quantity += stock.getQuantity();
                    }
                    UOMDTO uom = issueDocumentLine.getItem().getTrackUOM();
                    issueDocumentLine.setCurrentIssueStock(new Quantity(quantity, uom));
                }
            }
        return stockIssue;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public StockIssue findDetachedOne(Long id) {
        log.debug("Request to get StockIssue : {}", id);
        return stockIssueRepository.findOne(id);
    }

    @Override
    public StockIssue findOneWithLock(Long id) {
        log.debug("Request to get StockIssue with lock: {}", id);
        return stockIssueRepository.findOneWithLock(id);
    }

    /**
     * Delete the  stockIssue by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) throws BusinessRuleViolationException {
        log.debug("Request to delete StockIssue : {}", id);
        StockIssue stockIssue = stockIssueRepository.findOne(id);
        validateDelete(stockIssue);
        StockIndent stockIndent = null;
        if (!stockIssue.getDocument().getDocumentType().equals(Stock_Direct_Transfer)) {
            stockIndent = getStockIndentForIssue(stockIssue);
        }
        stockIssueRepository.delete(id);
        stockIssueSearchRepository.deleteById(id);
        if (stockIndent != null) {
            checkIndentConversion(stockIndent);
        }
    }

    /**
     * Delete the  stockIssue by documentId.
     *
     * @param documentId the id of the entity
     */
    @Override
    public void delete(DocumentId documentId) {
        log.debug("Request to delete StockIndent : {}", documentId);
        stockIssueRepository.deleteById(documentId);
        stockIssueSearchRepository.deleteById(documentId.getId());
    }

    /**
     * Search for the stockIssue corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockIssue> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockIssues for query {}", query);
        return stockIssueSearchRepository.search(queryStringQuery(query)
            .field("document.indentStore.name").field("document.indentUnit.name").field("document.issueStore.name")
            .field("document.status").field("documentNumber")
            .defaultOperator(Operator.AND), pageable);
    }

    /**
     * Search for the stockIssue corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockIssue> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockIssues for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND).
                field("document.indentStore.name").field("document.indentUnit.name").field("document.issueStore.name")
                .field("document.status").field("documentNumber")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return stockIssueSearchRepository.search(searchQuery);
    }

    /**
     * @param stockIssue
     * @param action
     * @return stock issue object
     * @throws Exception
     */
    @Transactional
    public StockIssue sendForApproval(StockIssue stockIssue, String action) throws Exception {
        log.debug("Request to Send for Approval for Issue : {}", stockIssue);
        StockIssue result;
        try {
            stockIssue.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
            if (stockIssue.getDocument().isDraft()) {
                stockIssue.getDocument().setDraft(false);
                stockIssue.documentNumber(sequenceGeneratorService.generateSequence(documentType(stockIssue.getDocument().getDocumentType()), "NH", stockIssue));
                result = save(stockIssue);
                reserveStock(result);
                //Get workflow configurations
                Map<String, Object> configurations = retrieveWorkflowConfigurations(stockIssue, true);
                //Start workflow if workflow enabled
                if ((Boolean) configurations.get("enableWorkflow")) {
                    startWorkflow(result, action, configurations);
                }
            } else {
                //In case of modification
                result = save(stockIssue);
                //deleteAndReserveStock(stockIssue);
                refreshReserveStock(stockIssue);
            }
           /* if (result.getDocument().getDocumentType().equals(TransactionType.Stock_Issue)) {
                checkForIndentConversionCompletion(result);
            }*/
            return result;
        } catch (Exception e) {
            stockService.deleteReservedStock(stockIssue.getId(), stockIssue.getDocument().getDocumentType());
            throw e;
        }
    }

    public void checkForIndentConversionCompletion(StockIssue stockIssue) {
       /* boolean isCompletelyConverted = Boolean.TRUE;
        Long stockIndentId = null;
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            SourceDocument indentSource = issueDocumentLine.getSourceDocument().stream().filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Indent)).findFirst().get();
            stockIndentId = indentSource.getId();
            if (indentSource.getPendingQuantity().getValue() - issueDocumentLine.getIssuedQuantity().getValue() != 0) {
                isCompletelyConverted = Boolean.FALSE;
                break;
            }
        }
        updateSourceDocumentConversionCompleted(isCompletelyConverted, stockIndentId);*/
        StockIndent stockIndent = getStockIndentForIssue(stockIssue);
        checkIndentConversion(stockIndent);
    }

    private StockIndent getStockIndentForIssue(StockIssue stockIssue) {
        StockIndent stockIndent = null;
        List<IssueDocumentLine> lines = stockIssue.getDocument().getLines();
        SourceDocument srcForIndent = lines.get(0) != null ? lines.get(0).getSourceDocument().stream().
            filter(sourceDocument -> sourceDocument.getType().equals(Stock_Indent) || sourceDocument.getType().equals(Inter_Unit_Stock_Indent)).findAny().orElse(null) : null;
        stockIndent = srcForIndent != null ? stockIndentService.findDetachedOne(srcForIndent.getId()) : null;
        return stockIndent;
    }

    private void checkIndentConversion(StockIndent stockIndent) {
        Map<Long, Float> map = new HashMap<>();
        Map<Long, Float> rejectedQuantityValue = new HashMap<>();
        Map<Long, Float> issuedQuantityValue = new HashMap<>();
        boolean isCompletelyConverted = Boolean.TRUE;

        for (IndentDocumentLine line : stockIndent.getDocument().getLines()) {
            map.put(line.getId(), line.getQuantity().getValue());
            Page<StockReversal> stockReversals = stockReversalSearchRepository.search(queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + stockIndent.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + line.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockReversal> stockReversalIterator = stockReversals.iterator();
            while (stockReversalIterator.hasNext()) {
                StockReversal stockReversal = stockReversalIterator.next();
                for (ReversalDocumentLine itemLine : stockReversal.getDocument().getLines()) {
                    Optional<SourceDocument> sourceDocument = itemLine.getSourceDocument().stream().
                        filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(stockIndent.getDocumentNumber())).findAny();
                    if (sourceDocument.isPresent() && sourceDocument.get().getLineId().equals(line.getId())) {
                        rejectedQuantityValue.put(line.getId(), rejectedQuantityValue.get(line.getId()) != null ? rejectedQuantityValue.get(line.getId()) + itemLine.getRejectedQuantity().getValue()
                            : itemLine.getRejectedQuantity().getValue());
                    }
                }

            }
            Page<StockIssue> stockIssues = stockIssueSearchRepository.search(queryStringQuery("NOT (document.status.raw:(" + Status.DRAFT + " OR " + Status.REJECTED + ")) document.lines.sourceDocument.documentNumber.raw:" + stockIndent.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + line.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockIssue> stockIssueIterator = stockIssues.iterator();
            while (stockIssueIterator.hasNext()) {
                StockIssue stockIssue = stockIssueIterator.next();
                for (IssueDocumentLine itemLine : stockIssue.getDocument().getLines()) {
                    Optional<SourceDocument> sourceDocument = itemLine.getSourceDocument().stream().
                        filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(stockIndent.getDocumentNumber())).findAny();
                    if (sourceDocument.isPresent() && sourceDocument.get().getLineId().equals(line.getId())) {
                        issuedQuantityValue.put(line.getId(), issuedQuantityValue.get(line.getId()) != null ? issuedQuantityValue.get(line.getId()) + itemLine.getIssuedQuantity().getValue()
                            : itemLine.getIssuedQuantity().getValue());

                    }
                }
            }
        }

        for (IndentDocumentLine line : stockIndent.getDocument().getLines()) {
            map.put(line.getId(),
                (map.get(line.getId()) + (rejectedQuantityValue.get(line.getId()) != null ? rejectedQuantityValue.get(line.getId()) : 0f)
                    - (issuedQuantityValue.get(line.getId()) != null ? issuedQuantityValue.get(line.getId()) : 0f)));
            if (map.get(line.getId()) != 0) {
                isCompletelyConverted = Boolean.FALSE;
                break;
            }
        }

        updateSourceDocumentConversionCompleted(isCompletelyConverted, stockIndent.getId());
    }

    private void updateSourceDocumentConversionCompleted(boolean isCompletelyConverted, Long stockIndentId) {
        StockIndent stockIndent = stockIndentService.findDetachedOne(stockIndentId);
        stockIndent.getDocument().setConversionCompleted(isCompletelyConverted);
        stockIndentService.updateSourceDocumentOnDestinationModification(stockIndent);
        stockIndentService.index(stockIndent);
    }

    private void reserveStock(StockIssue stockIssue) throws Exception {
        List<IssueDocumentLine> lines = stockIssue.getDocument().getLines();
        for (IssueDocumentLine issueDocumentLine : lines) {
            if (issueDocumentLine.getIssuedQuantity().getValue() > 0) {
                try {
                    stockService.reserveStock(issueDocumentLine.getStockId(), issueDocumentLine.getItem().getId(), issueDocumentLine.getBatchNumber(),
                        stockIssue.getDocument().getIssueStore().getId(), issueDocumentLine.getIssuedQuantity().getValue(), stockIssue.getId(),
                        stockIssue.getDocument().getDocumentType(), stockIssue.getDocumentNumber(),
                        issueDocumentLine.getId(), stockIssue.getDocument().getApprovedDate() != null ?
                            stockIssue.getDocument().getApprovedDate() : stockIssue.getDocument().getCreatedDate(),stockIssue.getDocument().getCreatedBy().getId());
                } catch (StockException e) {
                    e.setItemName(issueDocumentLine.getItem().getName());
                    e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                    throw e;
                }
            }
        }
    }

    private void deleteAndReserveStock(StockIssue stockIssue) throws Exception {
        stockService.deleteReservedStock(stockIssue.getId(), stockIssue.getDocument().getDocumentType());
        reserveStock(stockIssue);
    }

    private void refreshReserveStock(StockIssue stockIssue) throws Exception {
        List<ReserveStock> reserveStocks = stockService.findReserveStockByTransactionNo(stockIssue.getDocumentNumber());
        List<Long> deleteReserveStock = new ArrayList<>();
        List<IssueDocumentLine> refreshReserveStock = new ArrayList<>();
        List<IssueDocumentLine> addReserveStock = new ArrayList<>();
        List<IssueDocumentLine> tempDocumentLineList = new ArrayList<>(stockIssue.getDocument().getLines());
        List<ReserveStock> tempReserveStockList = new ArrayList<>(reserveStocks);

        reserveStocks.forEach(reserveStock -> stockIssue.getDocument().getLines().stream().filter(line -> reserveStock.getTransactionLineId().equals(line.getId())).forEachOrdered(line -> {
            tempReserveStockList.remove(reserveStock);
            tempDocumentLineList.remove(line);
            if (line.getIssuedQuantity().getValue() == 0) {
                deleteReserveStock.add(reserveStock.getId());
            } else {
                refreshReserveStock.add(line);
            }
        }));

        tempReserveStockList.stream().map(ReserveStock::getId).forEachOrdered(deleteReserveStock::add);
        tempDocumentLineList.stream().filter(line -> line.getIssuedQuantity().getValue() > 0).forEachOrdered(line -> {
            if (line.getId() == null) {
                line.setId(stockIssueRepository.getId());
            }
            addReserveStock.add(line);
        });

        reserveStock(stockIssue, addReserveStock);
        deleteAndReserveStock(stockIssue, refreshReserveStock);
        stockService.deleteReservedStocks(deleteReserveStock);
    }

    private void deleteAndReserveStock(StockIssue stockIssue, List<IssueDocumentLine> lines) throws Exception {
        for (IssueDocumentLine issueDocumentLine : lines) {
            try {
                stockService.deleteAndReserveStock(issueDocumentLine.getStockId(), issueDocumentLine.getItem().getId(), issueDocumentLine.getBatchNumber(),
                    stockIssue.getDocument().getIssueStore().getId(), issueDocumentLine.getIssuedQuantity().getValue(), stockIssue.getId(),
                    stockIssue.getDocument().getDocumentType(), stockIssue.getDocumentNumber(),
                    issueDocumentLine.getId(), stockIssue.getDocument().getApprovedDate() != null ?
                        stockIssue.getDocument().getApprovedDate() : stockIssue.getDocument().getCreatedDate(),stockIssue.getDocument().getCreatedBy().getId());
            } catch (StockException e) {
                e.setItemName(issueDocumentLine.getItem().getName());
                e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                throw e;
            }
        }
    }

    private void reserveStock(StockIssue stockIssue, List<IssueDocumentLine> lines) throws Exception {
        for (IssueDocumentLine issueDocumentLine : lines) {
            if (issueDocumentLine.getIssuedQuantity().getValue() > 0) {
                try {
                    stockService.reserveStock(issueDocumentLine.getStockId(), issueDocumentLine.getItem().getId(), issueDocumentLine.getBatchNumber(),
                        stockIssue.getDocument().getIssueStore().getId(), issueDocumentLine.getIssuedQuantity().getValue(), stockIssue.getId(),
                        stockIssue.getDocument().getDocumentType(), stockIssue.getDocumentNumber(),
                        issueDocumentLine.getId(), stockIssue.getDocument().getApprovedDate() != null ?
                            stockIssue.getDocument().getApprovedDate() : stockIssue.getDocument().getCreatedDate(),stockIssue.getDocument().getCreatedBy().getId());
                } catch (StockException e) {
                    e.setItemName(issueDocumentLine.getItem().getName());
                    e.setErrorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
                    throw e;
                }
            }
        }
    }

    /**
     * Get all stockIssue and publish
     */
    @Override
    public void produce(StockIssue stockIssue) {
        log.debug("Stock Issue Id {} has been published", stockIssue.getId());
        Map<String, Object> map = new HashedMap();
        map.put("Id", stockIssue.getId());
        map.put("DocumentNo", stockIssue.getDocumentNumber());
        //stockTransitChannel.send(MessageBuilder.withPayload(map).build());
        StockServiceAspect.threadLocal.get().put(Channels.MOVE_TO_TRANSIT_STOCK_OUTPUT, map);

    }


    /**
     * @param stockIssue
     * @return
     */
    @Transactional
    public StockIssue approveIssueDocument(StockIssue stockIssue) throws Exception {
        log.debug("Request to approve Issue Documents {}", stockIssue);
        StockIssue result = null;
        generateIdsIfRequiredForLines(stockIssue);
        //deleteAndReserveStock(stockIssue);
        refreshReserveStock(stockIssue);
        stockIssue.getDocument().setStatus(Status.APPROVED);
        result = save(stockIssue);
        produce(stockIssue);
        changeStatus(result);
        /*if (result.getDocument().getDocumentType().equals(TransactionType.Stock_Issue)) {
            checkForIndentConversionCompletion(result);
        }*/
        return result;

    }

    private void generateIdsIfRequiredForLines(StockIssue stockIssue) {
        if (CollectionUtils.isNotEmpty(stockIssue.getDocument().getLines())) {
            Set<Long> itemIds = new HashSet<>();
            for (IssueDocumentLine stockIssueLine : stockIssue.getDocument().getLines()) {
                if (null == stockIssueLine.getId() || itemIds.contains(stockIssueLine.getId())) {
                    stockIssueLine.setId(stockIssueRepository.getId());
                }
                itemIds.add(stockIssueLine.getId());
            }
        }
    }

    private String documentType(TransactionType documentType) {
        if (Inter_Unit_Stock_Issue.equals(documentType)) {
            return Stock_Issue.name();
        }
        return documentType.name();
    }

    private void changeStatus(StockIssue stockIssue) throws SequenceGenerateException {
        log.debug("Request to change document status");
        StockIndent indentDoc = null;
        Boolean issuedFlag = true;
        List<IssueDocumentLine> lines = stockIssue.getDocument().getLines();
        SourceDocument src = lines.get(0).getSourceDocument() != null ? lines.get(0).getSourceDocument().stream().
            filter(sourceDocument -> sourceDocument.getType().equals(Stock_Indent) || sourceDocument.getType().equals(Inter_Unit_Stock_Indent)).findAny().get() : null;
        Map<Long, Float> map = new HashMap<>();
        Map<Long, Float> rejectedQuantityValue = new HashMap<>();
        for (IssueDocumentLine line : lines) {
            if (src != null) {
                Long lineId = line.getSourceDocument().stream().filter(sourceDocument -> sourceDocument.getType().equals(Stock_Indent) || sourceDocument.getType().equals(Inter_Unit_Stock_Indent)).findFirst().get().getLineId();
                Page<StockReversal> stockReversals = stockReversalSearchRepository.search(queryStringQuery("document.status.raw:(" + Status.APPROVED + " OR " + Status.PROCESSED + ") document.lines.sourceDocument.documentNumber.raw:" + src.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + lineId)
                    .defaultOperator(Operator.AND), PageRequest.of(0,10000));
                Iterator<StockReversal> stockReversalIterator = stockReversals.iterator();
                while (stockReversalIterator.hasNext()) {
                    StockReversal stockReversal = stockReversalIterator.next();
                    for (ReversalDocumentLine itemLine : stockReversal.getDocument().getLines()) {
                        SourceDocument sourceDocument = itemLine.getSourceDocument().stream().
                            filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(src.getDocumentNumber())).findAny().get();
                        if (sourceDocument != null && sourceDocument.getLineId().equals(line.getId())) {
                            rejectedQuantityValue.put(line.getId(), rejectedQuantityValue.get(line.getId()) != null ? rejectedQuantityValue.get(line.getId()) + itemLine.getRejectedQuantity().getValue()
                                : itemLine.getRejectedQuantity().getValue());
                        }
                    }

                }

                Page<StockIssue> stockIssues = stockIssueSearchRepository.search(queryStringQuery("document.status.raw:(" + Status.APPROVED + " OR " + Status.PROCESSED + " OR " + Status.PARTIALLY_PROCESSED + ") document.lines.sourceDocument.documentNumber.raw:" + src.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + lineId)
                    .defaultOperator(Operator.AND), PageRequest.of(0,10000));
                Iterator<StockIssue> stockIssueIterator = stockIssues.iterator();
                while (stockIssueIterator.hasNext()) {
                    StockIssue stockIssue1 = stockIssueIterator.next();
                    if (stockIssue1.getId() != stockIssue.getId()) {
                        for (IssueDocumentLine itemLine : stockIssue1.getDocument().getLines()) {
                            SourceDocument sourceDocument = itemLine.getSourceDocument().stream().
                                filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(src.getDocumentNumber())).findAny().get();
                            if (sourceDocument != null && sourceDocument.getLineId().equals(lineId)) {
                                map.put(line.getId(), map.get(line.getId()) != null ? map.get(line.getId()) + itemLine.getIssuedQuantity().getValue()
                                    : itemLine.getIssuedQuantity().getValue());
                            }
                        }
                    }

                }
                for (IssueDocumentLine itemLine : stockIssue.getDocument().getLines()) {
                    SourceDocument sourceDocument = itemLine.getSourceDocument().stream().
                        filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(src.getDocumentNumber())).findAny().get();
                    if (sourceDocument != null && sourceDocument.getLineId().equals(lineId)) {
                        map.put(line.getId(), map.get(line.getId()) != null ? map.get(line.getId()) + itemLine.getIssuedQuantity().getValue()
                            : itemLine.getIssuedQuantity().getValue());
                    }
                }
                for (SourceDocument srcDoc : line.getSourceDocument()) {
                    if (srcDoc.getType() == Stock_Indent || srcDoc.getType() == Inter_Unit_Stock_Indent) {
                        Quantity requestedQuantity = srcDoc.getQuantity();
                        if (requestedQuantity.getValue() > (map.get(line.getId()) != null ? map.get(line.getId()) : line.getIssuedQuantity().getValue()) - (rejectedQuantityValue
                            .get(line.getId()) != null ? rejectedQuantityValue.get(line.getId()) : 0f)) {
                            issuedFlag = false;
                            break;
                        }
                    }
                }
                if (!issuedFlag) {
                    break;
                }
            }
        }
        if (src != null) {
            indentDoc = src != null ? stockIndentService.findDetachedOne(src.getId()) : null;
            if (!indentDoc.getDocument().getStatus().equals(Status.PARTIALLY_PROCESSED)
                && !indentDoc.getDocument().getStatus().equals(Status.CLOSED)) {
                if (issuedFlag) {
                    indentDoc.getDocument().setStatus(Status.ISSUED);
                } else {
                    indentDoc.getDocument().setStatus(Status.PARTIALLY_ISSUED);
                }
                indentDoc.getDocument().setModifiedDate(LocalDateTime.now());
                StockIndent result = stockIndentService.updateSourceDocumentOnDestinationModification(indentDoc);
                stockIndentService.index(result);
            }
        }
    }

    /**
     * @param stockIssue
     * @return
     */
    @Transactional
    public StockIssue rejectIssueDocument(StockIssue stockIssue) throws SequenceGenerateException {
        log.debug("Request to reject Issue Documents {}", stockIssue);
        stockIssue.getDocument().setStatus(Status.REJECTED);
        save(stockIssue);
        stockService.deleteReservedStock(stockIssue.getId(), stockIssue.getDocument().getDocumentType());
        if (stockIssue.getDocument().getDocumentType().equals(Stock_Issue) || stockIssue.getDocument().getDocumentType().equals(Inter_Unit_Stock_Issue)) {
            SourceDocument sourceDocument = stockIssue.getDocument().getLines().stream().findFirst().get().getSourceDocument().stream().filter(srcDocument -> Stock_Indent.equals(srcDocument.getType()) || Inter_Unit_Stock_Indent.equals(srcDocument.getType())).findFirst().get();
            updateSourceDocumentConversionCompleted(Boolean.FALSE, sourceDocument.getId());
        }
        return stockIssue;
    }

    @Override
    public StockIssue convertIndentToIssue(Long docId, String docNo) {
        StockIssue result = null;
        String query = null;
        if (docId != null) {
            query = "document.lines.sourceDocument.id:" + docId;
        } else {
            query = "document.lines.sourceDocument.documentNumber.raw:" + docNo;
        }
        Page<StockIssue> issuePage = stockIssueSearchRepository.search(queryStringQuery(query + " AND (document.status.raw:" + Status.DRAFT + ")"), PageRequest.of(0,1));
        if (issuePage.iterator().hasNext()) {
            result = issuePage.iterator().next();
        } else {
            query = docId != null ? "id:" + docId : "documentNumber:\"" + docNo + "\"";
            Page<StockIndent> page = stockIndentService.search(query, PageRequest.of(0,1));
            result = issueMapper.convertFromStockIndent(page.iterator().next());
            List<IssueDocumentLine> issueDocumentLines = result.getDocument().getLines();
            for (IssueDocumentLine issueDocumentLine : issueDocumentLines) {
                populateStockDetails(result.getDocument().getIssueStore().getId(), issueDocumentLine, "-");
            }
        }
        return result;
    }

    /**
     * Get the stockIssue status count corresponding to the query.
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
                .field("document.indentStore.name")
                .field("document.indentUnit.name")
                .field("document.issueStore.name")
                .field("document.status")
                .field("documentNumber")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "stockissue" );
        Terms aggregationTerms = aggregations.get("status_count");
        for (Terms.Bucket bucket : aggregationTerms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    /**
     * Execute workflow
     *
     * @param stockIssue the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return stockIssue object
     * @throws Exception
     */
    @Override
    @Transactional
    @PublishStockTransaction
    public StockIssue executeWorkflow(StockIssue stockIssue, String transition, Long taskId) throws Exception {
        StockIssue result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Send for Approval":
                action = "SENDFORAPPROVAL";
                validateSendForApproval(stockIssue);
                validateIssueDocument(stockIssue);
                validateSourceQuantity(stockIssue);
                result = sendForApproval(stockIssue, action);
                break;
            case "Approved":
                action = "APPROVED";
                //validateDocumentApprover(stockIssue);
                validateStockIssue(stockIssue);
                validateIssueDocument(stockIssue);
                validateSourceQuantity(stockIssue);
                result = approveIssueDocument(stockIssue);
                assignTransitStoreToMap(result);
                break;
            case "Rejected":
                action = "REJECTED";
                result = rejectIssueDocument(stockIssue);
                break;
            case "Modify":
                action = "DRAFT";
                stockIssue.getDocument().setStatus(Status.DRAFT);
                result = save(stockIssue);
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

    private Map<String, Object> getItemIdsForTransitStore(StockIssue result) {
        Map<String, Object> map = new HashMap<>();
        Set<Long> itemIds = new HashSet<>();
        result.getDocument().getLines().forEach(issueDocumentLine -> itemIds.add(issueDocumentLine.getId()));
        map.put("itemIds", itemIds);
        map.put("storeId", result.getDocument().getIndentStore().getId());
        map.put("transitStoreType", result.getDocument().getDocumentType());
        return map;
    }

    /**
     * Start workflow
     *
     * @param stockIssue
     * @param action
     * @param configurations
     */
    public void startWorkflow(StockIssue stockIssue, String action, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", stockIssue.getId());
            content.put("document_type", stockIssue.getDocument().getDocumentType());
            content.put("group_id", configurations.get("groupIds"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_number", stockIssue.getDocumentNumber());
            params.put("issue_date", stockIssue.getDocument().getIssueDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("from_store", stockIssue.getDocument().getIssueStore().getName());
            params.put("unit_id", String.valueOf(stockIssue.getDocument().getIssueUnit().getId()));
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
        StockIssue stockIssue = stockIssueRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(stockIssue, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) :workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,stockIssue.getDocument().getCreatedBy().getLogin());
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
     * @param stockIssue
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(StockIssue stockIssue, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        String enableWorkflow = (Stock_Direct_Transfer.equals(stockIssue.getDocument().getDocumentType())) ? "athma_stockdirecttransfer_enable_workflow" : "athma_stockissue_enable_workflow";
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData(enableWorkflow, stockIssue.getDocument().getIssueStore().getId(), stockIssue.getDocument().getIssueUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            String process = (Stock_Direct_Transfer.equals(stockIssue.getDocument().getDocumentType())) ? "athma_stockdirecttransfer_workflow_definition" : "athma_stockissue_workflow_definition";
            configurations.put("processId", ConfigurationUtil.getConfigurationData(process, stockIssue.getDocument().getIssueStore().getId(), stockIssue.getDocument().getIssueUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            Context context = (Stock_Direct_Transfer.equals(stockIssue.getDocument().getDocumentType())) ? DirectTransfer_Approval_Committee : Issue_Approval_Committee;
            configurations.put("groupIds", getGroupData(context, stockIssue.getDocument().getIssueUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", stockIssue.getDocument().getIssueStore().getId(), stockIssue.getDocument().getIssueUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of StockIssue");
        stockIssueSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockIssue latest=true");
        List<StockIssue> data = stockIssueRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockIssueSearchRepository.saveAll(data);
        }
    }

    private void validateDraft(StockIssue stockIssue) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockIssue.getDocument().getIssueStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_STORE));
        }
        if (stockIssue.getDocument().getIndentStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_STORE));
        }
        if (stockIssue.getDocument().getIssueUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_UNIT));
        }
        if (stockIssue.getDocument().getIndentUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_UNIT));
        }
        if (stockIssue.getDocument().getDocumentType() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_DOCUMENT_TYPE));
        }
        if (stockIssue.getDocument().getIssuedBy() == null || stockIssue.getDocument().getIssuedBy().getId() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ISSUER_NAME));
        }
        if (stockIssue.getDocument().getCreatedDate() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CREATED_DATE));
        }
        if (stockIssue.getDocument().getStatus() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STATUS));
        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    private void validateIssueDocument(StockIssue stockIssue) throws FieldValidationException {
        int zeroIssuedQuantityCounter = 0;
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockIssue.getDocument().getLines() != null) {
            for (IssueDocumentLine line : stockIssue.getDocument().getLines()) {
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

                if (line.getIssuedQuantity() != null) {
                    if (line.getIssuedQuantity().getUom() == null) {
                        Map<String, Object> source = new HashMap<String, Object>();
                        source.put("itemName", line.getItem().getName());
                        errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UOM, source));
                    }
                    if (line.getIssuedQuantity().getValue() == null) {
                        Map<String, Object> source = new HashMap<String, Object>();
                        source.put("itemName", line.getItem().getName());
                        errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY, source));
                    }
                    if (line.getIssuedQuantity().getValue() == 0) zeroIssuedQuantityCounter++;
                } else {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_QUANTITY_AND_UOM, source));
                }
            }
            if (stockIssue.getDocument().getLines().size() == zeroIssuedQuantityCounter) {
                Map<String, Object> source = new HashMap<String, Object>();
                source.put("document", stockIssue.getDocumentNumber());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ZERO_ISSUED_QUANTITY_IN_ALL_LINES));
            }
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockIssue.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ISSUE_DOCUMENT_LINES));
        }
        try {
            validateDraft(stockIssue);
            if (!errorMessages.isEmpty()) {
                throw new FieldValidationException(errorMessages, "Validation exception");
            }
        } catch (FieldValidationException exception) {
            exception.getErrorMessages().addAll(errorMessages);
            throw exception;
        }
    }

    private void validateSourceQuantity(StockIssue stockIssue) throws FieldValidationException {
        if (Stock_Direct_Transfer.equals(stockIssue.getDocument().getDocumentType())) {
            return;
        }
        List<ErrorMessage> errorMessages = new ArrayList<>();
        Map<Long, Float> map = new HashMap<>();
        if (stockIssue.getDocument().getLines() != null) {
            for (IssueDocumentLine line : stockIssue.getDocument().getLines()) {
                SourceDocument stockIndentSource = line.getSourceDocument().stream().filter(sourceDocument -> Stock_Indent.equals(sourceDocument.getType()) || Inter_Unit_Stock_Indent.equals(sourceDocument.getType())).findFirst().get();
                map.put(stockIndentSource.getLineId(), Optional.ofNullable(line.getIssuedQuantity().getValue()).orElse(Float.valueOf(0)));
                Page<StockIssue> savedStockIssues = stockIssueSearchRepository.search(queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + stockIndentSource.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + stockIndentSource.getLineId())
                    .defaultOperator(Operator.AND), PageRequest.of(0,10000));
                Iterator<StockIssue> iterator = savedStockIssues.getContent().iterator();
                while (iterator.hasNext()) {
                    StockIssue saveStockIssue = iterator.next();
                    if (!Status.REJECTED.equals(saveStockIssue.getDocument().getStatus())
                        && !saveStockIssue.getId().equals(stockIssue.getId())) {
                        Iterator<IssueDocumentLine> lineIterator = saveStockIssue.getDocument().getLines().iterator();
                        while (lineIterator.hasNext()) {
                            IssueDocumentLine issueDocumentLine = lineIterator.next();
                            if (issueDocumentLine.getSourceDocument().stream().anyMatch(sourceDocument ->
                                sourceDocument.getDocumentNumber().equals(stockIndentSource.getDocumentNumber())
                                    && sourceDocument.getLineId().equals(stockIndentSource.getLineId()))) {
                                map.put(stockIndentSource.getLineId(),
                                    map.get(stockIndentSource.getLineId()) + Optional.ofNullable(issueDocumentLine.getIssuedQuantity().getValue()).orElse(Float.valueOf(0)));
                            }
                        }
                    }
                }
                Page<StockReversal> stockReversals = stockReversalSearchRepository.search(queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + stockIndentSource.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + stockIndentSource.getLineId())
                    .defaultOperator(Operator.AND), PageRequest.of(0,10000));
                Iterator<StockReversal> rIterator = stockReversals.getContent().iterator();
                while (rIterator.hasNext()) {
                    StockReversal stockReversal = rIterator.next();
                    Iterator<ReversalDocumentLine> lineIterator = stockReversal.getDocument().getLines().iterator();
                    while (lineIterator.hasNext()) {
                        ReversalDocumentLine reversalDocumentLine = lineIterator.next();
                        if (reversalDocumentLine.getSourceDocument().stream().anyMatch(sourceDocument ->
                            sourceDocument.getDocumentNumber().equals(stockIndentSource.getDocumentNumber())
                                && sourceDocument.getLineId().equals(stockIndentSource.getLineId()))) {
                            map.put(stockIndentSource.getLineId(),
                                map.get(stockIndentSource.getLineId()) - Optional.ofNullable(reversalDocumentLine.getRejectedQuantity().getValue()).orElse(Float.valueOf(0)));
                        }
                    }
                }
                if (stockIndentSource.getQuantity().getValue() < map.get(stockIndentSource.getLineId())) {
                    Map<String, Object> source = new HashMap<>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.SUM_OF_ISSUED_QUANTITY_MISMATCH_WITH_REQUESTED_QUANTITY, source));
                }
            }
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockIssue.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_ISSUE_DOCUMENT_LINES));
        }
        if (!errorMessages.isEmpty()) {
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }


    /**
     * Validate entity
     *
     * @param stockIssue
     * @throws BusinessRuleViolationException
     */
    public void validateStockIssue(StockIssue stockIssue) throws BusinessRuleViolationException {
        //ruleExecutorService.executeByGroup(stockIssue, "stock_issue_rules");
    }


    public void validateSendForApproval(StockIssue stockIssue) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockIssue, "stock_issue_send_for_approval_validation");
    }

    public void validateDocumentApprover(StockIssue stockIssue) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockIssue, "stock_issue_document_approver_validation");
    }

    public void validateDelete(StockIssue stockIssue) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockIssue, "stock_issue_delete_validation");
    }

    /**
     * Get stockIssueLineItem
     *
     * @param storeId
     * @param itemId
     * @return issueDocumentLine
     */
    @Override
    public IssueDocumentLine getStockIssueLineItem(Long storeId, Long itemId) {
        IssueDocumentLine issueDocumentLine = new IssueDocumentLine();
        Item item = itemRepository.findById(itemId).get();
        if (item == null)
            throw new IllegalArgumentException("Item not found for id: " + itemId);

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(item.getId());
        itemDTO.setName(item.getName());
        itemDTO.setCode(item.getCode());
        itemDTO.setCategory(mapItemCategory(item.getCategory()));
        itemDTO.setPurchaseUOM(item.getPurchaseUOM().getUOMDTO());
        itemDTO.setSaleUOM(item.getSaleUOM().getUOMDTO());
        itemDTO.setTrackUOM(item.getTrackUOM().getUOMDTO());
        issueDocumentLine.setItem(itemDTO);
        issueDocumentLine.setIssuedQuantity(new Quantity(0f, issueDocumentLine.getItem().getTrackUOM()));
        issueDocumentLine = populateStockDetails(storeId, issueDocumentLine, "-");
        return issueDocumentLine;
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


    public IssueDocumentLine populateStockDetails(Long storeId, IssueDocumentLine issueDocumentLine, String docNumber) {
        Page<Stock> stockPage = stockService.getBatchDetails(storeId, issueDocumentLine.getItem().getCode(), null, docNumber, false,null);
        float quantity = 0.0f;
        for (Stock stock : stockPage) {
            quantity += stock.getQuantity();
        }
        UOMDTO uom = issueDocumentLine.getItem().getTrackUOM();
        issueDocumentLine.setCurrentIssueStock(new Quantity(quantity, uom));
        if (stockPage.getContent().size() == 1) {
            Stock stock = stockPage.getContent().get(0);
            issueDocumentLine.setBatchNumber(stock.getBatchNo());
            issueDocumentLine.setExpiryDate(stock.getExpiryDate());
            issueDocumentLine.setOwner(stock.getOwner());
            issueDocumentLine.setCost(stock.getCost());
            issueDocumentLine.setSku(stock.getSku());
            issueDocumentLine.setConsignment(stock.isConsignment());
            issueDocumentLine.setStockId(stock.getId());
            issueDocumentLine.setMrp(stock.getMrp());
            issueDocumentLine.setSupplier(stock.getSupplier());
            issueDocumentLine.setIssuedQuantity(issueDocumentLine.getIssuedQuantity().getValue() <= stock.getQuantity() ? issueDocumentLine.getIssuedQuantity() : new Quantity(stock.getQuantity(), uom));
            Locator locator = locatorRepository.findById(stock.getLocatorId()).get();
            issueDocumentLine.setLocator(locator.getLocatorDTO());
            issueDocumentLine.setBarCode(stock.getBarcode());
        }
        return issueDocumentLine;
    }

    @Override
    public void index(StockIssue stockIssue) {
        stockIssueSearchRepository.save(stockIssue);
    }


    @Override
    public Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException {
        log.debug("Gell all documents which is related to given stockIssue document:-" + documentNumber);

        Set<RelatedDocument> relatedDocumentList = new LinkedHashSet<>();
        Map<String, Set<RelatedDocument>> finalList = new LinkedHashMap<>();

        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest issueQueryReq= new SearchRequest("stockissue");
        SearchSourceBuilder issueSourceQueryReq = new SearchSourceBuilder()
            .query(QueryBuilders.queryStringQuery("documentNumber.raw:" + documentNumber));
        issueQueryReq.source(issueSourceQueryReq);

        SearchRequest receiptQueryReq= new SearchRequest("stockreceipt");
        SearchSourceBuilder receiptSourceQueryReq = new SearchSourceBuilder().size(100)
            .query(QueryBuilders.queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + documentNumber))
            .sort(SortBuilders.fieldSort("document.createdDate").order(SortOrder.DESC));
        receiptQueryReq.source(receiptSourceQueryReq);

        SearchRequest reversalQueryReq= new SearchRequest("stockreversal");
        SearchSourceBuilder reversalSourceQueryReq = new SearchSourceBuilder().size(100)
            .query(QueryBuilders.queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + documentNumber))
            .sort(SortBuilders.fieldSort("document.createdDate").order(SortOrder.DESC));
        reversalQueryReq.source(reversalSourceQueryReq);

        request.add(issueQueryReq);
        request.add(receiptQueryReq);
        request.add(reversalQueryReq);
        MultiSearchResponse mSearchResponse = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);

        MultiSearchResponse.Item[] items = mSearchResponse.getResponses();

        for (MultiSearchResponse.Item item : items) {
            SearchHit[] hits = item.getResponse().getHits().getHits();
            for (SearchHit hit : hits) {
                if (hit.getIndex().equals("stockissue")) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    Map<String, Object> document = (Map<String, Object>) source.get("document");

                    if (!document.get("documentType").toString().equals(Stock_Direct_Transfer.name())) {
                        List<Map<String, Object>> lines = (List<Map<String, Object>>) document.get("lines");
                        for (Map<String, Object> line : lines) {
                            List<Map<String, Object>> sourceDocuments = (List<Map<String, Object>>) line.get("sourceDocument");
                            for (Map<String, Object> sourceDocument : sourceDocuments) {
                                TransactionType txnType = findByTransactionType(sourceDocument.get("type").toString());
                                String dispDocumentType = txnType.getTransactionTypeDisplay();
                                if (null != finalList && null != finalList.get(dispDocumentType))
                                    relatedDocumentList = finalList.get(dispDocumentType);
                                else
                                    relatedDocumentList = new LinkedHashSet<>();

                                RelatedDocument relDoc = new RelatedDocument();
                                relDoc.setId(sourceDocument.get("id").toString());
                                relDoc.setDocumentType(valueOf(sourceDocument.get("type").toString()));
                                relDoc.setDocumentNumber(sourceDocument.get("documentNumber").toString());
                                relDoc.setCreatedDate(LocalDateTime.parse(sourceDocument.get("documentDate").toString()));
                                relatedDocumentList.add(relDoc);
                                finalList.put(dispDocumentType, relatedDocumentList);
                            }
                        }
                    }

                } else {
                    Map<String, Object> source = hit.getSourceAsMap();
                    Map<String, Object> sourceDocument = (Map<String, Object>) source.get("document");
                    TransactionType txnType = findByTransactionType(sourceDocument.get("documentType").toString());
                    String dispDocumentType = txnType.getTransactionTypeDisplay();
                    if (null != finalList && null != finalList.get(dispDocumentType))
                        relatedDocumentList = finalList.get(dispDocumentType);
                    else
                        relatedDocumentList = new LinkedHashSet<>();

                    RelatedDocument relDoc = new RelatedDocument();
                    relDoc.setId(sourceDocument.get("id").toString());
                    relDoc.setDocumentType(valueOf(sourceDocument.get("documentType").toString()));
                    relDoc.setDocumentNumber(sourceDocument.get("documentNumber").toString());
                    relDoc.setStatus(Status.valueOf(sourceDocument.get("status").toString()));
                    relDoc.setCreatedDate(LocalDateTime.parse(sourceDocument.get("createdDate").toString()));
                    relatedDocumentList.add(relDoc);
                    finalList.put(dispDocumentType, relatedDocumentList);
                }
            }
        }

        return finalList;
    }

    @Override
    public void reIndex(Long id) {
        StockIssue stockIssue = stockIssueRepository.findOne(id);
        if (stockIssue == null) {
            if (id != null)
                stockIssueSearchRepository.deleteById(id);
        } else {
            stockIssueSearchRepository.save(stockIssue);
        }

    }

    private void saveValidation(StockIssue stockIssue) {
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(stockIssue.getDocument().getIssueUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with logged in unit");
        }
    }

    @Override
    public Map<String, Object> getStockIssueHTML(Long issueId, String issueNumber,String documentType) throws Exception {
        log.debug("issueId: {}, issueNumber: {}", issueId, issueNumber);

        Map<String, Object> printFile = new HashMap<>();
        // Fixed template


        StockIssue stockIssue = getStockIssue(issueId, issueNumber);
        if(null!=documentType && documentType.equalsIgnoreCase((TransactionType.Inter_Unit_Stock_Issue).toString()))
        {   String templateFilePath = "inter-Unit-Stock-Issue.ftl";
            String fileName = stockIssue.getDocumentNumber();
            printFile.put("fileName", fileName);
            Map<String, Object> invoiceData = populateInterUnitStockIssueData(stockIssue);
            String html = freemarkerService.mergeTemplateIntoString(templateFilePath, invoiceData);
            printFile.put("html", html);
            byte[] contentInBytes = html.getBytes();
            printFile.put("content", contentInBytes);
            if (!Status.DRAFT.equals(stockIssue.getDocument().getStatus())) {
                createHTMLFile(html, fileName);
            }
        }
        else {
            String templateFilePath = "stock-issue.ftl";
            String fileName = stockIssue.getDocumentNumber();
            printFile.put("fileName", fileName);
            Map<String, Object> invoiceData = populateIssueData(stockIssue);
            String html = freemarkerService.mergeTemplateIntoString(templateFilePath, invoiceData);
            printFile.put("html", html);
            byte[] contentInBytes = html.getBytes();
            printFile.put("content", contentInBytes);
            if (!Status.DRAFT.equals(stockIssue.getDocument().getStatus())) {
                createHTMLFile(html, fileName);
            }
        }
        return printFile;
    }

    private Map<String, Object> populateInterUnitStockIssueData(StockIssue stockIssue)
    {
        String userDisplayName = getUserDisplayNameForInterUnit();
        Map<String, Object> stockMap = new HashMap<>();
        stockMap.put("invoiceNumber", "SISI"+stockIssue.getDocumentNumber().substring(3));
        stockMap.put("eWayBillNumber","");
        stockMap.put("nameOfTranspoter", "");
        stockMap.put("invoiceDate", stockIssue.getDocument().getApprovedDate() != null ? stockIssue.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")) : "-");
        stockMap.put("issueUnitConsignor",stockIssue.getDocument().getIssueUnit().getName());
        stockMap.put("indentUnitConsignor",stockIssue.getDocument().getIndentUnit().getName());
        stockMap.put("issueLines", stockIssue.getDocument().getLines());
        stockMap.put("approvedOn", stockIssue.getDocument().getApprovedDate() != null ? stockIssue.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")) : "-");
        stockMap.put("publishedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockMap.put("datetimeformatter", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        stockMap.put("userDisplayName", userDisplayName);
        Map<Long, String> hsnData = getHSNCodeData(stockIssue.getDocument().getLines());
        stockMap.put("hsnData", hsnData);
        Organization org = getOrganizationData(stockIssue.getDocument().getIssueUnit().getId());
        stockMap.put("eWayBillDate","");
        //List list=org.getAddresses().iterator().next().entrySet().stream().collect(Collectors.toList());
        stockMap.put("issueGSTIN",null!=org.getIdentifier()?!org.getIdentifier().isEmpty()?org.getIdentifier().get(0).getValue():null:null);
        stockMap.put("issueUnitAddress",org.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString()
            .equalsIgnoreCase("work")).findAny().orElse(null));
        stockMap.put("issueTelComePhone",org.getTelecoms().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("system")).get("code").toString()
            .equalsIgnoreCase("phone")).findAny().orElse(null));
        stockMap.put("issueTelComeEmail",org.getTelecoms().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("system")).get("code").toString()
            .equalsIgnoreCase("email")).findAny().orElse(null));
        stockMap.put("issueTelComeFax",org.getTelecoms().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("system")).get("code").toString()
            .equalsIgnoreCase("fax")).findAny().orElse(null));
        Organization indentorg = getOrganizationData(stockIssue.getDocument().getIndentUnit().getId());
        stockMap.put("indentUnitAddress",indentorg.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString()
            .equalsIgnoreCase("work")).findAny().orElse(null));
       stockMap.put("indentUnitTelComePhone",indentorg.getTelecoms().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("system")).get("code").toString()
           .equalsIgnoreCase("phone")).findAny().orElse(null));

        stockMap.put("indentUnitTelComeEmail",indentorg.getTelecoms().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("system")).get("code").toString()
            .equalsIgnoreCase("email")).findAny().orElse(null));
        stockMap.put("indentUnitTelComeFax",indentorg.getTelecoms().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("system")).get("code").toString()
            .equalsIgnoreCase("fax")).findAny().orElse(null));
        stockMap.put("indentGSTIN",null!=indentorg.getIdentifier()?!indentorg.getIdentifier().isEmpty()?indentorg.getIdentifier().get(0).getValue():null:null);
        String code="-";
        if(null!=org && null!=org.getAddresses() && org.getAddresses().iterator().hasNext())
        {
            code = org.getAddresses().iterator().next().get("state").toString();
        }
        stockMap.put("StateCodeName",getStateCode(code)+"/"+code);
        String indentcode="-";
        if(null!=indentorg && null!=indentorg.getAddresses() && indentorg.getAddresses().iterator().hasNext())
        {
             indentcode=indentorg.getAddresses().iterator().next().get("state").toString();
        }
        stockMap.put("indentStateCodeName",getStateCode(indentcode)+"/"+indentcode);

        stockMap.put("generatedBy",userDisplayName);
         stockMap.put("generatedOn",LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));

        return stockMap;

    }

    private String getStateCode(String stateName) {
        String stateCode = "-";
        SearchHits<Map> state=null;
        if(!stateName.equalsIgnoreCase("-")) {
            Query query = new NativeSearchQueryBuilder().withSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .withQuery(QueryBuilders.queryStringQuery("name:" + stateName)).build();
            state = elasticsearchTemplate.search(query, Map.class, IndexCoordinates.of("state"));
        }
        try {
            log.debug("State Detail" + state);
            if (state != null) {
                for (org.springframework.data.elasticsearch.core.SearchHit<Map> hit : state.getSearchHits()) {
                    Map<String, Object> stateDoc = hit.getContent();
                    if (null != stateDoc && stateDoc.containsKey("code")) {
                        stateCode = stateDoc.get("code").toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stateCode;
    }

    private Organization getOrganizationData(Long unitId) {
        Organization org;
        String cacheKey = "PHR: unit.id:"+unitId;
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            org = pharmacyRedisCacheService.fetchOrganization(elasticsearchTemplate,unitId,cacheKey);
        }else
        {
            org = getRecord("organization", "id:"+unitId, elasticsearchTemplate, Organization.class);
        }
        return org;
    }

    private Map<Long, String> getHSNCodeData(List<IssueDocumentLine> line) {
        Map<Long, String> hsnCodeData = new HashMap<>();
        line.stream().forEach(lineItem -> {
                    Query query = new NativeSearchQueryBuilder()
                        .withQuery(boolQuery()
                            .must(matchQuery("item.id", lineItem.getItem().getId()))
                        ).build();
                    List<ItemTaxMapping> itemTaxMapping = elasticsearchTemplate.search(query, ItemTaxMapping.class, IndexCoordinates.of("itemtaxmapping")).get().map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
                    if (nonNull(itemTaxMapping) && !itemTaxMapping.isEmpty())
                        hsnCodeData.put(itemTaxMapping.get(0).getItem().getId(), itemTaxMapping.get(0).getHsnCode());
                    log.debug("SearchQuery" + query);

            }
        );
        return hsnCodeData;
    }

    private StockIssue getStockIssue(Long issueId, String issueNumber) throws Exception {
        CriteriaQuery query = null;
        if (issueId != null) {
            query = new CriteriaQuery(new Criteria("id").is(issueId));
        } else {
            query = new CriteriaQuery(new Criteria("documentNumber.raw").is(issueNumber));
        }
        if (query == null) {
            throw new Exception("Atleast Id or Issue Number should be provied");
        }

        return ElasticSearchUtil.queryForObject("stockissue", query, elasticsearchTemplate, StockIssue.class);
    }

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

    private Map<String, Object> populateIssueData(StockIssue stockIssue) {
        String publishedBy = getUserDisplayName();
        Map<String, Object> stockMap = new HashMap<>();
        stockMap.put("issueNo", stockIssue.getDocumentNumber());
        stockMap.put("indentUnit", stockIssue.getDocument().getIndentUnit().getName());
        stockMap.put("indentStore", stockIssue.getDocument().getIndentStore().getName());
        stockMap.put("issueUnit", stockIssue.getDocument().getIssueUnit().getName());
        stockMap.put("issueStore", stockIssue.getDocument().getIssueStore().getName());
        stockMap.put("indentPriority", stockIssue.getDocument().getPriority().equals(Priority.ROUTINE) ? "Normal" : "Urgent");
        stockMap.put("indenter", "Indenter");
        stockMap.put("indentNumber", "some number");
        stockMap.put("status", stockIssue.getDocument().getStatus().getStatusDisplay());
        stockMap.put("issueLines", stockIssue.getDocument().getLines());
        stockMap.put("issueDate", stockIssue.getDocument().getIssueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockMap.put("createdBy", stockIssue.getDocument().getIssuedBy() != null ? stockIssue.getDocument().getIssuedBy().getDisplayName() : "-");
        stockMap.put("createdOn", stockIssue.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockMap.put("approvedBy", stockIssue.getDocument().getApprovedBy() != null ? stockIssue.getDocument().getApprovedBy().getDisplayName() : "-");
        stockMap.put("approvedOn", stockIssue.getDocument().getApprovedDate() != null ? stockIssue.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")) : "-");
        stockMap.put("publishedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockMap.put("datetimeformatter", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        stockMap.put("publishedBy", publishedBy);

        return stockMap;
    }

    private String getUserDisplayName() {
        String publishedBy;
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            String cacheKey = Constants.USER_LOGIN+SecurityUtils.getCurrentUserLogin().get();
            UserDTO user = pharmacyRedisCacheService.getUserData(cacheKey,elasticsearchTemplate);
            publishedBy = user.getDisplayName();
        }else {
            publishedBy= ElasticSearchUtil.queryForObject("user", new CriteriaQuery(new Criteria("login.raw").is(SecurityUtils.getCurrentUserLogin().get())), elasticsearchTemplate, User.class).getDisplayName();
        }
        return publishedBy;
    }

    private String getUserDisplayNameForInterUnit() {
        String publishedBy;
        if (applicationProperties.getRedisCache().isCacheEnabled()) {
            String cacheKey = Constants.USER_LOGIN + SecurityUtils.getCurrentUserLogin().get();
            UserDTO user = pharmacyRedisCacheService.getUserData(cacheKey, elasticsearchTemplate);
            publishedBy = user.getDisplayName() + (user.getEmployeeNo() != null ?", "  + user.getEmployeeNo() : "");
        } else {
            User user = ElasticSearchUtil.queryForObject("user", new CriteriaQuery(new Criteria("login.raw").is(SecurityUtils.getCurrentUserLogin().get())), elasticsearchTemplate, User.class);
            publishedBy = user.getDisplayName() + (user.getEmployeeNo() != null ?", " + user.getEmployeeNo() : "");
        }
        return publishedBy;
    }

    public byte[] getStockIssuePDF(Long issueId, String documentNumber, String original,String documentType) throws Exception {
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;
        String docType="";
        Map<String, Object> outData = this.getStockIssueHTML(issueId, documentNumber,documentType);
        String htmlData = outData.get("html").toString();
        contentInBytes = PdfGenerator.createPDF(htmlData);

        return contentInBytes;
    }

    public byte[] getDirectTransferPDF(StockIssue stockIssue) throws Exception {
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;
        Map<String, Object> outData = this.getDirectTransferHTML(stockIssue);
        String htmlData = outData.get("html").toString();
        contentInBytes = PdfGenerator.createPDF(htmlData);

        return contentInBytes;
    }

    @Override
    public Map<String, Object> getDirectTransferHTML(StockIssue stockIssue) throws Exception {
        log.debug("issueIssuer: {}", stockIssue);

        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "directTransfer.ftl"; // Fixed template
        Map<String, Object> directTransferData = populateDirectTransferData(stockIssue);
        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, directTransferData);
        printFile.put("html", html);
        return printFile;
    }

    private Map<String, Object> populateDirectTransferData(StockIssue stockIssue) {
        String publishedBy = getUserDisplayName();
        Map<String, Object> directTransferData = new HashMap<>();
        directTransferData.put("issueNo", stockIssue.getDocumentNumber());
        directTransferData.put("indentUnit", stockIssue.getDocument().getIndentUnit().getName());
        directTransferData.put("indentStore", stockIssue.getDocument().getIndentStore().getName());
        directTransferData.put("issueUnit", stockIssue.getDocument().getIssueUnit().getName());
        directTransferData.put("issueStore", stockIssue.getDocument().getIssueStore().getName());
        directTransferData.put("status", stockIssue.getDocument().getStatus().getStatusDisplay());
        directTransferData.put("issueLines", stockIssue.getDocument().getLines());
        directTransferData.put("issueDate", stockIssue.getDocument().getIssueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        directTransferData.put("createdBy", stockIssue.getDocument().getIssuedBy() != null ? stockIssue.getDocument().getIssuedBy().getDisplayName() : "-");
        directTransferData.put("createdOn", stockIssue.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        directTransferData.put("approvedBy", stockIssue.getDocument().getApprovedBy() != null ? stockIssue.getDocument().getApprovedBy().getDisplayName() : "-");
        directTransferData.put("approvedOn", stockIssue.getDocument().getApprovedDate() != null ? stockIssue.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")) : "-");
        directTransferData.put("publishedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        directTransferData.put("datetimeformatter", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        directTransferData.put("publishedBy", publishedBy);

        return directTransferData;
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<StockIssue> search = this.search("documentNumber.raw:" + documentNumber, PageRequest.of(0,1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        StockIssue stockIssue = search.iterator().next();
        Map<String, Object> configurations = retrieveWorkflowConfigurations(stockIssue, true);
        //Start workflow if workflow enabled
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(stockIssue, "SENDFORAPPROVAL", configurations);
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
