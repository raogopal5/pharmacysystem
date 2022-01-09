

package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Service Interface for managing PrescriptionAuditRequest.
 */
public interface PrescriptionAuditRequestService {

    /**
     * Save a PrescriptionAuditRequest.
     *
     * @param prescriptionAuditRequest the entity to save
     * @return the persisted entity
     */
    PrescriptionAuditRequest save(PrescriptionAuditRequest prescriptionAuditRequest);

    /**
     * Get all the pendingAuditRequests.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<PrescriptionAuditRequest> findAll(Pageable pageable);

    /**
     * Get the "id" pendingAuditRequest.
     *
     * @param id the id of the entity
     * @return the entity
     */
    PrescriptionAuditRequest findOne(Long id);

    /**
     * Delete the "id" PrescriptionAuditRequest.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the PrescriptionAuditRequest corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<PrescriptionAuditRequest> search(String query, Pageable pageable);


    /***
     *
     */
    void deleteIndex();

    /**
     * Do elastic index for PrescriptionAuditRequest
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);


    void reIndex(Long id);


    Map<String,String> exportPendingAudits(String query, Pageable pageable) throws IOException;

    void handleMedicationRequestInput(MedicationRequest medicationRequest);

    Map<String,Object> getTaskConstraintsForPrescriptionAudit(String prescriptionAuditDocNumber, String userId, Long taskId);

    PrescriptionAuditRequest executeWorkflowForPrescriptionAudit(PrescriptionAuditRequest prescriptionAuditRequest, String transition, Long taskId);

    PrescriptionAuditRequest executeWorkflowForPrescriptionAuditRequest(String prescriptionAuditRequestNumber, String transition, Long taskId);

    Map<String,Long> getStatusCount(String query);
}

