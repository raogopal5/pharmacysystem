package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.billing.domain.Invoice;
import org.nh.common.dto.PatientDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.domain.enumeration.DispenseType;
import org.nh.pharmacy.repository.DispenseRepository;
import org.nh.pharmacy.repository.search.DispenseSearchRepository;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST controller for managing Dispense.
 */
@RestController
@RequestMapping("/api")
public class DispenseResource {

    private final Logger log = LoggerFactory.getLogger(DispenseResource.class);

    private static final String ENTITY_NAME = "dispense";

    private final DispenseService dispenseService;

    private final BillingService billingService;

    private final PlanExecutionService planExecutionService;

    private final DispenseRepository dispenseRepository;

    private final DispenseSearchRepository dispenseSearchRepository;

    private final ApplicationProperties applicationProperties;

    private final IpPrintService ipPrintService;

    private final DispensePrintBarcodeService dispensePrintBarcodeService;

    public DispenseResource(DispenseService dispenseService, BillingService billingService, PlanExecutionService planExecutionService,
                            DispenseRepository dispenseRepository, DispenseSearchRepository dispenseSearchRepository,
                            ApplicationProperties applicationProperties, IpPrintService ipPrintService, DispensePrintBarcodeService dispensePrintBarcodeService) {
        this.dispenseService = dispenseService;
        this.billingService = billingService;
        this.planExecutionService = planExecutionService;
        this.dispenseRepository = dispenseRepository;
        this.dispenseSearchRepository = dispenseSearchRepository;
        this.applicationProperties = applicationProperties;
        this.ipPrintService=ipPrintService;
        this.dispensePrintBarcodeService = dispensePrintBarcodeService;
    }

