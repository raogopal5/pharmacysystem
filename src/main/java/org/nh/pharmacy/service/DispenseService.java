package org.nh.pharmacy.service;

import org.nh.billing.domain.Invoice;
import org.nh.billing.domain.dto.PamDocument;
import org.nh.common.dto.PatientDTO;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.ServiceActivator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing Dispense.
 */
public interface DispenseService {

    /**
     * save a dispense
     *
     * @param dispense
     * @return
     */
    Dispense save(Dispense dispense);

    /**
     * Save or update a dispense.
     *
     * @param dispense
     * @return
     */
    Dispense saveOrUpdate(Dispense dispense);

    /**
     * Get invoice by dispenseId or dispenseNumber
     *
     * @param dispenseId
     * @return
     */
    Invoice getInvoiceByDispenseId(Long dispenseId, String dispenseNumber);

    /***
     *
     * @param dispenseId
     * @param dispenseNumber
     * @return
     */

    Map<String, Object> getInvoiceHTMLByDispenseId(Long dispenseId, String dispenseNumber) throws Exception;

    /**
     * @param dispenseId
     * @param dispenseNumber
     * @return
     * @throws Exception
     */
    byte[] getInvoicePdfByDispense(Long dispenseId, String dispenseNumber, String original) throws Exception;

    /**
     * Get all the dispenses.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Dispense> findAll(Pageable pageable);

    /**
     * Get the "id" dispense.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Dispense findOne(Long id);

    /**
     * Get the "id" dispense.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Dispense findDetachedOne(Long id);

    /**
     * Delete the "id" dispense.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the dispense corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Dispense> search(String query, Pageable pageable);

    Page<Dispense> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    /**
     * Export dispenses corresponding to the query.
     *
     * @param query
     * @param pageable
     * @return
     */
    Map<String, String> exportDispenses(String query, Pageable pageable) throws IOException;

    /**
     * Produce Dispense Item
     *
     * @param dispense
     */
    public void produce(Dispense dispense);

    /**
     * publish the pamDocument entity
     *
     * @param pamDocument
     */
    void produce(PamDocument pamDocument);

    /**
     * Publish External patient
     *
     * @param patient
     */
    void publishExternalPatient(PatientDTO patient);

    /**
     * Search for the dispense to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    Map<String, Long> getStatusCount(String query);

    /**
     * Delete all elastic index of dispense
     */
    void deleteIndex();

    /**
     * Do elastic index for dispense
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * Reindex dispense elasticsearch for given id
     *
     * @param id
     */
    void reIndex(Long id);

    /**
     * Calculate Dispense Details
     *
     * @param dispense
     * @return
     */
    //Dispense calculateDispenseDetail(Dispense dispense);

    /**
     * Reset document details
     *
     * @param dispense
     */
    void resetDocument(Dispense dispense);

    /**
     * Search Patient details
     *
     * @param searchInput
     * @param pageable
     * @return
     */
    List<PatientDTO> searchPatient(String searchInput, Pageable pageable);

    Dispense calculate(Dispense dispense, String action, Integer lineIndex) throws Exception;

    @Deprecated
    void autoClose();

    void autoCloseDispenseDocument(LocalDateTime currentDateTime, Dispense dispense);

    void index(Dispense dispense);

    /**
     * Email invoice details
     *
     * @param dispenseId
     * @param dispenseNumber
     * @throws Exception
     */
    void sendDocument(Long dispenseId, String dispenseNumber) throws Exception;

    /****
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    Map<String, Object> dashboardDiscount(Long unitId, Date fromDate, Date toDate) throws Exception;

    /**
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    Map<String, Object> dashboardDispenseProductivity(Long unitId, Date fromDate, Date toDate) throws Exception;

    /***
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    Map<String, Object> dashboardDispenseProductivityTrend(Long unitId, String format, Date fromDate, Date toDate) throws Exception;

    /**
     * check and delete stockreserve on reindexing of dispense
     *
     * @param id
     */
    void validateAndDeleteStockReserve(Long id);

    Dispense updateItem(Long dispenseId, String dispenseNumber) throws Exception;

    /***
     *
     * @param dispenseNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     */
    Map<String, Object> compareVersion(String dispenseNumber, Integer versionFirst, Integer versionSecond);

    /**
     *
     * @param dispenseNumber
     * @return
     */
    List<Integer> getAllVersion(String dispenseNumber);

    /**
     *
     * @param dispense
     * @return
     */
    Map<String, Object> saveIPDispense(Dispense dispense);

    Map<String,String> exportIPDispenses(String query, Pageable pageable) throws IOException;

    void validateMedicationRequestStatus(Dispense dispense) throws CustomParameterizedException;

    void validateMandatoryFieldsForChargeRecord(Dispense dispense);

    void costPricingInclusiveDiscountValidation(Dispense dispense);

    /**
     *
     * @param dispense
     * @return
     */
    public Dispense setMedicationObjectDetails(Dispense dispense);

    @ServiceActivator(inputChannel = Channels.DISPENSE_RECORD_ENCOUNTER_UPDATE_INPUT)
    void dispenseRecordEncounterUpdate(List<Dispense> dispenseList);

    void publishDispenseRecordsToChargeRecord(List<Dispense> dispenseList);
}
