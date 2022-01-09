package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang.StringUtils;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.aop.producer.StockServiceAspect;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.FlowType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.OrganizationSearchRepository;
import org.nh.pharmacy.service.ItemUnitAverageCostService;
import org.nh.pharmacy.service.StockService;
import org.nh.pharmacy.service.StockSourceHeaderService;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.nh.common.util.BigDecimalUtil.add;
import static org.nh.common.util.BigDecimalUtil.multiply;
import static org.nh.pharmacy.domain.enumeration.TransactionType.*;
import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.domain.Sort.Order;

/**
 * Service Implementation for managing Stock.
 */
@Service
@Transactional
public class StockServiceImpl implements StockService {

    private final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

    private static final int DECIMAL_PLACES = 6;

    private final StockRepository stockRepository;

    private final ReserveStockRepository reserveStockRepository;

    private final StockFlowRepository stockFlowRepository;

    private final StockSourceRepository stockSourceRepository;

    private final ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    private final StockDataProcessorRepository stockDataProcessorRepository;

    private final ItemRepository itemRepository;

    private final ItemUnitAverageCostService itemUnitAverageCostService;

    private final MessageChannel itemStoreStockChannel;

    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    private final ApplicationProperties applicationProperties;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final StockTransitRepository stockTransitRepository;

    private final StockIssueRepository stockIssueRepository;

    private final MessageChannel itemStoreStockViewChannel;

    private final StockSourceHeaderService stockSourceHeaderService;

    private final OrganizationRepository organizationRepository;

    private final OrganizationSearchRepository organizationSearchRepository;

    public StockServiceImpl(StockRepository stockRepository, StockFlowRepository stockFlowRepository, ReserveStockRepository reserveStockRepository, StockSourceRepository stockSourceRepository, ItemUnitAverageCostRepository itemUnitAverageCostRepository, StockDataProcessorRepository stockDataProcessorRepository,
                            ItemRepository itemRepository, ItemUnitAverageCostService itemUnitAverageCostService, @Qualifier(Channels.ITEM_STORE_STOCK_OUTPUT) MessageChannel itemStoreStockChannel, EntityManager entityManager, ObjectMapper objectMapper, ApplicationProperties applicationProperties, ElasticsearchOperations elasticsearchTemplate, StockTransitRepository stockTransitRepository, StockSourceHeaderService stockSourceHeaderService, OrganizationRepository organizationRepository,
                            StockIssueRepository stockIssueRepository, @Qualifier(Channels.ITEM_STORE_STOCK_VIEW_OUTPUT) MessageChannel itemStoreStockViewChannel, OrganizationSearchRepository organizationSearchRepository) {

        this.stockRepository = stockRepository;
        this.stockFlowRepository = stockFlowRepository;
        this.reserveStockRepository = reserveStockRepository;
        this.stockSourceRepository = stockSourceRepository;
        this.itemUnitAverageCostRepository = itemUnitAverageCostRepository;
        this.stockDataProcessorRepository = stockDataProcessorRepository;
        this.itemRepository = itemRepository;
        this.itemUnitAverageCostService = itemUnitAverageCostService;
        this.itemStoreStockChannel = itemStoreStockChannel;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.applicationProperties = applicationProperties;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.stockTransitRepository = stockTransitRepository;
        this.stockSourceHeaderService = stockSourceHeaderService;
        this.organizationRepository = organizationRepository;
        this.stockIssueRepository = stockIssueRepository;
        this.itemStoreStockViewChannel = itemStoreStockViewChannel;
        this.organizationSearchRepository = organizationSearchRepository;
    }

    /**
     * Save a stock.
     *
     * @param stock the entity to save
     * @return the persisted entity
     */
    @Override
    public Stock save(Stock stock) {
        log.debug("Request to save Stock : {}", stock);
        Stock result = stockRepository.save(stock);
        return result;
    }

    /**
     * Get all the stocks.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Stock> findAll(Pageable pageable) {
        log.debug("Request to get all Stocks");
        Page<Stock> result = stockRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stock by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Stock findOne(Long id) {
        log.debug("Request to get Stock : {}", id);
        Optional<Stock> stock = stockRepository.findById(id);
        return stock.orElse(null);
    }

    /**
     * Delete the  stock by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Stock : {}", id);
        stockRepository.deleteById(id);
    }

    /**
     * Reserves stock during stock reservation process into the table
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @param requestedQuantity
     * @param transactionId
     * @param transactionType
     * @param transactionNo
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveStock(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity,
                             Long transactionId, TransactionType transactionType, String transactionNo, Long transactionLineId, LocalDateTime transactionDate,Long userId) throws StockException {
        log.debug("Request to reserve stock ");
        stockRepository.getStockWithLock(stockId);
        Float availableQuantity = (stockId != null ? getAvailableStock(stockId) : getAvailableStock(itemId, batchNo, storeId));
        if (requestedQuantity <= availableQuantity) {
            ReserveStock reserveStock = new ReserveStock();
            reserveStock.setStockId(stockId);
            reserveStock.setQuantity(requestedQuantity);
            reserveStock.setReservedDate(LocalDateTime.now());
            reserveStock.setTransactionId(transactionId);
            reserveStock.setTransactionType(transactionType);
            reserveStock.setTransactionLineId(transactionLineId);
            reserveStock.setTransactionNo(transactionNo);
            reserveStock.setTransactionDate(transactionDate);
            reserveStock.setUserId(userId);
            reserveStockRepository.save(reserveStock);
        } else {
            String message = "Insufficient stock for given Stock Details";
            throw new StockException(stockId, itemId, itemRepository.findById(itemId).get().getName(), batchNo, storeId, requestedQuantity, availableQuantity, message).errorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
        }


    }

    /**
     * Reserves stock during stock reservation process into the table
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @param requestedQuantity
     * @param transactionId
     * @param transactionType
     * @param transactionNo
     */
    @Override
    @Transactional
    public void reserveStockInSameTransaction(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity,
                                              Long transactionId, TransactionType transactionType, String transactionNo, Long transactionLineId, LocalDateTime transactionDate,Long userId) throws StockException {
        log.debug("Request to reserve stock in same Transaction ");
        stockRepository.getStockWithLock(stockId);
        //if (requestedQuantity <= (stockId != null ? getAvailableStock(stockId) : getAvailableStock(itemId, batchNo, storeId))) {
        ReserveStock reserveStock = new ReserveStock();
        reserveStock.setStockId(stockId);
        reserveStock.setQuantity(requestedQuantity);
        reserveStock.setReservedDate(LocalDateTime.now());
        reserveStock.setTransactionId(transactionId);
        reserveStock.setTransactionType(transactionType);
        reserveStock.setTransactionLineId(transactionLineId);
        reserveStock.setTransactionNo(transactionNo);
        reserveStock.setTransactionDate(transactionDate);
        reserveStock.setUserId(userId);
        reserveStockRepository.save(reserveStock);
        /*} else {
            String message = "Insufficient stock for given ID :" + stockId;
            throw new StockException(stockId, itemId, batchNo, storeId, requestedQuantity, message).errorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
        }*/


    }

    /**
     * Deletes Reserved stock
     *
     * @param transactionId
     * @param transactionType
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReservedStock(Long transactionId, TransactionType transactionType) {
        log.debug("Request to delete Stock for transactionId: {}, transactionType : {}", transactionId, transactionType);
        reserveStockRepository.deleteReservedStock(transactionId, transactionType);
    }

    /**
     * Deletes reserved stock by transaction id, transaction type, stock id
     *
     * @param transactionId
     * @param transactionType
     * @param stockId
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReservedStockByStockId(Long transactionId, TransactionType transactionType, Long stockId) {
        log.debug("Request to delete Stock for transactionId: {}, transactionType : {}, stockId : {}", transactionId, transactionType, stockId);
        reserveStockRepository.deleteReservedStockByStockId(transactionId, transactionType, stockId);
    }

    /**
     * Deletes reserved records based on transaction ID, transaction Type, stock Id and reserve again
     *
     * @param stockId
     * @param itemId
     * @param batchNo
     * @param storeId
     * @param requestedQuantity
     * @param transactionId
     * @param transactionType
     * @param transactionNo
     * @param transactionLineId
     * @param transactionDate
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deleteAndReserveStock(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity, Long transactionId, TransactionType transactionType, String transactionNo, Long transactionLineId, LocalDateTime transactionDate,Long userId) throws StockException {
        //deleteReservedStockByStockId(transactionId, transactionType, stockId);
        reserveStockRepository.deleteByTransactionNoAndTransactionLineId(transactionNo, transactionLineId);
        reserveStock(stockId, itemId, batchNo, storeId, requestedQuantity, transactionId, transactionType, transactionNo, transactionLineId, transactionDate,userId);

    }

    @Override
    public List<ReserveStock> findReserveStockByTransactionNo(String transactionNo) {
        return reserveStockRepository.findByTransactionNumber(transactionNo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReservedStocks(List<Long> ids) {
        for (Long id : ids) {
            reserveStockRepository.deleteById(id);
        }
    }

    /**
     * returns available stock based on the combination of item, batch and store
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @return
     */
    @Override
    public Float getAvailableStock(Long itemId, String batchNo, Long storeId) throws StockException {
        log.debug("Request to find Available stock with item : {}, batch : {}, store : {}", itemId, batchNo, storeId);
        List<Stock> stocks = stockRepository.getStockWithFields(itemId, batchNo, storeId);
        if (stocks == null || stocks.isEmpty()) {
            String message = "Insufficient stock for given combination of item :" + itemId + " batch :" + batchNo + " and store :" + storeId;
            throw new StockException(itemId, batchNo, storeId, message);
        }
        Float availableStock = Float.valueOf(0);
        Float totalReservedStock = Float.valueOf(0);

        for (Stock stock: stocks) {
            List<ReserveStock> reservedStockList = reserveStockRepository.findAllReservedStockByStockId(stock.getId());
            for (ReserveStock reservedStock : reservedStockList) {
                totalReservedStock += reservedStock.getQuantity();
            }
            availableStock += stock.getQuantity();
        }

        return availableStock - totalReservedStock;
    }

