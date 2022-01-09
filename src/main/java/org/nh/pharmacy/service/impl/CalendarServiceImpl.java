package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Calendar;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.CalendarRepository;
import org.nh.pharmacy.repository.search.CalendarSearchRepository;
import org.nh.pharmacy.service.CalendarService;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Calendar.
 */
@Service
@Transactional
public class CalendarServiceImpl implements CalendarService{

    private final Logger log = LoggerFactory.getLogger(CalendarServiceImpl.class);

    private final CalendarRepository calendarRepository;

    private final CalendarSearchRepository calendarSearchRepository;

    @Autowired
    private SystemAlertService systemAlertService;

    public CalendarServiceImpl(CalendarRepository calendarRepository, CalendarSearchRepository calendarSearchRepository) {
        this.calendarRepository = calendarRepository;
        this.calendarSearchRepository = calendarSearchRepository;
    }

    /**
     * Save a calendar.
     *
     * @param calendar the entity to save
     * @return the persisted entity
     */
    @Override
    public Calendar save(Calendar calendar) {
        log.debug("Request to save Calendar : {}", calendar);
        Calendar result = calendarRepository.save(calendar);
        calendarSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the calendars.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Calendar> findAll(Pageable pageable) {
        log.debug("Request to get all Calendars");
        Page<Calendar> result = calendarRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one calendar by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Calendar findOne(Long id) {
        log.debug("Request to get Calendar : {}", id);
        Calendar calendar = calendarRepository.findById(id).get();
        return calendar;
    }

    /**
     *  Delete the  calendar by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Calendar : {}", id);
        calendarRepository.deleteById(id);
        calendarSearchRepository.deleteById(id);
    }

    /**
     * Search for the calendar corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Calendar> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Calendars for query {}", query);
        Page<Calendar> result = calendarSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save calendar
     * @param calendar
     */
    @ServiceActivator(inputChannel = Channels.CALENDAR_INPUT)
    @Override
    public void consume(Calendar calendar){
        try {
            log.debug("Request to consume Calendar id : {}", calendar.getId());
            calendarRepository.save(calendar);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
