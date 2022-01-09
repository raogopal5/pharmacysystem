package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.Locator;
import org.nh.pharmacy.service.LocatorService;
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

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Locator.
 */
@RestController
@RequestMapping("/api")
public class LocatorResource {

    private final Logger log = LoggerFactory.getLogger(LocatorResource.class);

    private static final String ENTITY_NAME = "locator";

    private final LocatorService locatorService;

    public LocatorResource(LocatorService locatorService) {
        this.locatorService = locatorService;
    }

    /**
     * GET  /locators : get all the locators.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of locators in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/locators")
    //@Timed
    public ResponseEntity<List<Locator>> getAllLocators(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Locators");
        Page<Locator> page = locatorService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/locators");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /locators/:id : get the "id" locator.
     *
     * @param id the id of the locator to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the locator, or with status 404 (Not Found)
     */
    @GetMapping("/locators/{id}")
    //@Timed
    public ResponseEntity<Locator> getLocator(@PathVariable Long id) {
        log.debug("REST request to get Locator : {}", id);
        Locator locator = locatorService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(locator));
    }

    /**
     * DELETE  /locators/:id : delete the "id" locator.
     *
     * @param id the id of the locator to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/locators/{id}")
    //@Timed
    public ResponseEntity<Void> deleteLocator(@PathVariable Long id) {
        log.debug("REST request to delete Locator : {}", id);
        locatorService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/locators?query=:query : search for the locator corresponding
     * to the query.
     *
     * @param query the query of the locator search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/locators")
    //@Timed
    public ResponseEntity<List<Locator>> searchLocators(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Locators for query {}", query);
        Page<Locator> page = locatorService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/locators");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
