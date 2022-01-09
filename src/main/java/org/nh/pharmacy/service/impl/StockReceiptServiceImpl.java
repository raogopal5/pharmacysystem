package org.nh.pharmacy.service.impl;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
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
import org.nh.common.dto.LocatorDTO;
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
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.StockIssueRepository;
import org.nh.pharmacy.repository.StockReceiptRepository;
import org.nh.pharmacy.repository.StockRepository;
import org.nh.pharmacy.repository.StockTransitRepository;
import org.nh.pharmacy.repository.search.StockIssueSearchRepository;
import org.nh.pharmacy.repository.search.StockReceiptSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.mapper.IssueToReceiptMapper;
import org.nh.pharmacy.web.rest.mapper.ReversalToReceiptMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.domain.enumeration.Context.Receipt_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.TransactionType.Inter_Unit_Stock_Receipt;
import static org.nh.pharmacy.util.ConfigurationUtil.getCommaSeparatedGroupCodes;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;

/**
 * Service Implementation for managing StockReceipt.
 */
@Service
@Transactional
public class StockReceiptServiceImpl implements StockReceiptService {

    private final Logger log = LoggerFactory.getLogger(StockReceiptServiceImpl.class);

    private final StockReceiptRepository stockReceiptRepository;

    private final StockReceiptSearchRepository stockReceiptSearchRepository;

    private final StockIndentService stockIndentService;

    private final StockIssueRepository stockIssueRepository;

    private final StockIssueSearchRepository stockIssueSearchRepository;

    private final StockIssueService stockIssueService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final StockReversalService stockReversalService;

    private final StockService stockService;

    private final WorkflowService workflowService;

    private final IssueToReceiptMapper issueToReceiptMapper;

    private final ReversalToReceiptMapper reversalToReceiptMapper;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ItemStoreLocatorMapService itemStoreLocatorMapService;

    private final RuleExecutorService ruleExecutorService;

    private final GroupService groupService;

    private final ApplicationProperties applicationProperties;

    private final FreemarkerService freemarkerService;

    private final StockTransitRepository stockTransitRepository;

    private  final StockRepository stockRepository;

    private final RestHighLevelClient restHighLevelClient;

