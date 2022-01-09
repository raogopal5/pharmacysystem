package org.nh.pharmacy.service;

import java.io.IOException;
import java.time.LocalDate;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.SortedSet;

/**
 * Service Interface for managing StockReversal.
 */
public interface StockReversalService {

    /**
     * Save a stockReversal.
     *
     * @param stockReversal the entity to save
     * @return the persisted entity
     */
    StockReversal save(StockReversal stockReversal) throws SequenceGenerateException;

    /**
     * Save a stockIndent.
     *
     * @param stockReversal the entity to save
     * @param action        to be perform
     * @return the persisted entity
     */
    StockReversal save(StockReversal stockReversal, String action) throws Exception;

    /**
     * Get all the stockReversals.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockReversal> findAll(Pageable pageable);

    /**
     * Get the "documentId" stockReversal.
     *
     * @param documentId the id of the entity
     * @return the entity
     */
    StockReversal findOne(DocumentId documentId);

    /**
     * Get the "id" stockReversal.
     *
     * @param id the id of the entity
     * @return the entity
     */
    StockReversal findOne(Long id);

    /**
     * Delete the "id" stockReversal.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Delete the stockReversal by id,version.
     *
     * @param id,version the id of the entity
     */
    void delete(Long id, Integer version);

    /**
     * Search for the stockReversal corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<StockReversal> search(String query, Pageable pageable);

    Page<StockReversal> searchForDocument(String query, Pageable pageable);

    /**
     * Search for the stockReversal corresponding to the query.
     *
     * @param query         the query of the search
     * @param pageable      the pagination information
     * @param includeFields the fields which should be part of the return entity
     * @param excludeFields the fields which should not be part of return entity
     * @return the list of entities
     */
    Page<StockReversal> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * @param Id
     * @return
     */
    public StockReversal findDetachedOne(Long Id);

    /**
     * To Convert Stock Receipt to Stock Reversal
     *
     * @param stockReceipt
     * @return
     */
    public StockReversal convertReceiptToReversal(StockReceipt stockReceipt, Map<Long, String> lineSkuMap);

    /**
     * Search for the stockReversal to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Delete all elastic index of StockReversal
     */
    void deleteIndex();

    /**
     * Do elastic index for StockReversal
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    Map<String, SortedSet<RelatedDocument>> getRelatedDocuments(String documentNumber) throws IOException;

    /**
     * re-index elasticsearch on transaction failure
     *
     * @param id
     */
    public void reIndex(Long id);
}