    /**
     * POST  /dispenses : Create a new dispense.
     *
     * @param dispense the dispense to create
     * @return the ResponseEntity with status 201 (Created) and with body the new dispense, or with status 400 (Bad Request) if the dispense has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102103101')")
    public ResponseEntity<Object> createDispense(@Valid @RequestBody Dispense dispense, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to save action: {}, Dispense : {}", action, dispense);
        if (dispense.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new dispense cannot already have an ID")).body(null);
        }
        Map<String, Object> documentMap = null;
        try {
            //planExecutionService.validatePlanRule(dispense);
            documentMap = billingService.saveDispenseWithAction(dispense, action);
        } catch (Exception e) {
            log.error("Exception occured while dispense :{} ", e);
            billingService.reIndexBilling(dispense);
            dispenseService.validateAndDeleteStockReserve(dispense.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/dispenses/" + dispense.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, dispense.getId().toString()))
            .body(documentMap);
    }

    /**
     * PUT  /dispenses : Updates an existing dispense.
     *
     * @param dispense the dispense to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated dispense,
     * or with status 400 (Bad Request) if the dispense is not valid,
     * or with status 500 (Internal Server Error) if the dispense couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102103101')")
    public ResponseEntity<Object> updateDispense(@Valid @RequestBody Dispense dispense, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update Dispense : {}", dispense);
        if (dispense.getId() == null) {
            return createDispense(dispense, action);
        }
        Map<String, Object> documentMap = null;
        try {
//            dispense = dispenseService.calculateDispenseDetail(dispense);
            documentMap = billingService.saveDispenseWithAction(dispense, action);
        } catch (Exception e) {
            log.error("Exception occured while dispense :{} ", e);
            billingService.reIndexBilling(dispense);
            dispenseService.validateAndDeleteStockReserve(dispense.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dispense.getId().toString()))
            .body(documentMap);
    }

    /**
     * GET  /dispenses : get all the dispenses.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of dispenses in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102') OR hasPrivilege('102120102')")
    public ResponseEntity<List<Dispense>> getAllDispenses(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Dispenses");
        Page<Dispense> page = dispenseService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/dispenses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /dispenses/:id : get the "id" dispense.
     *
     * @param id the id of the dispense to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dispense, or with status 404 (Not Found)
     */
    @GetMapping("/dispenses/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102') OR hasPrivilege('102120102')")
    public ResponseEntity<Dispense> getDispense(@PathVariable Long id) {
        log.debug("REST request to get Dispense : {}", id);
        Dispense dispense = dispenseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dispense));
    }

    /**
     * GET  /dispenses-view/:id : get the "id" dispense with calculation in case of DRAFT status.
     *
     * @param id the id of the dispense to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dispense, or with status 404 (Not Found)
     */
    @GetMapping("/dispenses-view/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102') OR hasPrivilege('102120102')")
    public ResponseEntity<Dispense> getDispenseView(@PathVariable Long id) throws Exception {
        log.debug("REST request to get Dispense : {}", id);
        Dispense dispense = dispenseService.findOne(id);
        if (DispenseStatus.DRAFT.equals(dispense.getDocument().getDispenseStatus())) {
            dispense = dispenseService.calculate(dispense, "", null);
        }
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dispense));
    }

    /**
     * GET  _invoice/dispenses : get invoice by "dispenseId" or  dispenseNumber.
     *
     * @param dispenseId the id of the dispense to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dispense, or with status 404 (Not Found)
     */
    @GetMapping("_invoice/dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102')")
    public ResponseEntity<Invoice> getInvoiceByDispenseId(@RequestParam(required = false) Long dispenseId, @RequestParam(required = false) String dispenseNumber) {
        log.debug("REST request to get Invoice by dispenseId : {}, dispenseNumber : {}", dispenseId, dispenseNumber);

        Invoice invoice = dispenseService.getInvoiceByDispenseId(dispenseId, dispenseNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(invoice));
    }

    @GetMapping("_invoice/print")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102')")
    public ResponseEntity<byte[]> getInvoiceHTMLByDispenseId(@RequestParam(required = false) Long dispenseId, @RequestParam(required = false) String dispenseNumber) throws Exception {
        log.debug("REST request to get Invoice by dispenseId : {}, dispenseNumber : {}", dispenseId, dispenseNumber);
        Map<String, Object> fileOutPut = new HashMap<>();
        fileOutPut = dispenseService.getInvoiceHTMLByDispenseId(dispenseId, dispenseNumber);
        byte[] content = (byte[]) fileOutPut.get("content");
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + fileOutPut.get("fileName"));
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentLength(content.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @GetMapping("_invoice/print/pdf")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102')")
    public ResponseEntity<Resource> getInvoicePdfByDispenseId(@RequestParam(required = false) Long dispenseId, @RequestParam(required = false) String dispenseNumber,
                                                              @RequestParam(required = false) String original) throws Exception {
        log.debug("REST request to get Invoice by dispenseId : {}, dispenseNumber : {}", dispenseId, dispenseNumber);

        byte[] content = dispenseService.getInvoicePdfByDispense(dispenseId, dispenseNumber, original);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    /**
     * DELETE  /dispenses/:id : delete the "id" dispense.
     *
     * @param id the id of the dispense to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/dispenses/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('102103101')")
    public ResponseEntity<Void> deleteDispense(@PathVariable Long id) {
        log.debug("REST request to delete Dispense : {}", id);
        dispenseService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/dispenses?query=:query : search for the dispense corresponding
     * to the query.
     *
     * @param query    the query of the dispense search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/dispenses")
    //@Timed
    public ResponseEntity<List<Dispense>> searchDispenses(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Dispenses for query {}", query);
        Page<Dispense> page = dispenseService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/dispenses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param query
     * @param pageable
     * @param type
     * @param fields
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/dispenses/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<Dispense>> searchDispenses(@RequestParam String query, @ApiParam Pageable pageable,
                                                          @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Dispenses for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<Dispense> page = dispenseService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/dispenses");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (SearchPhaseExecutionException e) {
            log.error("No Index found for {}", e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/dispenses"),
                HttpStatus.OK);
        }
    }

    /**
     * PUT  /_workflow/dispenses : call execute workflow to complete the task and save the dispense
     *
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/dispenses")
    //@Timed
    public ResponseEntity<Dispense> executeWorkflow(@Valid @RequestBody Dispense dispense, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        Map<String, Object> documentMap = null;
        try {
            documentMap = billingService.executeWorkflow(dispense, transition, taskId);
        } catch (Exception e) {
            log.error("Exception occured while dispense :{} ", e);
            billingService.reIndexBilling(dispense);
            dispenseService.validateAndDeleteStockReserve(dispense.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dispense.getId().toString()))
            .body(dispense);
    }

    /**
     * Get  /_workflow/dispenses : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/dispenses")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = billingService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * GET  /status-count/dispenses?query=:query : get the status count for the dispense corresponding
     * to the query.
     *
     * @param query    the query of the dispense search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/dispenses")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllDispenseStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {

        log.debug("REST request to get a status count of dispenses");
        Map<String, Long> countMap = dispenseService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * INDEX  /_index/dispenses : do elastic index for the dispenses
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/dispenses")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexDispense(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Dispense");
        long resultCount = dispenseRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            dispenseService.doIndex(i, pageSize, fromDate, toDate);
        }
        dispenseSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    @GetMapping("/_index/dispense/{id}")
    public ResponseEntity<Long> reindexDispense(@PathVariable Long id) throws Exception {
        log.debug("REST request to get reindex for dispense by id : {}", id);
        dispenseService.reIndex(id);
        return new ResponseEntity<>(id, HttpStatus.OK);
    }
    /**
     * EXPORT  /_export/dispenses?query=:query : export csv file for the dispenses corresponding to the query.
     *
     * @param query    the query of the dispense search
     * @param pageable the pagination information
     * @throws URISyntaxException,IOException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_export/dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102103102') OR hasPrivilege('102120102')")
    public Map<String, String> exportDispenses(@RequestParam String query, Pageable pageable) throws URISyntaxException, IOException {

        log.debug("REST request to export dispenses for query {}", query);
        return dispenseService.exportDispenses(query, pageable);
    }

    /**
     * POST  /calculate/dispense calculate dispense details
     *
     * @param dispense dispense details
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @PostMapping("/_calculate/dispense")
    //@Timed
    public ResponseEntity<Dispense> calculateDispenseDetail(@RequestBody Dispense dispense, @RequestParam(required = false) String action,
                                                            @RequestParam(required = false) Integer lineIndex) throws Exception {

        log.debug("REST request to calculate dispense details {} with action {} for lineIndex {}", dispense, action, lineIndex);
        dispense = dispenseService.calculate(dispense, action != null ? action : "", lineIndex);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dispense));
    }

    /**
     * GET  /search-patient/dispense search external patient
     *
     * @param query Search data
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/search-patient/dispense")
    //@Timed
    public ResponseEntity<List<PatientDTO>> searchPatient(@RequestParam String query, @ApiParam Pageable pageable) throws URISyntaxException {

        log.debug("REST request to search patient by query :{}", query);
        List<PatientDTO> patients = dispenseService.searchPatient(query, pageable);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(patients));
    }

    /**
     * GET  /generate/transactionNumber to generate transaction number
     *
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/generate/transactionNumber")
    //@Timed
    public String generateTransactionNumber() throws URISyntaxException, SequenceGenerateException {

        log.debug("REST request to generate plutus transaction number");
        return billingService.generateTransactionNumber();
    }

    /**
     * GET  /_invoice/email to send email
     *
     * @throws Exception
     */
    @GetMapping("/_invoice/email")
    //@Timed
    public ResponseEntity<Void> sendEmail(@RequestParam(required = false) Long dispenseId, @RequestParam(required = false) String dispenseNumber) throws Exception {

        log.debug("REST request to email");
        dispenseService.sendDocument(dispenseId, dispenseNumber);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Invoice has been sent", "")).build();
    }

    /***
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws Exception
     */

    @GetMapping("_dashboard/dispenses/discount")
    public ResponseEntity<Map<String, Object>> getDispenseDiscount(@RequestParam(value = "unitId") Long unitId,
                                                                   @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
                                                                   @RequestParam(value = "toDate") @DateTimeFormat(pattern = "yyy-MM-dd") Date toDate) throws URISyntaxException, IOException, Exception {

        log.debug("REST request to dashboard Dispense Discount for query {unitId" + unitId + " " + fromDate + "-" + toDate + "} ");
        Map<String, Object> result = dispenseService.dashboardDiscount(unitId, fromDate, toDate);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /***
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws Exception
     */
    @GetMapping("_dashboard/dispenses/productivity")
    public ResponseEntity<Map<String, Object>> getDispenseProductivity(@RequestParam(value = "unitId") Long unitId,
                                                                       @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
                                                                       @RequestParam(value = "toDate") @DateTimeFormat(pattern = "yyy-MM-dd") Date toDate) throws URISyntaxException, IOException, Exception {

        log.debug("REST request to dashboard Dispense Productivity for query {unitId" + unitId + " " + fromDate + "-" + toDate + "} ");
        Map<String, Object> result = dispenseService.dashboardDispenseProductivity(unitId, fromDate, toDate);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /***
     *
     * @param unitId
     * @param format
     * @param fromDate
     * @param toDate
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws Exception
     */
    @GetMapping("_dashboard/dispenses/productivityTrend")
    public ResponseEntity<Map<String, Object>> getDispenseProductivityTrend(@RequestParam(value = "unitId") Long unitId,
                                                                            @RequestParam(value = "format") String format,
                                                                            @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
                                                                            @RequestParam(value = "toDate") @DateTimeFormat(pattern = "yyy-MM-dd") Date toDate) throws URISyntaxException, IOException, Exception {

        log.debug("REST request to dashboard Dispense Productivity Trend for query {unitId" + unitId + " " + fromDate + "-" + toDate + "} ");

        List<String> availableFormats = Arrays.asList("YEAR", "MONTH", "WEEK", "DAY");
        boolean isAvailableFormats = availableFormats.contains(format.toUpperCase());
        if (!isAvailableFormats)
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("Invalid Format", "format")).body(null);

        Map<String, Object> result = dispenseService.dashboardDispenseProductivityTrend(unitId, format, fromDate, toDate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET  /update-item/dispense
     *
     * @throws Exception
     */
    @GetMapping("/update-item/dispense")
    //@Timed
    public ResponseEntity<Dispense> updateItem(@RequestParam(required = false) Long dispenseId, @RequestParam(required = false) String dispenseNumber) throws Exception {

        log.debug("REST request to updateItem");
        Dispense dispense = dispenseService.updateItem(dispenseId, dispenseNumber);
        return new ResponseEntity<>(dispense, HttpStatus.OK);
    }

    /**
     * validate Plan rule authorization
     *
     * @param dispense
     * @param includePreAuthorizationAmount
     * @return
     * @throws Exception
     */
    @PostMapping("_validate/dispenses")
    //@Timed
    public ResponseEntity<Dispense> validateDispensePlanRule(@Valid @RequestBody Dispense dispense, @RequestParam(required = false) Boolean includePreAuthorizationAmount) throws Exception {
        log.debug("REST request to validate  Dispense : {}", dispense);
        if (null == includePreAuthorizationAmount || Boolean.FALSE.equals(includePreAuthorizationAmount)) {
            planExecutionService.validateDispensePlanRule(dispense);
        } else {
            planExecutionService.validatePlanRule(dispense);
        }
        return new ResponseEntity<>(dispense, HttpStatus.OK);
    }

    /***
     *
     * @param dispenseNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     * @throws Exception
     */
    @GetMapping("/_compare/document-version")
    //@Timed
    public ResponseEntity<Map<String, Object>> compareVersion(@RequestParam(required = true) String dispenseNumber,
                                                              @RequestParam(required = true) Integer versionFirst,
                                                              @RequestParam(required = true) Integer versionSecond) throws Exception {

        log.debug("REST request to get documents for comparision");
        Map<String, Object> result = dispenseService.compareVersion(dispenseNumber, versionFirst, versionSecond);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/ip-dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102120101')")
    public ResponseEntity<Object> createIPDispense(@Valid @RequestBody Dispense dispense,@RequestParam String action) throws Exception {
        log.debug("REST request to save Dispense : {}", dispense);
        if (dispense.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new dispense cannot already have an ID")).body(null);
        }
        Map<String, Object> documentMap;
        //validate medication request status before dispensing
        if(DispenseType.ORDER.equals(dispense.getDocument().getDispenseType()))
            dispenseService.validateMedicationRequestStatus(dispense);
        try {
            if("draft".equalsIgnoreCase(action))
                dispense.getDocument().setDispenseStatus(DispenseStatus.DRAFT);
            else {
                dispenseService.costPricingInclusiveDiscountValidation(dispense);
                dispense.getDocument().setDispenseStatus(DispenseStatus.DISPENSED);
            }
            documentMap = dispenseService.saveIPDispense(dispense);
        } catch (Exception e) {
            log.error("Exception occurred while ip dispense : {} ", e);
            dispenseService.reIndex(dispense.getId());
            dispenseService.validateAndDeleteStockReserve(dispense.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/ip-dispenses/" + dispense.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, dispense.getId().toString()))
            .body(documentMap);
    }

    @PostMapping("/ip-direct-dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102120104')")
    public ResponseEntity<Object> createIPDirectDispense(@Valid @RequestBody Dispense dispense) throws Exception {
        log.debug("REST request to save Dispense : {}", dispense);
        if (dispense.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new dispense cannot already have an ID")).body(null);
        }
        dispenseService.validateMandatoryFieldsForChargeRecord(dispense);
        dispenseService.costPricingInclusiveDiscountValidation(dispense);
        Map<String, Object> documentMap;
        try {
            dispense=dispenseService.setMedicationObjectDetails(dispense);
            documentMap = dispenseService.saveIPDispense(dispense);
        } catch (Exception e) {
            log.error("Exception occurred while doing direct dispense :{} ", e);
            dispenseService.reIndex(dispense.getId());
            dispenseService.validateAndDeleteStockReserve(dispense.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/ip-direct-dispenses/" + dispense.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, dispense.getId().toString()))
            .body(documentMap);
    }

    /****
     *
     * @param dispenseNumber
     * @return
     * @throws Exception
     */
    @GetMapping("/dispense/versions")
    //@Timed
    public ResponseEntity<List<Integer>> getALlVersions(@RequestParam(required = true) String dispenseNumber) throws Exception {

        log.debug("REST request to get all version for given dispense");
        List<Integer> result = dispenseService.getAllVersion(dispenseNumber);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/_regenerate_workflow/dispenses")
    //@Timed
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to get regenerate workflow for the document: {}", documentNumber);

        billingService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }
    @GetMapping("IP/issue-slip/print/pdf")
    //@Timed
    public ResponseEntity<Resource> getIPIssueSlipByDispenseId(@RequestParam(required = false) Long dispenseId, @RequestParam(required = false) String dispenseNumber,
                                                              @RequestParam(required = false) String original) throws Exception {
        log.debug("REST request to get IP issue Slip by dispenseId : {}, dispenseNumber : {}", dispenseId, dispenseNumber);

        byte[] content = ipPrintService.getIPIssueSlipByDispenseId(dispenseId, dispenseNumber, original);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }
    @GetMapping("/_export/ip-dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102120102')")
    public Map<String, String> exportIPDispenses(@RequestParam String query, Pageable pageable) throws URISyntaxException, IOException {

        log.debug("REST request to export dispenses for query {}", query);
        return dispenseService.exportIPDispenses(query, pageable);
    }

    @PutMapping("/ip-dispenses")
    //@Timed
    @PreAuthorize("hasPrivilege('102120101')")
    public ResponseEntity<Object> updateIPDispense(@Valid @RequestBody Dispense dispense,@RequestParam String action) throws Exception {
        log.debug("REST request to save Dispense : {}", dispense);
        if (dispense.getId() == null) {
            return createIPDispense(dispense,action);
        }
        Map<String, Object> documentMap;

        if(DispenseType.ORDER.equals(dispense.getDocument().getDispenseType()))
            dispenseService.validateMedicationRequestStatus(dispense);

        dispenseService.validateMandatoryFieldsForChargeRecord(dispense);
        try {
            if("draft".equalsIgnoreCase(action))
                dispense.getDocument().setDispenseStatus(DispenseStatus.DRAFT);
            else {
                dispenseService.costPricingInclusiveDiscountValidation(dispense);
                dispense.getDocument().setDispenseStatus(DispenseStatus.DISPENSED);
            }
            documentMap = dispenseService.saveIPDispense(dispense);
        } catch (Exception e) {
            log.error("Exception occurred while ip dispense update : {} ", e);
            dispenseService.reIndex(dispense.getId());
            dispenseService.validateAndDeleteStockReserve(dispense.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/ip-dispenses/" + dispense.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, dispense.getId().toString()))
            .body(documentMap);
    }

    /**
     *
     * @param dispenseDocumentLines
     * @param unitId the id of unit
     * @return the ResponseEntity with status 200 (OK), or with status 404 (Not Found)
     */
    @PutMapping("_print/ip-dispenses/{unitId}")
    //@Timed
    public ResponseEntity<List<Map<String, String>>> getBarcodeFormats(@Valid @RequestBody List<DispenseDocumentLine> dispenseDocumentLines, @PathVariable Long unitId) throws Exception {
        log.debug("REST request to get barcodeFormats for dispenseDocumentLines : {}", dispenseDocumentLines);
        List<Map<String, String>> barcodeMap = dispensePrintBarcodeService.findBarcodeFormats(dispenseDocumentLines,unitId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(barcodeMap));
    }

    @PutMapping("_print/label/ip-dispenses")
    //@Timed
    public ResponseEntity<List<String>> getBarcodeFormatForLabelPrint(@Valid @RequestBody List<DispenseDocumentLine> dispenseDocumentLines) throws Exception {
        log.debug("REST request to get label barcodeFormats for dispenseDocumentLines : {}", dispenseDocumentLines);
        List<String> labelPrintList = dispensePrintBarcodeService.findBarcodeFormatForLabelPrint(dispenseDocumentLines);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(labelPrintList));
    }

}
