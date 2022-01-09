package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.DispenseReturnDocumentLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing DispenseReturn.
 */
public interface DispenseReturnService {

    /**
     * Save a dispenseReturn.
     *
     * @param dispenseReturn the entity to save
     * @return the persisted entity
     */
    DispenseReturn save(DispenseReturn dispenseReturn);

    /**
     *
     * @return
     */
    Map<String, Object> processDispenseReturn(DispenseReturn dispenseReturn, String action) throws Exception;

    /**
     *  Get all the dispenseReturns.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<DispenseReturn> findAll(Pageable pageable);

    /**
     *  Get the "id" dispenseReturn.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    DispenseReturn findOne(Long id);

    DispenseReturn findDetachedOne(Long Id);

    /**
     *  Delete the "id" dispenseReturn.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the dispenseReturn corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<DispenseReturn> search(String query, Pageable pageable);

    Page<DispenseReturn> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * Search for the DispenseReturn to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Delete all elastic index of DispenseReturn
     */
    void deleteIndex();

    /**
     * Do elastic index for DispenseReturn
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    Map<String, String> exportDispenseReturn(String query, Pageable pageable) throws IOException;

    /**
     * Execute workflow
     *
     * @param dispenseReturn the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return result map
     */
    Map<String, Object> executeWorkflow(DispenseReturn dispenseReturn, String transition, Long taskId) throws Exception;

    /**
     * Get task constraints
     *
     * @param documentNumber
     * @param userId
     * @param taskId
     * @return taskId, constraints
     **/
    Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId);

   /**
     * Calculate tax per return line
     * @param document
     * @param unitCode
     * @return
     */
    DispenseReturnDocumentLine taxCalculationForLine(DispenseReturnDocumentLine document, String unitCode);

    /**
     * Calculate tax for Dispense Return
     * @param dispenseReturn
     * @return
     */
    DispenseReturn taxCalculationForReturn(DispenseReturn dispenseReturn);

    /**
     *
     * @param dispenseReturn
     * @return
     */
    DispenseReturn calculateDispenseReturnDetail(DispenseReturn dispenseReturn);

    /**
     *
     * @param dispenseReturn
     * @return
     */
    DispenseReturn  getUpdateDispenseDocument(DispenseReturn dispenseReturn);

    /**
     *
     * @param documentNumber
     * @return
     */
    DispenseReturn createDispenseReturnDocument(String documentNumber);

    /**
     *
     * @param dispenseReturnId
     * @param dispenseReturnNumber
     * @return
     * @throws Exception
     */
    Map<String,Object> getReturnHTMLByReturnId(Long dispenseReturnId, String dispenseReturnNumber) throws Exception;

    /**
     *
     * @param dispenseReturnId
     * @param dispenseReturnNumber
     * @return
     * @throws Exception
     */
    byte[] getReturnPdfByReturnId(Long dispenseReturnId, String dispenseReturnNumber) throws Exception;

    public void index(DispenseReturn dispenseReturn);

    public void autoClose();

     void reIndexBilling(Map<String, Object> documentMap);

    /***
     *
     * @param dispenseReturnNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     */
    Map<String, Object> compareVersion(String dispenseReturnNumber, Integer versionFirst, Integer versionSecond);

    /**
     *
     * @param dispenseReturnNumber
     * @return
     */
    List<Integer> getAllVersion(String dispenseReturnNumber);

    DispenseReturn processIPDispenseReturn(DispenseReturn dispenseReturn) throws Exception;

    DispenseReturn reIndex(Long id);

    void regenerateWorkflow(String documentNumber);

    void reIndex(@Valid DispenseReturn dispenseReturn);
}
