package org.nh.pharmacy.web.rest;



import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.dto.ItemStoreStock;
import org.nh.pharmacy.service.ItemStoreStockService;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Rest Controller for search ItemStoreStock
 */
@RestController
@RequestMapping("/api")
public class ItemStoreStockResource {

    private final Logger log = LoggerFactory.getLogger(ItemStoreStockResource.class);

    private final ItemStoreStockService itemStoreStockService;

    public ItemStoreStockResource(ItemStoreStockService itemStoreStockService)
    {
        this.itemStoreStockService = itemStoreStockService;
    }

    /**
     * SEARCH  /_search/item-store-stock?query=:query : search for the ItemStoreStock corresponding
     * to the query.
     *
     * @param query the query of the ItemStoreStock search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */

    @GetMapping("_search/item-store-stock")
    //@Timed
    public ResponseEntity<List<ItemStoreStock>> searchItemStoreStock(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
            log.debug("REST request to search for a page of ItemStoreStock for query {}", query);
            Page<ItemStoreStock> page = itemStoreStockService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/item-store-stock");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