    /**
     * Returns Available Stocks based Stock Id
     *
     * @param stockId
     * @return
     */
    @Override
    public Float getAvailableStock(Long stockId) {
        log.debug("Request to find available stock with Id : {}", stockId);
        Float currentStock = stockRepository.findById(stockId).get().getQuantity();
        List<ReserveStock> reservedStockList = reserveStockRepository.findAllReservedStockByStockId(stockId);
        Float totalReservedStock = 0F;
        for (ReserveStock reservedStock : reservedStockList) {
            totalReservedStock += reservedStock.getQuantity();
        }

        return (currentStock - totalReservedStock);
    }


    /**
     * This method is used to perform stock in operations
     *
     * @param stockEntryList (DTO)
     * @return stockFlowList
     */
    @Override
    @Transactional
    public List<StockFlow> stockIn(List<StockEntry> stockEntryList) {
        log.debug("Start of stock in");
        List<Stock> stockList = new ArrayList<>();
        List<StockFlow> stockFlowList = new ArrayList<>();
        List<StockSource> stockSourceList = new ArrayList<>();
        List<ItemUnitAverageCost> itemUnitAvgCostList = new ArrayList<>();

        stockEntryList.stream().sorted(Comparator.comparing(stockEntry -> stockEntry.getItemId()))
            .forEach((StockEntry stockEntry) -> {
                if(isNull(stockEntry.getSupplier())) stockEntry.setSupplier("-");
                roundingOffBigDec(stockEntry);
                String sku = constructSKU(stockEntry);
                Stock stock = retrieveStock(stockEntry, sku, stockList);
                createStockSource(stockEntry, sku, stockSourceList);
                ItemUnitAverageCost itemUnitAverageCost = retrieveItemUnitAverageCost(stockEntry, itemUnitAvgCostList);
                createStockFlowObjForStockIn(stock, stockEntry, itemUnitAverageCost, stockFlowList);
            });
        log.debug("Start of stock in");
        stockRepository.saveAll(stockList);

        updateStockCount(stockFlowList);
        assignStockIdToStockFlow(stockList, stockFlowList);
        List<StockFlow> stockFlows = stockFlowRepository.saveAll(stockFlowList);
        stockFlowRepository.flush();
        saveStockDataProcessor(stockFlows);
        itemUnitAverageCostRepository.saveAll(itemUnitAvgCostList);
        stockSourceRepository.saveAll(stockSourceList);
        setValuesToThreadLocal(stockFlowList);
        return stockFlowList;
    }

    private void roundingOffBigDec(StockEntry stockEntry) {
        stockEntry.setTaxPerUnit(stockEntry.getTaxPerUnit() == null ? stockEntry.getTaxPerUnit() : BigDecimalUtil.roundOff(stockEntry.getTaxPerUnit(), DECIMAL_PLACES));
        stockEntry.setCost(BigDecimalUtil.roundOff(stockEntry.getCost(), DECIMAL_PLACES));
        stockEntry.setMrp(BigDecimalUtil.roundOff(stockEntry.getMrp(), DECIMAL_PLACES));
        stockEntry.setOriginalMRP(BigDecimalUtil.roundOff(stockEntry.getOriginalMRP(), DECIMAL_PLACES));
    }

    /**
     * This method is used to perform stock out operations
     *
     * @param transactionNumber
     * @return stockFlowList
     */
    @Override
    @Transactional
    @PublishStockTransaction
    public List<StockFlow> stockOut(String transactionNumber) {
        List<StockFlow> stockFlowList = new ArrayList<>();
        List<ReserveStock> reserveStockList = reserveStockRepository.findByTransactionNumber(transactionNumber);
        reserveStockList.forEach(reserveStock -> {
            Stock stock = stockRepository.findById(reserveStock.getStockId()).get();
            validateStockHasSufficientQty(stock, reserveStock.getQuantity());
            ItemUnitAverageCost itemUnitAverageCost = itemUnitAverageCostRepository.findByItemIdUnitId(stock.getItemId(), stock.getUnitId());
            createStockFlowObjForStockOut(stock, reserveStock, itemUnitAverageCost, stockFlowList);
        });
        log.debug("consignment stockFlowList: {}",stockFlowList);
        List<String> transactionLineIds = stockFlowList.stream().filter(StockFlow::getStockDataProcessor).map(stockFlow -> String.valueOf(stockFlow.getTransactionLineId())+stockFlow.isConsignment()).collect(Collectors.toList());
        log.debug("consignment transactionLineIds: {}",transactionLineIds);
        List<StockFlow> stockFlows = stockFlowRepository.saveAll(stockFlowList);
        List<StockFlow> stockFlowForDataProcess = stockFlows.stream().filter(stockFlow -> transactionLineIds.contains(String.valueOf(stockFlow.getTransactionLineId())+stockFlow.isConsignment())).collect(Collectors.toList());
        log.debug("consignment stockFlowForDataProcess: {}",stockFlowForDataProcess);
        stockFlowRepository.flush();
        saveStockDataProcessor(stockFlowForDataProcess);
        reserveStockRepository.deleteInBatch(reserveStockList);
        reserveStockList.forEach(reserveStock -> stockRepository.reduceStockQuantity(reserveStock.getQuantity(), reserveStock.getStockId()));
        setValuesToThreadLocal(stockFlowList);
        return stockFlowList;
    }

    /**
     * check stock has requested quantity.
     * @param stock
     * @param requestedQty
     */
    private void validateStockHasSufficientQty(Stock stock, Float requestedQty) {
        if (stock.getQuantity() < requestedQty) {
            throw new StockException(stock.getId(), stock.getItemId(), itemRepository.findById(stock.getItemId()).get().getName(), stock.getBatchNo(), stock.getStoreId(), requestedQty, stock.getQuantity()
                , "Insufficient stock for given Stock Details").errorCode(PharmacyErrorCodes.INSUFFICIENT_STOCK_FOR_ITEM_BATCH);
        }
    }

    @Override
    @Transactional
    @PublishStockTransaction
    public List<StockTransit> moveStockToTransit(String transactionNumber, String flowType) {
        List<StockFlow> stockFlowList = new ArrayList<>();

        StockIssue stockIssue = stockIssueRepository.findOneByDocumentNumber(transactionNumber);
        List<StockTransit> stockTransitList = new ArrayList<>();
        List<ReserveStock> reserveStockList = reserveStockRepository.findByTransactionNumber(transactionNumber);
        reserveStockList.forEach(reserveStock -> {
            createStockTransitRecord(reserveStock, stockTransitList);
        });
        stockTransitList.forEach(stockTransit -> {
            Stock stock = stockRepository.findById(stockTransit.getStockId()).get();
            ItemUnitAverageCost itemUnitAverageCost = itemUnitAverageCostRepository.findByItemIdUnitId(stock.getItemId(), stock.getUnitId());
            createStockFlowObjForStockOut(stock, stockTransit, itemUnitAverageCost, stockFlowList);
        });
        List<StockTransit> stockTransits = stockTransitRepository.saveAll(stockTransitList);
        stockTransitRepository.flush();
        reserveStockRepository.deleteInBatch(reserveStockList);
        reserveStockRepository.flush();
        stockTransits.forEach(reserveStock -> stockRepository.updateStockQuantity(reserveStock.getQuantity(), reserveStock.getStockId()));
        if (null == flowType) {
            setValuesToThreadLocalForMoveToTransit(stockTransitList);
            //StockServiceAspect.threadLocal.get().put(Channels.ITEM_STORE_STOCK_VIEW_OUTPUT, publishForItemStoreStockView(stockIssue, stockTransitList));
        }
        return stockTransits;
    }

