package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.UOM;
import org.nh.pharmacy.service.UOMService;
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
 * REST controller for managing UOM.
 */
@RestController
@RequestMapping("/api")
public class UOMResource {

    private final Logger log = LoggerFactory.getLogger(UOMResource.class);

    private static final String ENTITY_NAME = "uOM";

    private final UOMService uOMService;

    public UOMResource(UOMService uOMService) {
        this.uOMService = uOMService;
    }

    /**
     * GET  /uoms : get all the uOMS.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of uOMS in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/uoms")
    //@Timed
    public ResponseEntity<List<UOM>> getAllUOMS(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of UOMS");
        Page<UOM> page = uOMService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/uoms");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /uoms/:id : get the "id" uOM.
     *
     * @param id the id of the uOM to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the uOM, or with status 404 (Not Found)
     */
    @GetMapping("/uoms/{id}")
    //@Timed
    public ResponseEntity<UOM> getUOM(@PathVariable Long id) {
        log.debug("REST request to get UOM : {}", id);
        UOM uOM = uOMService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(uOM));
    }

    /**
     * SEARCH  /_search/uoms?query=:query : search for the uOM corresponding
     * to the query.
     *
     * @param query the query of the uOM search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/uoms")
    //@Timed
    public ResponseEntity<List<UOM>> searchUOMS(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of UOMS for query {}", query);
        Page<UOM> page = uOMService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/uoms");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
