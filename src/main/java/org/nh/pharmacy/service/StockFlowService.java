package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.StockFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing StockFlow.
 */
public interface StockFlowService {

    /**
     * Save a stockFlow.
     *
     * @param stockFlow the entity to save
     * @return the persisted entity
     */
    StockFlow save(StockFlow stockFlow);

    /**
     * Get all the stockFlows.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockFlow> findAll(Pageable pageable);

    /**
     * Get the "id" stockFlow.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockFlow findOne(Long id);

    /**
     * Delete the "id" stockFlow.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Get all the stock transactions (IN & OUT) for a unit or a store.
     *
     * @param entryDate
     * @param consignment
     * @param unitId
     * @param storeId
     * @param itemId
     * @return
     */
    List<Map<String, Object>> getStockTransactions(LocalDate entryDate, Boolean consignment, Long unitId, Long storeId, Long itemId);

    /**
     * Export stock-flow corresponding for the given details.
     *
     * @param entryDate,consignment,unitId,storeId,itemId
     * @return
     */
    Map<String, String> exportStockFlowTransactions(LocalDate entryDate, Boolean consignment, Long unitId, Long storeId, Long itemId) throws IOException;
}
