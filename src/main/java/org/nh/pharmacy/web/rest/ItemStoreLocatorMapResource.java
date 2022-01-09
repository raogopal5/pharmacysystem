package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.ItemStoreLocatorMap;
import org.nh.pharmacy.service.ItemStoreLocatorMapService;
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
 * REST controller for managing ItemStoreLocatorMap.
 */
@RestController
@RequestMapping("/api")
public class ItemStoreLocatorMapResource {

    private final Logger log = LoggerFactory.getLogger(ItemStoreLocatorMapResource.class);

    private static final String ENTITY_NAME = "itemStoreLocatorMap";

    private final ItemStoreLocatorMapService itemStoreLocatorMapService;

    public ItemStoreLocatorMapResource(ItemStoreLocatorMapService itemStoreLocatorMapService) {
        this.itemStoreLocatorMapService = itemStoreLocatorMapService;
    }

    /**
     * GET  /item-store-locator-maps : get all the itemStoreLocatorMaps.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of itemStoreLocatorMaps in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/item-store-locator-maps")
    //@Timed
    public ResponseEntity<List<ItemStoreLocatorMap>> getAllItemStoreLocatorMaps(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of ItemStoreLocatorMaps");
        Page<ItemStoreLocatorMap> page = itemStoreLocatorMapService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/item-store-locator-maps");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /item-store-locator-maps/:id : get the "id" itemStoreLocatorMap.
     *
     * @param id the id of the itemStoreLocatorMap to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the itemStoreLocatorMap, or with status 404 (Not Found)
     */
    @GetMapping("/item-store-locator-maps/{id}")
    //@Timed
    public ResponseEntity<ItemStoreLocatorMap> getItemStoreLocatorMap(@PathVariable Long id) {
        log.debug("REST request to get ItemStoreLocatorMap : {}", id);
        ItemStoreLocatorMap itemStoreLocatorMap = itemStoreLocatorMapService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(itemStoreLocatorMap));
    }

    /**
     * SEARCH  /_search/item-store-locator-maps?query=:query : search for the itemStoreLocatorMap corresponding
     * to the query.
     *
     * @param query the query of the itemStoreLocatorMap search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/item-store-locator-maps")
    //@Timed
    public ResponseEntity<List<ItemStoreLocatorMap>> searchItemStoreLocatorMaps(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemStoreLocatorMaps for query {}", query);
        Page<ItemStoreLocatorMap> page = itemStoreLocatorMapService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/item-store-locator-maps");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
