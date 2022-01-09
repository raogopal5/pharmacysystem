package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.service.ItemService;
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
 * REST controller for managing Item.
 */
@RestController
@RequestMapping("/api")
public class ItemResource {

    private final Logger log = LoggerFactory.getLogger(ItemResource.class);

    private static final String ENTITY_NAME = "item";

    private final ItemService itemService;

    public ItemResource(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * GET  /items : get all the items.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of items in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/items")
    //@Timed
    public ResponseEntity<List<Item>> getAllItems(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Items");
        Page<Item> page = itemService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/items");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /items/:id : get the "id" item.
     *
     * @param id the id of the item to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the item, or with status 404 (Not Found)
     */
    @GetMapping("/items/{id}")
    //@Timed
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        log.debug("REST request to get Item : {}", id);
        Item item = itemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(item));
    }

    /**
     * SEARCH  /_search/items?query=:query : search for the item corresponding
     * to the query.
     *
     * @param query the query of the item search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/items")
    //@Timed
    public ResponseEntity<List<Item>> searchItems(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Items for query {}", query);
        Page<Item> page = itemService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/items");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
