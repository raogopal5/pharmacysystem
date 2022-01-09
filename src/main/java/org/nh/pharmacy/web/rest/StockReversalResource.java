package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.repository.StockReversalRepository;
import org.nh.pharmacy.repository.search.StockReversalSearchRepository;
import org.nh.pharmacy.service.StockReversalService;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing StockReversal.
 */
@RestController
@RequestMapping("/api")
public class StockReversalResource {

    private final Logger log = LoggerFactory.getLogger(StockReversalResource.class);

    private static final String ENTITY_NAME = "stockReversal";

    private final StockReversalService stockReversalService;
    private final StockReversalRepository stockReversalRepository;
    private final StockReversalSearchRepository stockReversalSearchRepository;
    private final ApplicationProperties applicationProperties;

    public StockReversalResource(StockReversalService stockReversalService, StockReversalRepository stockReversalRepository,
                                 StockReversalSearchRepository stockReversalSearchRepository, ApplicationProperties applicationProperties) {
        this.stockReversalService = stockReversalService;
        this.stockReversalRepository = stockReversalRepository;
        this.stockReversalSearchRepository = stockReversalSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /stock-reversals : Create a new stockReversal.
     *
     * @param stockReversal the stockReversal to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockReversal, or with status 400 (Bad Request) if the stockReversal has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-reversals")
    //@Timed
    @PreAuthorize("hasPrivilege('101102101')")
    public ResponseEntity<StockReversal> createStockReversal(@Valid @RequestBody StockReversal stockReversal, @RequestParam(required = false) String act) throws Exception{
        log.debug("REST request to save StockReversal : {}", stockReversal);
        if(act==null){
            act="APPROVED";
        }
        if (stockReversal.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockReversal cannot already have an ID")).body(null);
        }
        StockReversal result = null;
        try {
            result = stockReversalService.save(stockReversal, act);
        } catch (Exception e) {
            if(result != null) stockReversalService.reIndex(result.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/stock-reversals/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-reversals : Updates an existing stockReversal.
     *
     * @param stockReversal the stockReversal to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockReversal,
     * or with status 400 (Bad Request) if the stockReversal is not valid,
     * or with status 500 (Internal Server Error) if the stockReversal couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-reversals")
    //@Timed
    @PreAuthorize("hasPrivilege('101102101') OR hasPrivilege('101102105')")
    public ResponseEntity<StockReversal> updateStockReversal(@Valid @RequestBody StockReversal stockReversal, @RequestParam(required = false) String act) throws Exception{
        log.debug("REST request to update StockReversal : {}", stockReversal);
        if (act == null) {
            act = "APPROVED";
        }
        if (stockReversal.getId() == null) {
            return createStockReversal(stockReversal, act);
        }
        StockReversal result = null;
        try {
            result = stockReversalService.save(stockReversal, act);
        } catch (Exception e) {
            if(result != null) stockReversalService.reIndex(result.getId());
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockReversal.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-reversals : get all the stockReversals.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockReversals in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-reversals")
    //@Timed
    @PreAuthorize("hasPrivilege('101102102')")
    public ResponseEntity<List<StockReversal>> getAllStockReversals(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockReversals");
        Page<StockReversal> page = stockReversalService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-reversals");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-reversals/:id : get the "id" stockReversal.
     *
     * @param id the id of the stockReversal to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockReversal, or with status 404 (Not Found)
     */
    @GetMapping("/stock-reversals/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101102102')")
    public ResponseEntity<StockReversal> getStockReversal(@PathVariable Long id) {
        log.debug("REST request to get StockReversal : {}", id);
        StockReversal stockReversal = stockReversalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockReversal));
    }

    /**
     * DELETE  /stock-reversals/:id : delete the "id" stockReversal.
     *
     * @param id the id of the stockReversal to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-reversals/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101102101')")
    public ResponseEntity<Void> deleteStockReversal(@PathVariable Long id) {
        log.debug("REST request to delete StockReversal : {}", id);
        stockReversalService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-reversals?query=:query : search for the stockReversal corresponding
     * to the query.
     *
     * @param query    the query of the stockReversal search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-reversals")
    //@Timed
    public ResponseEntity<List<StockReversal>> searchStockReversals(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockReversals for query {}", query);
        Page<StockReversal> page = stockReversalService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-reversals");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * SEARCH  /_search/stock-reversals?query=:query : search for the stockReversal corresponding
     * to the query.
     *
     * @param query the query of the stockReversal search
     * @param pageable the pagination information
     * @param type the type which should be either i for include or e for exclude fields
     * @param fields the fields which should be considered to include or exclude
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-reversals/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockReversal>> searchStockReversals(@RequestParam String query, @ApiParam Pageable pageable,
                                                                @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockReversals for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockReversal> page = stockReversalService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-reversals");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch(SearchPhaseExecutionException e) {
            log.error("No Index found for {}",e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-reversals"),
                HttpStatus.OK);
        }
    }

    /**
     * GET  /status-count/stock-reversals?query=:query : get the status count for the stockReversal corresponding
     * to the query.
     *
     * @param query the query of the stockReversal search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-reversals")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockIssueStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a status count of StockReversals");
        Map<String, Long> countMap = stockReversalService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * INDEX  /_index/stock-reversals : do elastic index for the stock reversals
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-reversals")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockIndent(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Stock Reversal");
        long resultCount = stockReversalRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockReversalService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockReversalSearchRepository.refresh();


        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    @GetMapping("/_relatedDocuments/stock-reversals")
    //@Timed
    public ResponseEntity<Map> relatedDocuments(@RequestParam String documentNumber) throws IOException {
        log.debug("REST request to get all RelatedDocuments");
        Map map = stockReversalService.getRelatedDocuments(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }
}
