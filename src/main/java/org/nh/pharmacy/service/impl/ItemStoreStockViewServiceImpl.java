package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.nh.common.dto.ItemStoreStockViewDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.IssueDocumentLine;
import org.nh.pharmacy.domain.dto.ItemStoreStockViewGroup;
import org.nh.pharmacy.domain.dto.ReceiptDocumentLine;
import org.nh.pharmacy.domain.dto.ReversalDocumentLine;
import org.nh.pharmacy.domain.enumeration.ItemStoreStockViewType;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.repository.ItemStoreStockViewRepository;
import org.nh.pharmacy.repository.ReserveStockRepository;
import org.nh.pharmacy.repository.StockFlowRepository;
import org.nh.pharmacy.repository.StockRepository;
import org.nh.pharmacy.repository.search.*;
import org.nh.pharmacy.service.ItemStoreStockViewService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Service Implementation for managing ItemStoreStockView.
 */
@Service
@Transactional
public class ItemStoreStockViewServiceImpl implements ItemStoreStockViewService {

    private final Logger log = LoggerFactory.getLogger(ItemStoreStockViewServiceImpl.class);

    private final ItemStoreStockViewRepository itemStoreStockViewRepository;

    private final ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository;

    private final MessageChannel itemStoreStockViewChannel;

    private final StockRepository stockRepository;

    private final StockFlowRepository stockFlowRepository;

    private final ReserveStockRepository reserveStockRepository;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ApplicationProperties applicationProperties;

    private final HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository;

    private final ItemSearchRepository itemSearchRepository;

    private final ItemBarcodeSearchRepository itemBarcodeSearchRepository;

    final ObjectMapper mapper = new ObjectMapper();

    private final ItemBatchInfoSearchRepository itemBatchInfoSearchRepository;

    public ItemStoreStockViewServiceImpl(ItemStoreStockViewRepository itemStoreStockViewRepository, ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository,
                                         @Qualifier(Channels.ITEM_STORE_STOCK_OUTPUT) MessageChannel itemStoreStockViewChannel, StockRepository stockRepository,
                                         StockFlowRepository stockFlowRepository, ReserveStockRepository reserveStockRepository, ElasticsearchOperations elasticsearchTemplate,
                                         ApplicationProperties applicationProperties, HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository,
                                         ItemSearchRepository itemSearchRepository, ItemBarcodeSearchRepository itemBarcodeSearchRepository, ItemBatchInfoSearchRepository itemBatchInfoSearchRepository) {
        this.itemStoreStockViewRepository = itemStoreStockViewRepository;
        this.itemStoreStockViewSearchRepository = itemStoreStockViewSearchRepository;
        this.itemStoreStockViewChannel = itemStoreStockViewChannel;
        this.stockFlowRepository = stockFlowRepository;
        this.stockRepository = stockRepository;
        this.reserveStockRepository = reserveStockRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.applicationProperties = applicationProperties;
        this.healthcareServiceCenterSearchRepository = healthcareServiceCenterSearchRepository;
        this.itemSearchRepository = itemSearchRepository;
        this.itemBarcodeSearchRepository = itemBarcodeSearchRepository;
        this.itemBatchInfoSearchRepository = itemBatchInfoSearchRepository;
    }

    /**
     * Save a itemStoreStockView.
     *
     * @param itemStoreStockView the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemStoreStockView save(ItemStoreStockView itemStoreStockView) {
        log.debug("Request to save ItemStoreStockView : {}", itemStoreStockView);
        updateUnit(itemStoreStockView);
        updateItemGroup(itemStoreStockView);
        ItemStoreStockView result = itemStoreStockViewRepository.save(itemStoreStockView);
        itemStoreStockViewSearchRepository.save(result);
        return result;
    }

    /**
     * Update ItemGroup for all Brand Item
     * @param itemStoreStockView
     */
    private void updateItemGroup(ItemStoreStockView itemStoreStockView) {
        if (null == itemStoreStockView.getId() && ItemStoreStockViewType.BRAND.equals(itemStoreStockView.getType())) {
            String itemGroup = itemSearchRepository.findById(itemStoreStockView.getItemId()).get().getGroup().getCode();
            itemStoreStockView.setItemGroup(itemGroup);
        }
    }

