package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.nh.pharmacy.service.HealthcareServiceCenterService;
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
 * REST controller for managing HealthcareServiceCenter.
 */
@RestController
@RequestMapping("/api")
public class HealthcareServiceCenterResource {

    private final Logger log = LoggerFactory.getLogger(HealthcareServiceCenterResource.class);

    private final HealthcareServiceCenterService healthcareServiceCenterService;

    public HealthcareServiceCenterResource(HealthcareServiceCenterService healthcareServiceCenterService) {
        this.healthcareServiceCenterService = healthcareServiceCenterService;
    }

    /**
     * GET  /healthcare-service-centers : get all the healthcareServiceCenters.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of healthcareServiceCenters in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/healthcare-service-centers")
    //@Timed
    public ResponseEntity<List<HealthcareServiceCenter>> getAllHealthcareServiceCenters(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of HealthcareServiceCenters");
        Page<HealthcareServiceCenter> page = healthcareServiceCenterService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/healthcare-service-centers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /healthcare-service-centers/:id : get the "id" healthcareServiceCenter.
     *
     * @param id the id of the healthcareServiceCenter to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the healthcareServiceCenter, or with status 404 (Not Found)
     */
    @GetMapping("/healthcare-service-centers/{id}")
    //@Timed
    public ResponseEntity<HealthcareServiceCenter> getHealthcareServiceCenter(@PathVariable Long id) {
        log.debug("REST request to get HealthcareServiceCenter : {}", id);
        HealthcareServiceCenter healthcareServiceCenter = healthcareServiceCenterService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(healthcareServiceCenter));
    }

    /**
     * SEARCH  /_search/healthcare-service-centers?query=:query : search for the healthcareServiceCenter corresponding
     * to the query.
     *
     * @param query the query of the healthcareServiceCenter search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/healthcare-service-centers")
    //@Timed
    public ResponseEntity<List<HealthcareServiceCenter>> searchHealthcareServiceCenters(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of HealthcareServiceCenters for query {}", query);
        Page<HealthcareServiceCenter> page = healthcareServiceCenterService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/healthcare-service-centers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
