package org.nh.pharmacy.service;

import java.time.LocalDate;

import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.domain.StockWrapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing StockSource.
 */
public interface StockSourceService {

    /**
     * Save a stockSource.
     *
     * @param stockSource the entity to save
     * @return the persisted entity
     */
    StockSource save(StockSource stockSource);

    /**
     * Get all the stockSources.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockSource> findAll(Pageable pageable);

    /**
     * Get the "id" stockSource.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockSource findOne(Long id);

    /**
     * Delete the "id" stockSource.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * @param stockSource
     * @return
     */
    StockSource generateBarcode(StockSource stockSource) throws Exception;

    /**
     * @param transactionRefNo
     * @param fromDate
     * @param toDate
     * @param itemId
     * @return
     */
    Page<StockSource> getAllStockSource(String transactionRefNo, String fromDate, String toDate, Long itemId, Pageable pageable);

    /**
     * @param stockSourceId
     * @param unitId
     * @return
     */
    Map<String, String> findBarcodeFormat(Long stockSourceId, Long unitId);

    /**
     * Update stock source for stock out
     *
     * @param skuQuantityMap
     * @param transactionDate
     */
    void reduceStockSourceQuantity(Map<String, Double> skuQuantityMap, LocalDate transactionDate);

    /**
     * Update stock source for stock in
     *
     * @param skuQuantityMap
     * @param transactionDate
     */
    void increaseStockSourceQuantity(Map<String, Double> skuQuantityMap, LocalDate transactionDate);

    /**
     * Insert stock flow and stock source Ids in intermediate table
     *
     * @param skuStockFlowIdMap
     */
    void saveStockFlowStockSourceIds(Map<String, String> skuStockFlowIdMap);

    /**
     * Search for the stocksources to get stocksources corresponding to the transactionRefNo and unitCode.
     *
     * @param transactionRefNo
     * @param unitCode
     */
     List<StockSource> getStockSourceByTransactionRefNo(String transactionRefNo, String unitCode, Pageable pageable);

    /**
     * @param stockSources
     * @return
     */
    List<StockSource> generateBarcodes(List<StockSource> stockSources) throws Exception;

    /**
     * @param stockSources
     * @param unitId
     * @return
     */
    List<Map<String, String>> findBarcodeFormats(List<StockSource> stockSources, Long unitId);

    /**
     * Save a stockSources.
     *
     * @param stockSources the entities to save
     * @return the persisted entities
     */
    List<StockSource> save(List<StockSource> stockSources);

    /**
     * @param sku load stock source based on sku
     * @return stock wrapper
     */
    StockSource getStockSource(String sku);

    /**
     * @param sku load stock source based on sku, quantity>0 and order by first stock in date
     * @return stock wrapper
     */
    StockSource findBySkuIdByQuantityAndOrderByFirstStockInDate(String sku);

    StockSource findByOwnerItemIdBatchNoAndExpiry(String owner, Long itemId, String batchNo, LocalDate expiryDate);
}
