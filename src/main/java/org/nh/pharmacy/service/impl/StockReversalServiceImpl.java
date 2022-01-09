package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
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
import org.nh.common.dto.LocatorDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.domain.dto.ReversalDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockRepository;
import org.nh.pharmacy.repository.StockReversalRepository;
import org.nh.pharmacy.repository.search.StockReversalSearchRepository;
import org.nh.pharmacy.service.ElasticSearchQueryService;
import org.nh.pharmacy.service.StockReversalService;
import org.nh.pharmacy.service.StockService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.mapper.ReversalMapper;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;

/**
 * Service Implementation for managing StockReversal.
 */
@Service
@Transactional
public class StockReversalServiceImpl implements StockReversalService {

    private final Logger log = LoggerFactory.getLogger(StockReversalServiceImpl.class);

    private final StockReversalRepository stockReversalRepository;

    private final StockReversalSearchRepository stockReversalSearchRepository;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final StockService stockService;

    private final ReversalMapper reversalMapper;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final RestHighLevelClient restHighLevelClient;

    private final ElasticSearchQueryService elasticSearchQueryService;

    private final StockRepository stockRepository;

    private final ApplicationProperties applicationProperties;

    public StockReversalServiceImpl(StockReversalRepository stockReversalRepository, StockReversalSearchRepository stockReversalSearchRepository,
                                    SequenceGeneratorService sequenceGeneratorService, StockService stockService,
                                    ReversalMapper reversalMapper, ElasticsearchOperations elasticsearchTemplate, RestHighLevelClient restHighLevelClient, ElasticSearchQueryService elasticSearchQueryService,
                                    StockRepository stockRepository, ApplicationProperties applicationProperties) {
        this.stockReversalRepository = stockReversalRepository;
        this.stockReversalSearchRepository = stockReversalSearchRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.stockService = stockService;
        this.reversalMapper = reversalMapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.restHighLevelClient = restHighLevelClient;
        this.elasticSearchQueryService = elasticSearchQueryService;
        this.stockRepository = stockRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Save a stockReversal.
     *
     * @param stockReversal the entity to save
     * @return the persisted entity
     */
    @Override
    public StockReversal save(StockReversal stockReversal) throws SequenceGenerateException {
        log.debug("Request to save StockReversal : {}", stockReversal);
        if (stockReversal.getId() == null) {
            Long stockReversalSeqId = stockReversalRepository.getId();
            stockReversal.id(stockReversalSeqId);
            stockReversal.getDocument().setId(Long.toString(stockReversalSeqId));
            stockReversal.version(0);
            if ((Status.DRAFT).equals(stockReversal.getDocument().getStatus())) {
                stockReversal.documentNumber(StringUtils.join(new Object[]{Status.DRAFT, stockReversal.getId()}, "-"));
            }
        } else {
            stockReversalRepository.updateLatest(stockReversal.getId());
            int version = stockReversal.getVersion() + 1;
            stockReversal.version(version);
        }
        stockReversal.getDocument().setDocumentNumber(stockReversal.getDocumentNumber());
        stockReversal.setLatest(true);
        generateIdsIfRequiredForLines(stockReversal);
        StockReversal result = stockReversalRepository.save(stockReversal);
        stockReversalSearchRepository.save(result);
        return result;
    }

    private void generateIdsIfRequiredForLines(StockReversal stockReversal) {
        if (CollectionUtils.isNotEmpty(stockReversal.getDocument().getLines())) {
            Set<Long> itemIds = new HashSet<>();
            for (ReversalDocumentLine reversalDocumentLine : stockReversal.getDocument().getLines()) {
                if (null == reversalDocumentLine.getId() || itemIds.contains(reversalDocumentLine.getId())) {
                    reversalDocumentLine.setId(stockReversalRepository.getId());
                }
                itemIds.add(reversalDocumentLine.getId());
            }
        }
    }

    /**
     * Save a stockReversal.
     *
     * @param stockReversal the entity to save
     * @param action        to be performed
     * @return the persisted entity
     */
    @Override
    public StockReversal save(StockReversal stockReversal, String action) throws Exception {
        log.debug("Request to save stockReversal with action : {}", stockReversal);
        switch (action) {
            case "PROCESSED":
                log.debug("Request to process stockReversal : {}", stockReversal);
                stockReversal.getDocument().setStatus(Status.PROCESSED);
                return save(stockReversal);
            case "DRAFT":
                log.debug("Request to send approval stockReversal : {}", stockReversal);
                stockReversal.getDocument().setStatus(Status.DRAFT);
                return save(stockReversal);
            default:
                log.debug("Request to save as approved stockReversal : {}", stockReversal);
                if (stockReversal.getDocument().getStatus() == null) {
                    stockReversal.getDocument().setStatus(Status.APPROVED);
                    stockReversal.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Reversal.name(), "NH", stockReversal));
                }
                return approveStockReversal(stockReversal);
        }
    }

    private StockReversal approveStockReversal(StockReversal stockReversal) throws Exception {
        StockReversal result = null;
        result = save(stockReversal);
        reversalStockOut(result);
        return result;
    }

    private void reversalStockOut(StockReversal stockReversal) throws Exception {
        log.debug("Request to stock out reversal : {}", stockReversal);
        for (ReversalDocumentLine reversalDocumentLine : stockReversal.getDocument().getLines()) {
            stockService.reserveStockInSameTransaction(reversalDocumentLine.getStockId(), reversalDocumentLine.getItem().getId(), reversalDocumentLine.getBatchNumber(),
                stockReversal.getDocument().getIndentStore().getId(), reversalDocumentLine.getRejectedQuantity().getValue(), stockReversal.getId(),
                stockReversal.getDocument().getDocumentType(), stockReversal.getDocumentNumber(), reversalDocumentLine.getId(), stockReversal.getDocument().getApprovedDate(),stockReversal.getDocument().getCreatedBy().getId());
        }
        //stockService.stockOut(stockReversal.getDocumentNumber());
        stockService.moveStockToTransit(stockReversal.getDocumentNumber(),"REVERSAL");
    }

    /**
     * Get all the stockReversals.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockReversal> findAll(Pageable pageable) {
        log.debug("Request to get all StockReversals");
        Page<StockReversal> result = stockReversalRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockReversal by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockReversal findOne(Long id) {
        log.debug("Request to get StockReversal : {}", id);
        StockReversal stockReversal = stockReversalRepository.findOne(id);
        return stockReversal;
    }

    /**
     * Get one stockReversal by documentId.
     *
     * @param documentId the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockReversal findOne(DocumentId documentId) {
        log.debug("Request to get StockReversal : {}", documentId);
        StockReversal stockReversal = stockReversalRepository.findById(documentId).get();
        return stockReversal;
    }

    /**
     * Delete the  stockReversal by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockReversal : {}", id);
        stockReversalRepository.delete(id);
        stockReversalSearchRepository.deleteById(id);
    }

    /**
     * Delete the stockReversal by id,version.
     *
     * @param id,version the id of the entity
     */
    @Override
    public void delete(Long id, Integer version) {
        log.debug("Request to delete StockReversal : {}", id, version);
        stockReversalRepository.deleteById(new DocumentId(id, version));
        stockReversalSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockReversal corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockReversal> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockReversals for query {}", query);
        return stockReversalSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.status")
            .field("document.issueUnit.name").field("document.issueStore.name")
            .field("document.indentStore.name")
            .defaultOperator(Operator.AND), pageable);
    }

    @Override
    public Page<StockReversal> searchForDocument(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockReversals for query {}", query);
        return stockReversalSearchRepository.search(queryStringQuery(query)
            .defaultOperator(Operator.AND), pageable);
    }

    /**
     * Search for the stockReversal corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockReversal> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockReversals for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.status")
                .field("document.issueUnit.name").field("document.issueStore.name")
                .field("document.indentStore.name")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return ElasticSearchUtil.getPageRecords(searchQuery, StockReversal.class, elasticsearchTemplate, "stockreversal");
    }

    /**
     * Get one StockReversal by id.
     *
     * @param Id the id of the entity
     * @return the entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public StockReversal findDetachedOne(Long Id) {
        log.debug("Request to get StockReversal : {}", Id);
        return stockReversalRepository.findOne(Id);
    }

    private Long getTransactionLineId(List<SourceDocument> sourceDocuments) {
        Optional<SourceDocument> sourceDocument = sourceDocuments.stream().filter(sourceDoc -> TransactionType.Stock_Receipt.equals(sourceDoc.getType()) ||
            TransactionType.Inter_Unit_Stock_Receipt.equals(sourceDoc.getType())).findFirst();
        Long transactionLineId = null;
        if (sourceDocument.isPresent()) {
            transactionLineId = sourceDocument.get().getLineId();
        }
        return transactionLineId;
    }

    public StockReversal convertReceiptToReversal(StockReceipt stockReceipt, Map<Long, String> lineSkuMap) {
        StockReversal result = reversalMapper.convertFromstockReceipt(stockReceipt);
        result.getDocument().getLines().forEach((ReversalDocumentLine reversalDocumentLine) -> {
            Long transactionLineId = getTransactionLineId(reversalDocumentLine.getSourceDocument());
            String sku = lineSkuMap.get(transactionLineId);
            if(null == sku) sku = reversalDocumentLine.getSku();
            List<Stock> stockList = stockRepository.findAllByStoreIdAndItemIdAndSku(
                result.getDocument().getIndentStore().getId(),
                reversalDocumentLine.getItem().getId(),
                sku);
            if (!stockList.isEmpty()) {
                reversalDocumentLine.setStockId(stockList.get(0).getId());
                LocatorDTO locatorDTO = new LocatorDTO();
                locatorDTO.setId(stockList.get(0).getLocatorId());
                reversalDocumentLine.setLocator(locatorDTO);
            } else {
                throw new IllegalArgumentException("Unable to find stock record for sku");
            }
        });
        return result;
    }

    /**
     * Get the stockReversal status count corresponding to the query.
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
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "stockreversal");
        Terms aggregationTerms = aggregations.get("status_count");
        for (Terms.Bucket bucket : aggregationTerms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of StockReversal");
        stockReversalSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockReversal latest=true");
        List<StockReversal> data = stockReversalRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockReversalSearchRepository.saveAll(data);
        }
    }

    @Override
    public Map<String, SortedSet<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException {
        log.debug("Gell all documents which is related to given stockreversal document:-" + documentNumber);

        SortedSet<RelatedDocument> relatedDocumentList = new TreeSet<>();
        Map<String, SortedSet<RelatedDocument>> finalList = new LinkedHashMap<>();

        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest issueQueryReq= new SearchRequest("stockreversal");
        SearchSourceBuilder issueSourceQueryReq = new SearchSourceBuilder()
            .query(QueryBuilders.queryStringQuery("documentNumber.raw:" + documentNumber));
        issueQueryReq.source(issueSourceQueryReq);

        SearchRequest receiptQueryReq= new SearchRequest("stockreceipt");
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
                if (hit.getIndex().equals("stockreversal")) {
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
                                relatedDocumentList = new TreeSet<>();

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
                        relatedDocumentList = new TreeSet<>();

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
        StockReversal stockReversal = stockReversalRepository.findOne(id);
        if (stockReversal == null) {
            if (id != null) {
                if (stockReversalSearchRepository.existsById(id)) {
                    stockReversalSearchRepository.deleteById(id);
                }
            }
        }
    }
}
