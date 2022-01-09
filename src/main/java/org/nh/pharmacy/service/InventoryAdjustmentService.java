package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.InventoryAdjustment;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing InventoryAdjustment.
 */
public interface InventoryAdjustmentService {

    /**
     * Save a inventoryAdjustment.
     *
     * @param inventoryAdjustment the entity to save
     * @return the persisted entity
     */
    InventoryAdjustment save(InventoryAdjustment inventoryAdjustment);

    /**
     * Save Inventory Adjustment on the basis of action
     *
     * @param inventoryAdjustment
     * @param action
     * @return
     */
    InventoryAdjustment save(InventoryAdjustment inventoryAdjustment, String action) throws Exception;

    /**
     * Get all the inventoryAdjustments.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<InventoryAdjustment> findAll(Pageable pageable);

    /**
     * Get the "id" inventoryAdjustment.
     *
     * @param id the id of the entity
     * @return the entity
     */
    InventoryAdjustment findOne(Long id);

    /**
     * Find Inventory Adjustment with detached state
     *
     * @param id
     * @return
     */
    public InventoryAdjustment findDetachedOne(Long id);

    /**
     * Delete the "id" inventoryAdjustment.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the inventoryAdjustment corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<InventoryAdjustment> search(String query, Pageable pageable);

    /**
     * @param query
     * @param pageable
     * @param includeFields
     * @param excludeFields
     * @return
     */
    Page<InventoryAdjustment> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * Send for Approval
     *
     * @param inventoryAdjustment
     * @param action
     * @return
     * @throws Exception
     */
    public InventoryAdjustment sendForApproval(InventoryAdjustment inventoryAdjustment, String action) throws Exception;

    /**
     * Approve InventoryAdjustment
     *
     * @param inventoryAdjustment
     * @return
     */
    public InventoryAdjustment approveInventoryAdjustmentDocument(InventoryAdjustment inventoryAdjustment) throws Exception;

    /**
     * Reject Inventory Adjustment Document
     *
     * @param inventoryAdjustment
     * @return
     */
    public InventoryAdjustment rejectInventoryAdjustmentDocument(InventoryAdjustment inventoryAdjustment);

    /**
     * Performs Stock Correction
     *
     * @param inventoryAdjustment
     * @return
     * @throws Exception
     */
    public InventoryAdjustment doStockCorrection(InventoryAdjustment inventoryAdjustment) throws Exception;

    /**
     * Kafka implementation for producing inventory Adjustment
     *
     * @param inventoryAdjustment
     */
    public void produce(InventoryAdjustment inventoryAdjustment);

    /**
     * Search for the Inventory Adjustment to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Delete all elastic index of Inventory Adjustment
     */
    void deleteIndex();

    /**
     * Do elastic index for Inventory Adjustment
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * Execute workflow
     *
     * @param inventoryAdjustment the entity to save
     * @param transition          to be performed
     * @param taskId              task Id
     * @return inventoryAdjustment object
     * @throws Exception
     */
    InventoryAdjustment executeWorkflow(InventoryAdjustment inventoryAdjustment, String transition, Long taskId) throws Exception;

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
     * Do elastic index for Inventory Adjustment
     */
    void index(InventoryAdjustment inventoryAdjustment);

    /**
     * Reindex adjustment elasticsearch for given id
     *
     * @param id
     */
    void reIndex(Long id);

    /**
     * Reverse adjustment elasticsearch data for audit
     *
     * @param auditDocumentNumber
     */
    void reverseAdjustmentDataForAudit(String auditDocumentNumber);

    Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber);

    /**
     * Get adjustment list in csv file
     *
     * @param file
     * @param query
     * @param pageable
     * @throws IOException
     */
    void generateInventoryAdjustmentList(File file, String query, Pageable pageable) throws IOException;

    void regenerateWorkflow(String documentNumber);
}
