package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.OrganizationRepository;
import org.nh.pharmacy.repository.search.OrganizationSearchRepository;
import org.nh.pharmacy.service.OrganizationService;
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
 * Service Implementation for managing Organization.
 */
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    private final OrganizationRepository organizationRepository;

    private final OrganizationSearchRepository organizationSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;


    public OrganizationServiceImpl(OrganizationRepository organizationRepository, OrganizationSearchRepository organizationSearchRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationSearchRepository = organizationSearchRepository;
    }

    /**
     * Save a organization.
     *
     * @param organization the entity to save
     * @return the persisted entity
     */
    @Override
    public Organization save(Organization organization) {
        log.debug("Request to save Organization : {}", organization);
        Organization result = organizationRepository.save(organization);
        organizationSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the organizations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Organization> findAll(Pageable pageable) {
        log.debug("Request to get all Organizations");
        Page<Organization> result = organizationRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one organization by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Organization findOne(Long id) {
        log.debug("Request to get Organization : {}", id);
        Organization organization = organizationRepository.findById(id).get();
        return organization;
    }

    /**
     * Delete the  organization by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Organization : {}", id);
        organizationRepository.deleteById(id);
        organizationSearchRepository.deleteById(id);
    }

    /**
     * Search for the organization corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Organization> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Organizations for query {}", query);
        Page<Organization> result = organizationSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save organization
     *
     * @param organization
     */
    @ServiceActivator(inputChannel = Channels.ORGANIZATION_INPUT)
    @Override
    public void consume(Organization organization) {
        try {
            log.debug("Request to consume Organization code : {}", organization.getCode());
            organizationRepository.save(organization);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
