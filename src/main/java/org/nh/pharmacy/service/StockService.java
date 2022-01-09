package org.nh.pharmacy.service;

import java.time.LocalDate;

import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.StockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing Stock.
 */
public interface StockService {

    /**
     * Save a stock.
     *
     * @param stock the entity to save
     * @return the persisted entity
     */
    Stock save(Stock stock);

    /**
     * Get all the stocks.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Stock> findAll(Pageable pageable);

    /**
     * Get the "id" stock.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Stock findOne(Long id);

    /**
     * Delete the "id" stock.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Reserves stock for out transaction
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @param requestedQuantity
     * @param transactionId
     * @param transactionType
     * @param transactionNo
     */
    void reserveStock(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity,
                      Long transactionId, TransactionType transactionType, String transactionNo, Long transactionLineId, LocalDateTime transactionDate,Long userId) throws StockException;

    /**
     * Reserves stock for out transaction
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @param requestedQuantity
     * @param transactionId
     * @param transactionType
     * @param transactionNo
     */
    void reserveStockInSameTransaction(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity,
                                       Long transactionId, TransactionType transactionType, String transactionNo, Long transactionLineId, LocalDateTime transactionDate,Long userId) throws StockException;

    /**
     * Deletes reserved records based on transaction ID and transaction Type
     *
     * @param transactionId
     * @param transactionType
     */
    void deleteReservedStock(Long transactionId, TransactionType transactionType);

    /**
     * Deletes reserved stock by transaction id, transaction type, stock id
     *
     * @param transactionId
     * @param transactionType
     * @param stockId
     */
    void deleteReservedStockByStockId(Long transactionId, TransactionType transactionType, Long stockId);

    /**
     * Deletes reserved records based on transaction ID, transaction Type, stock Id and reserve again
     *
     * @param transactionId
     * @param transactionType
     */
    void deleteAndReserveStock(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity,
                               Long transactionId, TransactionType transactionType, String transactionNo, Long transactionLineId, LocalDateTime transactionDate,Long userId) throws StockException;

    /**
     * Get Stocks available using id
     *
     * @param stockId
     * @return
     */
    Float getAvailableStock(Long stockId);

    /**
     * Get Stocks available based on item, batch and store
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @return
     */
    Float getAvailableStock(Long itemId, String batchNo, Long storeId) throws StockException;

    /**
     * Perform stock in operations
     *
     * @param stockEntryList
     * @return stockFlowList
     */
    List<StockFlow> stockIn(List<StockEntry> stockEntryList);

    /**
     * Perform stock out operations
     *
     * @param transactionNumber
     * @return stockFlowList
     */
    List<StockFlow> stockOut(String transactionNumber);

    @Transactional
    @PublishStockTransaction
    List<StockTransit> moveStockToTransit(String transactionNumber,String flowType);

    List<StockFlow> moveFromTransitToStock(List<StockEntry> stockEntries, List<StockTransit> stockTransitList);

    /**
     * Get the batch details of selected itemCode on given storeId
     *
     * @param storeId,itemCode
     * @param docNumber
     * @return the list of entities
     */
    Page<Stock> getBatchDetails(Long storeId, String itemCode, String batchNo, String docNumber, Boolean filterBlockedBatch, Pageable pageable);

    /**
     * Get all batches based on store, item code, filterQuantity
     *
     * @param storeId
     * @param itemCode
     * @param filterQuantity
     * @param pageable
     * @return
     */
    Page<Stock> getAllBatchDetails(Long storeId, String itemCode, Boolean filterQuantity, Boolean removeBlockedBatch, Pageable pageable);

    /**
     * Get the items list based on the dispensableGenericName for given itemId and storeId.
     *
     * @param storeId,itemId
     * @return the list of entities
     */
    Page<Map<String, Object>> getItemListByDispensableGenericName(Long storeId, Long itemId, Pageable pageable);

    /**
     * Performs StockIn with New Transaction
     *
     * @param stockEntryList
     * @return
     */
    List<StockFlow> stockInWithTxn(List<StockEntry> stockEntryList);

    /**
     * Process Stock Move
     *
     * @param stockEntryList
     */
    void processStockMove(Map<String, Object> stockEntryList) throws IOException;

    /**
     * Find Reserve Stock by transaction number
     *
     * @param transactionNo
     * @return
     */
    List<ReserveStock> findReserveStockByTransactionNo(String transactionNo);

    /**
     * Deletes reserve stocks for list of id's
     *
     * @param ids
     */
    void deleteReservedStocks(List<Long> ids);

    /**
     * Get the current stock available for given item on given unit or given store.
     *
     * @param unitId
     * @param consignment
     * @param storeId
     * @param itemId
     * @return
     */
    List<Map<String, Object>> getCurrentAvailableStock(Long unitId, Boolean consignment, Long storeId, Long itemId);

    /**
     * Search for the stock corresponding to the query.
     *
     * @param unitId,consignment,storeId,itemId
     * @return the list of entities
     */
    List<Map<String, Object>> search(Long unitId, Boolean consignment, Long storeId, Long itemId);

    /**
     * Export stocks corresponding to the query.
     *
     * @param unitId,consignment,storeId,itemId
     * @return
     */
    Map<String, String> exportStocks(Long unitId, Boolean consignment, Long storeId, Long itemId) throws IOException;

    /**
     * @param barcode
     * @param sku
     */
    void updateBarcode(String barcode, String sku);

    /**
     * Checks weather a item is reserved
     *
     * @param stockId
     * @return
     */
    Float isItemReserved(Long stockId);

    /**
     * find barcode by given itemId
     *
     * @param itemId
     * @return
     */
    List<String> findBarcodeByItemId(Long itemId);

    /**
     * Reserves stock for stock entry
     *
     * @param stockEntry
     */
    void reserveStockForStockEntry(StockEntry stockEntry) throws StockException;

    /**
     * create StockSourceHeader for stock entry
     *
     * @param stockEntry
     */
    StockSourceHeader createStockSourceHeader(StockEntry stockEntry);


    /**
     * Update Stock with field having float value to bigdecimal value and sku
     */
    void updateStockFields(Long storeId);



    /**
     * Get the current expiry items for given unit and given from date and given to date or given store or given item.
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @param storeId
     * @param consignment
     * @return
     */
    List<Map<String, Object>> getExpiryItems(Long unitId, LocalDate fromDate, LocalDate toDate, Long storeId, Boolean consignment);

    /**
     * Export Expiry Items corresponding to the query.
     *
     * @param unitId,fromDate,toDate,storeId,consignment
     * @return
     */
    Map<String, String> exportExpiryItems(Long unitId, LocalDate fromDate, LocalDate toDate, Long storeId,Boolean consignment) throws IOException;

    /**
     *
     * @param itemId
     * @param batchNo
     * @param storeId
     * @return
     */
    public List<Stock> getStock(Long itemId, String batchNo, Long storeId);

    /**
     *
     * @param uniqueId
     * @param storeId
     * @return
     */
    public Stock getStockByUniqueIdAndStoreId(String uniqueId, Long storeId);

    public Stock getStockWithLock(Long stockId);

    List<Stock> getStockWithFields(Long itemId, String batchNo, Long storeId);
}