    private void updateUnit(ItemStoreStockView itemStoreStockView) {
        if (null == itemStoreStockView.getUnit()) {
            Long storeId = Long.parseLong(itemStoreStockView.getStore().get("id").toString());
            HealthcareServiceCenter store = healthcareServiceCenterSearchRepository.findById(storeId).get();
            Organization unit = store.getPartOf();
            itemStoreStockView.setUnit(new OrganizationDTO());
            itemStoreStockView.getUnit().setId(unit.getId());
            itemStoreStockView.getUnit().setCode(unit.getCode());
            itemStoreStockView.getUnit().setName(unit.getName());
        }
    }

    /**
     * Get all the itemStoreStockViews.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemStoreStockView> findAll(Pageable pageable) {
        log.debug("Request to get all ItemStoreStockViews");
        Page<ItemStoreStockView> result = itemStoreStockViewRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one itemStoreStockView by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemStoreStockView findOne(Long id) {
        log.debug("Request to get ItemStoreStockView : {}", id);
        ItemStoreStockView itemStoreStockView = itemStoreStockViewRepository.findById(id).get();
        return itemStoreStockView;
    }

    /**
     * Delete the  itemStoreStockView by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemStoreStockView : {}", id);
        itemStoreStockViewRepository.deleteById(id);
        itemStoreStockViewSearchRepository.deleteById(id);
    }

    /**
     * Search for the itemStoreStockView corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemStoreStockView> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ItemStoreStockViews for query {}", query);
        Page<ItemStoreStockView> result = itemStoreStockViewSearchRepository.search(queryStringQuery(query)
            .field("code").field("name")
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemStoreStockViewGroup> searchItemAvailabilityItemStoreStockViews(String query, Pageable pageable) {
        log.debug("Request to search for a page of searchItemAvailabilityItemStoreStockViews for query {}", query);
        List<ItemStoreStockView> storeStockViews = this.search(query, pageable).getContent();

        Map<Long, ItemStoreStockViewGroup> itemGroupMap = new HashMap<>();
        for (ItemStoreStockView itemStoreStockView : storeStockViews) {
            ItemStoreStockViewGroup itemStoreStockViewGroup = itemGroupMap.get(itemStoreStockView.getItemId());
            if (null == itemStoreStockViewGroup) {
                itemStoreStockViewGroup = new ItemStoreStockViewGroup();
                itemStoreStockViewGroup.setItemId(itemStoreStockView.getItemId());
                itemStoreStockViewGroup.setItemCode(itemStoreStockView.getCode());
                itemStoreStockViewGroup.setItemName(itemStoreStockView.getName());
                itemStoreStockViewGroup.getItemStoreStockViews().add(itemStoreStockView);
                itemGroupMap.put(itemStoreStockView.getItemId(), itemStoreStockViewGroup);
            } else {
                List<ItemStoreStockView> itemStoreStockViews = itemStoreStockViewGroup.getItemStoreStockViews();
                itemStoreStockViews.add(itemStoreStockView);
                itemStoreStockViewGroup.setItemStoreStockViews(itemStoreStockViews);
                itemGroupMap.put(itemStoreStockView.getItemId(), itemStoreStockViewGroup);
            }
        }
        return new ArrayList<>(itemGroupMap.values());
    }

    /****
     *
     * @param query
     * @param pageable
     * @return
     */
    public Page<ItemStoreStockView> searchUniqueItems(String query, Pageable pageable) {
        log.debug("Request to search unique items for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(QueryBuilders.queryStringQuery(query)
                .field("name")
                .field("code")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("byItems").field("name.raw").size(20).order(BucketOrder.count(true))
                .subAggregation(AggregationBuilders.topHits("topItem").size(1))).build();

        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "itemstorestockview");
        List<ItemStoreStockView> result = new ArrayList<>();
        Terms aggTerms = aggregations.get("byItems");
        for (Terms.Bucket bucket : aggTerms.getBuckets()) {
            TopHits hits = bucket.getAggregations().get("topItem");
            SearchHit[] sHits = hits.getHits().getHits();
            for (SearchHit hit : sHits) {
                Map<String, Object> itemMap = hit.getSourceAsMap();
                itemMap.put("stocklastSyncDate", null);
                itemMap.put("store", null);
                itemMap.put("availableStock", 0);
                itemMap.put("consumedQtyCurrMonth", 0);
                itemMap.put("consumedQtyLastMonth", 0);
                itemMap.put("transitQty", null);
                ItemStoreStockView itemStoreStockView = mapper.convertValue(itemMap, ItemStoreStockView.class);
                result.add(itemStoreStockView);
            }
        }
        Page<ItemStoreStockView> page = new PageImpl<ItemStoreStockView>(result);
        return page;
    }

    public Page<ItemStoreStockView> searchBarcodeItems(String query,String barcode, Long forStoreId, Pageable pageable){
        Iterable<ItemBarcode> itemBarcodeItr = itemBarcodeSearchRepository.search(queryStringQuery("barcode:" + barcode.trim()));
        StringJoiner joiner = new StringJoiner(" OR ");
        for (ItemBarcode itemBarcode : itemBarcodeItr) {
            joiner.add("\""+itemBarcode.getItemCode()+"\"");
        }
        log.debug("barcode:{} find itemBarcode:{}", barcode, joiner.toString());
        if(joiner.length() == 0){
            return new PageImpl<>(new ArrayList<>());
        }
        query = query + " code.raw:"+joiner.toString();
        log.debug("search by barcode query:{}", query);
        return searchItems(query, forStoreId, pageable);
    }

    public Page<ItemStoreStockView> searchBatchItems(String batch, Long forStoreId, Pageable pageable) {
        log.debug("Request to search for a page of ItemStoreStockViews for batch {}, forStoreId:{}", batch, forStoreId);
        Iterable<ItemBatchInfo> itemBatchItr = itemBatchInfoSearchRepository.search(queryStringQuery("batchNo.raw:" + batch.trim()));
        StringJoiner joiner = new StringJoiner(" OR ");
        for (ItemBatchInfo batchInfo : itemBatchItr) {
            joiner.add("\"" + batchInfo.getItemId() + "\"");
        }
        log.debug("batch:{} find itemIds:{}", batch, joiner.toString());
        if (joiner.length() == 0) {
            return new PageImpl<>(new ArrayList<>());
        }
        String query = " itemId: ( " + joiner.toString()+" )";
        log.debug("search by barcode query:{}", query);
        return searchItems(query, forStoreId, pageable);
    }

    /**
     * @param query
     * @param forStoreId
     * @return
     */
    public Page<ItemStoreStockView> searchItems(String query, Long forStoreId, Pageable pageable) {
        log.debug("Request to search for a page of ItemStoreStockViews for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(QueryBuilders.queryStringQuery(query)
                .field("name")
                .field("code")
                .defaultOperator(Operator.AND))
            .withSort(SortBuilders.fieldSort("name.raw").order(SortOrder.ASC))
            .addAggregation(AggregationBuilders.terms("allItems").field("itemId").size(10).minDocCount(2)
                .subAggregation(AggregationBuilders.filter("storeItem", termQuery("store.id", forStoreId))
                    .subAggregation(AggregationBuilders.topHits("items"))))
            .build();

        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate,"itemstorestockview");
        List<ItemStoreStockView> result = new ArrayList<>();
        Terms aggTerms = aggregations.get("allItems");
        for (Terms.Bucket bucket : aggTerms.getBuckets()) {
            ParsedFilter agr = bucket.getAggregations().get("storeItem");
            ParsedTopHits intHits =  agr.getAggregations().get("items");
            SearchHit[] sHits = intHits.getHits().getHits();
            for (SearchHit hit : sHits) {
                Map<String, Object> itemMap = hit.getSourceAsMap();
                itemMap.put("stocklastSyncDate", null);
                ItemStoreStockView itemStoreStockView = mapper.convertValue(itemMap, ItemStoreStockView.class);
                result.add(itemStoreStockView);
            }
        }
        Page<ItemStoreStockView> page = new PageImpl<ItemStoreStockView>(result);
        return page;
        /*Page<ItemStoreStockView> result = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Page<ItemStoreStockView>>() {
            @Override
            public Page<ItemStoreStockView> extract(SearchResponse response) {
                List<ItemStoreStockView> result = new ArrayList<>();
                Aggregations aggregations = response.getAggregations();
                Terms aggTerms = aggregations.get("allItems");
                for (Terms.Bucket bucket : aggTerms.getBuckets()) {
                    InternalAggregation agr = bucket.getAggregations().get("storeItem");
                    InternalTopHits intHits = (InternalTopHits) agr.getProperty("items");
                    SearchHit[] sHits = intHits.getHits().getHits();
                    for (SearchHit hit : sHits) {
                        Map<String, Object> itemMap = hit.getSourceAsMap();
                        itemMap.put("stocklastSyncDate", null);
                        ItemStoreStockView itemStoreStockView = mapper.convertValue(itemMap, ItemStoreStockView.class);
                        result.add(itemStoreStockView);
                    }
                }
                Page<ItemStoreStockView> page = new PageImpl<ItemStoreStockView>(result);
                return page;
            }
        });*/
    }


    @Override
    @Transactional(readOnly = true)
    public void doUpdate(Long storeId, Set<Long> itemIds) {
        Map<String, Object> data = new HashMap<>();
        data.put("itemIds", itemIds);
        data.put("storeId", storeId);
        this.itemStoreStockViewChannel.send(MessageBuilder.withPayload(data).build());
    }

    @Override
    public void updateItemStoreStockView(Set<Long> itemIds, Long storeId) {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();
        LocalDateTime firstDayOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime firstDayOfLastMonth = firstDayOfMonth.minusMonths(1l);
        LocalDateTime lastDayOfLastMonth = firstDayOfMonth.minusDays(1l).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime currentTime = LocalDateTime.now();
        Set<String> dispensableGenericNames = new HashSet<>();
        List<ItemStoreStockView> dirtyEntities = new ArrayList<ItemStoreStockView>();
        for (Long id : itemIds) {
            List<Object[]> stocks = stockRepository.findItemStoreDetailsByItemIdAndStoreId(id, storeId);
            Float lastMonthConsQuan = null;
            Float currMonthConsQuan = stockFlowRepository.findSumOfStockQuantityByItemIdAndTransactionDateBetween(id, firstDayOfMonth, LocalDateTime.now(), storeId);

            for (Object[] objects : stocks) {
                if (objects[6] != null && objects[6].toString().trim().length() > 0) {
                    dispensableGenericNames.add(objects[6].toString());
                }
                ItemStoreStockView itemStoreStockView = null;
                Page<ItemStoreStockView> itemStoreStockViews = itemStoreStockViewSearchRepository.search(queryStringQuery(
                    "itemId:" + id + " store.id:" + objects[3].toString()).defaultOperator(Operator.AND), PageRequest.of(0, 1));
                if (itemStoreStockViews.iterator().hasNext()) {
                    itemStoreStockView = itemStoreStockViews.iterator().next();
                } else {
                    itemStoreStockView = new ItemStoreStockView();
                    itemStoreStockView.setItemId((Long) objects[0]);
                    itemStoreStockView.setCode((String) objects[1]);
                    itemStoreStockView.setName((String) objects[2]);
                    Map<String, Object> map = new HashMap();
                    map.put("id", (Long) objects[3]);
                    map.put("code", (String) objects[4]);
                    map.put("name", (String) objects[5]);
                    itemStoreStockView.setStore(map);
                    itemStoreStockView.setType(ItemStoreStockViewType.BRAND);
                }
                if (itemStoreStockView.getStocklastSyncDate() == null || itemStoreStockView.getStocklastSyncDate().getMonthValue() != currentTime.getMonthValue()) {
                    lastMonthConsQuan = stockFlowRepository.findSumOfStockQuantityByItemIdAndTransactionDateBetween(id, firstDayOfLastMonth, lastDayOfLastMonth, storeId);
                    itemStoreStockView.setConsumedQtyLastMonth(lastMonthConsQuan != null ? lastMonthConsQuan : 0f);
                }
                itemStoreStockView.setConsumedQtyCurrMonth(currMonthConsQuan != null ? currMonthConsQuan : 0f);
                Float availableStock = ((Double) objects[7]).floatValue();
                itemStoreStockView.setAvailableStock(availableStock != null ? availableStock : 0f);
                itemStoreStockView.setStocklastSyncDate(currentTime);
                dirtyEntities.add(itemStoreStockView);
            }
        }
        for (String dispGenName : dispensableGenericNames) {
            List<Object[]> stocks = stockRepository.findItemStoreDetailsByDispensableGenericNameAndStoreId(dispGenName, storeId);
            Float lastMonthConsQuan = null;
            Float currMonthConsQuan = stockFlowRepository.findSumOfStockQuantityByDispensableGenericNameAndTransactionDateBetween(dispGenName,
                firstDayOfMonth, LocalDateTime.now(), storeId);

            for (Object[] objects : stocks) {
                ItemStoreStockView itemStoreStockView = null;
                Page<ItemStoreStockView> itemStoreStockViews = itemStoreStockViewSearchRepository.search(queryStringQuery(
                    "type:GENERIC name:\"" + dispGenName + "\" store.id:" + objects[0].toString()).defaultOperator(Operator.AND),
                    PageRequest.of(0, 10000));
                Iterator<ItemStoreStockView> itemStoreStockViewIterator = itemStoreStockViews.iterator();
                while (itemStoreStockViewIterator.hasNext()) {
                    ItemStoreStockView temp = itemStoreStockViewIterator.next();
                    if (temp.getItemId() == null && dispGenName.toUpperCase().equals(temp.getName().toUpperCase())) {
                        itemStoreStockView = temp;
                        break;
                    }
                }
                if (itemStoreStockView == null) {
                    itemStoreStockView = new ItemStoreStockView();
                    itemStoreStockView.setName((String) objects[3]);
                    Map<String, Object> map = new HashMap();
                    map.put("id", (Long) objects[0]);
                    map.put("code", (String) objects[1]);
                    map.put("name", (String) objects[2]);
                    itemStoreStockView.setStore(map);
                    itemStoreStockView.setType(ItemStoreStockViewType.GENERIC);
                }
                if (itemStoreStockView.getStocklastSyncDate() == null || itemStoreStockView.getStocklastSyncDate().getMonthValue() != currentTime.getMonthValue()) {
                    lastMonthConsQuan = stockFlowRepository.findSumOfStockQuantityByDispensableGenericNameAndTransactionDateBetween(dispGenName, firstDayOfLastMonth,
                        lastDayOfLastMonth, storeId);
                    itemStoreStockView.setConsumedQtyLastMonth(lastMonthConsQuan != null ? lastMonthConsQuan : 0f);
                }
                itemStoreStockView.setConsumedQtyCurrMonth(currMonthConsQuan != null ? currMonthConsQuan : 0f);
                Float availableStock = ((Double) objects[4]).floatValue();
                itemStoreStockView.setAvailableStock(availableStock != null ? availableStock : 0f);
                itemStoreStockView.setStocklastSyncDate(currentTime);
                dirtyEntities.add(itemStoreStockView);
            }
        }
        dirtyEntities.forEach(itemStoreStockView -> save(itemStoreStockView));
    }

    @Override
    public void updateTransitQuantity(Set<Long> itemIds, String transitStoreIds) {
        String transitStoreId[] = transitStoreIds.split(",");
        for (String storeId : transitStoreId) {
            for (Long id : itemIds) {
                /*QueryBuilder issueQueryBuilder = QueryBuilders.boolQuery()
                    .must(queryStringQuery("document.status.raw:(" + Status.APPROVED + " or " + Status.PARTIALLY_PROCESSED + ")" +
                        "and document.indentStore.id:" + storeId + " and document.lines.item.id:" + id));

                SumBuilder issueSumBuilder = AggregationBuilders.sum("issued_quantity").field("document.lines.issuedQuantity.value");

                SearchRequestBuilder issueSearchRequest = elasticsearchTemplate.getClient().prepareSearch()
                    .setIndices("stockissue").setTypes("stockissue").setSize(0)
                    .setQuery(issueQueryBuilder).addAggregation(issueSumBuilder);


                QueryBuilder receiptQueryBuilder = QueryBuilders.boolQuery()
                    .must(queryStringQuery("document.status.raw:(" + Status.APPROVED + " or " + Status.PROCESSED + ")" +
                        "and document.indentStore.id:" + storeId + " and document.lines.item.id:" + id));

                SumBuilder receiptAcceptedQuanValueSumBuilder = AggregationBuilders.sum("receipt_accepted_quantity").field("document.lines.acceptedQuantity");
                SumBuilder receiptRejectedQuanValueSumBuilder = AggregationBuilders.sum("receipt_rejected_quantity").field("document.lines.rejectedQuantity");

                SearchRequestBuilder receiptSearchRequest = elasticsearchTemplate.getClient().prepareSearch()
                    .setIndices("stockreceipt").setTypes("stockreceipt").setSize(0)
                    .setQuery(receiptQueryBuilder).addAggregation(receiptAcceptedQuanValueSumBuilder).addAggregation(receiptRejectedQuanValueSumBuilder);


                QueryBuilder reversalQueryBuilder = QueryBuilders.boolQuery()
                    .must(queryStringQuery("document.status.raw:("+ Status.APPROVED +" or "+Status.PROCESSED+")" +
                        "and document.issueStore.id:"+storeId+ " and document.lines.item.id:"+id));

                SumBuilder reversalSumBuilder = AggregationBuilders.sum("reversal_quantity").field("document.lines.rejectedQuantity");

                SearchRequestBuilder reversalSearchRequest =  elasticsearchTemplate.getClient().prepareSearch()
                    .setIndices("stockreversal").setTypes("stockreversal").setSize(0)
                    .setQuery(reversalQueryBuilder).addAggregation(reversalSumBuilder);

                MultiSearchResponse msResponse = elasticsearchTemplate.getClient().prepareMultiSearch()
                    .add(issueSearchRequest)
                    .add(receiptSearchRequest)
                    .add(reversalSearchRequest)
                    .get();

                MultiSearchResponse.Item[] item = msResponse.getResponses();
                Double issuedQuantity = (Double)item[0].getResponse().getAggregations().get("issued_quantity").getProperty("value");
                Double receiptAccQuantity = (Double)item[1].getResponse().getAggregations().get("receipt_accepted_quantity").getProperty("value");
                Double receiptRejQuantity = (Double)item[1].getResponse().getAggregations().get("receipt_rejected_quantity").getProperty("value");
                Double reversalQuantity = (Double)item[2].getResponse().getAggregations().get("reversal_quantity").getProperty("value");*/


                Query issueQuery = new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, 9999))
                    .withQuery(queryStringQuery("document.status.raw:(" + Status.APPROVED + " OR " + Status.REVERSAL_PENDING + " OR " + Status.PARTIALLY_PROCESSED + ")"
                        + " AND document.indentStore.id:" + storeId + " AND document.lines.item.id:" + id)).build();
                List<StockIssue> stockIssues = ElasticSearchUtil.getRecords(issueQuery, StockIssue.class, elasticsearchTemplate, "stockissue");

                Float issuedQuantity = 0.0f;
                Float receiptQuantity = 0.0f;
                for (StockIssue issue : stockIssues) {
                    for (IssueDocumentLine line : issue.getDocument().getLines()) {
                        if (line.getItem().getId().equals(id)) issuedQuantity += line.getIssuedQuantity().getValue();
                    }
                    if (issue.getDocument().getStatus().equals(Status.PARTIALLY_PROCESSED) || issue.getDocument().getStatus().equals(Status.REVERSAL_PENDING)) {
                        Query receiptQuery = new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, 9999))
                            .withQuery(queryStringQuery("document.status.raw:(" + Status.APPROVED + ")"
                                + " AND document.indentStore.id:" + storeId + " AND document.lines.item.id:" + id + " AND document.lines.sourceDocument.documentNumber.raw:" + issue.getDocumentNumber())).build();

                        List<StockReceipt> stockReceipts = ElasticSearchUtil.getRecords(receiptQuery, StockReceipt.class, elasticsearchTemplate, "stockreceipt");

                        for (StockReceipt receipt : stockReceipts) {
                            for (ReceiptDocumentLine line : receipt.getDocument().getLines()) {
                                if (line.getItem().getId().equals(id))
                                    receiptQuantity += (line.getAcceptedQuantity().getValue() + line.getRejectedQuantity().getValue());
                            }
                        }
                    }
                }


                Query reversalQuery = new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, 9999))
                    .withQuery(queryStringQuery("document.status.raw:(" + Status.APPROVED + ")"
                        + " AND document.issueStore.id:" + storeId + " AND document.lines.item.id:" + id)).build();
                List<StockReversal> stockReversals = ElasticSearchUtil.getRecords(reversalQuery, StockReversal.class, elasticsearchTemplate, "stockreversal");

                Float reversalQuantity = 0.0f;
                for (StockReversal reversal : stockReversals) {
                    for (ReversalDocumentLine line : reversal.getDocument().getLines()) {
                        if (line.getItem().getId().equals(id))
                            reversalQuantity += line.getRejectedQuantity().getValue();
                    }
                }


                Float transitQty = issuedQuantity - receiptQuantity + reversalQuantity;
                //Page<ItemStoreStockView> page = search("itemId:" + id + " AND store.id:" + storeId, null);
                Query searchQuery = new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, 9999))
                    .withQuery(queryStringQuery("itemId:" + id + " AND store.id:" + storeId).defaultOperator(Operator.AND)).build();

                Page<ItemStoreStockView> page = ElasticSearchUtil.getPageRecords(searchQuery, ItemStoreStockView.class, elasticsearchTemplate, "itemstorestockview");
                if (!page.getContent().isEmpty()) {
                    ItemStoreStockView itemStoreStockView = page.getContent().iterator().next();
                    itemStoreStockView.setTransitQty(transitQty);
                    save(itemStoreStockView);
                } else {
                    Query queryItem = new NativeSearchQueryBuilder().withQuery(queryStringQuery("id:" + id).defaultOperator(Operator.AND)).build();
                    List<Item> items = ElasticSearchUtil.getRecords(queryItem, Item.class, elasticsearchTemplate, "item");

                    Query queryHSC = new NativeSearchQueryBuilder().withQuery(queryStringQuery("id:" + storeId).defaultOperator(Operator.AND)).build();
                    List<HealthcareServiceCenter> healthCareServiceCenters = ElasticSearchUtil.getRecords(queryHSC, HealthcareServiceCenter.class, elasticsearchTemplate, "healthcareservicecenter");

                    ItemStoreStockView itemStoreStockView = new ItemStoreStockView();
                    itemStoreStockView.setItemId(items.get(0).getId());
                    itemStoreStockView.setCode(items.get(0).getCode());
                    itemStoreStockView.setName(items.get(0).getName());
                    Map<String, Object> map = new HashMap();
                    map.put("id", healthCareServiceCenters.get(0).getId());
                    map.put("code", healthCareServiceCenters.get(0).getCode());
                    map.put("name", healthCareServiceCenters.get(0).getName());
                    itemStoreStockView.setStore(map);
                    itemStoreStockView.setTransitQty(transitQty);
                    itemStoreStockView.setConsumedQtyLastMonth(0.0f);
                    itemStoreStockView.setConsumedQtyCurrMonth(0.0f);
                    itemStoreStockView.setType(ItemStoreStockViewType.BRAND);
                    save(itemStoreStockView);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of ItemStoreStockView");
        itemStoreStockViewSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on itemStoreStockView latest=true");
        List<ItemStoreStockView> data = itemStoreStockViewRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            itemStoreStockViewSearchRepository.saveAll(data);
        }
    }


    @Override
    public void updateISSVTransitQty(Map<String, Object> map) {
        log.debug("Request to update transit and available qty for store id : {}", map.get("issueStore"));
        Long receiveStoreId = Long.valueOf("" + map.get("receiveStore"));
        Long issueStoreId = Long.valueOf("" + map.get("issueStore"));
        List<Map<String, Object>> itemQtyMapList = (List) map.get("itemMap");
        itemQtyMapList.stream().forEach(stringObjectMap -> {
            Map<String, Object> itemQtyMap = (Map) stringObjectMap;
            Long itemId = Long.valueOf("" + itemQtyMap.get("itemId"));
            Float qty = Float.valueOf("" + itemQtyMap.get("transitQuantity"));
            Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("itemId:" + itemId + " store.id:" + issueStoreId)
                .defaultOperator(Operator.AND)).build();
            List<ItemStoreStockView> itemStoreStockViews = ElasticSearchUtil.getRecords(query, ItemStoreStockView.class, elasticsearchTemplate, "itemstorestockview");
            itemStoreStockViews.stream().forEach(itemStoreStockView -> {
                itemStoreStockView.setAvailableStock(itemStoreStockView.getAvailableStock() - qty);
                this.save(itemStoreStockView);
            });

        });
    }

    @Override
    public void updateItemName(Map<String, Object> itemNameMap) {
        log.debug("Request to update itemName:{} for itemCode:{} in itemStoreStockView", itemNameMap.get("itemCode"), itemNameMap.get("itemName"));
        LocalDateTime fromDate = LocalDateTime.now().minusMinutes(2l);
        String itemCode = String.valueOf(itemNameMap.get("itemCode"));
        String itemName = String.valueOf(itemNameMap.get("itemName"));
        int totalCount = itemStoreStockViewRepository.updateItemNameByCode(itemCode, itemName);
        log.info("Total Record updated:{} for itemCode:{}",totalCount, itemCode);
        LocalDateTime toDate = LocalDateTime.now().plusMinutes(1l);
        int pageSize = 10000;
        int lastPageNumber = (int) (Math.ceil(totalCount / pageSize));
        for (int pageNo = 0; pageNo <= lastPageNumber; pageNo++) {
            reIndex(pageNo, pageSize, fromDate, toDate);
        }
    }

    private void reIndex(int pageNo, int pageSize, LocalDateTime fromDate, LocalDateTime toDate) {
        List<ItemStoreStockView> data = itemStoreStockViewRepository.findByLocalDateTimeRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        log.info("Page:{}, Records to be re-indexed:{} date between fromDate:{}, to toDate:{}",pageNo,data.size(), fromDate, toDate);
        if (!data.isEmpty()) {
            itemStoreStockViewSearchRepository.saveAll(data);
        }
    }

    /****
     *
     * @param genericId
     * @param pageable
     * @return searchResult
     */
    public List<ItemStoreStockViewDTO> searchGenericItem(Long genericId, Long storeId, Pageable pageable, Boolean availableStock) {
        List<ItemStoreStockViewDTO> itemStoreStockViewDTOLIst = new ArrayList<>();
        log.debug("Request to search for a page of ItemStoreStock for query {}", genericId);
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("generic.id:" + genericId + " AND active:true")).withPageable(PageRequest.of(0, 9999)).build();
        ListIterator<Medication> medication = ElasticSearchUtil.getRecords(searchQuery, Medication.class, elasticsearchTemplate,"medication").listIterator();
        StringBuilder stringBuilder = new StringBuilder();
        String codesData=null;
        while (medication.hasNext()) {
            stringBuilder.append("\"" + medication.next().getCode() + "\" OR ");
        }
        if(stringBuilder.length()>3) {
            codesData = stringBuilder.substring(0, stringBuilder.length() - 3);
            codesData = "(" + codesData + ")";
        }
        if(null!=codesData) {
            Query itemSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryStringQuery((availableStock ? "availableStock:>0 AND " : "") + "store.id:" + storeId + " AND code.raw:" + codesData))
                .withPageable(pageable).build();
            itemStoreStockViewDTOLIst = ElasticSearchUtil.getRecords(itemSearchQuery, ItemStoreStockViewDTO.class, elasticsearchTemplate, "itemstorestockview");
        }
        return itemStoreStockViewDTOLIst;
    }

    @Override
    public List<ItemStoreStockViewDTO> searchStockItem(Medication medication, Long storeId) {
        List<ItemStoreStockViewDTO> itemStoreStockViewDTOLIst = new ArrayList<>();
        Medication medicationObject = ElasticSearchUtil.queryForObject("medication",new CriteriaQuery(new Criteria("code.raw").is(medication.getCode())), elasticsearchTemplate, Medication.class);
        if (null != medicationObject) {
            if (null != medicationObject.getGeneric())
                itemStoreStockViewDTOLIst = searchGenericItem(medicationObject.getGeneric().getId(), storeId, PageRequest.of(0, 9999), true);
        }
        return itemStoreStockViewDTOLIst;
    }


    /****
     *
     * @param storeId
     * @param categoryCode
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getItemStockByStoreIdAndCategoryCode(Long storeId, String categoryCode) {
        log.debug("Request to get Item Stock based on Store_id:"+storeId+" Category_code:"+categoryCode);
        List<Object[]> list = itemStoreStockViewRepository.findItemStockByStoreIdAndCategoryCode(storeId, categoryCode);
        List<Map<String, Object>> stockList = new ArrayList<>();
        for (Object[] item : list) {
            Map<String, Object> map = new HashMap();
            map.put("itemId", item[0]);
            map.put("name", (String) item[1]);
            map.put("code", (String) item[2]);
            map.put("description", (String) item[3]);
            map.put("stockId", (long) item[4]);
            map.put("availableStock", (float) item[5]);
            map.put("batchNo", (String) item[6]);
            map.put("storeId", (long) item[7]);
            map.put("unitId", (long) item[8]);
            stockList.add(map);
        }
        return stockList;
    }

}
