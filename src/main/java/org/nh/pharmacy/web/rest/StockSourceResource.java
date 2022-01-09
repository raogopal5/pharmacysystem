package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.service.StockSourceService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing StockSource.
 */
@RestController
@RequestMapping("/api")
public class StockSourceResource {

    private final Logger log = LoggerFactory.getLogger(StockSourceResource.class);

    private static final String ENTITY_NAME = "stockSource";

    private final StockSourceService stockSourceService;

    public StockSourceResource(StockSourceService stockSourceService) {
        this.stockSourceService = stockSourceService;
    }

    /**
     * POST  /stock-sources : Create a new stockSource.
     *
     * @param stockSource the stockSource to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockSource, or with status 400 (Bad Request) if the stockSource has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-sources")
    //@Timed
    public ResponseEntity<StockSource> createStockSource(@Valid @RequestBody StockSource stockSource) throws URISyntaxException {
        log.debug("REST request to save StockSource : {}", stockSource);
        if (stockSource.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockSource cannot already have an ID")).body(null);
        }
        StockSource result = stockSourceService.save(stockSource);
        return ResponseEntity.created(new URI("/api/stock-sources/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-sources : Updates an existing stockSource.
     *
     * @param stockSource the stockSource to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockSource,
     * or with status 400 (Bad Request) if the stockSource is not valid,
     * or with status 500 (Internal Server Error) if the stockSource couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-sources")
    //@Timed
    public ResponseEntity<StockSource> updateStockSource(@Valid @RequestBody StockSource stockSource) throws URISyntaxException {
        log.debug("REST request to update StockSource : {}", stockSource);
        if (stockSource.getId() == null) {
            return createStockSource(stockSource);
        }
        StockSource result = stockSourceService.save(stockSource);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockSource.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-sources : get all the stockSources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of stockSources in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-sources")
    //@Timed
    public ResponseEntity<List<StockSource>> getAllStockSources(@RequestParam(required = false) String transactionRefNo, @RequestParam(required = false) String fromDate,
                                                                @RequestParam(required = false)String toDate, @RequestParam(required = false) Long itemId, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockSources");
        if (fromDate == null ^ toDate == null) {    // XOR (^) operator is to check if only one condition is true ,
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if (transactionRefNo == null && itemId == null && fromDate == null && toDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Page<StockSource> page = stockSourceService.getAllStockSource(transactionRefNo, fromDate, toDate, itemId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-sources");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

    /**l
     * GET  /stock-sources/:id : get the "id" stockSource.
     *
     * @param id the id of the stockSource to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockSource, or with status 404 (Not Found)
     */
    @GetMapping("/stock-sources/{id}")
    //@Timed
    public ResponseEntity<StockSource> getStockSource(@PathVariable Long id) {
        log.debug("REST request to get StockSource : {}", id);
        StockSource stockSource = stockSourceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockSource));
    }

    /**
     * DELETE  /stock-sources/:id : delete the "id" stockSource.
     *
     * @param id the id of the stockSource to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-sources/{id}")
    //@Timed
    public ResponseEntity<Void> deleteStockSource(@PathVariable Long id) {
        log.debug("REST request to delete StockSource : {}", id);
        stockSourceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * PUT  _generate/stock-sources : Updates an existing stockSource with generated barcode.
     *
     * @param stockSource the stockSource to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockSource,
     * or with status 400 (Bad Request) if the stockSource is not valid,
     * or with status 500 (Internal Server Error) if the stockSource couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("_generate/stock-sources")
    //@Timed
    public ResponseEntity<StockSource> generateStockSourceBarcode(@Valid @RequestBody StockSource stockSource) throws Exception {
        log.debug("REST request to generate barcode and update StockSource : {}", stockSource);
        StockSource result = stockSourceService.generateBarcode(stockSource);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockSource.getId().toString()))
            .body(result);
    }

    /**
     *
     * @param stockSourceId the id of the stockSource
     * @param unitId the id of unit
     * @return the ResponseEntity with status 200 (OK), or with status 404 (Not Found)
     */
    @GetMapping("_print/stock-sources/{stockSourceId}/{unitId}")
    //@Timed
    public ResponseEntity<Map<String, String>> getBarcodeFormat(@PathVariable Long stockSourceId, @PathVariable Long unitId) throws Exception {
        log.debug("REST request to get StockSource : {}", stockSourceId);
        Map<String, String> barcodeMap = stockSourceService.findBarcodeFormat(stockSourceId,unitId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(barcodeMap));
    }

    /**
     * GET  /stock-sources/getGRN get  the stockSources by transactionRefNo And Unit Code.
     *
     * @return the result  list of StockSources
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-sources/getGRN")
    //@Timed
    public ResponseEntity<List<StockSource>> getStockSourceByTransactionRefNo(@RequestParam String transactionRefNo, @RequestParam String unitCode, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get  StockSources by transactionRefNo : {} AND unitCode : {}",transactionRefNo);
        List<StockSource>  stockSources= stockSourceService.getStockSourceByTransactionRefNo(transactionRefNo, unitCode, pageable);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockSources));
    }

    /**
     * PUT  _generate-barcodes/stock-sources : Updates an existing stockSource with generated barcode.
     *
     * @param stockSources the stockSource to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockSource,
     * or with status 400 (Bad Request) if the stockSource is not valid,
     * or with status 500 (Internal Server Error) if the stockSource couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("_generate-barcodes/stock-sources")
    //@Timed
    public ResponseEntity<List<StockSource>> generateBarcodesStockSource(@Valid @RequestBody List<StockSource> stockSources) throws Exception {
        log.debug("REST request to generate barcode and update StockSource : {}", stockSources);
        List<StockSource> result = stockSourceService.generateBarcodes(stockSources);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(result));
    }

    /**
     *
     * @param stockSources
     * @param unitId the id of unit
     * @return the ResponseEntity with status 200 (OK), or with status 404 (Not Found)
     */
    @PutMapping("_print/stock-sources/{unitId}")
    //@Timed
    public ResponseEntity<List<Map<String, String>>> getBarcodeFormats(@Valid @RequestBody List<StockSource> stockSources, @PathVariable Long unitId) throws Exception {
        log.debug("REST request to get barcodeFormats for StockSources : {}", stockSources);
        List<Map<String, String>> barcodeMap = stockSourceService.findBarcodeFormats(stockSources,unitId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(barcodeMap));
    }

    /**
     * PUT  /stock-sources/_update : Update mgr_barcode field an existing stockSource.
     *
     * @param stockSources the stockSource to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockSource,
     * or with status 400 (Bad Request) if the stockSource is not valid,
     * or with status 500 (Internal Server Error) if the stockSource couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-sources/_update")
    //@Timed
    public ResponseEntity<List<StockSource>> updateStockSources(@Valid @RequestBody List<StockSource> stockSources) throws URISyntaxException {
        log.debug("REST request to update StockSources : {}", stockSources);
        List<StockSource> result = stockSourceService.save(stockSources);
        return ResponseEntity.ok()
              .body(result);
    }

}
