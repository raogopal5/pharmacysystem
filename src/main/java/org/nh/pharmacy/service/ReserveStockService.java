package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ReserveStock;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing ReserveStock.
 */
public interface ReserveStockService {

    /**
     * Save a reserveStock.
     *
     * @param reserveStock the entity to save
     * @return the persisted entity
     */
    ReserveStock save(ReserveStock reserveStock);

    /**
     * Get all the reserveStocks.
     *
     * @return the list of entities
     */
    List<ReserveStock> findAll();

    /**
     * Get the "id" reserveStock.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ReserveStock findOne(Long id);

    /**
     * Delete the "id" reserveStock.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    List<ReserveStock> findAllReservedStockByStockId(Long stockId);

    Map<String, String> exportReserveStocks(List<String> unitCodes, String storeCode, LocalDate fromDate, LocalDate toDate) throws Exception;

    List<Map<String, Object>> getReserveStockReport(List<String> unitCodes, String storeCode, LocalDate fromDate, LocalDate toDate)throws Exception;
}
