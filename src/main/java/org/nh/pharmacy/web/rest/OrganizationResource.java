package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.service.OrganizationService;
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
 * REST controller for managing Organization.
 */
@RestController
@RequestMapping("/api")
public class OrganizationResource {

    private final Logger log = LoggerFactory.getLogger(OrganizationResource.class);

    private final OrganizationService organizationService;

    public OrganizationResource(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }


    /**
     * GET  /organizations : get all the organizations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of organizations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/organizations")
    //@Timed
    public ResponseEntity<List<Organization>> getAllOrganizations(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Organizations");
        Page<Organization> page = organizationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/organizations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /organizations/:id : get the "id" organization.
     *
     * @param id the id of the organization to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organization, or with status 404 (Not Found)
     */
    @GetMapping("/organizations/{id}")
    //@Timed
    public ResponseEntity<Organization> getOrganization(@PathVariable Long id) {
        log.debug("REST request to get Organization : {}", id);
        Organization organization = organizationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(organization));
    }

    /**
     * SEARCH  /_search/organizations?query=:query : search for the organization corresponding
     * to the query.
     *
     * @param query the query of the organization search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/organizations")
    //@Timed
    public ResponseEntity<List<Organization>> searchOrganizations(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Organizations for query {}", query);
        Page<Organization> page = organizationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/organizations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
