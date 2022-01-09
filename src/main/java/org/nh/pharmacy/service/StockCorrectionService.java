package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.StockCorrection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service Interface for managing StockCorrection.
 */
public interface StockCorrectionService {

    /**
     * Save a stockCorrection.
     *
     * @param stockCorrection the entity to save
     * @return the persisted entity
     */
    StockCorrection save(StockCorrection stockCorrection);


    /**
     * Save StockCorrection Based on Action
     *
     * @param stockCorrection
     * @param action
     * @return
     * @throws Exception
     */
    StockCorrection save(StockCorrection stockCorrection, String action, Boolean validationRequired) throws Exception;

    /**
     * Get all the stockCorrections.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockCorrection> findAll(Pageable pageable);

    /**
     * Get the "id" stockCorrection.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockCorrection findOne(Long id);

    /**
     * Get a stateless StockCorrection
     *
     * @param id
     * @return
     */
    public StockCorrection findDetachedOne(Long id);

    /**
     * Delete the "id" stockCorrection.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the stockCorrection corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockCorrection> search(String query, Pageable pageable);

    /**
     * Search for the stockCorrection corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    Page<StockCorrection> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * @param stockCorrection
     */
    public void produce(StockCorrection stockCorrection);

    /**
     * Search for the Stock correction to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Workflow
     *
     * @param stockCorrection the entity to save
     * @param transition      to be performed
     * @param taskId          task Id
     * @return stockCorrection object
     * @throws Exception
     */
    StockCorrection executeWorkflow(StockCorrection stockCorrection, String transition, Long taskId) throws Exception;

    /**
     * Get task constraints
     *
     * @param documentNumber
     * @param userId
     * @param taskId
     * @return taskId, constraints
     */
    Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId);

    /**
     * Do elastic index for StockCorrection
     *
     * @param stockCorrection
     */
    void index(StockCorrection stockCorrection);

    /**
     * Delete all elastic index of Stock Correction
     */
    void deleteIndex();

    /**
     * Do elastic index for Stock Correction
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * @param id
     */
    void reIndex(Long id);

    /**
     * Checks weather the items for correction are reserved
     *
     * @param stockCorrection
     */
    void verifyStockReserve(StockCorrection stockCorrection);

    void regenerateWorkflow(String documentNumber);
}