    public Map<String, Object> publishForItemStoreStockView(StockIssue stockIssue, List<StockTransit> stockTransitList) {
        log.debug("Issue Stock Out for Transit : {}", stockIssue.getDocument().getDocumentNumber());

        Map<String, Object> itemDetail = new HashMap<>();
        List<Map<String, Object>> StoreDetail = new ArrayList<>();
        Map<String, Object> itemInfo = null;
        if (null != stockIssue.getDocument()) {
            itemDetail.put("issueStore", stockIssue.getDocument().getIssueStore() != null ? stockIssue.getDocument().getIssueStore().getId() : null);
            itemDetail.put("receiveStore", stockIssue.getDocument().getIndentStore() != null ? stockIssue.getDocument().getIndentStore().getId() : null);
        }
        for (StockTransit stockTransit : stockTransitList) {
            Stock stock = stockRepository.findById(stockTransit.getStockId()).get();
            itemInfo = new HashMap<>();
            itemInfo.put("transitQuantity", stockTransit.getPendingQuantity());
            itemInfo.put("itemId", stock.getItemId());
            StoreDetail.add(itemInfo);
        }
        itemDetail.put("itemMap", StoreDetail);
        return itemDetail;
    }

    @Override
    @PublishStockTransaction
    @Transactional
    public List<StockFlow> moveFromTransitToStock(List<StockEntry> stockEntries, List<StockTransit> stockTransitList) {

        List<StockFlow> transitStockFlowList = new ArrayList<>();
        StockTransit result = null;
        stockTransitList.forEach(stockTransit -> {
            StockTransit stockTransitForLineId = stockTransitRepository.findByTransactionId(stockTransit.getTransactionLineId());
            stockTransit.setStockId(stockTransitForLineId.getStockId());
            stockTransit.setTransactionId(stockTransitForLineId.getTransactionId());
            Stock stock = stockRepository.findById(stockTransit.getStockId()).get();
            ItemUnitAverageCost itemUnitAverageCost = itemUnitAverageCostRepository.findByItemIdUnitId(stock.getItemId(), stock.getUnitId());
            createStockFlowObjForStockOut(stock, stockTransit, itemUnitAverageCost, transitStockFlowList);
        });
        List<StockFlow> transitStockFlows = stockFlowRepository.saveAll(transitStockFlowList);
        stockFlowRepository.flush();
        saveStockDataProcessor(transitStockFlows);

        for (StockTransit stockTransit:stockTransitList) {
            stockTransitRepository.updateStockTransitQuantity(stockTransit.getQuantity(), stockTransit.getStockId(), stockTransit.getTransactionLineId());
            stockRepository.reduceStockTransitQuantity(stockTransit.getQuantity(), stockTransit.getStockId());
            validateStockTransitPendingQuantity(stockTransit);
        }
        setValuesToThreadLocalForMoveTransitToStock(transitStockFlowList);
        return stockIn(stockEntries);
    }

