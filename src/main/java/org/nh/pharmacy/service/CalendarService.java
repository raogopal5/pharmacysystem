package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Calendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Calendar.
 */
public interface CalendarService {

    /**
     * Save a calendar.
     *
     * @param calendar the entity to save
     * @return the persisted entity
     */
    Calendar save(Calendar calendar);

    /**
     *  Get all the calendars.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<Calendar> findAll(Pageable pageable);

    /**
     *  Get the "id" calendar.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Calendar findOne(Long id);

    /**
     *  Delete the "id" calendar.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the calendar corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<Calendar> search(String query, Pageable pageable);

    /**
     *Calendar subscriber
     */
    void consume(Calendar calendar);
}
