package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.ItemPricingMethod;
import org.nh.pharmacy.service.ItemPricingMethodService;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ItemPricingMethod.
 */
@RestController
@RequestMapping("/api")
public class ItemPricingMethodResource {

    private final Logger log = LoggerFactory.getLogger(ItemPricingMethodResource.class);

    private static final String ENTITY_NAME = "itemPricingMethod";

    private final ItemPricingMethodService itemPricingMethodService;

    public ItemPricingMethodResource(ItemPricingMethodService itemPricingMethodService) {
        this.itemPricingMethodService = itemPricingMethodService;
    }

    /**
     * GET  /item-pricing-methods : get all the itemPricingMethods.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of itemPricingMethods in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/item-pricing-methods")
    //@Timed
    public ResponseEntity<List<ItemPricingMethod>> getAllItemPricingMethods(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of ItemPricingMethods");
        Page<ItemPricingMethod> page = itemPricingMethodService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/item-pricing-methods");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /item-pricing-methods/:id : get the "id" itemPricingMethod.
     *
     * @param id the id of the itemPricingMethod to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the itemPricingMethod, or with status 404 (Not Found)
     */
    @GetMapping("/item-pricing-methods/{id}")
    //@Timed
    public ResponseEntity<ItemPricingMethod> getItemPricingMethod(@PathVariable Long id) {
        log.debug("REST request to get ItemPricingMethod : {}", id);
        ItemPricingMethod itemPricingMethod = itemPricingMethodService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(itemPricingMethod));
    }

    /**
     * SEARCH  /_search/item-pricing-methods?query=:query : search for the itemPricingMethod corresponding
     * to the query.
     *
     * @param query the query of the itemPricingMethod search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/item-pricing-methods")
    //@Timed
    public ResponseEntity<List<ItemPricingMethod>> searchItemPricingMethods(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemPricingMethods for query {}", query);
        Page<ItemPricingMethod> page = itemPricingMethodService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/item-pricing-methods");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
