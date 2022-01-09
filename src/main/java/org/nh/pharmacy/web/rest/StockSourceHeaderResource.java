package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockSourceHeader;
import org.nh.pharmacy.repository.StockSourceHeaderRepository;
import org.nh.pharmacy.repository.search.StockSourceHeaderSearchRepository;
import org.nh.pharmacy.service.StockSourceHeaderService;
import org.nh.pharmacy.web.rest.errors.BadRequestAlertException;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing StockSourceHeader.
 */
@RestController
@RequestMapping("/api")
public class StockSourceHeaderResource {

    private final Logger log = LoggerFactory.getLogger(StockSourceHeaderResource.class);

    private static final String ENTITY_NAME = "stockSourceHeader";

    private final StockSourceHeaderService stockSourceHeaderService;

    private final StockSourceHeaderRepository stockSourceHeaderRepository;

    private final StockSourceHeaderSearchRepository stockSourceHeaderSearchRepository;

    private final ApplicationProperties applicationProperties;

    public StockSourceHeaderResource(StockSourceHeaderService stockSourceHeaderService,StockSourceHeaderRepository stockSourceHeaderRepository,StockSourceHeaderSearchRepository stockSourceHeaderSearchRepository,ApplicationProperties applicationProperties) {
        this.stockSourceHeaderService = stockSourceHeaderService;
        this.stockSourceHeaderRepository=stockSourceHeaderRepository;
        this.stockSourceHeaderSearchRepository=stockSourceHeaderSearchRepository;
        this.applicationProperties=applicationProperties;
    }

    /**
     * POST  /stock-source-headers : Create a new stockSourceHeader.
     *
     * @param stockSourceHeader the stockSourceHeader to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockSourceHeader, or with status 400 (Bad Request) if the stockSourceHeader has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-source-headers")
    //@Timed
    public ResponseEntity<StockSourceHeader> createStockSourceHeader(@Valid @RequestBody StockSourceHeader stockSourceHeader) throws URISyntaxException {
        log.debug("REST request to save StockSourceHeader : {}", stockSourceHeader);
        if (stockSourceHeader.getId() != null) {
            throw new BadRequestAlertException("A new stockSourceHeader cannot already have an ID", ENTITY_NAME, "idexists");
        }
        StockSourceHeader result = stockSourceHeaderService.save(stockSourceHeader);
        return ResponseEntity.created(new URI("/api/stock-source-headers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-source-headers : Updates an existing stockSourceHeader.
     *
     * @param stockSourceHeader the stockSourceHeader to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockSourceHeader,
     * or with status 400 (Bad Request) if the stockSourceHeader is not valid,
     * or with status 500 (Internal Server Error) if the stockSourceHeader couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-source-headers")
    //@Timed
    public ResponseEntity<StockSourceHeader> updateStockSourceHeader(@Valid @RequestBody StockSourceHeader stockSourceHeader) throws URISyntaxException {
        log.debug("REST request to update StockSourceHeader : {}", stockSourceHeader);
        if (stockSourceHeader.getId() == null) {
            return createStockSourceHeader(stockSourceHeader);
        }
        StockSourceHeader result = stockSourceHeaderService.save(stockSourceHeader);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockSourceHeader.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-source-headers : get all the stockSourceHeaders.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockSourceHeaders in body
     */
    @GetMapping("/stock-source-headers")
    //@Timed
    public ResponseEntity<List<StockSourceHeader>> getAllStockSourceHeaders(Pageable pageable) {
        log.debug("REST request to get a page of StockSourceHeaders");
        Page<StockSourceHeader> page = stockSourceHeaderService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-source-headers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-source-headers/:id : get the "id" stockSourceHeader.
     *
     * @param id the id of the stockSourceHeader to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockSourceHeader, or with status 404 (Not Found)
     */
    @GetMapping("/stock-source-headers/{id}")
    //@Timed
    public ResponseEntity<StockSourceHeader> getStockSourceHeader(@PathVariable Long id) {
        log.debug("REST request to get StockSourceHeader : {}", id);
        StockSourceHeader stockSourceHeader = stockSourceHeaderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockSourceHeader));
    }

    /**
     * DELETE  /stock-source-headers/:id : delete the "id" stockSourceHeader.
     *
     * @param id the id of the stockSourceHeader to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-source-headers/{id}")
    //@Timed
    public ResponseEntity<Void> deleteStockSourceHeader(@PathVariable Long id) {
        log.debug("REST request to delete StockSourceHeader : {}", id);
        stockSourceHeaderService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-source-headers?query=:query : search for the stockSourceHeader corresponding
     * to the query.
     *
     * @param query the query of the stockSourceHeader search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/stock-source-headers")
    //@Timed
    public ResponseEntity<List<StockSourceHeader>> searchStockSourceHeaders(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of StockSourceHeaders for query {}", query);
        Page<StockSourceHeader> page = stockSourceHeaderService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-source-headers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * GET  /stock-source-headers/getGRN  get all the stockSourceHeaders and count for given params.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of stockSourceHeaders and count
     */
    @GetMapping("/stock-source-headers/getGRN")
    //@Timed
    public ResponseEntity<List<Map<String,Object>>> getStockSourceHeaderByDocumentNo(@RequestParam(required = false) String documentNumber , @RequestParam String unitCode, @RequestParam(required = false) String fromDate,@RequestParam(required = false) String toDate,@RequestParam(required = false) Long itemId, @RequestParam(required = false) Integer size, @RequestParam(required = false) Integer pageNumber ) {
        log.debug("Request to get stockSourceHeaderDetail for documentNumber : {} unitCode : {}", documentNumber,unitCode);
        List<Map<String,Object>> mapList= stockSourceHeaderService.getStockSourceHeaderByDocumentNo(documentNumber, unitCode, fromDate , toDate, itemId, size, pageNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(mapList));
    }
    @GetMapping("/_index/stock-source-header")
    public ResponseEntity<Void> indexStockSourceHeader(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on stock source header");
        long resultCount =stockSourceHeaderRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockSourceHeaderService.doIndex(i, pageSize, fromDate, toDate);
            stockSourceHeaderSearchRepository.refresh();;
        }
        return ResponseEntity.ok().headers(org.nh.billing.web.rest.util.HeaderUtil.createAlert("Elastic search index is completed", "")).build();
    }
}
