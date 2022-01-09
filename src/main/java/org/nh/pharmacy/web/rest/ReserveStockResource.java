package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.service.ReserveStockService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
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
 * REST controller for managing ReserveStock.
 */
@RestController
@RequestMapping("/api")
public class ReserveStockResource {

    private final Logger log = LoggerFactory.getLogger(ReserveStockResource.class);

    private static final String ENTITY_NAME = "reserveStock";

    private final ReserveStockService reserveStockService;

    public ReserveStockResource(ReserveStockService reserveStockService) {
        this.reserveStockService = reserveStockService;
    }

    /**
     * POST  /reserve-stocks : Create a new reserveStock.
     *
     * @param reserveStock the reserveStock to create
     * @return the ResponseEntity with status 201 (Created) and with body the new reserveStock, or with status 400 (Bad Request) if the reserveStock has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/reserve-stocks")
    //@Timed
    public ResponseEntity<ReserveStock> createReserveStock(@Valid @RequestBody ReserveStock reserveStock) throws URISyntaxException {
        log.debug("REST request to save ReserveStock : {}", reserveStock);
        if (reserveStock.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new reserveStock cannot already have an ID")).body(null);
        }
        ReserveStock result = reserveStockService.save(reserveStock);
        return ResponseEntity.created(new URI("/api/reserve-stocks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /reserve-stocks : Updates an existing reserveStock.
     *
     * @param reserveStock the reserveStock to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated reserveStock,
     * or with status 400 (Bad Request) if the reserveStock is not valid,
     * or with status 500 (Internal Server Error) if the reserveStock couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/reserve-stocks")
    //@Timed
    public ResponseEntity<ReserveStock> updateReserveStock(@Valid @RequestBody ReserveStock reserveStock) throws URISyntaxException {
        log.debug("REST request to update ReserveStock : {}", reserveStock);
        if (reserveStock.getId() == null) {
            return createReserveStock(reserveStock);
        }
        ReserveStock result = reserveStockService.save(reserveStock);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, reserveStock.getId().toString()))
            .body(result);
    }

    /**
     * GET  /reserve-stocks : get all the reserveStocks.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of reserveStocks in body
     */
    @GetMapping("/reserve-stocks")
    //@Timed
    public List<ReserveStock> getAllReserveStocks() {
        log.debug("REST request to get all ReserveStocks");
        return reserveStockService.findAll();
    }

    /**
     * GET  /reserve-stocks/:id : get the "id" reserveStock.
     *
     * @param id the id of the reserveStock to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the reserveStock, or with status 404 (Not Found)
     */
    @GetMapping("/reserve-stocks/{id}")
    //@Timed
    public ResponseEntity<ReserveStock> getReserveStock(@PathVariable Long id) {
        log.debug("REST request to get ReserveStock : {}", id);
        ReserveStock reserveStock = reserveStockService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(reserveStock));
    }

    /**
     * DELETE  /reserve-stocks/:id : delete the "id" reserveStock.
     *
     * @param id the id of the reserveStock to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/reserve-stocks/{id}")
    //@Timed
    public ResponseEntity<Void> deleteReserveStock(@PathVariable Long id) {
        log.debug("REST request to delete ReserveStock : {}", id);
        reserveStockService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PreAuthorize("hasPrivilege('101132102')")
    @GetMapping("/reserve-stocks/report")
    public ResponseEntity<List<Map<String, Object>>> getReserveStockReport(@RequestParam(value = "unitCodes", required = false) List<String> unitCodes, @RequestParam(value = "storeCode", required = false) String storeCode,
        @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fromDate, @RequestParam(value = "toDate") @DateTimeFormat (pattern = "dd/MM/yyyy") LocalDate toDate) throws URISyntaxException, Exception {

        log.debug("REST request to get the reserve stock for given details unitCodes:{}, storeCode:{}, fromDate:{}, toDate:{}", unitCodes, storeCode, fromDate, toDate);
        List<Map<String, Object>> reserveStockList = reserveStockService.getReserveStockReport(unitCodes, storeCode, fromDate, toDate);
        return ResponseEntity.ok().body(reserveStockList);
    }

    @PreAuthorize("hasPrivilege('101132102')")
    @GetMapping("/reserve-stocks/report/_export")
    public ResponseEntity<Map<String, String>> getReserveStockReportExport(@RequestParam(value = "unitCodes", required = false) List<String> unitCodes, @RequestParam(value = "storeCode", required = false) String storeCode,
                                                                           @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fromDate, @RequestParam(value = "toDate") @DateTimeFormat (pattern = "dd/MM/yyyy") LocalDate toDate)
        throws URISyntaxException, Exception {
        log.debug("REST request to export reserve stock for given details unitCodes:{}, storeCode:{}, fromDate:{}, toDate:{}", unitCodes, storeCode, fromDate, toDate);
        Map<String, String> reserveStockList = reserveStockService.exportReserveStocks(unitCodes, storeCode, fromDate, toDate);
        return ResponseEntity.ok().body(reserveStockList);
    }
}
