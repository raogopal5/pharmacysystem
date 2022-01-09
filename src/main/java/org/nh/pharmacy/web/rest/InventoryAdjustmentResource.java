package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.InventoryAdjustment;
import org.nh.pharmacy.repository.InventoryAdjustmentRepository;
import org.nh.pharmacy.repository.search.InventoryAdjustmentSearchRepository;
import org.nh.pharmacy.service.InventoryAdjustmentService;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST controller for managing InventoryAdjustment.
 */
@RestController
@RequestMapping("/api")
public class InventoryAdjustmentResource {

    private final Logger log = LoggerFactory.getLogger(InventoryAdjustmentResource.class);

    private static final String ENTITY_NAME = "inventoryAdjustment";

    private final InventoryAdjustmentService inventoryAdjustmentService;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final InventoryAdjustmentSearchRepository inventoryAdjustmentSearchRepository;
    private final ApplicationProperties applicationProperties;

    public InventoryAdjustmentResource(InventoryAdjustmentService inventoryAdjustmentService, ApplicationProperties applicationProperties,
                                       InventoryAdjustmentRepository inventoryAdjustmentRepository, InventoryAdjustmentSearchRepository inventoryAdjustmentSearchRepository) {
        this.inventoryAdjustmentService = inventoryAdjustmentService;
        this.applicationProperties = applicationProperties;
        this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
        this.inventoryAdjustmentSearchRepository = inventoryAdjustmentSearchRepository;
    }

