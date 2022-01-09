package org.nh.pharmacy.service;

import org.nh.common.dto.SourceDTO;
import org.nh.pharmacy.domain.MedicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing MedicationRequest.
 */
public interface MedicationRequestService {

    /**
     * Save a medicationRequest.
     *
     * @param medicationRequest the entity to save
     * @return the persisted entity
     */
    MedicationRequest save(MedicationRequest medicationRequest);

    /**
     * Get all the medicationRequests.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<MedicationRequest> findAll(Pageable pageable);

    /**
     * Get the "id" medicationRequest.
     *
     * @param id the id of the entity
     * @return the entity
     */
    MedicationRequest findOne(Long id);

    /**
     * Delete the "id" medicationRequest.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the medicationRequest corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<MedicationRequest> search(String query, Pageable pageable);

    /***
     *
     * @param medicationRequest
     */
    void updateMedicationRequest(MedicationRequest medicationRequest);


    /***
     *
     */
    void deleteIndex();

    /**
     * Do elastic index for MedicationRequest
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    void updateMedicationRequest(SourceDTO dispense);

    void reIndex(Long id);

    Map<String,String> exportPharmacyRequest(String query, Pageable pageable) throws IOException;

    /***
     *
     * @param documentNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     */
    Map<String, Object> compareVersion(String documentNumber, Integer versionFirst, Integer versionSecond);

    /**
     *
     * @param documentNumber
     * @return
     */
    List<Integer> getAllVersion(String documentNumber);

    MedicationRequest findByDocumentNumber(String documentNumber);
}
