package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockAuditPlan;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.repository.StockAuditPlanRepository;
import org.nh.pharmacy.repository.search.StockAuditPlanSearchRepository;
import org.nh.pharmacy.service.StockAuditPlanService;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * REST controller for managing StockAuditPlan.
 */
@RestController
@RequestMapping("/api")
public class StockAuditPlanResource {

    private final Logger log = LoggerFactory.getLogger(StockAuditPlanResource.class);

    private static final String ENTITY_NAME = "stockAuditPlan";

    private final StockAuditPlanService stockAuditPlanService;

    private final StockAuditPlanRepository stockAuditPlanRepository;

    private final StockAuditPlanSearchRepository stockAuditPlanSearchRepository;

    private final ApplicationProperties applicationProperties;

    public StockAuditPlanResource(StockAuditPlanService stockAuditPlanService,StockAuditPlanRepository stockAuditPlanRepository,StockAuditPlanSearchRepository stockAuditPlanSearchRepository,ApplicationProperties applicationProperties) {
        this.stockAuditPlanService = stockAuditPlanService;
        this.stockAuditPlanRepository = stockAuditPlanRepository;
        this.stockAuditPlanSearchRepository = stockAuditPlanSearchRepository;
        this.applicationProperties = applicationProperties;
    }


    /**
     * POST  /stock-audit-plans : Create a new stockAuditPlan.
     *
     * @param stockAuditPlan the stockAuditPlan to create
     * @param action the action to be performed
     * @return the ResponseEntity with status 201 (Created) and with body the new stockAuditPlan, or with status 400 (Bad Request) if the stockAuditPlan has already an ID
     * @throws Exception
     */
    @PostMapping("/stock-audit-plans")
    //@Timed
    @PreAuthorize("hasPrivilege('101109101')")
    public ResponseEntity<StockAuditPlan> createStockAuditPlan(@Valid @RequestBody StockAuditPlan stockAuditPlan, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to save StockAuditPlan : {}", stockAuditPlan);
        if (stockAuditPlan.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockAuditPlan cannot already have an ID")).body(null);
        }
        if(!Optional.ofNullable(action).isPresent()) action = (Status.DRAFT).name();
        StockAuditPlan result = stockAuditPlanService.save(stockAuditPlan, action);
        return ResponseEntity.created(new URI("/api/stock-audit-plans/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-audit-plans : Updates an existing stockAuditPlan.
     *
     * @param stockAuditPlan the stockAuditPlan to update
     * @param action the action to be performed
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockAuditPlan,
     * or with status 400 (Bad Request) if the stockAuditPlan is not valid,
     * or with status 500 (Internal Server Error) if the stockAuditPlan couldnt be updated
     * @throws Exception
     */
    @PutMapping("/stock-audit-plans")
    //@Timed
    @PreAuthorize("hasPrivilege('101109101') OR hasPrivilege('101109105')")
    public ResponseEntity<StockAuditPlan> updateStockAuditPlan(@Valid @RequestBody StockAuditPlan stockAuditPlan, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update StockAuditPlan : {}", stockAuditPlan);
        if(!Optional.ofNullable(action).isPresent()) action = (Status.DRAFT).name();
        if (stockAuditPlan.getId() == null) {
            return createStockAuditPlan(stockAuditPlan, action);
        }
        StockAuditPlan result = stockAuditPlanService.save(stockAuditPlan, action);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockAuditPlan.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-audit-plans : get all the stockAuditPlans.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockAuditPlans in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-audit-plans")
    //@Timed
    @PreAuthorize("hasPrivilege('101109102')")
    public ResponseEntity<List<StockAuditPlan>> getAllStockAuditPlans(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockAuditPlans");
        Page<StockAuditPlan> page = stockAuditPlanService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-audit-plans");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-audit-plans/:id : get the "id" stockAuditPlan.
     *
     * @param id the id of the stockAuditPlan to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockAuditPlan, or with status 404 (Not Found)
     */
    @GetMapping("/stock-audit-plans/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101109102')")
    public ResponseEntity<StockAuditPlan> getStockAuditPlan(@PathVariable Long id) {
        log.debug("REST request to get StockAuditPlan : {}", id);
        StockAuditPlan stockAuditPlan = stockAuditPlanService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockAuditPlan));
    }

    /**
     * DELETE  /stock-audit-plans/:id : delete the "id" stockAuditPlan.
     *
     * @param id the id of the stockAuditPlan to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-audit-plans/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101109101')")
    public ResponseEntity<Void> deleteStockAuditPlan(@PathVariable Long id) {
        log.debug("REST request to delete StockAuditPlan : {}", id);
        stockAuditPlanService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-audit-plans?query=:query : search for the stockAuditPlan corresponding
     * to the query.
     *
     * @param query the query of the stockAuditPlan search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-audit-plans")
    //@Timed
    public ResponseEntity<List<StockAuditPlan>> searchStockAuditPlans(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockAuditPlans for query {}", query);
        Page<StockAuditPlan> page = stockAuditPlanService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-audit-plans");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * INDEX  /_index/stock-audit-plans : do elastic index for the stock audit plan
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-audit-plans")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockAuditPlan(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Stock Audit Plan");
        long resultCount = stockAuditPlanRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockAuditPlanService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockAuditPlanSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

}
