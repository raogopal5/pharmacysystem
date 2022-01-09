package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.domain.ItemBarcode;
import org.nh.pharmacy.domain.StockFlowStockSource;
import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.domain.dto.BarcodeConfiguration;
import org.nh.pharmacy.repository.ItemBarcodeRepository;
import org.nh.pharmacy.repository.ItemRepository;
import org.nh.pharmacy.repository.StockFlowStockSourceRepository;
import org.nh.pharmacy.repository.StockSourceRepository;
import org.nh.pharmacy.repository.search.ItemBarcodeSearchRepository;
import org.nh.pharmacy.repository.search.ItemSearchRepository;
import org.nh.pharmacy.service.ElasticSearchQueryService;
import org.nh.pharmacy.service.StockService;
import org.nh.pharmacy.service.StockSourceService;
import org.nh.pharmacy.web.rest.util.DateUtil;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Long.valueOf;
import static java.lang.Math.abs;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Service Implementation for managing StockSource.
 */
@Service
@Transactional
public class StockSourceServiceImpl implements StockSourceService {

    private final Logger log = LoggerFactory.getLogger(StockSourceServiceImpl.class);

    private final StockSourceRepository stockSourceRepository;
    private final StockService stockService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final EntityManager entityManager;
    private final ElasticSearchQueryService elasticSearchQueryService;
    private final ItemRepository itemRepository;
    private final ItemSearchRepository itemSearchRepository;
    private final ItemBarcodeRepository itemBarcodeRepository;
    private final ItemBarcodeSearchRepository itemBarcodeSearchRepository;
    private final StockFlowStockSourceRepository stockFlowStockSourceRepository;

    public StockSourceServiceImpl(StockSourceRepository stockSourceRepository, StockService stockService, SequenceGeneratorService sequenceGeneratorService, EntityManager entityManager,
                                  ElasticSearchQueryService elasticSearchQueryService, ItemRepository itemRepository,
                                  ItemSearchRepository itemSearchRepository, ItemBarcodeRepository itemBarcodeRepository, ItemBarcodeSearchRepository itemBarcodeSearchRepository, StockFlowStockSourceRepository stockFlowStockSourceRepository) {
        this.stockSourceRepository = stockSourceRepository;
        this.stockService = stockService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.entityManager = entityManager;
        this.elasticSearchQueryService = elasticSearchQueryService;
        this.itemRepository = itemRepository;
        this.itemSearchRepository = itemSearchRepository;
        this.itemBarcodeRepository = itemBarcodeRepository;
        this.itemBarcodeSearchRepository = itemBarcodeSearchRepository;
        this.stockFlowStockSourceRepository = stockFlowStockSourceRepository;
    }

    /**
     * Save a stockSource.
     *
     * @param stockSource the entity to save
     * @return the persisted entity
     */
    @Override
    public StockSource save(StockSource stockSource) {
        log.debug("Request to save StockSource : {}", stockSource);
        StockSource result = stockSourceRepository.save(stockSource);
        setItemName(result);
        updateStockBarcode(stockSource);
        upsertItemBarCode(stockSource);
        return result;
    }


