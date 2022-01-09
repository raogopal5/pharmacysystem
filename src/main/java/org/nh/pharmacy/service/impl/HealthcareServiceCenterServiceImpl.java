package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.HealthcareServiceCenterRepository;
import org.nh.pharmacy.repository.search.HealthcareServiceCenterSearchRepository;
import org.nh.pharmacy.service.HealthcareServiceCenterService;
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
 * Service Implementation for managing HealthcareServiceCenter.
 */
@Service
@Transactional
public class HealthcareServiceCenterServiceImpl implements HealthcareServiceCenterService {

    private final Logger log = LoggerFactory.getLogger(HealthcareServiceCenterServiceImpl.class);

    private final HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    private final HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;

    public HealthcareServiceCenterServiceImpl(HealthcareServiceCenterRepository healthcareServiceCenterRepository, HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository) {
        this.healthcareServiceCenterRepository = healthcareServiceCenterRepository;
        this.healthcareServiceCenterSearchRepository = healthcareServiceCenterSearchRepository;
    }

    /**
     * Save a healthcareServiceCenter.
     *
     * @param healthcareServiceCenter the entity to save
     * @return the persisted entity
     */
    @Override
    public HealthcareServiceCenter save(HealthcareServiceCenter healthcareServiceCenter) {
        log.debug("Request to save HealthcareServiceCenter : {}", healthcareServiceCenter);
        HealthcareServiceCenter result = healthcareServiceCenterRepository.save(healthcareServiceCenter);
        healthcareServiceCenterSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the healthcareServiceCenters.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HealthcareServiceCenter> findAll(Pageable pageable) {
        log.debug("Request to get all HealthcareServiceCenters");
        Page<HealthcareServiceCenter> result = healthcareServiceCenterRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one healthcareServiceCenter by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public HealthcareServiceCenter findOne(Long id) {
        log.debug("Request to get HealthcareServiceCenter : {}", id);
        HealthcareServiceCenter healthcareServiceCenter = healthcareServiceCenterRepository.findById(id).get();
        return healthcareServiceCenter;
    }

    /**
     * Delete the  healthcareServiceCenter by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete HealthcareServiceCenter : {}", id);
        healthcareServiceCenterRepository.deleteById(id);
        healthcareServiceCenterSearchRepository.deleteById(id);
    }

    /**
     * Search for the healthcareServiceCenter corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HealthcareServiceCenter> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of HealthcareServiceCenters for query {}", query);
        Page<HealthcareServiceCenter> result = healthcareServiceCenterSearchRepository.search(queryStringQuery(query)
            .field("code").field("name").field("displayName").field("active")
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save healthcareServiceCenter
     *
     * @param healthcareServiceCenter
     */
    @ServiceActivator(inputChannel = Channels.HSC_INPUT)
    @Override
    public void consume(HealthcareServiceCenter healthcareServiceCenter) {
        try {
            log.debug("Request to consume HealthcareServiceCenter code : {}", healthcareServiceCenter.getCode());
            healthcareServiceCenterRepository.save(healthcareServiceCenter);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
