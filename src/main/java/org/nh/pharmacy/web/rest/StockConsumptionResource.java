package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockConsumption;
import org.nh.pharmacy.domain.dto.ConsumptionDocumentLine;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.repository.StockConsumptionRepository;
import org.nh.pharmacy.repository.search.StockConsumptionSearchRepository;
import org.nh.pharmacy.service.StockConsumptionService;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing StockConsumption.
 */
@RestController
@RequestMapping("/api")
public class StockConsumptionResource {

    private final Logger log = LoggerFactory.getLogger(StockConsumptionResource.class);

    private static final String ENTITY_NAME = "stockConsumption";

    private final StockConsumptionService stockConsumptionService;
    private final StockConsumptionRepository stockConsumptionRepository;
    private final StockConsumptionSearchRepository stockConsumptionSearchRepository;
    private final ApplicationProperties applicationProperties;

    public StockConsumptionResource(StockConsumptionService stockConsumptionService, StockConsumptionRepository stockConsumptionRepository,
                                    StockConsumptionSearchRepository stockConsumptionSearchRepository, ApplicationProperties applicationProperties) {
        this.stockConsumptionService = stockConsumptionService;
        this.stockConsumptionRepository = stockConsumptionRepository;
        this.stockConsumptionSearchRepository = stockConsumptionSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /stock-consumptions : Create a new stockConsumption.
     *
     * @param stockConsumption the stockConsumption to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockConsumption, or with status 400 (Bad Request) if the stockConsumption has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-consumptions")
    //@Timed
    @PreAuthorize("hasPrivilege('101108101')")
    public ResponseEntity<StockConsumption> createStockConsumption(@Valid @RequestBody StockConsumption stockConsumption, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to save StockConsumption : {}", stockConsumption);
        if (stockConsumption.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockConsumption cannot already have an ID")).body(null);
        }
        if(!Optional.ofNullable(action).isPresent()) action = (Status.DRAFT).name();
        StockConsumption result = stockConsumptionService.save(stockConsumption, action);
        stockConsumptionService.index(result);
        return ResponseEntity.created(new URI("/api/stock-consumptions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-consumptions : Updates an existing stockConsumption.
     *
     * @param stockConsumption the stockConsumption to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockConsumption,
     * or with status 400 (Bad Request) if the stockConsumption is not valid,
     * or with status 500 (Internal Server Error) if the stockConsumption couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-consumptions")
    //@Timed
    @PreAuthorize("hasPrivilege('101108101') OR hasPrivilege('101108105')")
    public ResponseEntity<StockConsumption> updateStockConsumption(@Valid @RequestBody StockConsumption stockConsumption, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update StockConsumption : {}", stockConsumption);
        if(!Optional.ofNullable(action).isPresent()) action = (Status.DRAFT).name();
        if (stockConsumption.getId() == null) {
            return createStockConsumption(stockConsumption, action);
        }
        StockConsumption result = stockConsumptionService.save(stockConsumption, action);
        stockConsumptionService.index(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockConsumption.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-consumptions : get all the stockConsumptions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockConsumptions in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-consumptions")
    //@Timed
    @PreAuthorize("hasPrivilege('101108102')")
    public ResponseEntity<List<StockConsumption>> getAllStockConsumptions(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockConsumptions");
        Page<StockConsumption> page = stockConsumptionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-consumptions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-consumptions/:id : get the "id" stockConsumption.
     *
     * @param id the id of the stockConsumption to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockConsumption, or with status 404 (Not Found)
     */
    @GetMapping("/stock-consumptions/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101108102')")
    public ResponseEntity<StockConsumption> getStockConsumption(@PathVariable Long id) {
        log.debug("REST request to get StockConsumption : {}", id);
        StockConsumption stockConsumption = stockConsumptionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockConsumption));
    }

    /**
     * DELETE  /stock-consumptions/:id : delete the "id" stockConsumption.
     *
     * @param id the id of the stockConsumption to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-consumptions/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101108101')")
    public ResponseEntity<Void> deleteStockConsumption(@PathVariable Long id) throws BusinessRuleViolationException {
        log.debug("REST request to delete StockConsumption : {}", id);
        stockConsumptionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-consumptions?query=:query : search for the stockConsumption corresponding
     * to the query.
     *
     * @param query the query of the stockConsumption search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-consumptions")
    //@Timed
    public ResponseEntity<List<StockConsumption>> searchStockConsumptions(@RequestParam String query, @ApiParam Pageable pageable) throws URISyntaxException {
        log.debug("REST request to search for a page of StockConsumptions for query {}", query);
        HttpHeaders headers;
        try {
            Page<StockConsumption> page = stockConsumptionService.search(query, pageable);
            headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-consumptions");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Getting error while searching stock consumptions {}", e);
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-consumptions");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }
    }

    /**
     * SEARCH  /_search/stock-consumptions?query=:query : search for the stockConsumption corresponding
     * to the query.
     *
     * @param query the query of the stockConsumption search
     * @param pageable the pagination information
     * @param type the type which should be either i for include or e for exclude fields
     * @param fields the fields which should be considered to include or exclude
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-consumptions/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockConsumption>> searchStockConsumptions(@RequestParam String query, @ApiParam Pageable pageable,
                                                                @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockConsumptions for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockConsumption> page = stockConsumptionService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-consumptions");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch(SearchPhaseExecutionException e) {
            log.error("No Index found for {}",e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-consumptions"),
                HttpStatus.OK);
        }
    }

    /**
     * GET  /status-count/stock-consumptions?query=:query : get the status count for the stockConsumption corresponding
     * to the query.
     *
     * @param query the query of the stockConsumption search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-consumptions")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockConsumptionStatusCount(@RequestParam String query, @ApiParam Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get a status count of StockConsumptions");
        try{
            Map<String, Long> countMap = stockConsumptionService.getStatusCount(query);
            return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
        }catch (Exception e){
            log.error("Getting error while fetching stock consumption status count {} ", e);
            return ResponseUtil.wrapOrNotFound(Optional.of(Collections.EMPTY_MAP));
        }
    }

    /**
     * PUT  /_workflow/stock-consumptions : call execute workflow to complete the task and save the stock issue object.
     *
     * @param stockConsumption
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/stock-consumptions")
    //@Timed
    public ResponseEntity<StockConsumption> executeWorkflow(@Valid @RequestBody StockConsumption stockConsumption, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        StockConsumption result = stockConsumptionService.executeWorkflow(stockConsumption, transition, taskId);
        stockConsumptionService.index(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockConsumption.getId().toString()))
            .body(result);
    }

    /**
     *  Get  /_workflow/stock-consumptions : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/stock-consumptions")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = stockConsumptionService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * INDEX  /_index/stock-consumptions : do elastic index for the stock consumptions
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-consumptions")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> index(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on StockConsumption");
        long resultCount = stockConsumptionRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockConsumptionService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockConsumptionSearchRepository.refresh();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }
    @GetMapping("/_stock-consumption/print/pdf/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101108101')")
    public ResponseEntity<Resource> getStockConsumptionPrint(@PathVariable Long id)throws Exception {
        log.debug("REST request to get StockConsumptionPrint : {}", id);
        StockConsumption stockConsumption = stockConsumptionService.findOne(id);
        byte[] content = stockConsumptionService.getStockConsumptionPDF(stockConsumption);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    /**
     * GET  /consumption-reversal/items : get all the stockConsumption items for given criteria.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of List<ConsumptionDocumentLine> in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/consumption-reversal/items")
    //@Timed
    @PreAuthorize("hasPrivilege('101108102')")
    public ResponseEntity<List<ConsumptionDocumentLine>> getConsumptionReversalItems(@RequestParam Long itemId, @RequestParam Long consumptionHscId,
                     @RequestParam(required = false) Long forHscId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String mrn) throws Exception {
        log.debug("Request to get Consumption reversal items for itemId:{}, consumptionHscId:{}, forHscId:{}, departmentId:{}, mrn:{}", itemId, consumptionHscId,forHscId, departmentId, mrn);
        List<ConsumptionDocumentLine> items = stockConsumptionService.getConsumptionReversalItems(itemId, consumptionHscId, forHscId, departmentId, mrn);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

}
