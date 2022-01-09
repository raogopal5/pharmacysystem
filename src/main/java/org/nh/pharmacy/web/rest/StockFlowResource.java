package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.StockFlow;
import org.nh.pharmacy.service.StockFlowService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing StockFlow.
 */
@RestController
@RequestMapping("/api")
public class StockFlowResource {

    private final Logger log = LoggerFactory.getLogger(StockFlowResource.class);

    private static final String ENTITY_NAME = "stockFlow";

    private final StockFlowService stockFlowService;

    public StockFlowResource(StockFlowService stockFlowService) {
        this.stockFlowService = stockFlowService;
    }

    /**
     * POST  /stock-flows : Create a new stockFlow.
     *
     * @param stockFlow the stockFlow to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockFlow, or with status 400 (Bad Request) if the stockFlow has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-flows")
    //@Timed
    public ResponseEntity<StockFlow> createStockFlow(@Valid @RequestBody StockFlow stockFlow) throws URISyntaxException {
        log.debug("REST request to save StockFlow : {}", stockFlow);
        if (stockFlow.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockFlow cannot already have an ID")).body(null);
        }
        StockFlow result = stockFlowService.save(stockFlow);
        return ResponseEntity.created(new URI("/api/stock-flows/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-flows : Updates an existing stockFlow.
     *
     * @param stockFlow the stockFlow to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockFlow,
     * or with status 400 (Bad Request) if the stockFlow is not valid,
     * or with status 500 (Internal Server Error) if the stockFlow couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-flows")
    //@Timed
    public ResponseEntity<StockFlow> updateStockFlow(@Valid @RequestBody StockFlow stockFlow) throws URISyntaxException {
        log.debug("REST request to update StockFlow : {}", stockFlow);
        if (stockFlow.getId() == null) {
            return createStockFlow(stockFlow);
        }
        StockFlow result = stockFlowService.save(stockFlow);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockFlow.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-flows : get all the stockFlows.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockFlows in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-flows")
    //@Timed
    public ResponseEntity<List<StockFlow>> getAllStockFlows(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockFlows");
        Page<StockFlow> page = stockFlowService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-flows");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-flows/:id : get the "id" stockFlow.
     *
     * @param id the id of the stockFlow to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockFlow, or with status 404 (Not Found)
     */
    @GetMapping("/stock-flows/{id}")
    //@Timed
    public ResponseEntity<StockFlow> getStockFlow(@PathVariable Long id) {
        log.debug("REST request to get StockFlow : {}", id);
        StockFlow stockFlow = stockFlowService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockFlow));
    }

    /**
     * DELETE  /stock-flows/:id : delete the "id" stockFlow.
     *
     * @param id the id of the stockFlow to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-flows/{id}")
    //@Timed
    public ResponseEntity<Void> deleteStockFlow(@PathVariable Long id) {
        log.debug("REST request to delete StockFlow : {}", id);
        stockFlowService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * GET  /stock-flows/{entryDate}/{unitId}/{storeId} : get all the stock transactions (IN & OUT) for a unit or a store.
     *
     * @param entryDate,unitId,storeId,itemId,consignment
     * @return the ResponseEntity with status 200 (OK) and the record of all the stock transactions.
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @GetMapping("/stock-ledger/{entryDate}/{consignment}/{unitId}/{storeId}")
    //@Timed
    @PreAuthorize("hasPrivilege('101117102')")
    public ResponseEntity<List<Map<String, Object>>> getStockTransactions(@PathVariable("entryDate") String entryDate, @PathVariable("unitId") Long unitId,
                                                                          @PathVariable("storeId") Long storeId, @RequestParam(value = "itemId", required = false) Long itemId,
                                                                          @PathVariable("consignment") String consignment) throws URISyntaxException {
        log.debug("REST request to get the record of all the stock transactions");
        Boolean isConsignment = false;
        if (consignment.equalsIgnoreCase("both")) {
            isConsignment = null;
        } else {
            isConsignment = Boolean.parseBoolean(consignment);
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseStrict()
            .appendPattern("uuuu-MM-dd")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);
        LocalDate dateTime = LocalDate.parse(entryDate, formatter);
        List<Map<String, Object>> stockFlowList = stockFlowService.getStockTransactions(dateTime, isConsignment, unitId, storeId, itemId);
        return ResponseEntity.ok().body(stockFlowList);
    }

    /**
     * EXPORT  /_export/stock-flows/{entryDate}/{unitId}/{storeId} : export csv file for the stock-flow transactions for given details.
     *
     * @param entryDate,unitId,storeId,itemId,consignment
     * @throws URISyntaxException,IOException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-ledger/_export/{entryDate}/{consignment}/{unitId}/{storeId}")
    //@Timed
    @PreAuthorize("hasPrivilege('101117102')")
    public Map<String, String> exportStockFlowTransactions(@PathVariable("entryDate") String entryDate, @PathVariable("unitId") Long unitId,
                                                           @PathVariable("storeId") Long storeId, @RequestParam(value = "itemId", required = false) Long itemId,
                                                           @PathVariable("consignment") String consignment) throws URISyntaxException, IOException {
        log.debug("REST request to export stock flow transactions");
        Boolean isConsignment = false;
        if (consignment.equalsIgnoreCase("both")) {
            isConsignment = null;
        } else {
            isConsignment = Boolean.parseBoolean(consignment);
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseStrict()
            .appendPattern("uuuu-MM-dd")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);
        LocalDate dateTime = LocalDate.parse(entryDate, formatter);
        return stockFlowService.exportStockFlowTransactions(dateTime, isConsignment, unitId, storeId, itemId);
    }
}
