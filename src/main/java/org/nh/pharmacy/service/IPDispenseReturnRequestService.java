package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.dto.IPDispenseReturnDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseReturnRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing IPDispenseReturnRequest.
 */
public interface IPDispenseReturnRequestService {

    /**
     * Save a iPDispenseReturnRequest.
     *
     * @param iPDispenseReturnRequest the entity to save
     * @return the persisted entity
     */
    IPDispenseReturnRequest save(IPDispenseReturnRequest iPDispenseReturnRequest);

    /**
     *  Get all the iPDispenseReturnRequests.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<IPDispenseReturnRequest> findAll(Pageable pageable);

    /**
     *  Get the "id" iPDispenseReturnRequest.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    IPDispenseReturnRequest findOne(Long id);

    /**
     *  Delete the "id" iPDispenseReturnRequest.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the iPDispenseReturnRequest corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<IPDispenseReturnRequest> search(String query, Pageable pageable);

    /**
     *
     * @param query
     * @param pageable
     * @param includeFields
     * @param excludeFields
     * @return
     */
    Page<IPDispenseReturnRequest> searchByFields(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    List<IPDispenseReturnDocumentLine> constructIPDispenseReturnRequest(String query, String patientMrn, String visitNumber);

    Map<String, Object> processIPDispenseReturnRequest(IPDispenseReturnRequest ipDispenseReturnRequest, String action, Map<String, Object> responseMap) throws Exception;

    void validatePendingRequests(IPDispenseReturnRequest iPDispenseReturnRequest) throws Exception;

    Map<String, String> exportIPDispenseReturnRequest(String query, Pageable pageable) throws IOException;

    void updateAcceptedAndPendingReturnQty(IPDispenseReturnRequest iPDispenseReturnRequest);

    void deleteIndex();

    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    void reIndex(Long id);

    Map<String,String> exportIPDispenseReturn(String query, Pageable pageable) throws IOException;

    /**
     * This method is used for fetching IP Pending dispense orders
     * @param mrn
     * @param visitNumber
     * @param action
     * @return
     * @throws Exception
     */
    List<IPDispenseReturnRequestDTO> getIPDispenseReturns(String mrn, String visitNumber, String action) throws Exception;

    Map<String, Object> processIPDispenseDirectReturn(IPDispenseReturnRequest iPDispenseReturnRequest, Map<String, Object> dispenseReturnMap) throws Exception;

    void reIndexReturnRequest(Map<String,Object> responseMap);
}
