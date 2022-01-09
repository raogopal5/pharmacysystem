package org.nh.pharmacy.service;

import java.time.LocalDate;

import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.dto.AuditDocumentLine;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.exception.StockException;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing StockAudit.
 */
public interface StockAuditService {

    /**
     * Save a stockAudit.
     *
     * @param stockAudit the entity to save
     * @return the persisted entity
     * @throws SequenceGenerateException
     */
    StockAudit save(StockAudit stockAudit) throws SequenceGenerateException;

    /**
     * @param stockAudit
     * @param action
     * @return the persisted entity
     * @throws Exception
     */
    StockAudit save(StockAudit stockAudit, String action) throws Exception;

    /**
     * Change document status
     *
     * @param documentNumber
     */
    void changeDocumentStatus(String documentNumber);

    /**
     * Get all the stockAudits.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockAudit> findAll(Pageable pageable);

    /**
     * Get the "id" stockAudit.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockAudit findOne(Long id);

    /**
     * Get the "id" detached stockAudit.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockAudit findDetachedOne(Long id);

    /**
     * Get one stockAudit by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    StockAudit findOne(Long id, Integer version);

    /**
     * Delete the "id" stockAudit.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Delete the stockAudit by id,version.
     *
     * @param id,version the id of the entity
     */
    void delete(Long id, Integer version);

    /**
     * Search for the stockAudit corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockAudit> search(String query, Pageable pageable);

    /**
     * @param query
     * @param pageable
     * @param includeFields
     * @param excludeFields
     * @return
     */
    Page<StockAudit> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * Search for the Stock Audit to get status count corresponding to the query.
     *
     * @param query
     * @return
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Get all stock details for given item ids and store id
     *
     * @param itemIds
     * @param storeId
     * @return
     */
    List<AuditDocumentLine> getAllDocumentLines(Long itemIds, Long storeId) throws StockException;

    /**
     * Delete all elastic index of Stock Audit
     */
    void deleteIndex();

    /**
     * Do elastic index for Stock Audit
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * Execute workflow
     *
     * @param stockAudit the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return stockAudit object
     * @throws Exception
     */
    StockAudit executeWorkflow(StockAudit stockAudit, String transition, Long taskId) throws Exception;

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
     * Do elastic index for Stock Audit
     */
    void index(StockAudit stockAudit);

    /**
     * Reindex audit elasticsearch for given id
     *
     * @param id
     */
    void reIndex(Long id);

    void generateExcel(String documentNumber, File file) throws IOException;

    StockAudit createStockAuditForCriteria(StockAudit stockAudit) throws Exception;

    /**
     * Import audit excel. Varifies and saves it
     *
     * @param file
     * @param docId
     * @return
     * @throws Exception
     */
    StockAudit uploadAuditExcel(MultipartFile file, Long docId) throws Exception;

    /**
     * Adds new itemlines to the document
     *
     * @param itemCode
     * @param batchNo
     * @param stockAudit
     * @return
     */
    StockAudit addItemLines(String itemCode, String batchNo, StockAudit stockAudit);

    /**
     * Get list of Stock Audits csv file
     *
     * @param file
     * @param query
     * @param pageable
     * @throws IOException
     */
    void generateStockAuditList(File file, String query, Pageable pageable) throws IOException;

    /**
     * Adds stock items from excel
     *
     * @param file
     * @param storeId
     * @param userId
     * @return
     * @throws IOException
     */
    Map<String, Object> addAuditLinesFromExcel(MultipartFile file, Long storeId, Long userId) throws IOException;

    Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber);
}
