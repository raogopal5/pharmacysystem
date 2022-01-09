package org.nh.pharmacy.web.rest;

import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.nh.pharmacy.repository.PrescriptionAuditRequestRepository;
import org.nh.pharmacy.repository.search.PrescriptionAuditRequestSearchRepository;
import org.nh.pharmacy.service.PrescriptionAuditRequestService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing PrescriptionAuditRequest.
 */
@RestController
@RequestMapping("/api")
public class PendingAuditRequestResource {

    private final Logger log = LoggerFactory.getLogger(PendingAuditRequestResource.class);


    private final PrescriptionAuditRequestService prescriptionAuditRequestService;

    private final PrescriptionAuditRequestRepository prescriptionAuditRequestRepository;

    private final PrescriptionAuditRequestSearchRepository prescriptionAuditRequestSearchRepository;

    private final ApplicationProperties applicationProperties;

    public PendingAuditRequestResource(PrescriptionAuditRequestService prescriptionAuditRequestService,
                                       PrescriptionAuditRequestRepository prescriptionAuditRequestRepository, PrescriptionAuditRequestSearchRepository prescriptionAuditRequestSearchRepository, ApplicationProperties applicationProperties) {
        this.prescriptionAuditRequestService = prescriptionAuditRequestService;
        this.prescriptionAuditRequestRepository = prescriptionAuditRequestRepository;
        this.prescriptionAuditRequestSearchRepository = prescriptionAuditRequestSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /pending-audit-requests : Create a new pending audit request.
     *
     * @param prescriptionAuditRequest the prescriptionAuditRequest to create
     * @return the ResponseEntity with status 201 (Created) and with body the new prescriptionAuditRequest, or with status 400 (Bad Request) if the medicationRequest has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/prescription-audit-requests")
    @PreAuthorize("hasPrivilege('102124101')")
    public ResponseEntity<PrescriptionAuditRequest> createPendingAuditRequest(@Valid @RequestBody PrescriptionAuditRequest prescriptionAuditRequest) throws URISyntaxException {
        log.debug("REST request to save PrescriptionAuditRequest : {}", prescriptionAuditRequest);
        if (prescriptionAuditRequest.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("prescriptionAuditRequest", "idexists", "A new prescriptionAuditRequest cannot already have an ID")).body(null);
        }
        PrescriptionAuditRequest result = prescriptionAuditRequestService.save(prescriptionAuditRequest);
        return ResponseEntity.created(new URI("/api/pending-audit-requests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("prescriptionAuditRequest", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /medication-requests : Updates an existing medicationRequest.
     *
     * @param prescriptionAuditRequest the medicationRequest to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated prescriptionAuditRequest,
     * or with status 400 (Bad Request) if the medicationRequest is not valid,
     * or with status 500 (Internal Server Error) if the medicationRequest couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/prescription-audit-requests")
    //@Timed
    @PreAuthorize("hasPrivilege('102124101')")
    public ResponseEntity<PrescriptionAuditRequest> updatePendingAuditRequest(@Valid @RequestBody PrescriptionAuditRequest prescriptionAuditRequest) throws URISyntaxException {
        log.debug("REST request to update prescriptionAuditRequest : {}", prescriptionAuditRequest);
        if (prescriptionAuditRequest.getId() == null) {
            return createPendingAuditRequest(prescriptionAuditRequest);
        }
        PrescriptionAuditRequest result = prescriptionAuditRequestService.save(prescriptionAuditRequest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("prescriptionAuditRequest", prescriptionAuditRequest.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pending-audit-requests : get all the pending audit requests.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of pending audit request in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/prescription-audit-requests")
    @PreAuthorize("hasPrivilege('102124102')")
    public ResponseEntity<List<PrescriptionAuditRequest>> getAllPendingAuditRequests(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of PrescriptionAuditRequest");
        Page<PrescriptionAuditRequest> page = prescriptionAuditRequestService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pending-audit-requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /pending-audit-requests/:id : get the "id" pending audit request.
     *
     * @param id the id of the pending audit request to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pending audit request, or with status 404 (Not Found)
     */
    @GetMapping("/prescription-audit-requests/{id}")
    @PreAuthorize("hasPrivilege('102124102')")
    public ResponseEntity<PrescriptionAuditRequest> getPendingAuditRequest(@PathVariable Long id) {
        log.debug("REST request to get MedicationRequest : {}", id);
        PrescriptionAuditRequest prescriptionAuditRequest = prescriptionAuditRequestService.findOne(id);
        return Optional.ofNullable(prescriptionAuditRequest)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /medication-requests/:id : delete the "id" medicationRequest.
     *
     * @param id the id of the medicationRequest to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/prescription-audit-requests/{id}")
    @PreAuthorize("hasPrivilege('102124101')")
    public ResponseEntity<Void> deletePendingAuditRequest(@PathVariable Long id) {
        log.debug("REST request to delete Pending audit request : {}", id);
        prescriptionAuditRequestService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("pendingAuditRequest", id.toString())).build();
    }

    /**
     * SEARCH  /_search/pending-audit-requests?query=:query : search for the pending audit request corresponding
     * to the query.
     *
     * @param query    the query of the pending audit request search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/prescription-audit-requests")
    public ResponseEntity<List<PrescriptionAuditRequest>> searchPendingAuditRequest(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Pending Audit request for query {}", query);
        Page<PrescriptionAuditRequest> page = prescriptionAuditRequestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/pending-audit-requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /***
     *
     * @return
     */
    @GetMapping("/_index/prescription-audit-requests")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexPendingAuditRequest(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Pending Audit Requests");
        long resultCount = prescriptionAuditRequestRepository.getTotalLatestRecord( fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            log.debug("Indexing for i=" + i + " pgeSize=" + pageSize);
            prescriptionAuditRequestService.doIndex(i, pageSize, fromDate, toDate);
        }
        prescriptionAuditRequestSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed For Prescription audit request", "")).build();
    }

    /**
     * EXPORT  /_export/medication-requests?query=:query : export csv file for the medication request corresponding to the query.
     *
     * @param query    the query of the medication request search
     * @param pageable the pagination information
     * @throws URISyntaxException,IOException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_export/prescription-audit-requests")
    @PreAuthorize("hasPrivilege('102124102')")
    public Map<String, String> exportPrescriptionAuditRequests(@RequestParam String query, Pageable pageable) throws Exception {

        log.debug("REST request to export Medication Request for query {}", query);
        return prescriptionAuditRequestService.exportPendingAudits(query, pageable);
    }

   /* *//***
     *
     * @param documentNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     * @throws Exception
     *//*
    @GetMapping("/pending-audit-requests/compare-document-version")
    //@Timed
    public ResponseEntity<Map<String, Object>> compareVersion(@RequestParam(required = true) String documentNumber,
                                                              @RequestParam(required = true) Integer versionFirst,
                                                              @RequestParam(required = true) Integer versionSecond) throws Exception {

        log.debug("REST request to get documents for comparision");
        Map<String, Object> result = pendingAuditRequestService.compareVersion(documentNumber, versionFirst, versionSecond);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
*/
    /****
     *
     * @return
     * @throws Exception
     */
  /*  @GetMapping("/pending-audit-requests/versions")
    //@Timed
    public ResponseEntity<List<Integer>> getAllVersions(@RequestParam(required = true) String documentNumber) throws Exception {

        log.debug("REST request to get all version for given document");
        List<Integer> result = pendingAuditRequestService.getAllVersion(documentNumber);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }*/


    @GetMapping("/_workflow/prescription-audit-request")
    @PreAuthorize("hasPrivilege('102124101')")
    public ResponseEntity<Map<String, Object>> getTaskConstraintsForPrescriptionAudit(@RequestParam String prescriptionAuditDocNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", prescriptionAuditDocNumber);

        Map<String, Object> taskDetails = prescriptionAuditRequestService.getTaskConstraintsForPrescriptionAudit(prescriptionAuditDocNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", prescriptionAuditDocNumber))
            .body(taskDetails);
    }

    /**
     * PUT  /_workflow/receipts : call execute workflow to complete the task and save the prescription Audit
     *
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/execute-prescription-audit-request")
    @PreAuthorize("hasPrivilege('102124101')")
    public ResponseEntity<PrescriptionAuditRequest> executeWorkflowForPrescriptionAudit(@Valid @RequestBody PrescriptionAuditRequest prescriptionAuditRequest, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task : {} for retrospection", taskId);
        PrescriptionAuditRequest result = null;
        try {
            result = prescriptionAuditRequestService.executeWorkflowForPrescriptionAudit(prescriptionAuditRequest, transition, taskId);
        } catch (Exception e) {
            log.error("Error while doing retrospection of prescriptionAuditRequest number: {}, Ex: {} ",prescriptionAuditRequest.getDocumentNumber(), e);
            prescriptionAuditRequestService.reIndex(prescriptionAuditRequest.getId());
            throw e;
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert("Prescription Audit Request", prescriptionAuditRequest.getId().toString())).body(result);
    }

    @GetMapping("/_workflow/execute/prescription-audit-request")
    @PreAuthorize("hasPrivilege('102124101')")
    public ResponseEntity<PrescriptionAuditRequest> executeWorkflowForPrescriptionAuditWithDocNumber(@Valid @RequestParam String prescriptionAuditRequestNumber, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task : {} for retrospection", taskId);
        PrescriptionAuditRequest result = null;
        try {
            result = prescriptionAuditRequestService.executeWorkflowForPrescriptionAuditRequest(prescriptionAuditRequestNumber,transition, taskId);
        } catch (Exception e) {
            log.error("Error while doing retrospection of prescriptionAuditRequest number: {}, Ex: {} ",prescriptionAuditRequestNumber, e);
            throw e;
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert("Prescription Audit Request", prescriptionAuditRequestNumber)).body(result);
    }

    @GetMapping("/status-count/prescription-audit-requests")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllPrescriptionAuditRequestStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {

        log.debug("REST request to get a status count of prescription audit requests");
        Map<String, Long> countMap = prescriptionAuditRequestService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

}

