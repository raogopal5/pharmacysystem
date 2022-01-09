package org.nh.pharmacy.web.rest;


import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.pharmacy.domain.HSCGroupMapping;
import org.nh.pharmacy.service.HSCGroupMappingService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing HSCGroupMapping.
 */
@RestController
@RequestMapping("/api")
public class HSCGroupMappingResource {

    private final Logger log = LoggerFactory.getLogger(HSCGroupMappingResource.class);

    private static final String ENTITY_NAME = "hSCGroupMapping";

    private final HSCGroupMappingService hSCGroupMappingService;

    public HSCGroupMappingResource(HSCGroupMappingService hSCGroupMappingService) {
        this.hSCGroupMappingService = hSCGroupMappingService;
    }

    /**
     * POST  /hsc-group-mappings : Create a new hSCGroupMapping.
     *
     * @param hSCGroupMapping the hSCGroupMapping to create
     * @return the ResponseEntity with status 201 (Created) and with body the new hSCGroupMapping, or with status 400 (Bad Request) if the hSCGroupMapping has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/hsc-group-mappings")
    //@Timed
    public ResponseEntity<HSCGroupMapping> createHSCGroupMapping(@Valid @RequestBody HSCGroupMapping hSCGroupMapping) throws URISyntaxException {
        log.debug("REST request to save HSCGroupMapping : {}", hSCGroupMapping);
        if (hSCGroupMapping.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new hSCGroupMapping cannot already have an ID")).body(null);
        }
        HSCGroupMapping result = hSCGroupMappingService.save(hSCGroupMapping);
        return ResponseEntity.created(new URI("/api/hsc-group-mappings/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /hsc-group-mappings : Updates an existing hSCGroupMapping.
     *
     * @param hSCGroupMapping the hSCGroupMapping to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated hSCGroupMapping,
     * or with status 400 (Bad Request) if the hSCGroupMapping is not valid,
     * or with status 500 (Internal Server Error) if the hSCGroupMapping couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/hsc-group-mappings")
    //@Timed
    public ResponseEntity<HSCGroupMapping> updateHSCGroupMapping(@Valid @RequestBody HSCGroupMapping hSCGroupMapping) throws URISyntaxException {
        log.debug("REST request to update HSCGroupMapping : {}", hSCGroupMapping);
        if (hSCGroupMapping.getId() == null) {
            return createHSCGroupMapping(hSCGroupMapping);
        }
        HSCGroupMapping result = hSCGroupMappingService.save(hSCGroupMapping);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, hSCGroupMapping.getId().toString()))
            .body(result);
    }

    /**
     * GET  /hsc-group-mappings : get all the hSCGroupMappings.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of hSCGroupMappings in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/hsc-group-mappings")
    //@Timed
    public ResponseEntity<List<HSCGroupMapping>> getAllHSCGroupMappings(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of HSCGroupMappings");
        Page<HSCGroupMapping> page = hSCGroupMappingService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/hsc-group-mappings");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /hsc-group-mappings/:id : get the "id" hSCGroupMapping.
     *
     * @param id the id of the hSCGroupMapping to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the hSCGroupMapping, or with status 404 (Not Found)
     */
    @GetMapping("/hsc-group-mappings/{id}")
    //@Timed
    public ResponseEntity<HSCGroupMapping> getHSCGroupMapping(@PathVariable Long id) {
        log.debug("REST request to get HSCGroupMapping : {}", id);
        HSCGroupMapping hSCGroupMapping = hSCGroupMappingService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(hSCGroupMapping));
    }

    /**
     * DELETE  /hsc-group-mappings/:id : delete the "id" hSCGroupMapping.
     *
     * @param id the id of the hSCGroupMapping to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/hsc-group-mappings/{id}")
    //@Timed
    public ResponseEntity<Void> deleteHSCGroupMapping(@PathVariable Long id) {
        log.debug("REST request to delete HSCGroupMapping : {}", id);
        hSCGroupMappingService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/hsc-group-mappings?query=:query : search for the hSCGroupMapping corresponding
     * to the query.
     *
     * @param query the query of the hSCGroupMapping search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/hsc-group-mappings")
    //@Timed
    public ResponseEntity<List<HSCGroupMapping>> searchHSCGroupMappings(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of HSCGroupMappings for query {}", query);
        try {
            Page<HSCGroupMapping> page = hSCGroupMappingService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/hsc-group-mappings");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }catch(SearchPhaseExecutionException e){
            log.error("No Index found for {}",e);
            return new ResponseEntity(new ArrayList<>(), HttpStatus.OK);
        }
    }


}
