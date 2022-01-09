package org.nh.pharmacy.service;

import java.io.IOException;
import java.time.LocalDate;

import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing StockReceipt.
 */
public interface StockReceiptService {

    /**
     * Save a stockReceipt.
     *
     * @param stockReceipt the entity to save
     * @return the persisted entity
     */
    StockReceipt save(StockReceipt stockReceipt) throws SequenceGenerateException;

    /**
     * Save a stockReceipt.
     *
     * @param stockReceipt the entity to save
     * @param action       to perform
     * @return the persisted entity
     */
    StockReceipt save(StockReceipt stockReceipt, String action) throws Exception;

    /**
     * Get all the stockReceipts.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockReceipt> findAll(Pageable pageable);

    /**
     * Get the "id" stockReceipt.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockReceipt findOne(Long id);

    /**
     * Get the "documentId" StockReceipt.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    StockReceipt findOne(Long id, Integer version);

    /**
     * Delete the "id" stockReceipt.
     *
     * @param id the id of the entity
     */
    void delete(Long id) throws BusinessRuleViolationException, BusinessRuleViolationException;

    /**
     * Delete the "documentId" StockReceipt.
     *
     * @param id,version the id of the entity
     */
    void delete(Long id, Integer version);

    /**
     * Search for the stockReceipt corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockReceipt> search(String query, Pageable pageable);

    /**
     * Search for the stockReceipt corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    Page<StockReceipt> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);


    /**
     * Conversion of Stock Issue Document to Stock Receipt Document
     *
     * @param docId (Stock Issue Id)
     * @param docNo (Stock Issue Document Number)
     * @return
     */
    public StockReceipt convertIssueToReceipt(Long docId, String docNo);

    /**
     * Conversion of Stock Reversal Document to Stock Receipt Document
     *
     * @param docId (Stock Reversal Id)
     * @param docNo (Stock Reversal Document Number)
     * @return
     */
    public StockReceipt convertReversalToReceipt(Long docId, String docNo);

    /**
     * Search for the stockReceipt to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Workflow
     *
     * @param stockReceipt the entity to save
     * @param transition   to be performed
     * @param taskId       task Id
     * @return stockReceipt object
     * @throws Exception
     */
    StockReceipt executeWorkflow(StockReceipt stockReceipt, String transition, Long taskId) throws Exception;

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
     * Delete all elastic index of Stock Receipt
     */
    void deleteIndex();

    /**
     * Do elastic index for Stock Receipt
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    void index(StockReceipt stockReceipt);

    Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException;

    /**
     * reindex StockReceipt elasticsearch for given id
     *
     * @param id
     */
    void reIndex(Long id);

    void checkForCompleteConversionOfSourceDocument(StockReceipt stockReceipt, String action) throws SequenceGenerateException;

    Map<String, Object> getStockRecieptHTMLByReceiptId(Long receiptId, String receiptNumber)throws Exception;

    byte[] getStockReceiptPdfByReceiptId(Long receiptId, String documentNumber, String original)throws Exception;

    void regenerateWorkflow(String documentNumber);
}
