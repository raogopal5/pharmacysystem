package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Locator;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.LocatorRepository;
import org.nh.pharmacy.repository.search.LocatorSearchRepository;
import org.nh.pharmacy.service.LocatorService;
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
 * Service Implementation for managing Locator.
 */
@Service
@Transactional
public class LocatorServiceImpl implements LocatorService {

    private final Logger log = LoggerFactory.getLogger(LocatorServiceImpl.class);

    private final LocatorRepository locatorRepository;

    private final LocatorSearchRepository locatorSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;

    public LocatorServiceImpl(LocatorRepository locatorRepository, LocatorSearchRepository locatorSearchRepository) {
        this.locatorRepository = locatorRepository;
        this.locatorSearchRepository = locatorSearchRepository;
    }

    /**
     * Save a locator.
     *
     * @param locator the entity to save
     * @return the persisted entity
     */
    @Override
    public Locator save(Locator locator) {
        log.debug("Request to save Locator : {}", locator);
        Locator result = locatorRepository.save(locator);
        locatorSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the locators.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Locator> findAll(Pageable pageable) {
        log.debug("Request to get all Locators");
        Page<Locator> result = locatorRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one locator by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Locator findOne(Long id) {
        log.debug("Request to get Locator : {}", id);
        Locator locator = locatorRepository.findById(id).get();
        return locator;
    }

    /**
     * Delete the  locator by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Locator : {}", id);
        locatorRepository.deleteById(id);
        locatorSearchRepository.deleteById(id);
    }

    /**
     * Search for the locator corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Locator> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Locators for query {}", query);
        Page<Locator> result = locatorSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save locator
     *
     * @param locator
     */
    @ServiceActivator(inputChannel = Channels.LOCATOR_INPUT)
    @Override
    public void consume(Locator locator) {
        try {
            log.debug("Request to consume Locator code : {}", locator.getCode());
            locatorRepository.save(locator);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
