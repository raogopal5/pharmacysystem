package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.DispenseReturnDocumentLine;
import org.nh.pharmacy.repository.DispenseReturnRepository;
import org.nh.pharmacy.repository.search.DispenseReturnSearchRepository;
import org.nh.pharmacy.service.DispenseReturnService;
import org.nh.pharmacy.service.IpDispenseReturnPrintService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
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
 * REST controller for managing DispenseReturn.
 */
@RestController
@RequestMapping("/api")
public class DispenseReturnResource {

    private final Logger log = LoggerFactory.getLogger(DispenseReturnResource.class);

    private static final String ENTITY_NAME = "dispenseReturn";

    private final DispenseReturnService dispenseReturnService;
    private final IpDispenseReturnPrintService ipDispenseReturnPrintService;
    private final DispenseReturnRepository dispenseReturnRepository;
    private final DispenseReturnSearchRepository dispenseReturnSearchRepository;
    private final ApplicationProperties applicationProperties;

    public DispenseReturnResource(DispenseReturnService dispenseReturnService, DispenseReturnRepository dispenseReturnRepository,
                                  DispenseReturnSearchRepository dispenseReturnSearchRepository, ApplicationProperties applicationProperties,IpDispenseReturnPrintService ipDispenseReturnPrintService) {
        this.dispenseReturnService = dispenseReturnService;
        this.dispenseReturnRepository = dispenseReturnRepository;
        this.dispenseReturnSearchRepository = dispenseReturnSearchRepository;
        this.applicationProperties = applicationProperties;
        this.ipDispenseReturnPrintService = ipDispenseReturnPrintService;
    }