    private final StockSourceService stockSourceService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Value("${server.port}")
    private String portNo;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    public StockReceiptServiceImpl(StockReceiptRepository stockReceiptRepository, StockReceiptSearchRepository stockReceiptSearchRepository, StockIndentService stockIndentService,
                                   StockIssueRepository stockIssueRepository, StockIssueSearchRepository stockIssueSearchRepository, StockIssueService stockIssueService, StockService stockService,
                                   SequenceGeneratorService sequenceGeneratorService, StockReversalService stockReversalService,
                                   WorkflowService workflowService, IssueToReceiptMapper issueToReceiptMapper, ReversalToReceiptMapper reversalToReceiptMapper, ElasticsearchOperations elasticsearchTemplate, ItemStoreLocatorMapService itemStoreLocatorMapService,
                                   RuleExecutorService ruleExecutorService, GroupService groupService, ApplicationProperties applicationProperties, FreemarkerService freemarkerService, StockTransitRepository stockTransitRepository, StockRepository stockRepository, RestHighLevelClient restHighLevelClient, StockSourceService stockSourceService, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.stockReceiptRepository = stockReceiptRepository;
        this.stockReceiptSearchRepository = stockReceiptSearchRepository;
        this.stockIndentService = stockIndentService;
        this.stockIssueRepository = stockIssueRepository;
        this.stockIssueSearchRepository = stockIssueSearchRepository;
        this.stockIssueService = stockIssueService;
        this.stockService = stockService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.stockReversalService = stockReversalService;
        this.workflowService = workflowService;
        this.issueToReceiptMapper = issueToReceiptMapper;
        this.reversalToReceiptMapper = reversalToReceiptMapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.itemStoreLocatorMapService = itemStoreLocatorMapService;
        this.ruleExecutorService = ruleExecutorService;
        this.groupService = groupService;
        this.applicationProperties = applicationProperties;
        this.freemarkerService = freemarkerService;
        this.stockTransitRepository = stockTransitRepository;
        this.stockRepository=stockRepository;
        this.restHighLevelClient = restHighLevelClient;
        this.stockSourceService = stockSourceService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a stockReceipt.
     *
     * @param stockReceipt the entity to save
     * @return the persisted entity
     */
    @Override
    public StockReceipt save(StockReceipt stockReceipt) throws SequenceGenerateException {
        log.debug("Request to save StockReceipt : {}", stockReceipt);
        saveValidation(stockReceipt);
        if (stockReceipt.getId() == null) {
            stockReceipt.id(stockReceiptRepository.getId());
            stockReceipt.getDocument().setId(stockReceipt.getId().toString());
            stockReceipt.version(0);
            if ((Status.DRAFT).equals(stockReceipt.getDocument().getStatus())) {
                stockReceipt.documentNumber(StringUtils.join(new Object[]{Status.DRAFT, stockReceipt.getId()}, "-"));
            }
        } else {
            stockReceiptRepository.updateLatest(stockReceipt.getId());
            stockReceipt.version(stockReceipt.getVersion() + 1);
        }
        stockReceipt.getDocument().setDocumentNumber(stockReceipt.getDocumentNumber());
        stockReceipt.setLatest(true);
        stockReceipt.getDocument().getLines().forEach(stockReceiptLine -> {
            if (stockReceiptLine.getId() == null) stockReceiptLine.setId(stockReceiptRepository.getId());
        });
        StockReceipt result = stockReceiptRepository.saveAndFlush(stockReceipt);
//        stockReceiptSearchRepository.save(result);
        return result;
    }


    private void lockStockIssue(StockReceipt stockReceipt) {
        Optional<SourceDocument> sourceDocumentOptional = stockReceipt.getDocument().getLines().stream().findFirst().get()
            .getSourceDocument().stream()
            .filter(srcDocument -> TransactionType.Stock_Issue.equals(srcDocument.getType()) || TransactionType.Inter_Unit_Stock_Issue.equals(srcDocument.getType())
                || TransactionType.Stock_Direct_Transfer.equals(srcDocument.getType())).findFirst();
        if(sourceDocumentOptional.isPresent()){
            stockIssueService.findOneWithLock(sourceDocumentOptional.get().getId());
        }
    }

    /**
     * Save a stockReceipt.
     *
     * @param stockReceipt the entity to save
     * @param action       to perform
     * @return the persisted entity
     */
    @Override
    @PublishStockTransaction
    public StockReceipt save(StockReceipt stockReceipt, String action) throws Exception {
        log.debug("Request to save StockReceipt with action : {}", stockReceipt);
        StockReceipt result;
        lockStockIssue(stockReceipt);
        switch (action) {
            case "SENDFORAPPROVAL":
                log.debug("Request to send approval StockReceipt : {}", stockReceipt);
                validateSendForApproval(stockReceipt);
                validateReceiptDocument(stockReceipt);
                if(!stockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal)) {
                    if(!stockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
                        validateSourceQuantity(stockReceipt);
                    }
                }
                stockReceipt.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
                if (stockReceipt.getDocument().isDraft()) {
                    stockReceipt.getDocument().setDraft(false);
                    stockReceipt.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Receipt.name(), "NH", stockReceipt));
                    result = save(stockReceipt);
                    //Get workflow configurations
                    Map<String, Object> configurations = retrieveWorkflowConfigurations(stockReceipt, true);
                    //Start workflow if workflow enabled
                    if ((Boolean) configurations.get("enableWorkflow")) {
                        startWorkflow(result, action, configurations);
                    }
                    index(result);
                    //  checkForCompleteConversionOfSourceDocument(result, action);
                    return result;
                }
                break;
            case "APPROVED":
                log.debug("Request to approve StockReceipt : {}", stockReceipt);
                validateReceiptDocument(stockReceipt);
                validatestockReceipt(stockReceipt);
                if(!stockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal)) {
                    if(!stockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
                        validateSourceQuantity(stockReceipt);
                    }
                }
                result = approveStockReceipt(stockReceipt);
                //StockServiceAspect.threadLocal.get().put("transitStoreType",result.getDocument().getDocumentType());
                assignTransitStoreToMap(result);
                publishStockAutoConsumption(result);
                index(result);
                //  checkForCompleteConversionOfSourceDocument(result, action);
                return result;
            case "REJECTED":
                log.debug("Request to reject StockReceipt : {}", stockReceipt);
                //validateReceiptDocument(stockReceipt);
                //validatestockReceipt(stockReceipt);
                stockReceipt.getDocument().setStatus(Status.REJECTED);
                break;
            default:
                log.debug("Request to save as draft StockReceipt : {}", stockReceipt);
                if (stockReceipt.getDocument().getStatus() == null) {
                    stockReceipt.getDocument().setStatus(Status.DRAFT);
                    validateDraft(stockReceipt);
                } else {
                    validateReceiptDocument(stockReceipt);
                }
                break;
        }
        result = save(stockReceipt);
        index(result);
        //  checkForCompleteConversionOfSourceDocument(result, action);
        return result;
    }

    public void checkForCompleteConversionOfSourceDocument(StockReceipt stockReceipt, String action) throws SequenceGenerateException {
        if ("REJECTED".equals(action)) {
            if (!stockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal) && !stockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
                SourceDocument sourceDocument = stockReceipt.getDocument().getLines().stream().findFirst().get()
                    .getSourceDocument().stream()
                    .filter(srcDocument -> TransactionType.Stock_Issue.equals(srcDocument.getType()) || TransactionType.Inter_Unit_Stock_Issue.equals(srcDocument.getType())
                        || TransactionType.Stock_Direct_Transfer.equals(srcDocument.getType())).findFirst().get();
                updateStockIssueConversionCompleted(Boolean.FALSE, sourceDocument.getId());
            }
            return;
        } else if (!"SENDFORAPPROVAL".equals(action) && !"APPROVED".equals(action)) {
            return;
        }
        if (stockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal) || stockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
            SourceDocument sourceDocument = stockReceipt.getDocument().getLines().stream().findFirst().get()
                .getSourceDocument().stream()
                .filter(srcDocument -> TransactionType.Stock_Reversal.equals(srcDocument.getType()) || TransactionType.Inter_Unit_Stock_Reversal.equals(srcDocument.getType())).findFirst().get();
            updateStockReversalConversionCompleted(Boolean.TRUE, sourceDocument.getId());
            return;
        }
       /* boolean isCompletelyConverted = Boolean.TRUE;
        Long stockIssueId = null;
        for (ReceiptDocumentLine receiptDocumentLine: stockReceipt.getDocument().getLines()) {
            SourceDocument issueSource = receiptDocumentLine.getSourceDocument().stream()
                .filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Issue)
                    || TransactionType.Stock_Direct_Transfer.equals(sourceDocument.getType())).findFirst().get();
            stockIssueId = issueSource.getId();
            if (issueSource.getPendingQuantity().getValue() -
                receiptDocumentLine.getAcceptedQuantity().getValue()
                - receiptDocumentLine.getRejectedQuantity().getValue() != 0) {
                isCompletelyConverted = Boolean.FALSE;
                break;
            }
        }
        updateStockIssueConversionCompleted(isCompletelyConverted, stockIssueId);*/
        StockIssue stockIssue = null;
        List<ReceiptDocumentLine> lines = stockReceipt.getDocument().getLines();
        SourceDocument src = lines.get(0) != null ? lines.get(0).getSourceDocument().stream().
            filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Issue) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Issue)
                || sourceDocument.getType().equals(TransactionType.Stock_Direct_Transfer)).findAny().get() : null;
        stockIssue = src != null ? stockIssueService.findDetachedOne(src.getId()) : null;

        checkIssueConversion(stockIssue);
    }

    private void checkIssueConversion(StockIssue stockIssue) throws SequenceGenerateException {

        Map<Long, Float> map = new HashMap<>();
        Map<Long, Float> receivedQuantityValue = new HashMap<>();
        boolean isCompletelyConverted = Boolean.TRUE;
        for (IssueDocumentLine line : stockIssue.getDocument().getLines()) {
            map.put(line.getId(), line.getIssuedQuantity().getValue());
            Page<StockReceipt> stockReceipts = stockReceiptSearchRepository.search(queryStringQuery("NOT (document.status.raw:(" + Status.DRAFT + " OR " + Status.REJECTED + ")) document.lines.sourceDocument.documentNumber.raw:" + stockIssue.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + line.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockReceipt> stockReceiptIterator = stockReceipts.iterator();
            while (stockReceiptIterator.hasNext()) {
                StockReceipt stockReceiptTemp = stockReceiptIterator.next();
                List<ReceiptDocumentLine> linesForReversal = stockReceiptTemp.getDocument().getLines();
                SourceDocument srcForReversal = linesForReversal.get(0) != null ? linesForReversal.get(0).getSourceDocument().stream().
                    filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Reversal) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Reversal)).findAny().orElse(null) : null;
                if (srcForReversal == null) {
                    for (ReceiptDocumentLine itemLine : stockReceiptTemp.getDocument().getLines()) {
                        Optional<SourceDocument> sourceDocument = itemLine.getSourceDocument().stream().filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(stockIssue.getDocumentNumber())).findAny();
                        if (sourceDocument.isPresent() && sourceDocument.get().getLineId().equals(line.getId())) {
                            receivedQuantityValue.put(line.getId(), receivedQuantityValue.get(line.getId()) != null ? receivedQuantityValue.get(line.getId()) + itemLine.getAcceptedQuantity().getValue() + itemLine.getRejectedQuantity().getValue()
                                : itemLine.getAcceptedQuantity().getValue() + itemLine.getRejectedQuantity().getValue());

                        }
                    }
                }
            }

        }

        for (IssueDocumentLine line : stockIssue.getDocument().getLines()) {
            map.put(line.getId(),
                (map.get(line.getId())
                    - (receivedQuantityValue.get(line.getId()) != null ? receivedQuantityValue.get(line.getId()) : 0f)));
            if (map.get(line.getId()) != 0) {
                isCompletelyConverted = Boolean.FALSE;
                break;
            }
        }
        updateStockIssueConversionCompleted(isCompletelyConverted, stockIssue.getId());
    }


    private void updateStockIssueConversionCompleted(boolean isCompletelyConverted, Long docId) throws SequenceGenerateException {
        StockIssue stockIssue = stockIssueService.findDetachedOne(docId);
        stockIssue.getDocument().setConversionCompleted(isCompletelyConverted);
        stockIssueService.updateSourceDocumentOnDestinationModification(stockIssue);
        stockIssueService.index(stockIssue);
    }

    private void updateStockReversalConversionCompleted(boolean isCompletelyConverted, Long docId) throws SequenceGenerateException {
        StockReversal stockReversal = stockReversalService.findDetachedOne(docId);
        stockReversal.getDocument().setConversionCompleted(isCompletelyConverted);
        stockReversalService.save(stockReversal);
    }

    /**
     * @param stockReceipt
     * @return the persist entity
     */
    private StockReceipt approveStockReceipt(StockReceipt stockReceipt) throws Exception {
        StockReceipt result = null;
        stockReceipt.getDocument().setStatus(Status.APPROVED);
        result = save(stockReceipt);
        List<StockFlow> stockFlows = stockService.moveFromTransitToStock(getListStockEntry(result), getListTransitEntry(result));
        Map<Long, String> lineSkuMap = new HashMap<>();
        stockFlows.forEach(stockFlow -> {
            lineSkuMap.put(stockFlow.getTransactionLineId(), stockFlow.getSku());
        });
        changeStatus(result, lineSkuMap);
        return result;
    }


    private List<StockTransit> getListTransitEntry(StockReceipt stockReceipt) {

        List<StockTransit> stockTransits = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        stockReceipt.getDocument().getLines().forEach(receiptDocumentLine -> {
            receiptDocumentLine.getSourceDocument().stream().filter(souceDoc -> stockReceipt.getDocument().getSourceType().equals(souceDoc.getType()))
                .forEach(sourceDocument -> {
                    if((receiptDocumentLine.getAcceptedQuantity().getValue() + receiptDocumentLine.getRejectedQuantity().getValue())>0) {
                        StockTransit stockTransit = new StockTransit();
                        stockTransit.setTransactionLineId(sourceDocument.getLineId());
                        stockTransit.setTransactionType(sourceDocument.getType());
                        stockTransit.setTransactionNo(sourceDocument.getDocumentNumber());
                        stockTransit.setTransactionDate(sourceDocument.getDocumentDate());
                        stockTransit.setQuantity(receiptDocumentLine.getAcceptedQuantity().getValue() + receiptDocumentLine.getRejectedQuantity().getValue());
                        stockTransit.setUserId(sourceDocument.getCreatedBy().getId());
                        stockTransits.add(stockTransit);
                    }
                });

        });


        return stockTransits;
    }

    private void changeStatus(StockReceipt stockReceipt, Map<Long, String> lineSkuMap) throws Exception {
        log.debug("Request to change document status");
        StockIssue issueDoc = null;
        StockIndent indentDoc = null;

        List<ReceiptDocumentLine> lines = stockReceipt.getDocument().getLines();
        SourceDocument src = lines.get(0) != null ? lines.get(0).getSourceDocument().stream().
            filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Issue) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Issue)
                || sourceDocument.getType().equals(TransactionType.Stock_Direct_Transfer)).findAny().get() : null;
        issueDoc = src != null ? stockIssueService.findDetachedOne(src.getId()) : null;

        SourceDocument srcForIndent = lines.get(0) != null ? lines.get(0).getSourceDocument().stream().
            filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Indent) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Indent)).findAny().orElse(null) : null;
        indentDoc = srcForIndent != null ? stockIndentService.findDetachedOne(srcForIndent.getId()) : null;

        boolean isReversalCreated = checkAndCreateStockReversal(stockReceipt, indentDoc, issueDoc, lineSkuMap);
        if (isReversalCreated) {
            return;
        }
        if (stockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal) || stockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
            SourceDocument srcStockReversal = lines.get(0) != null ? lines.get(0).getSourceDocument().stream().
                filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Reversal) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Reversal)).findAny().orElse(null) : null;
            StockReversal stockReversal = srcStockReversal != null ? stockReversalService.findDetachedOne(srcStockReversal.getId()) : null;
            stockReversal.getDocument().setStatus(Status.PROCESSED);
            stockReversal.getDocument().setModifiedDate(LocalDateTime.now());
            stockReversalService.save(stockReversal);
        }
        updateStockIssueStatus(stockReceipt, issueDoc);
        if (indentDoc != null && !indentDoc.getDocument().getStatus().equals(Status.CLOSED))
            updateStockIndentStatus(stockReceipt, indentDoc, issueDoc);

    }

    private void updateStockIndentStatus(StockReceipt stockReceipt, StockIndent indentDoc, StockIssue stockIssue) {
        Map<Long, Float> map = new HashMap<>();
        Page<StockReversal> stockReversals = stockReversalService.searchForDocument("document.status.raw:" + Status.APPROVED + " document.lines.sourceDocument.documentNumber.raw:" + indentDoc.getDocumentNumber(),
            PageRequest.of(0, 1));
        if (stockReversals.hasContent()) {
            updateIndentStatus(indentDoc, Status.REVERSAL_PENDING);
            return;
        }

        if (stockIssueSearchRepository.search(queryStringQuery("document.status.raw:" + Status.PARTIALLY_PROCESSED
                + " AND document.lines.sourceDocument.documentNumber.raw:" + indentDoc.getDocumentNumber()),
            PageRequest.of(0,1)).hasContent()) {
            updateIndentStatus(indentDoc, Status.PARTIALLY_RECEIVED);
            return;
        }

        indentDoc.getDocument().getLines().forEach(indentDocumentLine -> {
            populateProcessedQuantityForIndent(indentDoc, map, indentDocumentLine, stockReceipt);
            Page<StockReceipt> stockReceipts = stockReceiptSearchRepository.search(queryStringQuery("document.status.raw:" + Status.APPROVED + " document.lines.sourceDocument.documentNumber.raw:" + indentDoc.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + indentDocumentLine.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            stockReceipts.forEach(approvedStockReceipt -> {
                populateProcessedQuantityForIndent(indentDoc, map, indentDocumentLine, approvedStockReceipt);
            });
            populateStockReversalQuantityForIndentLine(indentDoc, indentDocumentLine, map);
        });
        Set<Boolean> isStatusUpdated = new HashSet<>(2);

        for (IndentDocumentLine indentDocumentLine : indentDoc.getDocument().getLines()) {
            if(map.get(indentDocumentLine.getId()) == null) {
                map.put(indentDocumentLine.getId(), 0f);
            }
            if (!map.get(indentDocumentLine.getId()).equals(indentDocumentLine.getQuantity().getValue())) {
                updateIndentStatus(indentDoc, Status.PARTIALLY_PROCESSED);
                isStatusUpdated.add(Boolean.TRUE);
                return;
            }
        }

        if (isStatusUpdated.size() == 0)
            updateIndentStatus(indentDoc, Status.PROCESSED);
    }

    private void populateProcessedQuantityForIndent(StockIndent indentDoc, Map<Long, Float> map, IndentDocumentLine indentDocumentLine, StockReceipt approvedStockReceipt) {
        if (approvedStockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal) || approvedStockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
            return;
        }
        approvedStockReceipt.getDocument().getLines().forEach(receiptDocumentLine -> {
            boolean isSameLine = receiptDocumentLine.getSourceDocument().stream().
                anyMatch(sourceDocument -> sourceDocument.getDocumentNumber().equals(indentDoc.getDocumentNumber())
                    && sourceDocument.getLineId().equals(indentDocumentLine.getId()));
            if (isSameLine) {
                map.put(indentDocumentLine.getId(), map.get(indentDocumentLine.getId()) != null ? map.get(indentDocumentLine.getId()) + receiptDocumentLine.getAcceptedQuantity().getValue()
                    : receiptDocumentLine.getAcceptedQuantity().getValue());
            }
        });
    }

    private void populateStockReversalQuantityForIndentLine(StockIndent stockIndent, IndentDocumentLine indentDocumentLine, Map<Long, Float> map) {
        Page<StockReversal> stockReversals = stockReversalService.searchForDocument("document.status.raw:" + Status.APPROVED + " document.lines.sourceDocument.documentNumber.raw:" + stockIndent.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + indentDocumentLine.getId(),
            PageRequest.of(0, 9999));
        stockReversals.forEach(approvedStockReversal -> {
            approvedStockReversal.getDocument().getLines().forEach(reversalDocumentLine -> {
                boolean isSameLine = reversalDocumentLine.getSourceDocument().stream().
                    anyMatch(sourceDocument -> sourceDocument.getDocumentNumber().equals(stockIndent.getDocumentNumber())
                        && sourceDocument.getLineId().equals(indentDocumentLine.getId()));
                if (isSameLine) {
                    map.put(indentDocumentLine.getId(), map.get(indentDocumentLine.getId()) != null ? map.get(indentDocumentLine.getId()) - reversalDocumentLine.getRejectedQuantity().getValue()
                        : 0 - reversalDocumentLine.getRejectedQuantity().getValue());
                }
            });
        });
    }

    private void updateStockIssueStatus(StockReceipt stockReceipt, StockIssue issueDoc) {
        Map<Long, Float> map = new HashMap<>();
        if (issueDoc.getDocument().getDocumentType().equals(TransactionType.Stock_Direct_Transfer)) {
            Page<StockReversal> stockReversals = stockReversalService.searchForDocument("document.status.raw:" + Status.APPROVED + " document.lines.sourceDocument.documentNumber.raw:" + issueDoc.getDocumentNumber(),
                PageRequest.of(0, 1));
            if (stockReversals.hasContent()) {
                updateIssueStatus(issueDoc, Status.REVERSAL_PENDING);
                return;
            }
        }
        issueDoc.getDocument().getLines().forEach(issueDocumentLine -> {
            populateProcessedQuantityForIssue(issueDoc, map, issueDocumentLine, stockReceipt);
            Page<StockReceipt> stockReceipts = stockReceiptSearchRepository.search(queryStringQuery("document.status.raw:" + Status.APPROVED + " document.lines.sourceDocument.documentNumber.raw:" + issueDoc.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + issueDocumentLine.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            stockReceipts.forEach(approvedStockReceipt -> {
                populateProcessedQuantityForIssue(issueDoc, map, issueDocumentLine, approvedStockReceipt);
            });
        });
        Set<Boolean> isStatusUpdated = new HashSet<>(2);
        for (IssueDocumentLine issueDocumentLine : issueDoc.getDocument().getLines()) {
            if (!map.get(issueDocumentLine.getId()).equals(issueDocumentLine.getIssuedQuantity().getValue())) {
                updateIssueStatus(issueDoc, Status.PARTIALLY_PROCESSED);
                isStatusUpdated.add(Boolean.TRUE);
                return;
            }
        }
        if (isStatusUpdated.size() == 0)
            updateIssueStatus(issueDoc, Status.PROCESSED);
    }

    private void populateProcessedQuantityForIssue(StockIssue issueDoc, Map<Long, Float> map, IssueDocumentLine issueDocumentLine, StockReceipt approvedStockReceipt) {
        if (approvedStockReceipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal) || approvedStockReceipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
            return;
        }
        approvedStockReceipt.getDocument().getLines().forEach(receiptDocumentLine -> {
            boolean isSameLine = receiptDocumentLine.getSourceDocument().stream().
                anyMatch(sourceDocument -> sourceDocument.getDocumentNumber().equals(issueDoc.getDocumentNumber())
                    && sourceDocument.getLineId().equals(issueDocumentLine.getId()));
            if (isSameLine) {
                map.put(issueDocumentLine.getId(), map.get(issueDocumentLine.getId()) != null ? map.get(issueDocumentLine.getId()) + receiptDocumentLine.getAcceptedQuantity().getValue() + receiptDocumentLine.getRejectedQuantity().getValue()
                    : receiptDocumentLine.getAcceptedQuantity().getValue() + receiptDocumentLine.getRejectedQuantity().getValue());
            }
        });
    }

    private boolean checkAndCreateStockReversal(StockReceipt stockReceipt, StockIndent stockIndent, StockIssue stockIssue, Map<Long, String> lineSkuMap) throws Exception {
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            if (receiptDocumentLine.getRejectedQuantity().getValue() != 0) {
                List<SourceDocument> sourceDocumentList = receiptDocumentLine.getSourceDocument();
                for (SourceDocument sourceDocument : sourceDocumentList) {
                    StockReversal stockReversal = stockReversalService.convertReceiptToReversal(stockReceipt, lineSkuMap);
                    stockReversalService.save(stockReversal, "APPROVED");
                    if (sourceDocument.getType().equals(TransactionType.Stock_Indent) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Indent)) {
                        updateStockIssueStatus(stockReceipt, stockIssue);
                        stockIndent.getDocument().setConversionCompleted(Boolean.FALSE);
                        updateIndentStatus(stockIndent, Status.REVERSAL_PENDING);
                        return true;
                    }
                    if (sourceDocument.getType().equals(TransactionType.Stock_Direct_Transfer)) {
                        updateIssueStatus(stockIssue, Status.REVERSAL_PENDING);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateIndentStatus(StockIndent stockIndent, Status status) {
        stockIndent.getDocument().setStatus(status);
        stockIndent.getDocument().setModifiedDate(LocalDateTime.now());
        StockIndent result = stockIndentService.updateSourceDocumentOnDestinationModification(stockIndent);
        stockIndentService.index(result);
    }

    private void updateIssueStatus(StockIssue stockIssue, Status status) {
        stockIssue.getDocument().setStatus(status);
        stockIssue.getDocument().setModifiedDate(LocalDateTime.now());
        StockIssue result = stockIssueService.updateSourceDocumentOnDestinationModification(stockIssue);
        stockIssueService.index(result);
    }

    private List<StockEntry> getListStockEntry(StockReceipt stockReceipt) throws Exception {
        List<StockEntry> stockEntryList = new ArrayList<>();
        List<ReceiptDocumentLine> receiptDocumentLineList = stockReceipt.getDocument().getLines();
        for (ReceiptDocumentLine receiptDocumentLine : receiptDocumentLineList) {
            if (receiptDocumentLine.getAcceptedQuantity().getValue()
                + receiptDocumentLine.getRejectedQuantity().getValue() > 0) {
                stockEntryList.add(setStockEntry(stockReceipt, receiptDocumentLine));
            }
        }
        return stockEntryList;
    }

    private StockEntry setStockEntry(StockReceipt stockReceipt, ReceiptDocumentLine receiptDocumentLine) throws Exception {
        StockEntry stockEntry = new StockEntry();
        stockEntry.setTransactionLineId(receiptDocumentLine.getId());
        stockEntry.setItemId(receiptDocumentLine.getItem().getId());
        stockEntry.setLocatorId(receiptDocumentLine.getLocator().getId());
//        stockEntry.setStockId(receiptDocumentLine.getStockId());
        for (SourceDocument sourceDocument : receiptDocumentLine.getSourceDocument()) {
            if (sourceDocument.getType().equals(stockReceipt.getDocument().getSourceType())) {
                if(null!=sourceDocument.getStockId()) {
                    Stock stockSource = stockRepository.findById(sourceDocument.getStockId()).get();
                        stockEntry.setOriginalBatchNo(stockSource.getOriginalBatchNo());
                        stockEntry.setOriginalExpiryDate(stockSource.getOriginalExpiryDate());
                        stockEntry.setOriginalMRP(stockEntry.getOriginalMRP());
                }
           }
        }
//        for (SourceDocument sourceDocument : receiptDocumentLine.getSourceDocument()) {
//            if (sourceDocument.getType().equals(TransactionType.Stock_Reversal)) {
//                stockEntry.setStoreId(stockReceipt.getDocument().getIssueStore().getId());
//            } else {
        stockEntry.setStoreId(stockReceipt.getDocument().getIndentStore().getId());
//            }
//        }
        stockEntry.setBatchNo(receiptDocumentLine.getBatchNumber());
        if(receiptDocumentLine.getConsignment()){
            stockEntry.setOwner(receiptDocumentLine.getOwner());
        } else {
            stockEntry.setOwner(stockReceipt.getDocument().getIndentUnit().getCode());
        }
        stockEntry.setUnitId(stockReceipt.getDocument().getIndentUnit().getId());
        stockEntry.setTransactionDate(stockReceipt.getDocument().getApprovedDate() != null ?
            stockReceipt.getDocument().getApprovedDate() : stockReceipt.getDocument().getReceiptDate());
        stockEntry.setTransactionLineId(receiptDocumentLine.getId());
        stockEntry.setQuantity(receiptDocumentLine.getAcceptedQuantity().getValue() + receiptDocumentLine.getRejectedQuantity().getValue());
        stockEntry.setUomId(receiptDocumentLine.getAcceptedQuantity().getUom().getId());
        stockEntry.setCost(receiptDocumentLine.getCost());
        stockEntry.setConsignment(receiptDocumentLine.getConsignment());
        stockEntry.setSupplier(receiptDocumentLine.getSupplier());
        stockEntry.setBarCode(receiptDocumentLine.getBarCode());
        stockEntry.setTransactionId(stockReceipt.getId());
        stockEntry.setTransactionNumber(stockReceipt.getDocumentNumber());
        stockEntry.setTransactionType(stockReceipt.getDocument().getDocumentType());
        stockEntry.setMrp(receiptDocumentLine.getMrp());
        stockEntry.setExpiryDate(receiptDocumentLine.getExpiryDate());
        stockEntry.setUserId(stockReceipt.getDocument().getCreatedBy().getId());
        if(Inter_Unit_Stock_Receipt.equals(stockEntry.getTransactionType())){
            stockEntry.transactionRefNo(stockReceipt.getDocumentNumber()+"-"+receiptDocumentLine.getId());
            stockEntry.setFirstStockInDate(LocalDate.now());
            stockEntry.availableQuantity(stockEntry.getQuantity());
            StockSource stockSource= stockSourceService.findBySkuIdByQuantityAndOrderByFirstStockInDate(receiptDocumentLine.getSku());
            log.info("Inter_Unit_Stock_Receipt sku:{} stockSource:{}", receiptDocumentLine.getSku(), stockSource);
            if (null == stockSource || null == stockSource.getCostWithoutTax()) {
                SourceDocument src = receiptDocumentLine.getSourceDocument().stream().
                    filter(sourceDocument -> (sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Issue))).findAny().get();
                Stock stock = stockRepository.getOne(src.getStockId());

                String sku = StockServiceImpl.constructSKU(stock.getOwner(), stock.getItemId(), stock.getOriginalBatchNo(), stock.getOriginalExpiryDate(),
                    stock.getCost(), stock.getOriginalMRP() == null ? stock.getMrp() : stock.getOriginalMRP(),
                    stock.isConsignment());
                log.info("new sku:{} using stock data for receiptNumber:{}", sku, stockReceipt.getDocumentNumber());
                stockSource = stockSourceService.findBySkuIdByQuantityAndOrderByFirstStockInDate(sku);
                if (null == stockSource || null == stockSource.getCostWithoutTax()) {
                    log.info("Find StockSource by owner:{}, itemId:{}, batchNo:{}, expiryDate:{}", stockReceipt.getDocument().getIssueUnit().getCode(),
                        receiptDocumentLine.getItem().getId(),receiptDocumentLine.getBatchNumber(), receiptDocumentLine.getExpiryDate());
                    StockSource tempStockSource = stockSourceService.findByOwnerItemIdBatchNoAndExpiry(stockReceipt.getDocument().getIssueUnit().getCode(),
                        receiptDocumentLine.getItem().getId(), receiptDocumentLine.getBatchNumber(), receiptDocumentLine.getExpiryDate());
                    if (null != tempStockSource) stockSource = tempStockSource;
                }
            }
            if (null != stockSource) {
                stockEntry.setRecoverableTax(stockSource.getRecoverableTax());
                stockEntry.setCostWithoutTax(stockSource.getCostWithoutTax());
                stockEntry.setTaxPerUnit(stockSource.getTaxPerUnit());
            } else {
                log.warn("Stock source not found for StockReceipt number:{} itemId:{}, sku:{}",stockReceipt.getDocumentNumber(), receiptDocumentLine.getItem().getId(), receiptDocumentLine.getSku());
            }
        }
        return stockEntry;
    }


    /**
     * Get all the stockReceipts.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockReceipt> findAll(Pageable pageable) {
        log.debug("Request to get all StockReceipts");
        Page<StockReceipt> result = stockReceiptRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockReceipt by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockReceipt findOne(Long id) {
        log.debug("Request to get StockReceipt : {}", id);
        StockReceipt stockReceipt = stockReceiptRepository.findOne(id);
        return stockReceipt;
    }

    /**
     * Get one StockReceipt by documentId.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockReceipt findOne(Long id, Integer version) {
        log.debug("Request to get StockReceipt : {}", id, version);
        StockReceipt stockReceipt = stockReceiptRepository.findById(new DocumentId(id, version)).get();
        return stockReceipt;
    }

    /**
     * Delete the  stockReceipt by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) throws BusinessRuleViolationException {
        log.debug("Request to delete StockReceipt : {}", id);
        StockReceipt stockReceipt = stockReceiptRepository.findOne(id);
        validateDelete(stockReceipt);
        stockReceiptRepository.delete(id);
        stockReceiptSearchRepository.deleteById(id);
        checkForCompleteConversionOfSourceDocument(stockReceipt, "DELETE");
    }

    /**
     * Delete the  StockReceipt by documentId.
     *
     * @param id,version the id of the entity
     */
    @Override
    public void delete(Long id, Integer version) {
        log.debug("Request to delete StockReceipt : {}", id, version);
        stockReceiptRepository.deleteById(new DocumentId(id, version));
        stockReceiptSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockReceipt corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockReceipt> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockReceipts for query {}", query);
        return stockReceiptSearchRepository.search(queryStringQuery(query)
            .field("document.indentStore.name").field("document.indentUnit.name").field("document.issueStore.name")
            .field("document.status").field("documentNumber")
            .defaultOperator(Operator.AND), pageable);
    }

    /**
     * Search for the stockReceipt corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockReceipt> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockReceipts for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("document.indentStore.name").field("document.indentUnit.name").field("document.issueStore.name")
                .field("document.status").field("documentNumber")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return stockReceiptSearchRepository.search(searchQuery);
    }

    @Override
    public StockReceipt convertIssueToReceipt(Long docId, String docNo) {
        StockReceipt result = null;
        String query = null;
        if (docId != null) {
            query = "document.lines.sourceDocument.id:" + docId;
        } else {
            query = "document.lines.sourceDocument.documentNumber.raw:" + docNo;
        }
        query = query + " AND (document.lines.sourceDocument.type:" + TransactionType.Stock_Issue + " OR document.lines.sourceDocument.type:" + TransactionType.Inter_Unit_Stock_Issue + " OR document.lines.sourceDocument.type:" + TransactionType.Stock_Direct_Transfer + ")";
        Page<StockReceipt> issuePage = stockReceiptSearchRepository.search(queryStringQuery(query + " AND (document.status.raw:" + Status.DRAFT + ")"), PageRequest.of(0,1));
        if (issuePage.iterator().hasNext()) {
            result = issuePage.iterator().next();
        } else {
            query = docId != null ? "id:" + docId : "documentNumber.raw:" + docNo;
            Page<StockIssue> page = stockIssueService.search(query, PageRequest.of(0,1));
            result = issueToReceiptMapper.convertFromstockIssue(page.iterator().next());
            populateLocators(result);
        }
        return result;
    }

    @Override
    public StockReceipt convertReversalToReceipt(Long docId, String docNo) {
        StockReceipt result = null;
        String query = null;
        if (docId != null) {
            query = "document.lines.sourceDocument.id:" + docId;
        } else {
            query = "document.lines.sourceDocument.documentNumber.raw:" + docNo;
        }
        query = query + " AND document.lines.sourceDocument.type:Stock_Reversal";
        Page<StockReceipt> issuePage = stockReceiptSearchRepository.search(queryStringQuery(query + " AND (document.status.raw:" + Status.DRAFT + ")"), PageRequest.of(0,1));
        if (issuePage.iterator().hasNext()) {
            result = issuePage.iterator().next();
        } else {
            query = docId != null ? "id:" + docId : "documentNumber.raw:" + docNo;
            Page<StockReversal> page = stockReversalService.search(query, PageRequest.of(0,1));
            result = reversalToReceiptMapper.convertFromstockReversal(page.iterator().next());
            populateLocators(result);
        }
        return result;
    }

    private void populateLocators(StockReceipt stockReceipt) {
        StringBuilder errorMessage = new StringBuilder();
        stockReceipt.getDocument().getLines().forEach(receiptDocumentLine -> {
            Page<ItemStoreLocatorMap> itemStoreLocatorMaps = this.itemStoreLocatorMapService.search(
                "active:true item.id:" + receiptDocumentLine.getItem().getId() + " healthCareServiceCenter.id:" + stockReceipt.getDocument().getIndentStore().getId(), PageRequest.of(0,1));
            if (itemStoreLocatorMaps.hasContent()) {
                Locator locator = itemStoreLocatorMaps.iterator().next().getLocator();
                if (locator != null) {
                    LocatorDTO locatorDTO = new LocatorDTO();
                    locatorDTO.setId(locator.getId());
                    locatorDTO.setCode(locator.getCode());
                    locatorDTO.setName(locator.getName());
                    receiptDocumentLine.setLocator(locatorDTO);
                }
            }
            if (receiptDocumentLine.getLocator() == null) {
                errorMessage
                    .append(errorMessage.length() > 0 ? ", " : "")
                    .append(errorMessage.length() == 0 ? "Locators not mapped for items:" : "")
                    .append(receiptDocumentLine.getItem().getName());
            }
        });
        if (errorMessage.length() > 0) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

    /**
     * Get the stockReceipt status count corresponding to the query.
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
                .field("document.indentStore.name").field("document.indentUnit.name").field("document.issueStore.name")
                .field("document.status").field("documentNumber")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "stockreceipt");
        Terms aggregationTerms = aggregations.get("status_count");
        for (Terms.Bucket bucket : aggregationTerms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }


    /**
     * Execute workflow
     *
     * @param stockReceipt the entity to save
     * @param transition   to be performed
     * @param taskId       task Id
     * @return stockReceipt object
     * @throws Exception
     */
    @Override
    @Transactional
    @PublishStockTransaction
    public StockReceipt executeWorkflow(StockReceipt stockReceipt, String transition, Long taskId) throws Exception {
        StockReceipt result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Send for Approval":
                action = "SENDFORAPPROVAL";
                result = save(stockReceipt, action);
                break;
            case "Approved":
                action = "APPROVED";
                //validateDocumentApprover(stockReceipt);
                result = save(stockReceipt, action);
                //StockServiceAspect.threadLocal.get().put("transitStoreType",result.getDocument().getIndentStore().getId());
                assignTransitStoreToMap(result);
                break;
            case "Rejected":
                action = "REJECTED";
                result = save(stockReceipt, action);
                break;
            case "Modify":
                action = "DRAFT";
                stockReceipt.getDocument().setStatus(Status.DRAFT);
                result = save(stockReceipt);
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

    /**
     * Start workflow
     *
     * @param stockReceipt
     * @param action
     * @Param configurations
     */
    public void startWorkflow(StockReceipt stockReceipt, String action, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", stockReceipt.getId());
            content.put("document_type", stockReceipt.getDocument().getDocumentType());
            content.put("group_id", configurations.get("groupIds"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_number", stockReceipt.getDocumentNumber());
            params.put("receipt_date", stockReceipt.getDocument().getReceiptDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("from_store", stockReceipt.getDocument().getIndentStore().getName());
            params.put("unit_id", String.valueOf(stockReceipt.getDocument().getIndentUnit().getId()));
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
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId) {
        Map<String, Object> configurations, taskDetails;
        StockReceipt stockReceipt = stockReceiptRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(stockReceipt, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) :workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,stockReceipt.getDocument().getCreatedBy().getLogin());
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
     * @param stockReceipt
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(StockReceipt stockReceipt, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_stockreceipt_enable_workflow", stockReceipt.getDocument().getIndentStore().getId(), stockReceipt.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_stockreceipt_workflow_definition", stockReceipt.getDocument().getIndentStore().getId(), stockReceipt.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(Receipt_Approval_Committee, stockReceipt.getDocument().getIndentUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", stockReceipt.getDocument().getIndentStore().getId(), stockReceipt.getDocument().getIndentUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of StockReceipt");
        stockReceiptSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockReceipt latest=true");
        List<StockReceipt> data = stockReceiptRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockReceiptSearchRepository.saveAll(data);
        }
    }


    private void validateDraft(StockReceipt stockReceipt) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockReceipt.getDocument().getIssueStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_STORE));
        }
        if (stockReceipt.getDocument().getIndentStore() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_STORE));
        }
        if (stockReceipt.getDocument().getIssueUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_FROM_UNIT));
        }
        if (stockReceipt.getDocument().getIndentUnit() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_TO_UNIT));
        }
        if (stockReceipt.getDocument().getDocumentType() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_DOCUMENT_TYPE));
        }
        if (stockReceipt.getDocument().getReceivedBy() == null || stockReceipt.getDocument().getReceivedBy().getId() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_RECEIVER_NAME));
        }
        if (stockReceipt.getDocument().getCreatedBy() == null || stockReceipt.getDocument().getCreatedBy().getId() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_NAME));
        }
        if (stockReceipt.getDocument().getCreatedDate() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_CREATED_DATE));
        }
        if (stockReceipt.getDocument().getStatus() == null) {
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_STATUS));
        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    private void validateReceiptDocument(StockReceipt stockReceipt) throws FieldValidationException {
        int zeroReceiptQuantityCounter = 0;
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (stockReceipt.getDocument().getLines() != null) {
            for (ReceiptDocumentLine line : stockReceipt.getDocument().getLines()) {
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
                if (Optional.ofNullable(line.getAcceptedQuantity().getValue()).orElse(Float.valueOf(0)) > 0
                    && line.getAcceptedQuantity().getUom() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UOM, source));
                }
                if (Optional.ofNullable(line.getRejectedQuantity().getValue()).orElse(Float.valueOf(0)) > 0
                    && line.getRejectedQuantity().getUom() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_UOM, source));
                }
                if (Optional.ofNullable(line.getAcceptedQuantity().getValue()).orElse(Float.valueOf(0))
                    + Optional.ofNullable(line.getRejectedQuantity().getValue()).orElse(Float.valueOf(0)) == 0)
                    zeroReceiptQuantityCounter++;

                if (stockReceipt.getDocument().getLines().size() == zeroReceiptQuantityCounter) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("document", stockReceipt.getDocumentNumber());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.ZERO_RECEIPT_QUANTITY_IN_ALL_LINES));
                }
                if ((Optional.ofNullable(line.getAcceptedQuantity().getValue()).orElse(Float.valueOf(0))
                    + Optional.ofNullable(line.getRejectedQuantity().getValue()).orElse(Float.valueOf(0))) > line.getSourceDocument().stream()
                    .filter(sourceDocument -> stockReceipt.getDocument().getSourceType().equals(sourceDocument.getType())).findAny().get().getPendingQuantity().getValue()) {
                    Map<String, Object> source = new HashMap<>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(PharmacyErrorCodes.SUM_OF_ACCEPTED_REJECTED_QUANTITY_MISMATCH_WITH_PENDING_QUANTITY, source));
                }
            }
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockReceipt.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_RECEIPT_DOCUMENT_LINES));
        }
        try {
            validateDraft(stockReceipt);
            if (!errorMessages.isEmpty()) {
                throw new FieldValidationException(errorMessages, "Validation exception");
            }
        } catch (FieldValidationException exception) {
            exception.getErrorMessages().addAll(errorMessages);
            throw exception;
        }
    }

    private void validateSourceQuantity(StockReceipt stockReceipt) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        StockIssue stockIssue = null;
        List<ReceiptDocumentLine> lines = stockReceipt.getDocument().getLines();
        SourceDocument src = lines.get(0) != null ? lines.get(0).getSourceDocument().stream().
            filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Issue) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Issue)
                || sourceDocument.getType().equals(TransactionType.Stock_Direct_Transfer)).findAny().get() : null;
        stockIssue = src != null ? stockIssueService.findDetachedOne(src.getId()) : null;
        if (stockReceipt.getDocument().getLines() != null) {
            errorMessages = validateReceivedQuantity(stockIssue, stockReceipt);
        } else {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("document", stockReceipt.getDocumentNumber());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.NULL_RECEIPT_DOCUMENT_LINES));
        }
        if (!errorMessages.isEmpty()) {
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }

    private List<ErrorMessage> validateReceivedQuantity(StockIssue stockIssue, StockReceipt stockReceipt) {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        Map<Long, Float> map = new HashMap<>();
        Map<Long, Float> receivedQuantityValue = new HashMap<>();
        for(ReceiptDocumentLine receiptLine : stockReceipt.getDocument().getLines()) {
            receiptLine.getSourceDocument().stream().forEach(sourceDocument -> {
                if(sourceDocument.getType().equals(TransactionType.Stock_Issue) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Issue) || sourceDocument.getType().equals(TransactionType.Stock_Direct_Transfer)) {
                    receivedQuantityValue.put(sourceDocument.getLineId(), receiptLine.getAcceptedQuantity().getValue() + receiptLine.getRejectedQuantity().getValue());
                }
            });
        }
        for (IssueDocumentLine line : stockIssue.getDocument().getLines()) {
            map.put(line.getId(), line.getIssuedQuantity().getValue());
            Page<StockReceipt> stockReceipts = stockReceiptSearchRepository.search(queryStringQuery("NOT (document.status.raw:(" + Status.DRAFT + " OR " + Status.REJECTED + ")) document.lines.sourceDocument.documentNumber.raw:" + stockIssue.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + line.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockReceipt> stockReceiptIterator = stockReceipts.iterator();
            while (stockReceiptIterator.hasNext()) {
                StockReceipt stockReceiptTemp = stockReceiptIterator.next();
                List<ReceiptDocumentLine> linesForReversal = stockReceiptTemp.getDocument().getLines();
                SourceDocument srcForReversal = linesForReversal.get(0) != null ? linesForReversal.get(0).getSourceDocument().stream().
                    filter(sourceDocument -> sourceDocument.getType().equals(TransactionType.Stock_Reversal) || sourceDocument.getType().equals(TransactionType.Inter_Unit_Stock_Reversal)).findAny().orElse(null) : null;
                if (srcForReversal == null) {
                    if (!stockReceiptTemp.getId().equals(stockReceipt.getId())) {
                        for (ReceiptDocumentLine itemLine : stockReceiptTemp.getDocument().getLines()) {
                            Optional<SourceDocument> sourceDocument = itemLine.getSourceDocument().stream().filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(stockIssue.getDocumentNumber())).findAny();
                            if (sourceDocument.isPresent() && sourceDocument.get().getLineId().equals(line.getId())) {
                                receivedQuantityValue.put(line.getId(), receivedQuantityValue.get(line.getId()) != null ? receivedQuantityValue.get(line.getId()) + itemLine.getAcceptedQuantity().getValue() + itemLine.getRejectedQuantity().getValue()
                                    : itemLine.getAcceptedQuantity().getValue() + itemLine.getRejectedQuantity().getValue());

                            }
                        }
                    }
                }
            }

        }

        for (IssueDocumentLine line : stockIssue.getDocument().getLines()) {
            if ((map.get(line.getId())
                < (receivedQuantityValue.get(line.getId()) != null ? receivedQuantityValue.get(line.getId()) : 0f))) {
                Map<String, Object> source = new HashMap<>();
                source.put("itemName", line.getItem().getName());
                errorMessages.add(new ErrorMessage(PharmacyErrorCodes.SUM_OF_ACCEPTED_REJECTED_QUANTITY_MISMATCH_WITH_ISSUED_QUANTITY, source));
            }

        }
        return errorMessages;
    }


    /**
     * Validate entity
     *
     * @param stockReceipt
     * @throws BusinessRuleViolationException
     */
    public void validatestockReceipt(StockReceipt stockReceipt) throws BusinessRuleViolationException {
        //ruleExecutorService.executeByGroup(stockReceipt, "stock_receipt_rules");
    }


    public void validateSendForApproval(StockReceipt stockReceipt) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockReceipt, "stock_receipt_send_for_approval_validation");
    }

    public void validateDocumentApprover(StockReceipt stockReceipt) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockReceipt, "stock_receipt_document_approver_validation");
    }

    public void validateDelete(StockReceipt stockReceipt) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockReceipt, "stock_receipt_delete_validation");
    }

    @Override
    public void index(StockReceipt stockReceipt) {
        stockReceiptSearchRepository.save(stockReceipt);
    }

    private void assignTransitStoreToMap(StockReceipt stockReceipt) {
        Map<String, Object> map = (Map) StockServiceAspect.threadLocal.get().get(Channels.ITEM_STORE_STOCK_OUTPUT);
        if (map == null) map = new HashMap<>();
        map.put("transitStoreType", stockReceipt.getDocument().getDocumentType());
        map.put("transitStore", stockReceipt.getDocument().getIndentStore().getId());
        ReceiptDocumentLine line = stockReceipt.getDocument().getLines().stream().filter(receiptDocumentLine -> receiptDocumentLine.getRejectedQuantity().getValue() > 0).findAny().orElse(null);
        if (line != null) {
            map.put("transitStore", map.get("transitStore") + "," + stockReceipt.getDocument().getIssueStore().getId());
        }
        StockServiceAspect.threadLocal.get().put(Channels.ITEM_STORE_STOCK_OUTPUT, map);
    }

    @Override
    public Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException {
        log.debug("Gell all documents which is related to given stockreceipt document:-" + documentNumber);

        Set<RelatedDocument> relatedDocumentList = new LinkedHashSet<>();
        Map<String, Set<RelatedDocument>> finalList = new LinkedHashMap<>();

        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest issueQueryReq= new SearchRequest("stockreceipt");
        SearchSourceBuilder issueSourceQueryReq = new SearchSourceBuilder()
            .query(QueryBuilders.queryStringQuery("documentNumber.raw:" + documentNumber));
        issueQueryReq.source(issueSourceQueryReq);

        SearchRequest receiptQueryReq= new SearchRequest("stockreversal");
        SearchSourceBuilder receiptSourceQueryReq = new SearchSourceBuilder().size(100)
            .query(QueryBuilders.queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + documentNumber))
            .sort(SortBuilders.fieldSort("document.createdDate").order(SortOrder.DESC));
        receiptQueryReq.source(receiptSourceQueryReq);

        request.add(issueQueryReq);request.add(receiptQueryReq);
        MultiSearchResponse mSearchResponse = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);

        MultiSearchResponse.Item[] items = mSearchResponse.getResponses();

        for (MultiSearchResponse.Item item : items) {
            SearchHit[] hits = item.getResponse().getHits().getHits();
            for (SearchHit hit : hits) {
                if (hit.getIndex().equals("stockreceipt")) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    Map<String, Object> document = (Map<String, Object>) source.get("document");
                    List<Map<String, Object>> lines = (List<Map<String, Object>>) document.get("lines");
                    for (Map<String, Object> line : lines) {
                        List<Map<String, Object>> sourceDocuments = (List<Map<String, Object>>) line.get("sourceDocument");
                        for (Map<String, Object> sourceDocument : sourceDocuments) {
                            TransactionType txnType = TransactionType.findByTransactionType(sourceDocument.get("type").toString());
                            String dispDocumentType = txnType.getTransactionTypeDisplay();
                            if (null != finalList && null != finalList.get(dispDocumentType))
                                relatedDocumentList = finalList.get(dispDocumentType);
                            else
                                relatedDocumentList = new LinkedHashSet<>();

                            RelatedDocument relDoc = new RelatedDocument();
                            relDoc.setId(sourceDocument.get("id").toString());
                            relDoc.setDocumentType(TransactionType.valueOf(sourceDocument.get("type").toString()));
                            relDoc.setDocumentNumber(sourceDocument.get("documentNumber").toString());
                            relDoc.setCreatedDate(LocalDateTime.parse(sourceDocument.get("documentDate").toString()));
                            relatedDocumentList.add(relDoc);
                            finalList.put(dispDocumentType, relatedDocumentList);
                        }
                    }
                } else {
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
        }
        return finalList;
    }

    @Override
    public void reIndex(Long id) {
        StockReceipt stockReceipt = stockReceiptRepository.findOne(id);
        if (stockReceipt == null) {
            stockReceiptSearchRepository.deleteById(id);
        } else {
            stockReceiptSearchRepository.save(stockReceipt);
        }
    }

    private void saveValidation(StockReceipt stockReceipt) {
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(stockReceipt.getDocument().getIndentUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with logged in unit");
        }
    }

    /**
     * creates stock-receipt html to print
     *
     * @param receiptId
     * @param documentNumber
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getStockRecieptHTMLByReceiptId(Long receiptId, String documentNumber) throws Exception {
        log.debug("receiptId: {}, receiptNumber: {}", receiptId, documentNumber);
        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "stock-receipt.ftl"; // Fixed template
        StockReceipt stockReceipt = getStockReceipt(receiptId,documentNumber);
        String fileName = stockReceipt.getDocument().getDocumentNumber();
        printFile.put("fileName", fileName);
        Map<String, Object> receiptData = populateStockReceiptData(stockReceipt);
        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, receiptData);
        printFile.put("html", html);
        byte[] contentInBytes = html.getBytes();
        printFile.put("content", contentInBytes);
        if (!(Status.DRAFT.equals(stockReceipt.getDocument().getStatus()) || Status.PENDING_APPROVAL.equals(stockReceipt.getDocument().getStatus()))){
            createHTMLFile(html, fileName);
        }
        return printFile;
    }

    /**
     * returns stock receipt based on id or receipt number
     *
     * @param receiptId
     * @param documentNumber
     * @return
     * @throws Exception
     */
    private StockReceipt getStockReceipt(Long receiptId, String documentNumber) throws Exception{
        CriteriaQuery query = null;

        if(receiptId != null){
            query = new CriteriaQuery(new Criteria("id").is(receiptId));
        } else if(documentNumber != null) {
            query = new CriteriaQuery(new Criteria("documentNumber.raw").is(documentNumber));
        }

        if(query == null) {
            throw new Exception("Atleast Id or Receipt Number should be provided");
        }
        return ElasticSearchUtil.queryForObject("stockreceipt", query, elasticsearchTemplate,StockReceipt.class);
    }


    /**
     *returns stock receipt data
     *
     * @param stockReceipt
     * @return
     */
    private Map<String, Object> populateStockReceiptData(StockReceipt stockReceipt) {
        String displayName = getUserDisplayName();
        Map<String, Object> stockReceiptData = new HashMap<>();
        stockReceiptData.put("receiptNumber", stockReceipt.getDocumentNumber());
        stockReceiptData.put("receiptDate", stockReceipt.getDocument().getReceiptDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        stockReceiptData.put("receivingUnit", stockReceipt.getDocument().getIndentUnit().getName());
        stockReceiptData.put("receivingStore", stockReceipt.getDocument().getIndentStore().getName());
        stockReceiptData.put("issueUnit", stockReceipt.getDocument().getIssueUnit().getName());
        stockReceiptData.put("issueStore", stockReceipt.getDocument().getIssueStore().getName());
        stockReceiptData.put("status", stockReceipt.getDocument().getStatus().getStatusDisplay());
        stockReceiptData.put("lineItems", stockReceipt.getDocument().getLines());
        stockReceiptData.put("createdBy", stockReceipt.getDocument().getReceivedBy()!=null?stockReceipt.getDocument().getReceivedBy().getDisplayName():"-");
        stockReceiptData.put("createdOn", stockReceipt.getDocument().getCreatedDate()!=null?stockReceipt.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")):"-");
        stockReceiptData.put("approvedBy", stockReceipt.getDocument().getApprovedBy()!=null?stockReceipt.getDocument().getApprovedBy().getDisplayName():"-");
        stockReceiptData.put("approvedOn", stockReceipt.getDocument().getApprovedDate()!=null?stockReceipt.getDocument().getApprovedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")):"-");
        stockReceiptData.put("publishedBy",displayName);
        stockReceiptData.put("publishedOn",LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm a")));
        return stockReceiptData;
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
    public byte[] getStockReceiptPdfByReceiptId(Long receiptId, String documentNumber, String original) throws Exception {
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;
            Map<String, Object> outData = this.getStockRecieptHTMLByReceiptId(receiptId, documentNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPDF(htmlData);

        return contentInBytes;
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<StockReceipt> search = this.search("documentNumber.raw:" + documentNumber, PageRequest.of(0,1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        StockReceipt stockReceipt = search.iterator().next();
        Map<String, Object> configurations = retrieveWorkflowConfigurations(stockReceipt, true);
        //Start workflow if workflow enabled
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(stockReceipt, "SENDFORAPPROVAL", configurations);
        }
    }

    private void publishStockAutoConsumption(StockReceipt stockReceipt) {
        TransactionType transactionType = stockReceipt.getDocument().getSourceType();
        if (TransactionType.Stock_Issue.name().equals(transactionType.getTransactionType())
            || TransactionType.Stock_Direct_Transfer.name().equals(transactionType.getTransactionType())
            || TransactionType.Inter_Unit_Stock_Issue.name().equals(transactionType.getTransactionType())) {

            Map<String, Object> map = (Map) StockServiceAspect.threadLocal.get().get(Channels.STORE_AUTO_CONSUMPTION_OUTPUT);
            if (map == null) map = new HashMap<>();
            map.put("documentNumber", stockReceipt.getDocumentNumber());
            map.put("type", stockReceipt.getDocument().getDocumentType());
            log.debug("publishing receipt for auto consumption: {}", map);
            StockServiceAspect.threadLocal.get().put(Channels.STORE_AUTO_CONSUMPTION_OUTPUT, map);
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

}