    private void validateStockTransitPendingQuantity(StockTransit stockTransit) throws FieldValidationException {
        StockTransit queriedObj = stockTransitRepository.findByTransactionId(stockTransit.getTransactionLineId());
        if (queriedObj.getPendingQuantity() < 0) {
            Stock stock = stockRepository.findById(stockTransit.getStockId()).get();
            Item item = itemRepository.findById(stock.getId()).get();
            List<ErrorMessage> errorMessages = new ArrayList<>();
            Map<String, Object> source = new HashMap<>();
            source.put("itemName", item.getName());
            source.put("itemCode", item.getCode());
            errorMessages.add(new ErrorMessage(PharmacyErrorCodes.RECEIVED_QUANTITY_IS_MORE_THAN_ISSUED_QUANTITY, source));
            log.error("Received Quantity is more than issued quantity for stockId:{}, itemName:{} ", stockTransit.getStockId(), item.getName());
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }

    private void setValuesToThreadLocal(List<StockFlow> stockFlowList) {
        Map<String, Object> itemStoreStockMap = (Map) StockServiceAspect.threadLocal.get().get(Channels.ITEM_STORE_STOCK_OUTPUT);
        Map<String, Object> stockSourceUpdateMap = (Map) StockServiceAspect.threadLocal.get().get(Channels.STOCK_SOURCE_OUTPUT);
        if (itemStoreStockMap == null) itemStoreStockMap = new HashMap<>();
        if (stockSourceUpdateMap == null) stockSourceUpdateMap = new HashMap<>();
        StockServiceAspect.threadLocal.get().put(Channels.ITEM_STORE_STOCK_OUTPUT, getValuesForPublishing(stockFlowList, itemStoreStockMap));
        Map<String, Object> tempStockSourceMap = getValuesForStockSourcePublishing(stockFlowList, stockSourceUpdateMap);
        if (null != tempStockSourceMap) {
            StockServiceAspect.threadLocal.get().put(Channels.STOCK_SOURCE_OUTPUT, tempStockSourceMap);
        }

    }

    private void setValuesToThreadLocalForMoveToTransit(List<StockTransit> stockTransitList) {
        Map<String, Object> itemStoreStockMap = (Map) StockServiceAspect.threadLocal.get().get(Channels.ITEM_STORE_STOCK_OUTPUT);
        if (itemStoreStockMap == null) itemStoreStockMap = new HashMap<>();
        StockServiceAspect.threadLocal.get().put(Channels.ITEM_STORE_STOCK_OUTPUT, getValuesForPublishingRecordForTransit(stockTransitList, itemStoreStockMap));
    }

    private void setValuesToThreadLocalForMoveTransitToStock(List<StockFlow> stockFlowList) {
        Map<String, Object> stockSourceUpdateMap = (Map) StockServiceAspect.threadLocal.get().get(Channels.STOCK_SOURCE_OUTPUT);
        if (stockSourceUpdateMap == null) stockSourceUpdateMap = new HashMap<>();
        Map<String, Object> tempStockSourceMap = getValuesForStockSourcePublishing(stockFlowList, stockSourceUpdateMap);
        if (null != tempStockSourceMap) {
            StockServiceAspect.threadLocal.get().put(Channels.STOCK_SOURCE_OUTPUT, tempStockSourceMap);
        }
    }

    private void createStockTransitRecord(ReserveStock reserveStock, List<StockTransit> stockTransitList) {

        StockTransit stockTransit = new StockTransit();
        stockTransit.setQuantity(reserveStock.getQuantity());
        stockTransit.setStockId(reserveStock.getStockId());
        stockTransit.setTransactionDate(reserveStock.getTransactionDate());
        stockTransit.setTransactionId(reserveStock.getTransactionId());
        stockTransit.setTransactionLineId(reserveStock.getTransactionLineId());
        stockTransit.setTransactionNo(reserveStock.getTransactionNo());
        stockTransit.setTransactionType(reserveStock.getTransactionType());
        stockTransit.setTransitDate(LocalDateTime.now());
        stockTransit.setPendingQuantity(reserveStock.getQuantity());
        stockTransit.setUserId(reserveStock.getUserId());
        stockTransitList.add(stockTransit);
    }

    /**
     * Retrieve stock
     *
     * @param stockEntry,sku,stockList
     * @return stock object
     */
    private Stock retrieveStock(StockEntry stockEntry, String sku, List<Stock> stockList) {

        Stock stock;
        Optional<Long> stockId = Optional.ofNullable(stockEntry.getStockId());

        stock = stockId.isPresent() ? stockRepository.findById(stockEntry.getStockId()).get() : null;
        stock = Optional.ofNullable(stock).orElseGet(() -> stockRepository.findOneByUniqueIdStoreId(sku, stockEntry.getStoreId()));
        stock = Optional.ofNullable(stock).orElseGet(() -> {
            Stock stockObj;
            Optional<Stock> optionalStock = stockList.stream()
                .filter(stockDetail -> sku.equals(stockDetail.getSku()))
                .filter(stockDetail -> stockEntry.getStoreId().equals(stockDetail.getStoreId()))
                .findFirst();

            if (optionalStock.isPresent()) {
                stockObj = optionalStock.get();
                stockList.get(stockList.indexOf(stockObj)).setQuantity(Float.sum(stockObj.getQuantity(), stockEntry.getQuantity()));
                stockList.get(stockList.indexOf(stockObj)).setStockValue(
                    multiply(stockObj.getCost(), stockList.get(stockList.indexOf(stockObj)).getQuantity(), DECIMAL_PLACES)
                );

            } else {
                Iterator<Stock> stockItr = stockRepository.findOneByUniqueId(sku, PageRequest.of(0, 1, Sort.by(new Order(Direction.DESC, "id")))).iterator();
                String barCode = null;
                if (stockItr.hasNext()) {
                    Stock stockObject = stockItr.next();
                    if (isNull(stockEntry.getOriginalBatchNo()) || isNull(stockEntry.getOriginalExpiryDate()) || isNull(stockEntry.getOriginalMRP())) {
                        stockEntry.setOriginalBatchNo(stockObject.getOriginalBatchNo());
                        stockEntry.setOriginalExpiryDate(stockObject.getOriginalExpiryDate());
                        stockEntry.setOriginalMRP(stockObject.getOriginalMRP());
                    }
                    barCode = stockObject.getBarcode();
                }

                stockObj = new Stock().itemId(stockEntry.getItemId())
                    .storeId(stockEntry.getStoreId())
                    .locatorId(stockEntry.getLocatorId())
                    .uomId(stockEntry.getUomId())
                    .sku(sku)
                    .batchNo(stockEntry.getBatchNo())
                    .expiryDate(stockEntry.getExpiryDate())
                    .owner(stockEntry.getOwner())
                    .cost(stockEntry.getCost())
                    .mrp(stockEntry.getMrp())
                    .quantity(stockEntry.getQuantity())
                    .consignment(stockEntry.isConsignment())
                    .stockValue(multiply(stockEntry.getCost(), stockEntry.getQuantity(), DECIMAL_PLACES))
                    .unitId(stockEntry.getUnitId())
                    .supplier(stockEntry.getSupplier())
                    .originalBatchNo(stockEntry.getOriginalBatchNo())
                    .originalExpiryDate(stockEntry.getOriginalExpiryDate())
                    .originalMRP(stockEntry.getOriginalMRP())
                    .barcode(barCode);
                stockList.add(stockObj);
            }
            return stockObj;
        });
        return stock;
    }

    /**
     * Retrieve ItemUnitAverageCost
     *
     * @return itemUnitAverageCost
     * @Param stock, stockEntry, stockFlowList, itemUnitAvgCostList
     */
    private ItemUnitAverageCost retrieveItemUnitAverageCost(StockEntry stockEntry, List<ItemUnitAverageCost> itemUnitAvgCostList) {

        ItemUnitAverageCost itemUnitAverageCost;

        Optional<ItemUnitAverageCost> optionalItemUnitAvgCost = itemUnitAvgCostList.stream()
            .filter(itemUnitAvgCost -> itemUnitAvgCost.getItemId().equals(stockEntry.getItemId()))
            .filter(itemUnitAvgCost -> itemUnitAvgCost.getUnitId().equals(stockEntry.getUnitId()))
            .findFirst();

        if (!stockEntry.getTransactionType().equals(GRN)
            && !stockEntry.getTransactionType().equals(Inter_Unit_Stock_Receipt)) {
            if (!optionalItemUnitAvgCost.isPresent()) {
                ItemUnitAverageCost availableItemUnitAvgCost = itemUnitAverageCostRepository.findByItemIdUnitId(stockEntry.getItemId(), stockEntry.getUnitId());
                itemUnitAvgCostList.add(availableItemUnitAvgCost);
                return availableItemUnitAvgCost;
            } else {
                return optionalItemUnitAvgCost.get();
            }
        } else {
            if (!optionalItemUnitAvgCost.isPresent()) {
                ItemUnitAverageCost availableItemUnitAvgCost = itemUnitAverageCostRepository.findByItemIdUnitIdByLock(stockEntry.getItemId(), stockEntry.getUnitId());
                if (availableItemUnitAvgCost == null) {
                    try {
                        itemUnitAverageCostService.saveInNewTransaction(new ItemUnitAverageCost()
                            .itemId(stockEntry.getItemId())
                            .unitId(stockEntry.getUnitId())
                            .averageCost(BigDecimalUtil.ZERO));
                    } catch (Exception ex) {
                        log.info("Duplicate insertion issue in item unit average cost, ignoring the error");
                    }
                    availableItemUnitAvgCost = itemUnitAverageCostRepository.findByItemIdUnitIdByLock(stockEntry.getItemId(), stockEntry.getUnitId());
                }
                ItemUnitAverageCost itemUnitAverageCostObj = stockRepository.findStockValueQuantityByItemIdUnitId(stockEntry.getItemId(), stockEntry.getUnitId());

                availableItemUnitAvgCost.setStockQuantity(itemUnitAverageCostObj == null ? 0 : itemUnitAverageCostObj.getStockQuantity());
                availableItemUnitAvgCost.setStockValue(
                    multiply(availableItemUnitAvgCost.getStockQuantity(), availableItemUnitAvgCost.getAverageCost()));

                itemUnitAvgCostList.add(availableItemUnitAvgCost);

                optionalItemUnitAvgCost = itemUnitAvgCostList.stream()
                    .filter(itemUnitAvgCost -> itemUnitAvgCost.getItemId().equals(stockEntry.getItemId()))
                    .filter(itemUnitAvgCost -> itemUnitAvgCost.getUnitId().equals(stockEntry.getUnitId()))
                    .findFirst();
            }

            itemUnitAverageCost = optionalItemUnitAvgCost.get();
            itemUnitAverageCost.setStockValue(add(itemUnitAverageCost.getStockValue(), multiply(stockEntry.getCost(), stockEntry.getQuantity(), DECIMAL_PLACES), DECIMAL_PLACES));
            itemUnitAverageCost.setStockQuantity(add(itemUnitAverageCost.getStockQuantity(), stockEntry.getQuantity(), DECIMAL_PLACES));
            itemUnitAverageCost.setAverageCost(calculateAverageCost(itemUnitAverageCost));
        }
        return itemUnitAverageCost;
    }

    /**
     * Create stockSource
     *
     * @param stockEntry,sku,stockList
     */
    private void createStockSource(StockEntry stockEntry, String sku, List<StockSource> stockSourceList) {

        if (stockEntry.getTransactionType().equals(GRN) || Inter_Unit_Stock_Receipt.equals(stockEntry.getTransactionType())) {
            stockSourceList.add(new StockSource().itemId(stockEntry.getItemId())
                .uomId(stockEntry.getUomId())
                .sku(sku)
                .batchNo(stockEntry.getBatchNo())
                .expiryDate(stockEntry.getExpiryDate())
                .owner(stockEntry.getOwner())
                .cost(stockEntry.getCost())
                .mrp(stockEntry.getMrp())
                .transactionType(stockEntry.getTransactionType())
                .transactionRefNo(stockEntry.getTransactionRefNo())
                .firstStockInDate(stockEntry.getFirstStockInDate())
                .lastStockOutDate(stockEntry.getLastStockOutDate())
                .consignment(stockEntry.isConsignment())
                .quantity(stockEntry.getQuantity())
                .availableQuantity(stockEntry.getAvailableQuantity())
                .supplier(stockEntry.getSupplier())
                .taxName(stockEntry.getTaxName())
                .taxPerUnit(stockEntry.getTaxPerUnit())
                .taxType(stockEntry.getTaxType())
                .barCode(stockEntry.getBarCode())
                .originalBatchNo(stockEntry.getOriginalBatchNo())
                .originalExpiryDate(stockEntry.getOriginalExpiryDate())
                .originalMRP(stockEntry.getOriginalMRP())
                .costWithoutTax(stockEntry.getCostWithoutTax())
                .recoverableTax(stockEntry.getRecoverableTax())
                .taxes(stockEntry.getTaxes()));
        }
    }

    /**
     * Prepare stock flow object for StockIn
     *
     * @param stock,stockEntry,itemUnitAverageCost,stockFlowList
     */
    private void createStockFlowObjForStockIn(Stock stock, StockEntry stockEntry, ItemUnitAverageCost itemUnitAverageCost, List<StockFlow> stockFlowList) {

        stockFlowList.add(new StockFlow().itemId(stock.getItemId())
            .stockId(stock.getId())
            .storeId(stock.getStoreId())
            .locatorId(stock.getLocatorId())
            .uomId(stock.getUomId())
            .sku(stock.getSku())
            .batchNo(stock.getBatchNo())
            .expiryDate(stock.getExpiryDate())
            .owner(stock.getOwner())
            .cost(stock.getCost())
            .mrp(stock.getMrp())
            .flowType(FlowType.StockIn)
            .quantity(stockEntry.getQuantity())
            .transactionId(stockEntry.getTransactionId())
            .transactionDate(stockEntry.getTransactionDate())
            .transactionType(stockEntry.getTransactionType())

            .transactionNumber(stockEntry.getTransactionNumber())
            .transactionLineId(stockEntry.getTransactionLineId())
            .averageCost(itemUnitAverageCost.getAverageCost())
            .averageCostValue(multiply(itemUnitAverageCost.getAverageCost(), stockEntry.getQuantity(), DECIMAL_PLACES))
            .consignment(stock.isConsignment())
            .costValue(multiply(stockEntry.getCost(), stockEntry.getQuantity(), DECIMAL_PLACES))
            .barCode(stockEntry.getBarCode())
            .userId(stockEntry.getUserId()));

    }

    /**
     * Create stock flow object for StockOut
     *
     * @param stock,reserveStock,itemUnitAverageCost,stockFlowList
     */
    private void createStockFlowObjForStockOut(Stock stock, ReserveStock reserveStock, ItemUnitAverageCost itemUnitAverageCost, List<StockFlow> stockFlowList) {

        stockFlowList.add(createStockFlowForStockOut(stock, reserveStock, itemUnitAverageCost));
        //If its consignment Stock than add extra entry for stock-out & stock-in  -> owner as unit and consignment flag = false
        if (TransactionType.Dispense.equals(reserveStock.getTransactionType()) && stock.isConsignment()) {
            String unitCode = organizationSearchRepository.findById(stock.getUnitId()).get().getCode();
            //Stock-in entry
            StockFlow stockIn = createStockFlowForStockOut(stock, reserveStock, itemUnitAverageCost);
            stockIn.setFlowType(FlowType.StockIn);
            stockIn.setBarCode(stock.getBarcode());
            stockIn.setConsignment(Boolean.FALSE);
            stockIn.setOwner(unitCode);
            stockIn.setStockDataProcessor(Boolean.FALSE);
            stockFlowList.add(stockIn);

            StockFlow stockOut = createStockFlowForStockOut(stock, reserveStock, itemUnitAverageCost);
            stockOut.setConsignment(Boolean.FALSE);
            stockOut.setStockDataProcessor(Boolean.FALSE);
            stockOut.setOwner(unitCode);
            stockFlowList.add(stockOut);
        }

    }

    private StockFlow createStockFlowForStockOut(Stock stock, ReserveStock reserveStock, ItemUnitAverageCost itemUnitAverageCost){
        StockFlow stockFlow = new StockFlow().itemId(stock.getItemId())
            .stockId(stock.getId())
            .storeId(stock.getStoreId())
            .locatorId(stock.getLocatorId())
            .uomId(stock.getUomId())
            .sku(stock.getSku())
            .batchNo(stock.getBatchNo())
            .expiryDate(stock.getExpiryDate())
            .owner(stock.getOwner())
            .cost(stock.getCost())
            .mrp(stock.getMrp())
            .flowType(FlowType.StockOut)
            .quantity(reserveStock.getQuantity())
            .transactionId(reserveStock.getTransactionId())
            .transactionDate(reserveStock.getTransactionDate())
            .transactionType(reserveStock.getTransactionType())
            .transactionNumber(reserveStock.getTransactionNo())
            .transactionLineId(reserveStock.getTransactionLineId())
            .consignment(stock.isConsignment())
            .averageCost(itemUnitAverageCost.getAverageCost())
            .averageCostValue(multiply(itemUnitAverageCost.getAverageCost(), reserveStock.getQuantity(), DECIMAL_PLACES))
            .costValue(multiply(stock.getCost(), reserveStock.getQuantity(), DECIMAL_PLACES))
            .barCode(StringUtils.EMPTY)
            .userId(reserveStock.getUserId());
        return stockFlow;
    }

    /**
     * Create stock flow object for StockOut
     *
     * @param stock,stockTransit,itemUnitAverageCost,stockFlowList
     */
    private void createStockFlowObjForStockOut(Stock stock, StockTransit stockTransit, ItemUnitAverageCost itemUnitAverageCost, List<StockFlow> stockFlowList) {

        stockFlowList.add(new StockFlow().itemId(stock.getItemId())
            .stockId(stock.getId())
            .storeId(stock.getStoreId())
            .locatorId(stock.getLocatorId())
            .uomId(stock.getUomId())
            .sku(stock.getSku())
            .batchNo(stock.getBatchNo())
            .expiryDate(stock.getExpiryDate())
            .owner(stock.getOwner())
            .cost(stock.getCost())
            .mrp(stock.getMrp())
            .flowType(FlowType.StockOut)
            .quantity(stockTransit.getQuantity()) // Stock receipt service is setting the current receiving quantity (sum of accepted + rejected in stock transit quantity field)
            .transactionId(stockTransit.getTransactionId())
            .transactionDate(stockTransit.getTransactionDate())
            .transactionType(stockTransit.getTransactionType())
            .transactionNumber(stockTransit.getTransactionNo())
            .transactionLineId(stockTransit.getTransactionLineId())
            .consignment(stock.isConsignment())
            .averageCost(itemUnitAverageCost.getAverageCost())
            .averageCostValue(multiply(itemUnitAverageCost.getAverageCost(), stockTransit.getQuantity(), DECIMAL_PLACES))
            .costValue(multiply(stock.getCost(), stockTransit.getQuantity(), DECIMAL_PLACES))
            .barCode(StringUtils.EMPTY)
            .userId(stockTransit.getUserId()));

    }

    /**
     * Assign stock id to stock flow object
     *
     * @param stockList,stockFlowList
     */
    private void assignStockIdToStockFlow(List<Stock> stockList, List<StockFlow> stockFlowList) {
        stockFlowList
            .forEach(stockFlow -> stockList.stream()
                .filter(stock -> stockFlow.getSku().equals(stock.getSku()))
                .filter(stock -> stockFlow.getStoreId().equals(stock.getStoreId()))
                .forEach(stock -> stockFlow.setStockId(stock.getId())));
    }

    /**
     * Calculate average cost
     *
     * @param itemUnitAverageCost,stockEntry
     * @return averageCost
     */
    private BigDecimal calculateAverageCost(ItemUnitAverageCost itemUnitAverageCost) {
        return BigDecimalUtil.divide(itemUnitAverageCost.getStockValue(), itemUnitAverageCost.getStockQuantity(), DECIMAL_PLACES);
    }

    /**
     * Update stock count for existing stocks
     *
     * @param stockFlowList
     */
    private void updateStockCount(List<StockFlow> stockFlowList) {
        stockFlowList
            .stream().filter(stockFlow -> stockFlow.getStockId() != null)
            .forEach(stockFlow -> stockRepository.increaseStockQuantity(stockFlow.getQuantity(), stockFlow.getStockId()));
    }

    /**
     * Construct SKU using fields- Owner,Item Id,Batch No,Expiry Date,Cost,MRP,Is Consignment
     *
     * @param stockEntry
     * @return sku
     */
    private String constructSKU(StockEntry stockEntry) {

        Object[] uniqueIdentifiers = {stockEntry.getOwner(), stockEntry.getItemId(), stockEntry.getBatchNo(), stockEntry.getExpiryDate(), stockEntry.getCost(), stockEntry.getMrp(), stockEntry.isConsignment()};
        return StringUtils.join(uniqueIdentifiers, "~");

    }

    public static final String constructSKU(String owner, Long itemId, String batchNo, LocalDate expiryDate, BigDecimal cost, BigDecimal mrp, boolean consignment) {

        cost = BigDecimalUtil.roundOff(cost, DECIMAL_PLACES);
        mrp = BigDecimalUtil.roundOff(mrp, DECIMAL_PLACES);

        Object[] uniqueIdentifiers = {owner, itemId, batchNo, expiryDate, cost, mrp, consignment};
        return StringUtils.join(uniqueIdentifiers, "~");
    }

    public List<StockDataProcessor> saveStockDataProcessor(List<StockFlow> stockFlowList) {
        log.debug("Save Stock Data Processor list {}", stockFlowList);
        List<StockDataProcessor> stockDataProcessorList = new ArrayList<>();
        for (StockFlow stockFlow : stockFlowList) {
            stockDataProcessorList.add(new StockDataProcessor(stockFlow.getId()));
        }
        List<StockDataProcessor> stockDataProcessors = stockDataProcessorRepository.saveAll(stockDataProcessorList);
        stockDataProcessorRepository.flush();
        log.debug("Stock Data Processor save result {}", stockDataProcessors);
        return stockDataProcessors;
    }

    private Map<String, Object> getValuesForPublishing(List<StockFlow> stockFlowList, Map<String, Object> map) {
        Set<Long> itemIds = new HashSet<>();
        Long storeId = stockFlowList.get(0).getStoreId();
        stockFlowList.forEach(stockFlow -> itemIds.add(stockFlow.getItemId()));
        map.put("itemIds", itemIds);
        map.put("storeId", storeId);
        return map;
    }

    private Map<String, Object> getValuesForPublishingRecordForTransit(List<StockTransit> stockTransits, Map<String, Object> map) {
        Set<Long> itemIds = new HashSet<>();

        for (StockTransit stockTransit : stockTransits) {
            Stock stock = stockRepository.findById(stockTransit.getStockId()).get();
            itemIds.add(stock.getItemId());
            map.put("itemIds", itemIds);
            map.put("storeId", stock.getStoreId());

        }
        return map;
    }
    /**
     * Get all the batch details.
     *
     * @param storeId,itemCode
     * @param docNumber
     * @return page of batch details
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Stock> getBatchDetails(Long storeId, String itemCode, String batchNumber, String docNumber, Boolean filterBlockedBatch, Pageable pageable) {
        log.debug("Request to get batch details on given item code {}", itemCode);

        if(null == filterBlockedBatch) filterBlockedBatch = Boolean.FALSE;

        Map<String, Object> params = new HashMap<>();

        StringBuilder query = new StringBuilder("select new org.nh.pharmacy.domain.Stock( s.id, s.blocked, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, cast((s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id AND rs.transactionNo <> :docNo),0)))as float), s.stockValue, s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId)) from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.storeId = :storeId AND item.code = :itemCode AND(s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id AND rs.transactionNo <> :docNo),0)))> 0");

        if (filterBlockedBatch) {
            query.append(" AND s.blocked = false");
        }
        params.put("storeId", storeId);
        params.put("itemCode", itemCode);
        params.put("docNo", docNumber);

        if (nonNull(batchNumber)) {
            query.append(" AND LOWER(s.batchNo) LIKE :batchNo");
            params.put("batchNo", ("%" + batchNumber + "%").toLowerCase());
        }
        query.append(" ORDER BY s.expiryDate ASC");

        Query batchDetailsQuery = entityManager.createQuery(query.toString());
        params.forEach((key, value) -> batchDetailsQuery.setParameter(key, value));
        List<Stock> stockList = batchDetailsQuery.getResultList();

        return new PageImpl(stockList, pageable == null ? Pageable.unpaged() : pageable, stockList.size());
    }

    /**
     * Get all batches based on store, item code, filterQuantity
     *
     * @param storeId
     * @param itemCode
     * @param filterQuantity
     * @param filterBlockedBatch
     * @param pageable
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Stock> getAllBatchDetails(Long storeId, String itemCode, Boolean filterQuantity,Boolean filterBlockedBatch, Pageable pageable) {
        log.debug("Request to get batch details on given item code");
        List<Stock> list;
        if(filterBlockedBatch){
            if (filterQuantity) {
                list = stockRepository.findAllUnblockedBatchDetailsByStoreIdAndItemCodeAndNonZeroQty(storeId, itemCode);
            } else {
                list = stockRepository.findAllUnblockedBatchDetailsByStoreIdAndItemCode(storeId, itemCode);
            }
        } else {
            if (filterQuantity) {
                list = stockRepository.findAllBatchDetailsByStoreIdAndItemCodeAndNonZeroQty(storeId, itemCode);
            } else {
                list = stockRepository.findAllBatchDetailsByStoreIdAndItemCode(storeId, itemCode);
            }
        }

        return new PageImpl(list, pageable, list.size());
    }

    /**
     * Get the items list based on the dispensableGenericName for given itemId and storeId.
     *
     * @param storeId,itemId
     * @return page of items based on dispensableGenericName
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getItemListByDispensableGenericName(Long storeId, Long itemId, Pageable pageable) {
        log.debug("Request to get page of items based on dispensableGenericName");
        List<Object[]> list = stockRepository.findItemDetailsByStoreIdAndItemId(storeId, itemId);
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (Object[] objects : list) {
            Map<String, Object> map = new HashMap();
            map.put("id", objects[0]);
            map.put("code", (String) objects[1]);
            map.put("name", (String) objects[2]);
            map.put("quantity", (float) objects[3]);
            itemsList.add(map);
        }
        return new PageImpl(itemsList, pageable, itemsList.size());
    }

    @Override
    @Transactional
    @PublishStockTransaction
    public List<StockFlow> stockInWithTxn(List<StockEntry> stockEntryList) {
        return stockIn(stockEntryList);
    }

    @Override
    @Transactional
    @PublishStockTransaction
    public void processStockMove(Map<String, Object> stockEntryList) throws IOException {
        stockOut(stockEntryList.get("DocumentNo").toString());
        String stockEntryValue = objectMapper.writeValueAsString(stockEntryList.get("StockEntries"));
        List<StockEntry> stockEntries = objectMapper.readValue(stockEntryValue, new TypeReference<List<StockEntry>>() {
        });
        stockIn(stockEntries);
    }

    /**
     * Get the current stock available for given details.
     *
     * @param unitId,consignment,storeId,itemId
     * @return available stock details
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCurrentAvailableStock(Long unitId, Boolean consignment, Long storeId, Long itemId) {
        log.debug("REST request to get the current stock available for given details");

        Map<String, Object> params = new HashMap<>();

        StringBuilder query = new StringBuilder("select ic.description AS item_category,i.item_type ->> 'display' AS item_type,i.code AS item_code,i.name AS item_name, s.batch_no AS batch_code,s.expiry_date,i.dispensable_generic_name AS generic_name,hsc.name AS store,l.name AS locator, s.quantity AS current_stock,u.name AS uom,s.cost AS unit_rate,s.mrp AS unit_mrp,iuac.average_cost as average_cost, s.transit_quantity from  Stock AS s INNER JOIN item AS i on s.item_id = i.id INNER JOIN item_category AS ic on i.category_id = ic.id INNER JOIN healthcare_service_center  AS hsc on s.store_id = hsc.id INNER JOIN locator AS l on s.locator_id = l.id INNER JOIN uom as u on s.uom_id = u.id INNER JOIN item_unit_average_cost AS iuac  on i.id = iuac.item_id AND s.unit_id = iuac.unit_id where s.unit_id =:unitId ");

        params.put("unitId", unitId);

        if (nonNull(consignment)) {
            query.append(" and s.consignment =:consignment");
            params.put("consignment", consignment);
        }

        if (nonNull(storeId)) {
            query.append(" and s.store_id = :storeId");
            params.put("storeId", storeId);
        }

        if (nonNull(itemId)) {
            query.append(" and s.item_id= :itemId");
            params.put("itemId", itemId);
        }
        query.append(" and s.quantity > 0 order by hsc.name, i.name, s.batch_no");

        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        params.forEach((key, value) -> nativeQuery.setParameter(key, value));
        List<Object[]> objects = nativeQuery.getResultList();

        List<Map<String, Object>> stockList = new ArrayList<>();
        for (Object[] object : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("itemCategory", object[0]);
            map.put("itemType", object[1]);
            map.put("itemCode", object[2]);
            map.put("itemName", object[3]);
            map.put("batchCode", object[4]);
            map.put("expiryDate", object[5]);
            map.put("genericName", object[6].toString().split("-")[0]);
            map.put("store", object[7]);
            map.put("locator", object[8]);
            map.put("quantity", add(BigDecimal.valueOf(Float.valueOf(object[9].toString())),BigDecimal.valueOf(Float.valueOf(object[14].toString()))));
            map.put("uom", object[10]);
            map.put("unitRate", object[11]);
            map.put("unitMrp", object[12]);
            map.put("stockValue", multiply((BigDecimal)map.get("quantity"), (BigDecimal) object[11], DECIMAL_PLACES));
            map.put("averageCost", object[13]);
            map.put("averageStockValue", multiply((BigDecimal)map.get("quantity"), (BigDecimal) object[13], DECIMAL_PLACES));
            map.put("transitQuantity", object[14]);
            map.put("transitValue", multiply(BigDecimal.valueOf(Float.valueOf(object[14].toString())), (BigDecimal) object[11], DECIMAL_PLACES));
            map.put("averageTransitValue", multiply(BigDecimal.valueOf(Float.valueOf(object[14].toString())), (BigDecimal) object[13], DECIMAL_PLACES));

            stockList.add(map);
        }
        return stockList;
    }

    /**
     * Get the current stock available for given details for export.
     *
     * @param unitId,consignment,storeId,itemId
     * @return available stock details
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCurrentAvailableStockExport(Long unitId, Boolean consignment, Long storeId, Long itemId) {
        log.debug("REST request to get the current stock available for given details");

        Map<String, Object> params = new HashMap<>();

        StringBuilder query = new StringBuilder("select ic.description AS item_category,i.item_type ->> 'display' AS item_type,i.code AS item_code,i.name AS item_name, s.batch_no AS batch_code,s.expiry_date,i.dispensable_generic_name AS generic_name,hsc.name AS store,l.name AS locator, s.quantity AS current_stock,u.name AS uom,s.cost AS unit_rate,s.mrp AS unit_mrp,iuac.average_cost as average_cost, s.transit_quantity,s.consignment as consignment,s.owner as owner_code from  Stock AS s INNER JOIN item AS i on s.item_id = i.id INNER JOIN item_category AS ic on i.category_id = ic.id INNER JOIN healthcare_service_center  AS hsc on s.store_id = hsc.id INNER JOIN locator AS l on s.locator_id = l.id INNER JOIN uom as u on s.uom_id = u.id INNER JOIN item_unit_average_cost AS iuac  on i.id = iuac.item_id AND s.unit_id = iuac.unit_id where s.unit_id =:unitId ");

        params.put("unitId", unitId);

        if (nonNull(consignment)) {
            query.append(" and s.consignment =:consignment");
            params.put("consignment", consignment);
        }

        if (nonNull(storeId)) {
            query.append(" and s.store_id = :storeId");
            params.put("storeId", storeId);
        }

        if (nonNull(itemId)) {
            query.append(" and s.item_id= :itemId");
            params.put("itemId", itemId);
        }
        query.append(" and s.quantity > 0 order by hsc.name, i.name, s.batch_no");

        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        params.forEach((key, value) -> nativeQuery.setParameter(key, value));
        List<Object[]> objects = nativeQuery.getResultList();

        List<Map<String, Object>> stockList = new ArrayList<>();
        for (Object[] object : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("itemCategory", object[0]);
            map.put("itemType", object[1]);
            map.put("itemCode", object[2]);
            map.put("itemName", object[3]);
            map.put("batchCode", object[4]);
            map.put("expiryDate", object[5]);
            map.put("genericName", object[6].toString().split("-")[0]);
            map.put("store", object[7]);
            map.put("locator", object[8]);
            map.put("quantity", add(BigDecimal.valueOf(Float.valueOf(object[9].toString())),BigDecimal.valueOf(Float.valueOf(object[14].toString()))));
            map.put("uom", object[10]);
            map.put("unitRate", object[11]);
            map.put("unitMrp", object[12]);
            map.put("stockValue", multiply((BigDecimal)map.get("quantity"), (BigDecimal) object[11], DECIMAL_PLACES));
            map.put("averageCost", object[13]);
            map.put("averageStockValue", multiply((BigDecimal)map.get("quantity"), (BigDecimal) object[13], DECIMAL_PLACES));
            map.put("transitQuantity", object[14]);
            map.put("transitValue", multiply(BigDecimal.valueOf(Float.valueOf(object[14].toString())), (BigDecimal) object[11], DECIMAL_PLACES));
            map.put("averageTransitValue", multiply(BigDecimal.valueOf(Float.valueOf(object[14].toString())), (BigDecimal) object[13], DECIMAL_PLACES));
            map.put("consignment", (Boolean.valueOf((Boolean) object[15]) == true) ? "true" : "false");
            map.put("ownerCode", object[16]);

            stockList.add(map);
        }
        return stockList;
    }

    /**
     * Export stocks corresponding to the query.
     *
     * @param unitId,consignment,storeId,itemId
     * @return
     * @throws IOException
     */
    @Override
    public Map<String, String> exportStocks(Long unitId, Boolean consignment, Long storeId, Long itemId) throws IOException {
        Iterator<Map<String, Object>> stockIterator = this.getCurrentAvailableStockExport(unitId, consignment, storeId, itemId).iterator();
        File file = ExportUtil.getCSVExportFile("stock_value_report", applicationProperties.getAthmaBucket().getMasterExport());
        FileWriter stockFileWriter = new FileWriter(file);
        Map<String, String> stockFileDetails = new HashMap<>();
        stockFileDetails.put("fileName", file.getName());
        stockFileDetails.put("pathReference", "masterExport");

        //Header for stock csv file
        final String[] stockFileHeader = {"Store Name", "Item Category", "Item Code", "Item Name", "UOM", "Locator", "Batch No", "Expiry Date", "Quantity",
            "Unit Rate", "Stock Value", "Unit Average Cost", "Average Stock Value", "Unit MRP", "Item Type", "Generic Name", "Transit Quantity", "Transit Value", "Average Transit Value", "Consignment" ,"Owner Code"};

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(stockFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(stockFileHeader);
            while (stockIterator.hasNext()) {
                Map<String, Object> map = stockIterator.next();
                List stockRow = new ArrayList();
                stockRow.add(map.get("store"));
                stockRow.add(map.get("itemCategory"));
                stockRow.add(map.get("itemCode"));
                stockRow.add(map.get("itemName"));
                stockRow.add(map.get("uom"));
                stockRow.add(map.get("locator"));
                stockRow.add(map.get("batchCode"));
                stockRow.add(map.get("expiryDate"));
                stockRow.add(map.get("quantity"));
                stockRow.add(map.get("unitRate"));
                stockRow.add(map.get("stockValue"));
                stockRow.add(map.get("averageCost"));
                stockRow.add(map.get("averageStockValue"));
                stockRow.add(map.get("unitMrp"));
                stockRow.add(map.get("itemType"));
                stockRow.add(map.get("genericName"));
                stockRow.add(map.get("transitQuantity"));
                stockRow.add(map.get("transitValue"));
                stockRow.add(map.get("averageTransitValue"));
                stockRow.add(map.get("consignment"));
                stockRow.add(map.get("ownerCode"));
                csvFilePrinter.printRecord(stockRow);
            }
        }
        return stockFileDetails;
    }

    @Override
    public void updateBarcode(String barcode, String sku) {
        log.debug("Request to update barCode {}", barcode);
        stockRepository.updateStockBarcode(barcode, sku);
    }

    /**
     * Search for the stock corresponding to the query.
     *
     * @param unitId,consignment,storeId,itemId
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> search(Long unitId, Boolean consignment, Long storeId, Long itemId) {
        log.debug("Request to search for available Stocks for given details");
        List<Map<String, Object>> result = getCurrentAvailableStock(unitId, consignment, storeId, itemId);
        return result;
    }


    @Override
    public Float isItemReserved(Long stockId) {
        Float totalQuantity = 0.0f;
        if (!reserveStockRepository.findAllReservedStockByStockId(stockId).isEmpty()) {
            List<ReserveStock> reserveStockList = reserveStockRepository.findAllReservedStockByStockId(stockId);
            for (ReserveStock reserveStock : reserveStockList) {
                totalQuantity += reserveStock.getQuantity();
            }
        }
        return totalQuantity;
    }

    @Override
    public List<String> findBarcodeByItemId(Long itemId) {
        return stockRepository.findBarcodeByItemId(itemId);
    }

    /**
     * Get values to publish stock source update
     *
     * @param stockFlowList
     * @param stockSourceUpdateMap
     * @return stockSourceUpdateMap
     */
    private Map<String, Object> getValuesForStockSourcePublishing(List<StockFlow> stockFlowList, Map<String, Object> stockSourceUpdateMap) {
        final List<TransactionType> transactionTypeList = Arrays.asList(Stock_Consumption, Inventory_Adjustment, Dispense, Dispense_Return);
        Map<String, Float> skuQuantityMap = new HashMap();
        for (StockFlow stockFlow : stockFlowList) {
            if (transactionTypeList.contains(stockFlow.getTransactionType())) {
                stockSourceUpdateMap.put("TransactionNumber", stockFlow.getTransactionNumber());
                stockSourceUpdateMap.put("TransactionDate", stockFlow.getTransactionDate());
                stockSourceUpdateMap.put("FlowType", stockFlow.getFlowType());
                skuQuantityMap.put(stockFlow.getSku(), stockFlow.getQuantity());
            }
        }
        if(skuQuantityMap.isEmpty()){
            return null;// stock source is only for Stock_Consumption, Inventory_Adjustment, Dispense, Dispense_Return, other transactions are ignored
        }
        stockSourceUpdateMap.put("SkuQuantityMap", skuQuantityMap);

        Map<Long, String> stockFlowIdSkuMap = new HashMap<>();
        stockFlowList.stream().forEachOrdered(stockFlow -> {
            stockFlowIdSkuMap.put(stockFlow.getId(), stockFlow.getSku());
            stockSourceUpdateMap.put("stockFlowIdSkuMap", stockFlowIdSkuMap);
        });
        return stockSourceUpdateMap;
    }

    /**
     * Reserves stock for Stock Entry
     *
     * @param stockEntry
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveStockForStockEntry(StockEntry stockEntry) throws StockException {
        log.debug("Request to reserve stock for Stock Entry ");

        if (stockEntry.getStockId() == null) {
            String sku = constructSKU(stockEntry);
            stockEntry.setStockId(stockRepository.findOneByUniqueIdStoreId(sku, stockEntry.getStoreId()).getId());
        }
        reserveStock(stockEntry.getStockId(), stockEntry.getItemId(), stockEntry.getBatchNo(), stockEntry.getStoreId(), stockEntry.getQuantity(),
            stockEntry.getTransactionId(), stockEntry.getTransactionType(), stockEntry.getTransactionNumber(), stockEntry.getTransactionLineId(), stockEntry.getTransactionDate(),stockEntry.getUserId());
    }

    /**
     * create StockSourceHeader for stock entry
     *
     * @param stockEntry
     */
    @Override
    public StockSourceHeader createStockSourceHeader(StockEntry stockEntry){
        return stockSourceHeaderService.save(new StockSourceHeader().unitCode(organizationRepository.findById(stockEntry.getUnitId()).get().getCode()).documentNumber(stockEntry.getTransactionNumber()).transactionDate(stockEntry.getTransactionDate()));
    }

    @Override()
    public void updateStockFields(Long storeId) {
        log.debug("Request to update all Float fields value to BigDecimal value");

        // storeId = 0 for updating all records from table
        if (storeId == 0) {
            long resultCount = stockRepository.count();
            int pageSize = applicationProperties.getConfigs().geIndexPageSize();
            int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
            for (int i = 0; i <= lastPageNumber; i++) {
                List<Stock> data = stockRepository.findAll(Sort.by(Direction.ASC, "id"));
                if (!data.isEmpty()) {
                    data.stream().forEach(stock -> {
                        bigDecimalRoundOff(stock);
                        stock.setSku(constructSku(stock));
                    });
                    stockRepository.saveAll(data);
                }
            }
        } else {
            List<Stock> stockList = stockRepository.findByStoreId(storeId);
            if (!stockList.isEmpty()) {
                stockList.stream().forEach(stock -> {
                    bigDecimalRoundOff(stock);
                    stock.setSku(constructSku(stock));
                });
            }
        }
    }


    /**
     * Get the stock expiry items for given details.
     *
     * @param unitId,,fromDate,toDate,consignment,storeId,itemId
     * @return expiry item details
     */
    @Override
    public List<Map<String, Object>> getExpiryItems(Long unitId, LocalDate fromDate, LocalDate toDate, Long storeId,Boolean consignment) {

        log.debug("REST request to get the current stock expiry items available for given details");

        Map<String, Object> params = new HashMap<>();

        StringBuilder query = new StringBuilder("select hsc.code AS store_code,hsc.name AS store_name,ic.code AS item_category_code,ic.description AS item_category_name,i.item_type ->> 'code' AS item_type_code,i.item_type ->> 'display' AS item_type_name,i.code AS item_code,i.name AS item_name, u.name AS uom,s.batch_no AS batch_code,s.expiry_date,o.code as supplier_code,o.name as supplier_name,i.dispensable_generic_name AS generic_name,s.cost AS unit_rate,s.quantity as actual_quantity,s.transit_quantity as transit_quantity from  Stock AS s INNER JOIN item AS i on s.item_id = i.id INNER JOIN item_category AS ic on i.category_id = ic.id INNER JOIN healthcare_service_center  AS hsc on s.store_id = hsc.id INNER JOIN uom as u on s.uom_id = u.id  LEFT JOIN organization as o on s.supplier||'_V' = o.code where s.unit_id = :unitId ");

        query.append(" and s.expiry_date between :fromDate and :toDate ");

        params.put("unitId", unitId);
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        if (nonNull(consignment)) {
            query.append(" and s.consignment =:consignment");
            params.put("consignment", consignment);
        }

        if (nonNull(storeId)) {
            query.append(" and s.store_id = :storeId");
            params.put("storeId", storeId);
        }

        query.append(" and s.quantity > 0 order by hsc.name, i.name, s.batch_no");

        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        params.forEach((key, value) -> nativeQuery.setParameter(key, value));
        List<Object[]> objects = nativeQuery.getResultList();

        List<Map<String, Object>> expiryItemsList = new ArrayList<>();
        for (Object[] object : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("store", object[1]);
            map.put("itemCategory", object[3]);
            map.put("itemType", object[5]);
            map.put("itemCode", object[6]);
            map.put("itemName", object[7]);
            map.put("uom", object[8]);
            map.put("batchCode", object[9]);
            map.put("expiryDate", object[10]);
            map.put("supplier", object[12]);
            map.put("unitRate", object[14]);
            map.put("actualQuantity", object[15]);
            map.put("transitQuantity", object[16]);

            map.put("quantity", add(BigDecimal.valueOf(Float.valueOf(object[15].toString())),BigDecimal.valueOf(Float.valueOf(object[16].toString()))));

            map.put("itemValue", multiply((BigDecimal)map.get("quantity"), (BigDecimal)map.get("unitRate"), DECIMAL_PLACES));

            LocalDate today = LocalDate.now();
            LocalDate expiryDate = LocalDate.parse(map.get("expiryDate").toString());
            Long range = ChronoUnit.DAYS.between(today,expiryDate);

            map.put("ShelfLifeRemaining", range + " Days");

            expiryItemsList.add(map);
        }

        return expiryItemsList;
    }



    /**
     * Export stocks corresponding to the query.
     *
     * @param unitId,consignment,storeId,itemId
     * @return
     * @throws IOException
     */
    @Override
    public Map<String, String> exportExpiryItems(Long unitId, LocalDate fromDate, LocalDate toDate, Long storeId,Boolean consignment) throws IOException {
        Iterator<Map<String, Object>> expiryItemIterator = this.getExpiryItems(unitId, fromDate, toDate, storeId,consignment).iterator();
        File file = ExportUtil.getCSVExportFile("expiry_item_report", applicationProperties.getAthmaBucket().getMasterExport());
        FileWriter expiryFileWriter = new FileWriter(file);
        Map<String, String> expiryFileDetails = new HashMap<>();
        expiryFileDetails.put("fileName", file.getName());
        expiryFileDetails.put("pathReference", "masterExport");

        //Header for expiry item csv file
        final String[] expiryFileHeader = {"Store Name", "Item Category", "Item Type", "Item Code", "Item Name", "Base UOM", "Batch No", "Expiry Date", "Supplier",
            "Shelf life remaining (Days)", "Rate (Cost Value)", "Qty (Inclusive of Transit Qty)", "Transit Qty", "Item Value"};

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(expiryFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(expiryFileHeader);
            while (expiryItemIterator.hasNext()) {
                Map<String, Object> map = expiryItemIterator.next();
                List stockRow = new ArrayList();

                stockRow.add(map.get("store"));
                stockRow.add(map.get("itemCategory"));
                stockRow.add(map.get("itemType"));
                stockRow.add(map.get("itemCode"));
                stockRow.add(map.get("itemName"));
                stockRow.add(map.get("uom"));
                stockRow.add(map.get("batchCode"));
                stockRow.add(map.get("expiryDate"));
                stockRow.add(map.get("supplier"));
                stockRow.add(map.get("ShelfLifeRemaining"));
                stockRow.add(map.get("unitRate"));
                stockRow.add(map.get("quantity"));
                stockRow.add(map.get("transitQuantity"));
                stockRow.add(map.get("itemValue"));

                csvFilePrinter.printRecord(stockRow);
            }
        }
        return expiryFileDetails;
    }

    @Override
    public List<Stock> getStock(Long itemId, String batchNo, Long storeId) {
        return stockRepository.getStockWithFields(itemId, batchNo, storeId);
    }

    @Override
    public Stock getStockByUniqueIdAndStoreId(String uniqueId, Long storeId) {
        return stockRepository.findOneByUniqueIdStoreId(uniqueId, storeId);
    }

    @Override
    public Stock getStockWithLock(Long stockId) {
        return stockRepository.getStockWithLock(stockId);
    }

    @Override
    public List<Stock> getStockWithFields(Long itemId, String batchNo, Long storeId) {
        return stockRepository.getStockWithFields(itemId, batchNo, storeId);
    }

    private void bigDecimalRoundOff(Stock stock) {
        BigDecimal cost = BigDecimalUtil.roundOff(stock.getCost(), DECIMAL_PLACES);
        stock.setCost(cost);
        stock.setItemUnitAvgCost(BigDecimalUtil.roundOff(stock.getItemUnitAvgCost(), DECIMAL_PLACES));
        stock.setMrp((BigDecimalUtil.roundOff(stock.getMrp(), DECIMAL_PLACES)));
        stock.setStockValue((BigDecimalUtil.roundOff(stock.getStockValue(), DECIMAL_PLACES)));
        stock.setOriginalMRP((BigDecimalUtil.roundOff(stock.getOriginalMRP(), DECIMAL_PLACES)));
    }

    private String constructSku(Stock stock) {
        Object[] uniqueIdentifiers = {stock.getOwner(), stock.getItemId(), stock.getBatchNo(), stock.getExpiryDate(), stock.getCost(), stock.getMrp(), stock.isConsignment()};
        return StringUtils.join(uniqueIdentifiers, "~");
    }
}
