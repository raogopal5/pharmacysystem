package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.Calendar;
import org.nh.pharmacy.service.CalendarService;
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
 * REST controller for managing Calendar.
 */
@RestController
@RequestMapping("/api")
public class CalendarResource {

    private final Logger log = LoggerFactory.getLogger(CalendarResource.class);

    private static final String ENTITY_NAME = "calendar";

    private final CalendarService calendarService;

    public CalendarResource(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * GET  /calendars : get all the calendars.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of calendars in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/calendars")
    //@Timed
    public ResponseEntity<List<Calendar>> getAllCalendars(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Calendars");
        Page<Calendar> page = calendarService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/calendars");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /calendars/:id : get the "id" calendar.
     *
     * @param id the id of the calendar to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the calendar, or with status 404 (Not Found)
     */
    @GetMapping("/calendars/{id}")
    //@Timed
    public ResponseEntity<Calendar> getCalendar(@PathVariable Long id) {
        log.debug("REST request to get Calendar : {}", id);
        Calendar calendar = calendarService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(calendar));
    }

    /**
     * SEARCH  /_search/calendars?query=:query : search for the calendar corresponding
     * to the query.
     *
     * @param query the query of the calendar search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/calendars")
    //@Timed
    public ResponseEntity<List<Calendar>> searchCalendars(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Calendars for query {}", query);
        Page<Calendar> page = calendarService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/calendars");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
