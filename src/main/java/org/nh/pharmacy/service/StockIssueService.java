package org.nh.pharmacy.service;

import java.io.IOException;
import java.time.LocalDate;

import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.dto.IssueDocumentLine;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.exception.StockException;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing StockIssue.
 */
public interface StockIssueService {

    /**
     * Save a stockIssue.
     *
     * @param stockIssue the entity to save
     * @return the persisted entity
     */
    StockIssue save(StockIssue stockIssue) throws SequenceGenerateException;

    /**
     * Get all the stockIssues.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockIssue> findAll(Pageable pageable);

    /**
     * Get the "documentId" stockIssue.
     *
     * @param documentId the id of the entity
     * @return the entity
     */
    StockIssue findOne(DocumentId documentId);

    /**
     * Get the "id" stockIssue.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockIssue findOne(Long id);

    StockIssue findOneWithLock(Long id);

    /**
     * Delete the "documentId" stockIssue.
     *
     * @param documentId the id of the entity
     */
    void delete(DocumentId documentId);

    /**
     * Delete the "id" stockIssue.
     *
     * @param id the id of the entity
     */
    void delete(Long id) throws BusinessRuleViolationException;

    /**
     * Search for the stockIssue corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockIssue> search(String query, Pageable pageable);

    /**
     * Search for the stockIssue corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    Page<StockIssue> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * Send Issue Document for Approval
     *
     * @param stockIssue
     */
    public StockIssue sendForApproval(StockIssue stockIssue, String action) throws Exception;

    /**
     * StockIssue publisher
     */
    void produce(StockIssue stockIssue);

    /**
     * To approve Issue documnet
     *
     * @param stockIssue
     * @return
     */
    public StockIssue approveIssueDocument(StockIssue stockIssue) throws Exception;

    /**
     * To Reject Issue document
     *
     * @param stockIssue
     * @return
     */
    public StockIssue rejectIssueDocument(StockIssue stockIssue) throws SequenceGenerateException;


    /**
     * @param stockIssue
     * @param act
     * @return
     * @throws StockException
     */
    public StockIssue save(StockIssue stockIssue, String act) throws Exception;

    /**
     * @param Id
     * @return
     */
    public StockIssue findDetachedOne(Long Id);

    /**
     * To convert Stock Indent document to Issue Document
     *
     * @param docId (Document Id)
     * @param docNo (Dcoument Number)
     * @return
     */
    StockIssue convertIndentToIssue(Long docId, String docNo);

    /**
     * Search for the stockIssue to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Workflow
     *
     * @param stockIssue the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return stockIssue object
     * @throws Exception
     */
    StockIssue executeWorkflow(StockIssue stockIssue, String transition, Long taskId) throws Exception;

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
     * Delete all elastic index of StockIssue
     */
    void deleteIndex();

    /**
     * Do elastic index for StockIssue
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * Get stockIssueLineItem
     *
     * @param storeId
     * @param itemId
     * @return issueDocumentLine
     */
    IssueDocumentLine getStockIssueLineItem(Long storeId, Long itemId);

    void index(StockIssue stockIssue);

    Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException;

    /**
     * reindex StockIssue elasticsearch for given id
     *
     * @param id
     */
    void reIndex(Long id);

    void checkForIndentConversionCompletion(StockIssue stockIssue);

    /**
     * Save stockIssue document. Called from all services for which issue is source document
     *
     * @param stockIssue
     * @return the persisted entity
     */
    StockIssue updateSourceDocumentOnDestinationModification(StockIssue stockIssue);


    /**
     * print document
     *
     * @param issueId
     * @param issueNumber
     * @return
     * @throws Exception
     */
    public Map<String, Object> getStockIssueHTML(Long issueId, String issueNumber,String documentType) throws Exception;

    /**
     * @param issueId
     * @param documentNumber
     * @param original
     * @return
     */
    byte[] getStockIssuePDF(Long issueId, String documentNumber, String original,String documentType) throws Exception;

    byte[] getDirectTransferPDF(StockIssue stockIssue) throws Exception;

    public Map<String, Object> getDirectTransferHTML(StockIssue stockIssue) throws Exception;

    void regenerateWorkflow(String documentNumber);
}
