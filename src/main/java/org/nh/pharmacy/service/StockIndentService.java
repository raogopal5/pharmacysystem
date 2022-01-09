package org.nh.pharmacy.service;

import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.domain.StockIndent;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing StockIndent.
 */
public interface StockIndentService {

    /**
     * Save a stockIndent.
     *
     * @param stockIndent the entity to save
     * @return the persisted entity
     */
    StockIndent save(StockIndent stockIndent);

    /**
     * Save stockIndent document. Called from all services for which indent is source document
     *
     * @param stockIndent
     * @return the persisted entity
     */
    StockIndent updateSourceDocumentOnDestinationModification(StockIndent stockIndent);

    /**
     * Save a stockIndent.
     *
     * @param stockIndent the entity to save
     * @param action      to be perform
     * @return the persisted entity
     */
    StockIndent save(StockIndent stockIndent, String action) throws SequenceGenerateException, BusinessRuleViolationException, FieldValidationException;

    /**
     * Get all the stockIndents.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockIndent> findAll(Pageable pageable);


    /**
     * Get one stockIndent by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    StockIndent findOne(Long id, Integer version);

    /**
     * Get the "id" stockIndent.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockIndent findOne(Long id);

    /**
     * Delete the "id" stockIndent.
     *
     * @param id the id of the entity
     */
    void delete(Long id) throws BusinessRuleViolationException;

    /**
     * Search for the stockIndent corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockIndent> search(String query, Pageable pageable);

    /**
     * Search for the stockIndent corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    Page<StockIndent> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * @param Id
     * @return
     */
    StockIndent findDetachedOne(Long Id);

    /**
     * Workflow
     *
     * @param stockIndent the entity to save
     * @param transition  to be performed
     * @param taskId      task Id
     * @return stockIndent object
     * @throws Exception
     */
    StockIndent executeWorkflow(StockIndent stockIndent, String transition, Long taskId) throws Exception;

    /**
     * Search for the stockIndent to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

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
     * Copies Stock Indent to another Stock Indent
     *
     * @param id
     * @param docNum
     * @return
     */
    StockIndent copyStockIndent(Long id, String docNum);

    /**
     * Delete all elastic index of Stock Indent
     */
    void deleteIndex();

    /**
     * Do elastic index for Stock Indent
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * Import a csv file to stockIndentDocumentLine
     *
     * @param multipartFile
     * @param storeId
     * @param indentStoreId
     * @return
     */
    Map<String, Object> importStockIndentDocumentLine(MultipartFile multipartFile, Long storeId, Long indentStoreId) throws IOException;

    public void assignValidityDate(StockIndent stockIndent);

    public void doCloseBySystem();

    void index(StockIndent stockIndent);

    /**
     * getRelatedDocuments from all
     */
    Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException;

    public void autoRejectBySystem();

    Map<String, Object> getStockIndentHTMLByIndentId(Long indentId,String documentNumber) throws Exception;

    byte[] getStockIndentPdfByIndentId(Long indentId, String documentNumber, String original)throws Exception;

    void regenerateWorkflow(String documentNumber);

}
