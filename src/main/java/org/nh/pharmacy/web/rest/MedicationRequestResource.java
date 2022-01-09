package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.repository.MedicationRequestRepository;
import org.nh.pharmacy.repository.search.MedicationSearchRepository;
import org.nh.pharmacy.service.MedicationRequestService;
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
 * REST controller for managing MedicationRequest.
 */
@RestController
@RequestMapping("/api")
public class MedicationRequestResource {

    private final Logger log = LoggerFactory.getLogger(MedicationRequestResource.class);


    private final MedicationRequestService medicationRequestService;

    private final MedicationRequestRepository medicationRequestRepository;

    private final MedicationSearchRepository medicationSearchRepository;

    private final ApplicationProperties applicationProperties;

    public MedicationRequestResource(MedicationRequestService medicationRequestService,
                                     MedicationRequestRepository medicationRequestRepository,
                                     MedicationSearchRepository medicationSearchRepository,
                                     ApplicationProperties applicationProperties) {
        this.medicationRequestService = medicationRequestService;
        this.medicationRequestRepository = medicationRequestRepository;
        this.medicationSearchRepository = medicationSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /medication-requests : Create a new medicationRequest.
     *
     * @param medicationRequest the medicationRequest to create
     * @return the ResponseEntity with status 201 (Created) and with body the new medicationRequest, or with status 400 (Bad Request) if the medicationRequest has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/medication-requests")
    //@Timed
    public ResponseEntity<MedicationRequest> createMedicationRequest(@Valid @RequestBody MedicationRequest medicationRequest) throws URISyntaxException {
        log.debug("REST request to save MedicationRequest : {}", medicationRequest);
        if (medicationRequest.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("medicationRequest", "idexists", "A new medicationRequest cannot already have an ID")).body(null);
        }
        MedicationRequest result = medicationRequestService.save(medicationRequest);
        return ResponseEntity.created(new URI("/api/medication-requests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("medicationRequest", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /medication-requests : Updates an existing medicationRequest.
     *
     * @param medicationRequest the medicationRequest to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated medicationRequest,
     * or with status 400 (Bad Request) if the medicationRequest is not valid,
     * or with status 500 (Internal Server Error) if the medicationRequest couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/medication-requests")
    //@Timed
    public ResponseEntity<MedicationRequest> updateMedicationRequest(@Valid @RequestBody MedicationRequest medicationRequest) throws URISyntaxException {
        log.debug("REST request to update MedicationRequest : {}", medicationRequest);
        if (medicationRequest.getId() == null) {
            return createMedicationRequest(medicationRequest);
        }
        MedicationRequest result = medicationRequestService.save(medicationRequest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("medicationRequest", medicationRequest.getId().toString()))
            .body(result);
    }

    /**
     * GET  /medication-requests : get all the medicationRequests.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of medicationRequests in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/medication-requests")
    //@Timed
    public ResponseEntity<List<MedicationRequest>> getAllMedicationRequests(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of MedicationRequests");
        Page<MedicationRequest> page = medicationRequestService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/medication-requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /medication-requests/:id : get the "id" medicationRequest.
     *
     * @param id the id of the medicationRequest to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the medicationRequest, or with status 404 (Not Found)
     */
    @GetMapping("/medication-requests/{id}")
    //@Timed
    public ResponseEntity<MedicationRequest> getMedicationRequest(@PathVariable Long id) {
        log.debug("REST request to get MedicationRequest : {}", id);
        MedicationRequest medicationRequest = medicationRequestService.findOne(id);
        return Optional.ofNullable(medicationRequest)
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
    @DeleteMapping("/medication-requests/{id}")
    //@Timed
    public ResponseEntity<Void> deleteMedicationRequest(@PathVariable Long id) {
        log.debug("REST request to delete MedicationRequest : {}", id);
        medicationRequestService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("medicationRequest", id.toString())).build();
    }

    /**
     * SEARCH  /_search/medication-requests?query=:query : search for the medicationRequest corresponding
     * to the query.
     *
     * @param query    the query of the medicationRequest search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/medication-requests")
    //@Timed
    public ResponseEntity<List<MedicationRequest>> searchMedicationRequests(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of MedicationRequests for query {}", query);
        Page<MedicationRequest> page = medicationRequestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/medication-requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /***
     *
     * @return
     */
    @GetMapping("/_index/medication-requests")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexMedicationRequest(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Medication Requests");
        long resultCount = medicationRequestRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            log.debug("Indexing for i="+i+" pgeSize="+pageSize);
            medicationRequestService.doIndex(i, pageSize, fromDate, toDate);
        }
        medicationSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    /**
     * EXPORT  /_export/medication-requests?query=:query : export csv file for the medication request corresponding to the query.
     *
     * @param query    the query of the medication request search
     * @param pageable the pagination information
     * @throws URISyntaxException,IOException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_export/medication-requests")
    public Map<String, String> exportPharmacyRequest(@RequestParam String query, Pageable pageable) throws Exception {

        log.debug("REST request to export Medication Request for query {}", query);
        return medicationRequestService.exportPharmacyRequest(query, pageable);
    }

    /***
     *
     * @param documentNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     * @throws Exception
     */
    @GetMapping("/medication-requests/compare-document-version")
    //@Timed
    public ResponseEntity<Map<String, Object>> compareVersion(@RequestParam(required = true) String documentNumber,
                                                              @RequestParam(required = true) Integer versionFirst,
                                                              @RequestParam(required = true) Integer versionSecond) throws Exception {

        log.debug("REST request to get documents for comparision");
        Map<String, Object> result = medicationRequestService.compareVersion(documentNumber, versionFirst, versionSecond);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /****
     *
     * @param documentNumber
     * @return
     * @throws Exception
     */
    @GetMapping("/medication-requests/versions")
    //@Timed
    public ResponseEntity<List<Integer>> getALlVersions(@RequestParam(required = true) String documentNumber) throws Exception {

        log.debug("REST request to get all version for given document");
        List<Integer> result = medicationRequestService.getAllVersion(documentNumber);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
