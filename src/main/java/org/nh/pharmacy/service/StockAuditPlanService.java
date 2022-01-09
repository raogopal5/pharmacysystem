package org.nh.pharmacy.service;

import java.time.LocalDate;

import org.nh.pharmacy.domain.StockAuditPlan;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing StockAuditPlan.
 */
public interface StockAuditPlanService {

    /**
     * Save a stockAuditPlan.
     *
     * @param stockAuditPlan the entity to save
     * @return the persisted entity
     * @throws SequenceGenerateException
     */
    StockAuditPlan save(StockAuditPlan stockAuditPlan) throws SequenceGenerateException;

    /**
     * Save a stockAuditPlan.
     *
     * @param stockAuditPlan the entity to save
     * @param action         to be performed
     * @return the persisted entity
     * @throws SequenceGenerateException
     */
    StockAuditPlan save(StockAuditPlan stockAuditPlan, String action) throws SequenceGenerateException;

    /**
     * Get all the stockAuditPlans.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockAuditPlan> findAll(Pageable pageable);

    /**
     * Get the "id" stockAuditPlan.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockAuditPlan findOne(Long id);

    /**
     * Get one stockAuditPlan by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    StockAuditPlan findOne(Long id, Integer version);

    /**
     * Delete the "id" stockAuditPlan.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Delete the stockAuditPlan by id,version.
     *
     * @param id,version the id of the entity
     */
    void delete(Long id, Integer version);

    /**
     * Search for the stockAuditPlan corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockAuditPlan> search(String query, Pageable pageable);

    /**
     * Do elastic index for StockAuditPlan
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);


    /**
     * Delete all elastic index of StockAuditPlan
     */
    void deleteIndex();
}
