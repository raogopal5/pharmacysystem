package org.nh.pharmacy.service;

import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.domain.StockConsumption;
import org.nh.pharmacy.domain.dto.ConsumptionDocumentLine;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing StockConsumption.
 */
public interface StockConsumptionService {

    /**
     * Save a stockConsumption.
     *
     * @param stockConsumption the entity to save
     * @return the persisted entity
     */
    StockConsumption save(StockConsumption stockConsumption) throws SequenceGenerateException;

    /**
     * @param stockConsumption
     * @param action
     * @return
     * @throws Exception
     */
    StockConsumption save(StockConsumption stockConsumption, String action) throws Exception;

    /**
     * Get all the stockConsumptions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockConsumption> findAll(Pageable pageable);

    /**
     * Get the "id" stockConsumption.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockConsumption findOne(Long id);

    /**
     * Get one stockConsumption by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    StockConsumption findOne(Long id, Integer version);

    /**
     * Delete the "id" stockConsumption.
     *
     * @param id the id of the entity
     */
    void delete(Long id) throws BusinessRuleViolationException;

    /**
     * Delete the stockConsumption by id,version.
     *
     * @param id,version the id of the entity
     */
    void delete(Long id, Integer version);

    /**
     * Search for the stockConsumption corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockConsumption> search(String query, Pageable pageable);

    /**
     * Search for the stockConsumption corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    Page<StockConsumption> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * Send consumption document for approval
     *
     * @param stockConsumption
     * @param action
     * @return the entity
     */
    StockConsumption sendForApproval(StockConsumption stockConsumption, String action) throws Exception;

    /**
     * StockConsumption publisher
     */
    void produce(StockConsumption stockConsumption);

    /**
     * Approve consumption document
     *
     * @param stockConsumption
     * @return the entity
     */
    StockConsumption approveConsumptionDocument(StockConsumption stockConsumption) throws Exception;

    /**
     * Reject consumption document
     *
     * @param stockConsumption
     * @return the entity
     */
    StockConsumption rejectConsumptionDocument(StockConsumption stockConsumption) throws Exception;

    /**
     * Search for the StockConsumption to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Workflow
     *
     * @param stockConsumption the entity to save
     * @param transition       to be performed
     * @param taskId           task Id
     * @return stockConsumption object
     * @throws Exception
     */
    StockConsumption executeWorkflow(StockConsumption stockConsumption, String transition, Long taskId) throws Exception;

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
     * Delete all elastic index of Stock Consumption
     */
    void deleteIndex();

    /**
     * Do elastic index for Stock Consumption
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    void index(StockConsumption stockConsumption);

    byte[] getStockConsumptionPDF(StockConsumption stockConsumption) throws Exception;

    Map<String, Object> getStockConsumptionHTML(StockConsumption stockIssue) throws Exception;

    /**
     * @param params
     * @return
     */
    StockConsumption stockAutoConsumption(Map<String, Object> params) throws Exception;

    List<ConsumptionDocumentLine> getConsumptionReversalItems(Long stockId, Long consumptionHscId, Long forHscId, Long departmentId, String mrn) throws Exception;

    void consumeExternalStockConsumption(StockConsumption stockConsumption)throws Exception;
}