    /**
     * POST  /dispense-returns : Create a new dispenseReturn.
     *
     * @param dispenseReturn the dispenseReturn to create
     * @return the ResponseEntity with status 201 (Created) and with body the new dispenseReturn, or with status 400 (Bad Request) if the dispenseReturn has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/dispense-returns")
    //@Timed
    @PreAuthorize("hasPrivilege('102104101')")
    public ResponseEntity<DispenseReturn> createDispenseReturn(@Valid @RequestBody DispenseReturn dispenseReturn, @RequestParam(required = false) String action ) throws Exception {
        log.debug("REST request to save DispenseReturn : {}, with action {}", dispenseReturn, action);
        Map<String, Object> documentMap = null;
        if (dispenseReturn.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new dispenseReturn cannot already have an ID")).body(null);
        }
        try {
            documentMap = dispenseReturnService.processDispenseReturn(dispenseReturn, action);
            dispenseReturn = (DispenseReturn) documentMap.get("dispenseReturn");
        } catch (Exception e) {
            try {
                dispenseReturnService.reIndex(dispenseReturn);
            } catch (Exception ex) {
                log.error("Getting error while reindexing the dispense return creation : ", e);
            }
            throw e;
        }
        return ResponseEntity.created(new URI("/api/dispense-returns/" + dispenseReturn.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, dispenseReturn.getId().toString()))
            .body(dispenseReturn);
    }

    /**
     * PUT  /dispense-returns : Updates an existing dispenseReturn.
     *
     * @param dispenseReturn the dispenseReturn to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated dispenseReturn,
     * or with status 400 (Bad Request) if the dispenseReturn is not valid,
     * or with status 500 (Internal Server Error) if the dispenseReturn couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/dispense-returns")
    //@Timed
    @PreAuthorize("hasPrivilege('102104101')")
    public ResponseEntity<DispenseReturn> updateDispenseReturn(@Valid @RequestBody DispenseReturn dispenseReturn,  @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update DispenseReturn : {}, with action {}", dispenseReturn, action);
        if (dispenseReturn.getId() == null) {
            return createDispenseReturn(dispenseReturn, action);
        }
        Map<String, Object> documentMap = null;
        try {
            documentMap = dispenseReturnService.processDispenseReturn(dispenseReturn, action);
            dispenseReturn = (DispenseReturn) documentMap.get("dispenseReturn");
        } catch (Exception e) {
            try {
                dispenseReturnService.reIndex(dispenseReturn);
            } catch (Exception ex) {
                log.error("Getting error while reindexing the dispense return updation : ", e);
            }
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dispenseReturn.getId().toString()))
            .body(dispenseReturn);
    }

    /**
     * GET  /dispense-returns : get all the dispenseReturns.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of dispenseReturns in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/dispense-returns")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102')")
    public ResponseEntity<List<DispenseReturn>> getAllDispenseReturns(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of DispenseReturns");
        Page<DispenseReturn> page = dispenseReturnService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/dispense-returns");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /dispense-returns/:id : get the "id" dispenseReturn.
     *
     * @param id the id of the dispenseReturn to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dispenseReturn, or with status 404 (Not Found)
     */
    @GetMapping("/dispense-returns/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102') OR hasPrivilege('102121102')")
    public ResponseEntity<DispenseReturn> getDispenseReturn(@PathVariable Long id) {
        log.debug("REST request to get DispenseReturn : {}", id);
        DispenseReturn dispenseReturn = dispenseReturnService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dispenseReturn));
    }

    /**
     * DELETE  /dispense-returns/:id : delete the "id" dispenseReturn.
     *
     * @param id the id of the dispenseReturn to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/dispense-returns/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('102104101')")
    public ResponseEntity<Void> deleteDispenseReturn(@PathVariable Long id) {
        log.debug("REST request to delete DispenseReturn : {}", id);
        dispenseReturnService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/dispense-returns?query=:query : search for the dispenseReturn corresponding
     * to the query.
     *
     * @param query the query of the dispenseReturn search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/dispense-returns")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102') OR hasPrivilege('102121102') OR hasPrivilege('102121105')")
    public ResponseEntity<List<DispenseReturn>> searchDispenseReturns(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of DispenseReturns for query {}", query);
        Page<DispenseReturn> page = dispenseReturnService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/dispense-returns");
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
    @GetMapping("/_search/dispense-returns/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<DispenseReturn>> searchDispenseReturns(@RequestParam String query, @ApiParam Pageable pageable,
                                                          @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of DispenseReturns for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<DispenseReturn> page = dispenseReturnService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/dispense-returns");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (SearchPhaseExecutionException e) {
            log.error("No Index found for {}", e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/dispense-returns"),
                HttpStatus.OK);
        }
    }

    /**
     * GET  /status-count/dispense-returns?query=:query : get the status count for the Dispense Return corresponding
     * to the query.
     *
     * @param query    the query of the dispense search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/dispense-returns")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllDispenseReturnStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {

        log.debug("REST request to get a status count of Dispense Return");
        Map<String, Long> countMap = dispenseReturnService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * INDEX  /_index/dispense-returns : do elastic index for the Dispense Return
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/dispense-returns")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexDispenseReturn(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Dispense Return");
        long resultCount = dispenseReturnRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            dispenseReturnService.doIndex(i, pageSize, fromDate, toDate);
        }
        dispenseReturnSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    @GetMapping("/_index/dispense-returns/{id}")
    public ResponseEntity<DispenseReturn> reindexReturns(@PathVariable Long id) throws Exception {
        log.debug("REST request to get reindex for return by id : {}", id);
        DispenseReturn dispenseReturn = dispenseReturnService.reIndex(id);
        return new ResponseEntity<>(dispenseReturn, HttpStatus.OK);
    }

    /**
     * EXPORT  /_export/dispense-returns?query=:query : export csv file for the dispense-returns corresponding to the query.
     *
     * @param query the query of the dispense-returns search
     * @param pageable the pagination information
     * @throws URISyntaxException,IOException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_export/dispense-returns")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102')")
    public Map<String, String> exportDispenseReturn(@RequestParam String query, Pageable pageable) throws URISyntaxException, IOException {

        log.debug("REST request to export dispense Return for query {}", query);
        return dispenseReturnService.exportDispenseReturn(query, pageable);
    }

    /**
     * PUT  /_workflow/dispense-returns : call execute workflow to complete the task and save the dispense returns
     *
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/dispense-returns")
    //@Timed
    public ResponseEntity<DispenseReturn> executeWorkflow(@Valid @RequestBody DispenseReturn dispenseReturn, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        Map<String, Object> documentMap =null;
        try {
            documentMap=dispenseReturnService.executeWorkflow(dispenseReturn, transition, taskId);
            dispenseReturn = (DispenseReturn) documentMap.get("dispenseReturn");
        } catch (Exception e) {
            try {
                dispenseReturnService.reIndex(dispenseReturn);
            } catch (Exception ex) {
                log.error("Getting error while reindexing the dispense return creation : ", e);
            }
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dispenseReturn.getId().toString()))
            .body(dispenseReturn);
    }

    /**
     * Get  /_workflow/dispenses : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/dispense-returns")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = dispenseReturnService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * POST  /calculate-tax/dispense-returns-line calculate tax for line
     *
     * @param documentLine line details
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @PostMapping("/calculate-tax/dispense-returns-line")
    //@Timed
    public ResponseEntity<DispenseReturnDocumentLine> taxCalculationForLine(@RequestBody DispenseReturnDocumentLine documentLine,
                                                                            @RequestParam String unitCode) throws URISyntaxException {

        log.debug("REST request to calculate tax for unitCode: {}, return line {}", documentLine);
        documentLine = dispenseReturnService.taxCalculationForLine(documentLine, unitCode);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(documentLine));
    }

    /**
     * POST  /calculate-tax/dispense-returns calculate tax for dispense returns
     *l
     * @param dispenseReturn dispense return details
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @PostMapping("/calculate-tax/dispense-returns")
    //@Timed
    public ResponseEntity<DispenseReturn> taxCalculationForReturn(@RequestBody DispenseReturn dispenseReturn) throws URISyntaxException {

        log.debug("REST request to calculate tax for dispenseReturn {}", dispenseReturn);
        dispenseReturn = dispenseReturnService.calculateDispenseReturnDetail(dispenseReturn);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dispenseReturn));
    }

    /**
     * GET  /dispense-returns/:id : get the "id" dispenseReturn.
     *
     * @param documentNumber the id of the dispenseReturn to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dispenseReturn, or with status 404 (Not Found)
     */
    @GetMapping("/dispense-returns-invoice/{documentNumber}")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102')")
    public ResponseEntity<DispenseReturn> getDispenseReturnbyInvoice(@PathVariable String documentNumber) {
        log.debug("REST request to get DispenseReturn : {}");
        DispenseReturn dispenseReturn = dispenseReturnService.createDispenseReturnDocument(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dispenseReturn));
    }

    @GetMapping("/dispense-returns/print")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102')")
    public ResponseEntity<byte[]> getReturnHTMLByReturnId(@RequestParam(required = false) Long dispenseReturnId, @RequestParam(required = false) String dispenseReturnNumber) throws Exception{
        log.debug("REST request to get dispenseReturn by dispenseReturnId : {}, dispenseReturnNumber : {}", dispenseReturnId, dispenseReturnNumber);
        Map<String, Object> fileOutPut = new HashMap<>();
        fileOutPut = dispenseReturnService.getReturnHTMLByReturnId(dispenseReturnId, dispenseReturnNumber);
        byte[] content = (byte[]) fileOutPut.get("content");
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + fileOutPut.get("fileName"));
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentLength(content.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @GetMapping("/dispense-returns/print/pdf")
    //@Timed
    @PreAuthorize("hasPrivilege('102104102')")
    public ResponseEntity<Resource> getReturnPdfByReturnId(@RequestParam(required = false) Long dispenseReturnId,
                                                           @RequestParam(required = false) String dispenseReturnNumber) throws Exception {

        log.debug("REST request to get dispenseReturn by dispenseReturnId : {}, dispenseReturnNumber : {}", dispenseReturnId, dispenseReturnNumber);
        byte[] content = dispenseReturnService.getReturnPdfByReturnId(dispenseReturnId, dispenseReturnNumber);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    /***
     *
     * @param dispenseReturnNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     * @throws Exception
     */
    @GetMapping("/dispense-returns/compare-document-version")
    //@Timed
    public ResponseEntity<Map<String, Object>> compareVersion(@RequestParam(required = true) String dispenseReturnNumber,
                                                              @RequestParam(required = true) Integer versionFirst,
                                                              @RequestParam(required = true) Integer versionSecond) throws Exception {

        log.debug("REST request to get documents for comparision");
        Map<String, Object> result = dispenseReturnService.compareVersion(dispenseReturnNumber, versionFirst, versionSecond);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /****
     *
     * @param dispenseReturnNumber
     * @return
     * @throws Exception
     */
    @GetMapping("/dispense-returns/versions")
    //@Timed
    public ResponseEntity<List<Integer>> getALlVersions(@RequestParam(required = true) String dispenseReturnNumber) throws Exception {

        log.debug("REST request to get all version for given dispenseReturn");
        List<Integer> result = dispenseReturnService.getAllVersion(dispenseReturnNumber);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    /****
     *
     * @param dispenseReturnNumber
     * @return
     * @throws Exception
     */
    @GetMapping("/IP/dispense-returns/print/pdf")
    //@Timed
    public ResponseEntity<Resource> getIpDispenseReturnPdf(@RequestParam(required = false) Long dispenseReturnId,
                                                           @RequestParam(required = false) String dispenseReturnNumber) throws Exception {

        log.debug("REST request to get dispenseReturn by dispenseReturnId : {}, dispenseReturnNumber : {}", dispenseReturnId, dispenseReturnNumber);
        byte[] content = ipDispenseReturnPrintService.getIpDispenseReturnPdf(dispenseReturnId, dispenseReturnNumber);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    @GetMapping("/_regenerate_workflow/dispense-returns")
    //@Timed
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to get regenerate workflow for the document: {}", documentNumber);

        dispenseReturnService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }

}
