package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.ItemCategory;
import org.nh.pharmacy.service.ItemCategoryService;
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
 * REST controller for managing ItemCategory.
 */
@RestController
@RequestMapping("/api")
public class ItemCategoryResource {

    private final Logger log = LoggerFactory.getLogger(ItemCategoryResource.class);

    private static final String ENTITY_NAME = "itemCategory";

    private final ItemCategoryService itemCategoryService;

    public ItemCategoryResource(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    /**
     * GET  /item-categories : get all the itemCategories.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of itemCategories in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/item-categories")
    //@Timed
    public ResponseEntity<List<ItemCategory>> getAllItemCategories(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of ItemCategories");
        Page<ItemCategory> page = itemCategoryService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/item-categories");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /item-categories/:id : get the "id" itemCategory.
     *
     * @param id the id of the itemCategory to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the itemCategory, or with status 404 (Not Found)
     */
    @GetMapping("/item-categories/{id}")
    //@Timed
    public ResponseEntity<ItemCategory> getItemCategory(@PathVariable Long id) {
        log.debug("REST request to get ItemCategory : {}", id);
        ItemCategory itemCategory = itemCategoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(itemCategory));
    }

    /**
     * SEARCH  /_search/item-categories?query=:query : search for the itemCategory corresponding
     * to the query.
     *
     * @param query the query of the itemCategory search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/item-categories")
    //@Timed
    public ResponseEntity<List<ItemCategory>> searchItemCategories(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemCategories for query {}", query);
        Page<ItemCategory> page = itemCategoryService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/item-categories");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