    /**
     * Get all the stockSources.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockSource> findAll(Pageable pageable) {
        log.debug("Request to get all StockSources");
        Page<StockSource> result = stockSourceRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockSource by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockSource findOne(Long id) {
        log.debug("Request to get StockSource : {}", id);
        StockSource stockSource = stockSourceRepository.findById(id).get();
        return stockSource;
    }

    /**
     * Delete the  stockSource by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockSource : {}", id);
        stockSourceRepository.deleteById(id);
    }

    @Override
    public StockSource generateBarcode(StockSource stockSource) throws Exception {
        log.debug("Request to generateBarcode");
        List<StockSource> stockSourceList = stockSourceRepository.findBarcode(stockSource.getSku());
        StockSource queriedStockSource = stockSourceRepository.findById(stockSource.getId()).get();
        String barcode = null;
        if (!stockSourceList.isEmpty())
            barcode = stockSourceList.get(0).getBarCode();
        if (barcode == null) {
            queriedStockSource.setBarCode(sequenceGeneratorService.generateNumber("Stock_Barcode", "NH", null));
        } else {
            queriedStockSource.setBarCode(barcode);
        }
        stockSource.setBarCode(queriedStockSource.getBarCode());
        stockSourceRepository.updateBarcode(queriedStockSource.getBarCode(), queriedStockSource.getSku());
        save(queriedStockSource);
        return stockSource;
    }

    @Override
    public Page<StockSource> getAllStockSource(String transactionRefNo, String fromDate, String toDate, Long itemId, Pageable pageable) {
        log.debug("Request to get all StockSource based on transactionRefNo: {}, fromDate: {}, toDate: {}, itemId: {}, pageable: {}", transactionRefNo, fromDate, toDate, itemId, pageable);
        StringBuilder query = new StringBuilder("select new org.nh.pharmacy.domain.StockSource(s.id,s.itemId,s.uomId,s.sku,s.batchNo,s.expiryDate,s.owner,s.cost," +
            "s.mrp,s.transactionType,s.transactionRefNo,s.firstStockInDate,s.lastStockOutDate,s.consignment,s.quantity,s.availableQuantity,s.supplier," +
            "s.taxName,s.taxPerUnit,s.taxType,s.barCode,s.originalBatchNo,s.originalMRP,s.originalExpiryDate,s.mfrBarcode,(select item.name from Item item where item.id = s.itemId)) " +
            "from StockSource s where ");
        Map<String, Object> params = new HashMap<>();
        if (transactionRefNo != null) {
            query.append("lower(s.transactionRefNo) like ");
            query.append(":transactionRefNo");
            params.put("transactionRefNo", new StringBuilder("%").append(transactionRefNo).append("%").toString().toLowerCase());
        }
        if (itemId != null) {
            if (params.size() > 0) {
                query.append(" AND ");
            }
            query.append("s.itemId = ");
            query.append(":itemId");
            params.put("itemId", valueOf(itemId));

        }
        if (fromDate != null && toDate != null) {
            if (params.size() > 0) {
                query.append(" AND ");
            }
            query.append("s.expiryDate BETWEEN ");
            query.append(":fromDate");
            params.put("fromDate", DateUtil.getLocalDateFromStringDate(fromDate));
            query.append(" AND ");
            query.append(":toDate ");
            params.put("toDate", DateUtil.getLocalDateFromStringDate(toDate));

        }
        if (pageable.getSort() != null) {
            if (params.size() > 0) {
                query.append(" ORDER BY ");
            }
            query.append(getSortOrder(pageable.getSort()));
        }

        TypedQuery<StockSource> typedQuery = entityManager.createQuery(query.toString(), StockSource.class);
        params.forEach((key, value) -> {
            typedQuery.setParameter(key, value);
        });
        List<StockSource> stockSourceList = typedQuery.getResultList();
        Page<StockSource> page = new PageImpl<StockSource>(stockSourceList);
        return page;
    }

    private StringBuilder getSortOrder(Sort sort) {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        for (Sort.Order order : sort) {
            result.append(count++ > 0 ? ", " : "");
            result.append(String.format("%1$s %2$s", order.getProperty(), order.getDirection()));
        }
        return result;
    }

    @Override
    public Map<String, String> findBarcodeFormat(Long stockSourceId, Long unitId) {
        log.debug("Request to get Barcode configuration : {} ", stockSourceId);
        StockSource stockSource = stockSourceRepository.findById(stockSourceId).get();
        setItemName(stockSource);
        return getBarcodeFormat(stockSource, unitId);
    }

    private Map<String, String> getBarcodeFormat(StockSource stockSource, Long id) {
        log.debug("Request to getBarcodeFormat : {} ", id);
        Map<String, String> map = new HashedMap();
        String query = "unit.id:" + id;
        List<BarcodeConfiguration> barcodeConfigurationList = elasticSearchQueryService.queryElasticSearch(query, BarcodeConfiguration.class, "barcodeconfiguration");
        if (!barcodeConfigurationList.isEmpty()) {
            BarcodeConfiguration barcodeConfiguration = barcodeConfigurationList.iterator().next();
            String barcodeFormat = getFormat(stockSource, barcodeConfiguration.getFormat());
            if (barcodeConfiguration.getPrintNewLine()) {
                barcodeFormat = barcodeFormat.concat("\n\n");
            }
            map.put("format", barcodeFormat);
            map.put("printerName", barcodeConfiguration.getPrinterName());
            map.put("columnCount", barcodeConfiguration.getColumnCount().toString());
            map.put("printQuantity",stockSource.getPrintQuantity()!=null?stockSource.getPrintQuantity().toString() : "0");
        }
        return map;
    }

    private String getFormat(StockSource stockSource, String format) {
        log.debug("Request to getFormat");
        String source[] = {"{ITEMNAME}", "{BATCH_CODE}", "{EXPIRY_DATE}", "{BARCODE}", "{MRP}", "{BRANDNAME}"};
        String target[] = {stockSource.getItemName(), stockSource.getBatchNo(), stockSource.getExpiryDate().toString(), stockSource.getBarCode(), stockSource.getMrp().toString(), getBrandNameFromItemName(stockSource.getItemName())};
        String barcodeformat = StringUtils.replaceEach(format, source, target);
        return barcodeformat;
    }

    private String getBrandNameFromItemName(String itemName) {

        String[] splitedItemName = itemName.trim().split("-");
        return splitedItemName[3];
    }

    private void updateStockBarcode(StockSource stockSource) {
        log.debug("Request to updateStockBarcode");
        Set<String> mfrBarcodeSet = new LinkedHashSet<>();
        mfrBarcodeSet.add(stockSource.getBarCode());
        Iterator<StockSource> stockSourceIterator = stockSourceRepository.findOneByUniqueId(stockSource.getSku()).iterator();
        while (stockSourceIterator.hasNext()) {
            mfrBarcodeSet.add(stockSourceIterator.next().getMfrBarcode());
        }
        stockService.updateBarcode(StringUtils.join(mfrBarcodeSet, " "), stockSource.getSku());
    }

    /**
     * insert or update itemBarCode
     *
     * @param stockSource
     */
    private void upsertItemBarCode(StockSource stockSource) {
        log.debug("Request to save ItemBarcode for itemId : {} ", stockSource.getItemId());
        Long itemId = stockSource.getItemId();
        Item item = itemSearchRepository.findById(itemId).get();
        ItemBarcode itemBarCode = itemBarcodeSearchRepository.findByItemId(item.getId());
        if (itemBarCode == null) {
            itemBarCode = new ItemBarcode();
            itemBarCode.setItemId(item.getId());
            itemBarCode.setItemCode(item.getCode());
            itemBarCode.setItemName(item.getName());
        }
        itemBarCode.setBarcode(getUniqueBarcode(itemId));
        itemBarcodeRepository.save(itemBarCode);
        itemBarcodeSearchRepository.save(itemBarCode);
    }

