package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.service.SystemAlertService;
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

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing SystemAlert.
 */
@RestController
@RequestMapping("/api")
public class SystemAlertResource {

    private final Logger log = LoggerFactory.getLogger(SystemAlertResource.class);

    private static final String ENTITY_NAME = "systemAlert";

    private final SystemAlertService systemAlertService;

    public SystemAlertResource(SystemAlertService systemAlertService) {
        this.systemAlertService = systemAlertService;
    }

    /**
     * POST  /system-alerts : Create a new systemAlert.
     *
     * @param systemAlert the systemAlert to create
     * @return the ResponseEntity with status 201 (Created) and with body the new systemAlert, or with status 400 (Bad Request) if the systemAlert has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/system-alerts")
    //@Timed
    public ResponseEntity<SystemAlert> createSystemAlert(@Valid @RequestBody SystemAlert systemAlert) throws URISyntaxException {
        log.debug("REST request to save SystemAlert : {}", systemAlert);
        if (systemAlert.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new systemAlert cannot already have an ID")).body(null);
        }
        SystemAlert result = systemAlertService.save(systemAlert);
        return ResponseEntity.created(new URI("/api/system-alerts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /system-alerts : Updates an existing systemAlert.
     *
     * @param systemAlert the systemAlert to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated systemAlert,
     * or with status 400 (Bad Request) if the systemAlert is not valid,
     * or with status 500 (Internal Server Error) if the systemAlert couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/system-alerts")
    //@Timed
    public ResponseEntity<SystemAlert> updateSystemAlert(@Valid @RequestBody SystemAlert systemAlert) throws URISyntaxException {
        log.debug("REST request to update SystemAlert : {}", systemAlert);
        if (systemAlert.getId() == null) {
            return createSystemAlert(systemAlert);
        }
        SystemAlert result = systemAlertService.save(systemAlert);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, systemAlert.getId().toString()))
            .body(result);
    }

    /**
     * GET  /system-alerts : get all the systemAlerts.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of systemAlerts in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/system-alerts")
    //@Timed
    public ResponseEntity<List<SystemAlert>> getAllSystemAlerts(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of SystemAlerts");
        Page<SystemAlert> page = systemAlertService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/system-alerts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /system-alerts/:id : get the "id" systemAlert.
     *
     * @param id the id of the systemAlert to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the systemAlert, or with status 404 (Not Found)
     */
    @GetMapping("/system-alerts/{id}")
    //@Timed
    public ResponseEntity<SystemAlert> getSystemAlert(@PathVariable Long id) {
        log.debug("REST request to get SystemAlert : {}", id);
        SystemAlert systemAlert = systemAlertService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(systemAlert));
    }

    /**
     * DELETE  /system-alerts/:id : delete the "id" systemAlert.
     *
     * @param id the id of the systemAlert to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/system-alerts/{id}")
    //@Timed
    public ResponseEntity<Void> deleteSystemAlert(@PathVariable Long id) {
        log.debug("REST request to delete SystemAlert : {}", id);
        systemAlertService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
