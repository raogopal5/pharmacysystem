package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockCorrection;
import org.nh.pharmacy.repository.StockCorrectionRepository;
import org.nh.pharmacy.repository.search.StockCorrectionSearchRepository;
import org.nh.pharmacy.service.StockCorrectionService;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing StockCorrection.
 */
@RestController
@RequestMapping("/api")
public class StockCorrectionResource {

    private final Logger log = LoggerFactory.getLogger(StockCorrectionResource.class);

    private static final String ENTITY_NAME = "stockCorrection";

    private final StockCorrectionService stockCorrectionService;
    private final StockCorrectionRepository stockCorrectionRepository;
    private final StockCorrectionSearchRepository stockCorrectionSearchRepository;
    private final ApplicationProperties applicationProperties;

    public StockCorrectionResource(StockCorrectionService stockCorrectionService, StockCorrectionRepository stockCorrectionRepository,
                                   StockCorrectionSearchRepository stockCorrectionSearchRepository, ApplicationProperties applicationProperties) {
        this.stockCorrectionService = stockCorrectionService;
        this.stockCorrectionRepository = stockCorrectionRepository;
        this.stockCorrectionSearchRepository = stockCorrectionSearchRepository;
        this.applicationProperties = applicationProperties;
    }
    /**
     * POST  /stock-corrections : Create a new stockCorrection.
     *
     * @param stockCorrection the stockCorrection to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockCorrection, or with status 400 (Bad Request) if the stockCorrection has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-corrections")
    //@Timed
    public ResponseEntity<StockCorrection> createStockCorrection(@Valid @RequestBody StockCorrection stockCorrection,@RequestParam(required = false) String action, @RequestParam(required = false) Boolean validationRequired) throws Exception {
        log.debug("REST request to save StockCorrection : {}", stockCorrection);
        if (stockCorrection.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockCorrection cannot already have an ID")).body(null);
        }
        StockCorrection result = null;
        if(action == null) action = "DRAFT";
        try {
            result = stockCorrectionService.save(stockCorrection, action, validationRequired);
        } catch (Exception e){
            if(stockCorrection.getId() != null) stockCorrectionService.reIndex(stockCorrection.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/stock-corrections/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-corrections : Updates an existing stockCorrection.
     *
     * @param stockCorrection the stockCorrection to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockCorrection,
     * or with status 400 (Bad Request) if the stockCorrection is not valid,
     * or with status 500 (Internal Server Error) if the stockCorrection couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-corrections")
    //@Timed
    public ResponseEntity<StockCorrection> updateStockCorrection(@Valid @RequestBody StockCorrection stockCorrection, @RequestParam(required = false) String action, @RequestParam(required = false) Boolean validationRequired) throws Exception {
        log.debug("REST request to update StockCorrection : {}", stockCorrection);
        if(action == null) action = "DRAFT";
        if (stockCorrection.getId() == null) {
            return createStockCorrection(stockCorrection, action, validationRequired);
        }
        StockCorrection result;
        try{
            result = stockCorrectionService.save(stockCorrection, action, validationRequired);
        } catch (Exception e){
            if(stockCorrection.getId() != null) stockCorrectionService.reIndex(stockCorrection.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockCorrection.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-corrections : get all the stockCorrections.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockCorrections in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-corrections")
    //@Timed
    public ResponseEntity<List<StockCorrection>> getAllStockCorrections(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockCorrections");
        Page<StockCorrection> page = stockCorrectionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-corrections");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-corrections/:id : get the "id" stockCorrection.
     *
     * @param id the id of the stockCorrection to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockCorrection, or with status 404 (Not Found)
     */
    @GetMapping("/stock-corrections/{id}")
    //@Timed
    public ResponseEntity<StockCorrection> getStockCorrection(@PathVariable Long id) {
        log.debug("REST request to get StockCorrection : {}", id);
        StockCorrection stockCorrection = stockCorrectionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockCorrection));
    }

    /**
     * DELETE  /stock-corrections/:id : delete the "id" stockCorrection.
     *
     * @param id the id of the stockCorrection to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-corrections/{id}")
    //@Timed
    public ResponseEntity<Void> deleteStockCorrection(@PathVariable Long id) {
        log.debug("REST request to delete StockCorrection : {}", id);
        stockCorrectionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-corrections?query=:query : search for the stockCorrection corresponding
     * to the query.
     *
     * @param query the query of the stockCorrection search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-corrections")
    //@Timed
    public ResponseEntity<List<StockCorrection>> searchStockCorrections(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockCorrections for query {}", query);
        Page<StockCorrection> page = stockCorrectionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-corrections");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * SEARCH  /_search/stock-corrections?query=:query : search for the stockCorrection corresponding
     * to the query.
     *
     * @param query the query of the stockCorrection search
     * @param pageable the pagination information
     * @param type the type which should be either i for include or e for exclude fields
     * @param fields the fields which should be considered to include or exclude
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-corrections/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockCorrection>> searchStockCorrections(@RequestParam String query, @ApiParam Pageable pageable,
                                                                @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockCorrections for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockCorrection> page = stockCorrectionService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-corrections");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch(SearchPhaseExecutionException e) {
            log.error("No Index found for {}",e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-corrections"),
                HttpStatus.OK);
        }
    }

    /**
     * GET  /status-count/stock-corrections?query=:query : get the status count for the stock correction corresponding
     * to the query.
     *
     * @param query    the query of the dispense search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-corrections")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockCorrectionStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {

        log.debug("REST request to get a status count of stock correction");
        Map<String, Long> countMap = stockCorrectionService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * PUT  /_workflow/stock-corrections : call execute workflow to complete the task and save the stock correction object.
     *
     * @param stockCorrection
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/stock-corrections")
    //@Timed
    public ResponseEntity<StockCorrection> executeWorkflow(@Valid @RequestBody StockCorrection stockCorrection, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        StockCorrection result = stockCorrectionService.executeWorkflow(stockCorrection, transition, taskId);
        stockCorrectionService.index(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockCorrection.getId().toString()))
            .body(result);
    }

    /**
     *  Get  /_workflow/stock-corrections : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/stock-corrections")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = stockCorrectionService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * INDEX  /_index/stock-corrections : do elastic index for the stock correction
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-corrections")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockCorrection(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Stock Correction");
        long resultCount = stockCorrectionRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockCorrectionService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockCorrectionSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    @GetMapping("/_regenerate_workflow/stock-corrections")
    //@Timed
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to regenerate workflow for the document: {}", documentNumber);

        stockCorrectionService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }

}