    private String getUniqueBarcode(Long itemId) {
        log.debug("Request to getUniqueBarcode");
        List<String> barcodeList = stockService.findBarcodeByItemId(itemId);
        Set<String> barcodeSet = new HashSet<>();
        if (barcodeList != null) {
            for (String barcode : barcodeList) {
                if (StringUtils.isNotEmpty(barcode)) {
                    barcodeSet.addAll(Arrays.asList(barcode.split(" ")));
                }
            }
            return StringUtils.join(barcodeSet, " ");
        }
        return null;
    }

    private void setItemName(StockSource result) {
        Item item = itemRepository.findById(result.getItemId()).get();
        result.setItemName(item.getName());
    }

    /**
     * Update stock source for stock outs
     *
     * @param skuQuantityMap
     * @param transactionDate
     */
    @Override
    public void reduceStockSourceQuantity(Map<String, Double> skuQuantityMap, LocalDate transactionDate) {
        log.debug("Request to reduceStockSourceQuantity");
        for(Map.Entry entryMap: skuQuantityMap.entrySet()){
            Double requestedQuantity = 0d;
            if(entryMap.getValue() instanceof BigDecimal) {
                requestedQuantity = ((BigDecimal) entryMap.getValue()).doubleValue();
            } else if(entryMap.getValue() instanceof Double){
                requestedQuantity = (Double)entryMap.getValue();
            }
            do {
                List<StockSource> stockSourceList = stockSourceRepository.findBySkuIdByQuantity(entryMap.getKey().toString(), PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id")));
                if (isEmpty(stockSourceList)) break;
                for (StockSource stockSource : stockSourceList) {
                    if ((requestedQuantity = stockSource.getAvailableQuantity() - abs(requestedQuantity)) < 0f) {
                        reduceAvailableQuantity(stockSource.getId(), stockSource.getAvailableQuantity(), transactionDate);
                    } else {
                        reduceAvailableQuantity(stockSource.getId(), (stockSource.getAvailableQuantity() - requestedQuantity.floatValue()), transactionDate);
                        break;
                    }
                }
            } while (requestedQuantity < 0f);
        }
        /*skuQuantityMap.forEach((sku, requestedQuantity) -> {
            do {
                List<StockSource> stockSourceList = stockSourceRepository.findBySkuIdByQuantity(sku, new PageRequest(0, 2, new Sort(Sort.Direction.ASC, "id")));
                if (isEmpty(stockSourceList)) break;
                for (StockSource stockSource : stockSourceList) {
                    if ((requestedQuantity = stockSource.getAvailableQuantity() - abs(requestedQuantity)) < 0f) {
                        reduceAvailableQuantity(stockSource.getId(), stockSource.getAvailableQuantity(), transactionDate);
                    } else {
                        reduceAvailableQuantity(stockSource.getId(), (stockSource.getAvailableQuantity() - requestedQuantity.floatValue()), transactionDate);
                        break;
                    }
                }
            } while (requestedQuantity < 0f);
        });*/
    }

    /**
     * Update stock source for stock in
     *
     * @param skuQuantityMap
     * @param transactionDate
     */
    @Override
    public void increaseStockSourceQuantity(Map<String, Double> skuQuantityMap, LocalDate transactionDate) {
        log.debug("Request to increaseStockSourceQuantity");
        for(Map.Entry entryMap: skuQuantityMap.entrySet()) {
            Double requestedQuantity = 0d;
            if (entryMap.getValue() instanceof BigDecimal) {
                requestedQuantity = ((BigDecimal) entryMap.getValue()).doubleValue();
            } else if (entryMap.getValue() instanceof Double) {
                requestedQuantity = (Double) entryMap.getValue();
            }
            List<StockSource> stockSourceList = stockSourceRepository.findBySkuIdByQuantity(entryMap.getKey().toString(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));
            if (isEmpty(stockSourceList)) {
                stockSourceList = stockSourceRepository.findBySkuIdByZeroQuantity(entryMap.getKey().toString(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));
            }
            if (isNotEmpty(stockSourceList)) {
                StockSource stockSource = stockSourceList.get(0);
                increaseAvailableQuantity(stockSource.getId(), requestedQuantity.floatValue(), transactionDate);
            }
        }
    }

    /**
     * Save stock flow and stock source Ids in StockFlowStockSource
     *
     * @param stockFlowIdSkuMap
     */
    @Override
    public void saveStockFlowStockSourceIds(Map<String, String> stockFlowIdSkuMap) {
        log.debug("Request to insert stock flow and stock source ids");
        stockFlowIdSkuMap.forEach((stockFlowId, sku) -> {
            List<StockSource> sourceList = stockSourceRepository.findBySkuIdByQuantityByLastStockOutDate(sku, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastStockOutDate")));
            for (StockSource stockSource : sourceList) {
                stockFlowStockSourceRepository.save(new StockFlowStockSource().stockFlowId(valueOf(stockFlowId)).stockSourceId(stockSource.getId()));
            }
        });
    }

    /**
     * Update available quantity in stock source
     *
     * @param id
     * @param requestedQuantity
     * @param transactionDate
     */
    public void reduceAvailableQuantity(Long id, Float requestedQuantity, LocalDate transactionDate) {
        stockSourceRepository.reduceAvailableQuantity(id, requestedQuantity, transactionDate);
    }

    /**
     * Update available quantity in stock source
     *
     * @param id
     * @param requestedQuantity
     * @param transactionDate
     */
    public void increaseAvailableQuantity(Long id, Float requestedQuantity, LocalDate transactionDate) {
        stockSourceRepository.increaseAvailableQuantity(id, requestedQuantity, transactionDate);
    }

    /**
     * Search for the stocksources to corresponding to the transactionRefNo And unitCode.
     *
     * @param transactionRefNo
     * @param unitCode
     */
    @Override
    public List<StockSource> getStockSourceByTransactionRefNo(String transactionRefNo, String unitCode, Pageable pageable) {
        log.debug("Request to get StockSources by transactionRefNo:{}",transactionRefNo);

        StringBuilder query = new StringBuilder("select new org.nh.pharmacy.domain.StockSource(s.id,s.itemId,s.uomId,s.sku,s.batchNo,s.expiryDate,s.owner,s.cost," +
            "s.mrp,s.transactionType,s.transactionRefNo,s.firstStockInDate,s.lastStockOutDate,s.consignment,s.quantity,s.availableQuantity,s.supplier," +
            "s.taxName,s.taxPerUnit,s.taxType,s.barCode,s.originalBatchNo,s.originalMRP,s.originalExpiryDate,s.mfrBarcode,(select item.name from Item item where item.id = s.itemId)) " +
            "from StockSource s where s.transactionRefNo like :transactionRefNo AND s.owner = :unitCode");

        Map<String, Object> params = new HashMap<>();
        params.put("unitCode", unitCode);
        params.put("transactionRefNo", transactionRefNo + "%");
        query.append(" ORDER BY ");
        query.append(getSortOrder(pageable.getSort()));

        TypedQuery<StockSource> typedQuery = entityManager.createQuery(query.toString(), StockSource.class);
        params.forEach((key, value) -> {
            typedQuery.setParameter(key, value);
        });
        List<StockSource> stockSourceList = typedQuery.getResultList();
        return stockSourceList;
    }

    @Override
    public List<StockSource> generateBarcodes(List<StockSource> stockSources) throws Exception
    {     log.debug("Request to generateBarCodes");
        for (StockSource stockSource:stockSources) {
            StockSource queriedStockSource = stockSourceRepository.findById(stockSource.getId()).get();
            List<StockSource> stockSourceList = stockSourceRepository.findBarcode(stockSource.getSku());
            String barcode = null;
            if (!stockSourceList.isEmpty())
                barcode = stockSourceList.get(0).getBarCode();
            if (barcode == null) {
                queriedStockSource.setBarCode(sequenceGeneratorService.generateNumber("Stock_Barcode", "NH", null));
            } else {
                queriedStockSource.setBarCode(barcode);
            }
            stockSource.setBarCode(queriedStockSource.getBarCode());
            stockSourceRepository.updateBarcode(queriedStockSource.getBarCode(), queriedStockSource.getSku());
            save(queriedStockSource);
        }
        return stockSources;
    };

    @Override
    public List<Map<String, String>> findBarcodeFormats(List<StockSource> stockSources, Long unitId) {
        log.debug("Request to get Barcode configurations");
        List<Map<String, String>> barcodeFormat = new ArrayList<>();
        for (StockSource stockSource : stockSources) {
            setItemName(stockSource);
            barcodeFormat.add(getBarcodeFormat(stockSource,unitId));
        }
        return barcodeFormat;
    }

    /**
     * Update only mfrBarcode field in stock_source
     * @param stockSources the entities to save
     * @return
     */
    @Override
    public List<StockSource> save(List<StockSource> stockSources){
        log.debug("REST request to update StockSources : {}", stockSources);
        List<StockSource> stockSourceList = new ArrayList<>();
        for (StockSource stockSource: stockSources) {
            StockSource queriedStockSource = stockSourceRepository.findById(stockSource.getId()).get();
            if(!stockSource.getMfrBarcode().equals(queriedStockSource.getMfrBarcode())){
                queriedStockSource.setMfrBarcode(stockSource.getMfrBarcode());
                stockSourceList.add(save(queriedStockSource));
            }
        }
        return stockSourceList;
    }


    @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public StockSource getStockSource(String sku){
        List<StockSource> stockSourceList= stockSourceRepository.findBySkuIdByQuantityByLastStockOutDate(sku, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastStockOutDate")));
        if(CollectionUtils.isNotEmpty(stockSourceList))
        {

            log.debug("stock source found. stock source id = {}", stockSourceList.get(0).getId());
            return stockSourceList.get(0);

        }
        log.error("Stock source not found for the sku = {} ", sku);
        return null;
    }

    @Override
    public StockSource findBySkuIdByQuantityAndOrderByFirstStockInDate(String sku){
        log.debug("findBySkuIdByQuantityAndOrderByFirstStockInDate sku = {}", sku);
        return stockSourceRepository.findBySkuIdByQuantityAndOrderByDate(sku);
    }

    @Override
    public StockSource findByOwnerItemIdBatchNoAndExpiry(String owner, Long itemId, String batchNo, LocalDate expiryDate) {
        log.info("findByOwnerItemIdBatchNoAndExpiry owner:{}, itemId:{}, batchNo:{}, expiryDate:{}", owner,itemId,batchNo,expiryDate);
        return stockSourceRepository.findByOwnerItemIdBatchNoAndExpiry(owner,itemId,batchNo,expiryDate);
    }

}
