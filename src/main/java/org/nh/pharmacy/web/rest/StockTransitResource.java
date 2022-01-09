package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import org.nh.pharmacy.domain.StockTransit;
import org.nh.pharmacy.service.StockTransitService;
import org.nh.pharmacy.web.rest.errors.BadRequestAlertException;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing StockTransit.
 */
@RestController
@RequestMapping("/api")
public class StockTransitResource {

    private final Logger log = LoggerFactory.getLogger(StockTransitResource.class);

    private static final String ENTITY_NAME = "stockTransit";

    private final StockTransitService stockTransitService;

    public StockTransitResource(StockTransitService stockTransitService) {
        this.stockTransitService = stockTransitService;
    }

    /**
     * POST  /stock-transits : Create a new stockTransit.
     *
     * @param stockTransit the stockTransit to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockTransit, or with status 400 (Bad Request) if the stockTransit has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-transits")
    //@Timed
    public ResponseEntity<StockTransit> createStockTransit(@Valid @RequestBody StockTransit stockTransit) throws URISyntaxException {
        log.debug("REST request to save StockTransit : {}", stockTransit);
        if (stockTransit.getId() != null) {
            throw new BadRequestAlertException("A new stockTransit cannot already have an ID", ENTITY_NAME, "idexists");
        }
        StockTransit result = stockTransitService.save(stockTransit);
        return ResponseEntity.created(new URI("/api/stock-transits/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-transits : Updates an existing stockTransit.
     *
     * @param stockTransit the stockTransit to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockTransit,
     * or with status 400 (Bad Request) if the stockTransit is not valid,
     * or with status 500 (Internal Server Error) if the stockTransit couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-transits")
    //@Timed
    public ResponseEntity<StockTransit> updateStockTransit(@Valid @RequestBody StockTransit stockTransit) throws URISyntaxException {
        log.debug("REST request to update StockTransit : {}", stockTransit);
        if (stockTransit.getId() == null) {
            return createStockTransit(stockTransit);
        }
        StockTransit result = stockTransitService.save(stockTransit);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockTransit.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-transits : get all the stockTransits.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of stockTransits in body
     */
    @GetMapping("/stock-transits")
    //@Timed
    public List<StockTransit> getAllStockTransits() {
        log.debug("REST request to get all StockTransits");
        return stockTransitService.findAll();
        }

    /**
     * GET  /stock-transits/:id : get the "id" stockTransit.
     *
     * @param id the id of the stockTransit to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockTransit, or with status 404 (Not Found)
     */
    @GetMapping("/stock-transits/{id}")
    //@Timed
    public ResponseEntity<StockTransit> getStockTransit(@PathVariable Long id) {
        log.debug("REST request to get StockTransit : {}", id);
        StockTransit stockTransit = stockTransitService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockTransit));
    }

    /**
     * DELETE  /stock-transits/:id : delete the "id" stockTransit.
     *
     * @param id the id of the stockTransit to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-transits/{id}")
    //@Timed
    public ResponseEntity<Void> deleteStockTransit(@PathVariable Long id) {
        log.debug("REST request to delete StockTransit : {}", id);
        stockTransitService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
