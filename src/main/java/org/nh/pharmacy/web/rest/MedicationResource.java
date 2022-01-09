package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.service.MedicationService;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Medication.
 */
@RestController
@RequestMapping("/api")
public class MedicationResource {

    private final Logger log = LoggerFactory.getLogger(MedicationResource.class);

    private static final String ENTITY_NAME = "medication";
        
    private final MedicationService medicationService;

    public MedicationResource(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    /**
     * POST  /medications : Create a new medication.
     *
     * @param medication the medication to create
     * @return the ResponseEntity with status 201 (Created) and with body the new medication, or with status 400 (Bad Request) if the medication has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/medications")
    //@Timed
    public ResponseEntity<Medication> createMedication(@Valid @RequestBody Medication medication) throws URISyntaxException {
        log.debug("REST request to save Medication : {}", medication);
        if (medication.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new medication cannot already have an ID")).body(null);
        }
        Medication result = medicationService.save(medication);
        return ResponseEntity.created(new URI("/api/medications/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /medications : Updates an existing medication.
     *
     * @param medication the medication to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated medication,
     * or with status 400 (Bad Request) if the medication is not valid,
     * or with status 500 (Internal Server Error) if the medication couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/medications")
    //@Timed
    public ResponseEntity<Medication> updateMedication(@Valid @RequestBody Medication medication) throws URISyntaxException {
        log.debug("REST request to update Medication : {}", medication);
        if (medication.getId() == null) {
            return createMedication(medication);
        }
        Medication result = medicationService.save(medication);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, medication.getId().toString()))
            .body(result);
    }

    /**
     * GET  /medications : get all the medications.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of medications in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/medications")
    //@Timed
    public ResponseEntity<List<Medication>> getAllMedications(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Medications");
        Page<Medication> page = medicationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/medications");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /medications/:id : get the "id" medication.
     *
     * @param id the id of the medication to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the medication, or with status 404 (Not Found)
     */
    @GetMapping("/medications/{id}")
    //@Timed
    public ResponseEntity<Medication> getMedication(@PathVariable Long id) {
        log.debug("REST request to get Medication : {}", id);
        Medication medication = medicationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(medication));
    }

    /**
     * DELETE  /medications/:id : delete the "id" medication.
     *
     * @param id the id of the medication to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/medications/{id}")
    //@Timed
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        log.debug("REST request to delete Medication : {}", id);
        medicationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/medications?query=:query : search for the medication corresponding
     * to the query.
     *
     * @param query the query of the medication search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/medications")
    //@Timed
    public ResponseEntity<List<Medication>> searchMedications(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Medications for query {}", query);
        Page<Medication> page = medicationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/medications");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