    /**
     * POST  /inventory-adjustments : Create a new inventoryAdjustment.
     *
     * @param inventoryAdjustment the inventoryAdjustment to create
     * @return the ResponseEntity with status 201 (Created) and with body the new inventoryAdjustment, or with status 400 (Bad Request) if the inventoryAdjustment has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/inventory-adjustments")
    //@Timed
    @PreAuthorize("hasPrivilege('101111101')")
    public ResponseEntity<InventoryAdjustment> createInventoryAdjustment(@Valid @RequestBody InventoryAdjustment inventoryAdjustment, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to save InventoryAdjustment : {}", inventoryAdjustment);
        if (inventoryAdjustment.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new inventoryAdjustment cannot already have an ID")).body(null);
        }
        if (action == null) action = "DRAFT";
        InventoryAdjustment result;
        try {
            result = inventoryAdjustmentService.save(inventoryAdjustment, action);
        } catch (Exception e) {
            inventoryAdjustmentService.reIndex(inventoryAdjustment.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/inventory-adjustments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /inventory-adjustments : Updates an existing inventoryAdjustment.
     *
     * @param inventoryAdjustment the inventoryAdjustment to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated inventoryAdjustment,
     * or with status 400 (Bad Request) if the inventoryAdjustment is not valid,
     * or with status 500 (Internal Server Error) if the inventoryAdjustment couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/inventory-adjustments")
    //@Timed
    @PreAuthorize("hasPrivilege('101111101') OR hasPrivilege('101111105')")
    public ResponseEntity<InventoryAdjustment> updateInventoryAdjustment(@Valid @RequestBody InventoryAdjustment inventoryAdjustment, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update InventoryAdjustment : {}", inventoryAdjustment);
        if (action == null) action = "DRAFT";
        if (inventoryAdjustment.getId() == null) {
            return createInventoryAdjustment(inventoryAdjustment, action);
        }
        InventoryAdjustment result;
        try {
            result = inventoryAdjustmentService.save(inventoryAdjustment, action);
        } catch (Exception e) {
            inventoryAdjustmentService.reIndex(inventoryAdjustment.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, inventoryAdjustment.getId().toString()))
            .body(result);
    }

    /**
     * GET  /inventory-adjustments : get all the inventoryAdjustments.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of inventoryAdjustments in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/inventory-adjustments")
    //@Timed
    @PreAuthorize("hasPrivilege('101111102')")
    public ResponseEntity<List<InventoryAdjustment>> getAllInventoryAdjustments(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of InventoryAdjustments");
        Page<InventoryAdjustment> page = inventoryAdjustmentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/inventory-adjustments");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /inventory-adjustments/:id : get the "id" inventoryAdjustment.
     *
     * @param id the id of the inventoryAdjustment to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the inventoryAdjustment, or with status 404 (Not Found)
     */
    @GetMapping("/inventory-adjustments/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101111102')")
    public ResponseEntity<InventoryAdjustment> getInventoryAdjustment(@PathVariable Long id) {
        log.debug("REST request to get InventoryAdjustment : {}", id);
        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(inventoryAdjustment));
    }

    /**
     * DELETE  /inventory-adjustments/:id : delete the "id" inventoryAdjustment.
     *
     * @param id the id of the inventoryAdjustment to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/inventory-adjustments/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101111101')")
    public ResponseEntity<Void> deleteInventoryAdjustment(@PathVariable Long id) {
        log.debug("REST request to delete InventoryAdjustment : {}", id);
        inventoryAdjustmentService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/inventory-adjustments?query=:query : search for the inventoryAdjustment corresponding
     * to the query.
     *
     * @param query the query of the inventoryAdjustment search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/inventory-adjustments")
    //@Timed
    public ResponseEntity<List<InventoryAdjustment>> searchInventoryAdjustments(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of InventoryAdjustments for query {}", query);
        Page<InventoryAdjustment> page = inventoryAdjustmentService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/inventory-adjustments");
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
    @GetMapping("/_search/inventory-adjustments/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<InventoryAdjustment>> searchInventoryAdjustments(@RequestParam String query, @ApiParam Pageable pageable,
                                                              @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of InventoryAdjustments for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<InventoryAdjustment> page = inventoryAdjustmentService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/inventory-adjustments");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (SearchPhaseExecutionException e) {
            log.error("No Index found for {}", e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/inventory-adjustments"),
                HttpStatus.OK);
        }
    }


    /**
     * GET  /status-count/inventory-adjustments?query=:query : get the status count for the Inventory Adjustment corresponding
     * to the query.
     *
     * @param query    the query of the inventory adjustment search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/inventory-adjustments")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllInventoryAdjustmentStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {

        log.debug("REST request to get a status count of inventory adjustment");
        Map<String, Long> countMap = inventoryAdjustmentService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * INDEX  /_index/inventory-adjustments : do elastic index for the inventory adjustment
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/inventory-adjustments")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexInventoryAdjustment(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on InventoryAdjustment");
        long resultCount = inventoryAdjustmentRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            inventoryAdjustmentService.doIndex(i, pageSize, fromDate, toDate);
        }
        inventoryAdjustmentSearchRepository.refresh();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    /**
     * PUT  /_workflow/inventory-adjustments : call execute workflow to complete the task and save the inventory adjustment object.
     *
     * @param inventoryAdjustment
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/inventory-adjustments")
    //@Timed
    public ResponseEntity<InventoryAdjustment> executeWorkflow(@Valid @RequestBody InventoryAdjustment inventoryAdjustment, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        InventoryAdjustment result;
        try {
            result = inventoryAdjustmentService.executeWorkflow(inventoryAdjustment, transition, taskId);
            inventoryAdjustmentService.index(result);
        } catch (Exception e) {
            inventoryAdjustmentService.reIndex(inventoryAdjustment.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, inventoryAdjustment.getId().toString()))
            .body(result);
    }

    /**
     *  Get  /_workflow/inventory-adjustments : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/inventory-adjustments")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = inventoryAdjustmentService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    @GetMapping("/_relatedDocuments/inventory-adjustments")
    //@Timed
    public ResponseEntity<Map> relatedDocuments(@RequestParam String documentNumber) throws IOException {
        log.debug("REST request to get all RelatedDocuments");
        Map map = inventoryAdjustmentService.getRelatedDocuments(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }

    /**
     * GET  /_download/inventory-adjustments/all : export adjustment lists
     *
     * @return
     */
    @GetMapping("/_download/inventory-adjustments/all")
    //@Timed
    public Map<String, String> downloadInventoryAdjustmentList(@RequestParam String query, Pageable pageable) throws Exception {
        File inventoryAdjustmentFile = ExportUtil.getCSVExportFile("adjustment", applicationProperties.getAthmaBucket().getTempExport());
        inventoryAdjustmentService.generateInventoryAdjustmentList(inventoryAdjustmentFile, query, pageable);
        Map<String, String> adjustmentFileDetails = new HashMap<>();
        adjustmentFileDetails.put("fileName", inventoryAdjustmentFile.getName());
        adjustmentFileDetails.put("pathReference", "tempExport");
        return adjustmentFileDetails;
    }

    @GetMapping("/_regenerate_workflow/inventory-adjustments")
    //@Timed
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to regenerate workflow for the document: {}", documentNumber);

        inventoryAdjustmentService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }

}
