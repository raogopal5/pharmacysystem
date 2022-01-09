package org.nh.pharmacy.web.rest;


import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;

import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.SavedAuditCriteria;
import org.nh.pharmacy.repository.SavedAuditCriteriaRepository;
import org.nh.pharmacy.repository.search.SavedAuditCriteriaSearchRepository;
import org.nh.pharmacy.service.SavedAuditCriteriaService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing SavedAuditCriteria.
 */
@RestController
@RequestMapping("/api")
public class SavedAuditCriteriaResource {

    private final Logger log = LoggerFactory.getLogger(SavedAuditCriteriaResource.class);

    private static final String ENTITY_NAME = "savedAuditCriteria";

    private final SavedAuditCriteriaService savedAuditCriteriaService;

    private final SavedAuditCriteriaRepository savedAuditCriteriaRepository;

    private final SavedAuditCriteriaSearchRepository savedAuditCriteriaSearchRepository;

    private final ApplicationProperties applicationProperties;


    public SavedAuditCriteriaResource(SavedAuditCriteriaService savedAuditCriteriaService,SavedAuditCriteriaRepository savedAuditCriteriaRepository,SavedAuditCriteriaSearchRepository savedAuditCriteriaSearchRepository,ApplicationProperties applicationProperties) {
        this.savedAuditCriteriaService = savedAuditCriteriaService;
        this.savedAuditCriteriaRepository=savedAuditCriteriaRepository;
        this.savedAuditCriteriaSearchRepository=savedAuditCriteriaSearchRepository;
        this.applicationProperties=applicationProperties;
    }

    /**
     * POST  /saved-audit-criteria : Create a new savedAuditCriteria.
     *
     * @param savedAuditCriteria the savedAuditCriteria to create
     * @return the ResponseEntity with status 201 (Created) and with body the new savedAuditCriteria, or with status 400 (Bad Request) if the savedAuditCriteria has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/saved-audit-criteria")
    //@Timed
    public ResponseEntity<SavedAuditCriteria> createSavedAuditCriteria(@Valid @RequestBody SavedAuditCriteria savedAuditCriteria) throws URISyntaxException {
        log.debug("REST request to save SavedAuditCriteria : {}", savedAuditCriteria);
        if (savedAuditCriteria.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new savedAuditCriteria cannot already have an ID")).body(null);
        }
        SavedAuditCriteria result = savedAuditCriteriaService.save(savedAuditCriteria);
        return ResponseEntity.created(new URI("/api/saved-audit-criteria/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /saved-audit-criteria : Updates an existing savedAuditCriteria.
     *
     * @param savedAuditCriteria the savedAuditCriteria to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated savedAuditCriteria,
     * or with status 400 (Bad Request) if the savedAuditCriteria is not valid,
     * or with status 500 (Internal Server Error) if the savedAuditCriteria couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/saved-audit-criteria")
    //@Timed
    public ResponseEntity<SavedAuditCriteria> updateSavedAuditCriteria(@Valid @RequestBody SavedAuditCriteria savedAuditCriteria) throws URISyntaxException {
        log.debug("REST request to update SavedAuditCriteria : {}", savedAuditCriteria);
        if (savedAuditCriteria.getId() == null) {
            return createSavedAuditCriteria(savedAuditCriteria);
        }
        SavedAuditCriteria result = savedAuditCriteriaService.save(savedAuditCriteria);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, savedAuditCriteria.getId().toString()))
            .body(result);
    }

    /**
     * GET  /saved-audit-criteria : get all the savedAuditCriteria.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of savedAuditCriteria in body
     */
    @GetMapping("/saved-audit-criteria")
    //@Timed
    public List<SavedAuditCriteria> getAllSavedAuditCriteria() {
        log.debug("REST request to get all SavedAuditCriteria");
        return savedAuditCriteriaService.findAll();
    }

    /**
     * GET  /saved-audit-criteria/:id : get the "id" savedAuditCriteria.
     *
     * @param id the id of the savedAuditCriteria to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the savedAuditCriteria, or with status 404 (Not Found)
     */
    @GetMapping("/saved-audit-criteria/{id}")
    //@Timed
    public ResponseEntity<SavedAuditCriteria> getSavedAuditCriteria(@PathVariable Long id) {
        log.debug("REST request to get SavedAuditCriteria : {}", id);
        SavedAuditCriteria savedAuditCriteria = savedAuditCriteriaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(savedAuditCriteria));
    }

    /**
     * DELETE  /saved-audit-criteria/:id : delete the "id" savedAuditCriteria.
     *
     * @param id the id of the savedAuditCriteria to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/saved-audit-criteria/{id}")
    //@Timed
    public ResponseEntity<Void> deleteSavedAuditCriteria(@PathVariable Long id) {
        log.debug("REST request to delete SavedAuditCriteria : {}", id);
        savedAuditCriteriaService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/saved-audit-criteria?query=:query : search for the savedAuditCriteria corresponding
     * to the query.
     *
     * @param query the query of the savedAuditCriteria search
     * @return the result of the search
     */
    @GetMapping("/_search/saved-audit-criteria")
    //@Timed
    public List<SavedAuditCriteria> searchSavedAuditCriteria(@RequestParam String query) {
        log.debug("REST request to search SavedAuditCriteria for query {}", query);
        return savedAuditCriteriaService.search(query);
    }

    /**
     * @param query
     * @param pageable
     * @param type
     * @param fields
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/saved-audit-criteria/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<SavedAuditCriteria>> searchSavedAuditCriteria(@RequestParam String query, @ApiParam Pageable pageable,
                                                          @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Dispenses for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<SavedAuditCriteria> page = savedAuditCriteriaService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/saved-audit-criteria");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (SearchPhaseExecutionException e) {
            log.error("No Index found for {}", e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/saved-audit-criteria"),
                HttpStatus.OK);
        }
    }
    @GetMapping("/_index/saved-audit-criteria")
    public ResponseEntity<Void> indexSavedAuditCriteria(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on saved audit criteria");
        long resultCount = savedAuditCriteriaRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            savedAuditCriteriaService.doIndex(i, pageSize, fromDate, toDate);
            savedAuditCriteriaSearchRepository.refresh();;
        }
        return ResponseEntity.ok().headers(org.nh.billing.web.rest.util.HeaderUtil.createAlert("Elastic search index is completed", "")).build();
    }

}
