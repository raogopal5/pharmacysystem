package org.nh.pharmacy.service;

import java.time.LocalDate;

import org.nh.pharmacy.domain.StockSourceHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing StockSourceHeader.
 */
public interface StockSourceHeaderService {

    /**
     * Save a stockSourceHeader.
     *
     * @param stockSourceHeader the entity to save
     * @return the persisted entity
     */
    StockSourceHeader save(StockSourceHeader stockSourceHeader);

    /**
     * Get all the stockSourceHeaders.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockSourceHeader> findAll(Pageable pageable);

    /**
     * Get the "id" stockSourceHeader.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockSourceHeader findOne(Long id);

    /**
     * Delete the "id" stockSourceHeader.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the stockSourceHeader corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockSourceHeader> search(String query, Pageable pageable);

    List<Map<String, Object>> getStockSourceHeaderByDocumentNo(String documentNo, String unitCode, String fromDate, String toDate, Long itemid, Integer size, Integer pageNumber);

    void deleteIndex();

    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);
}
