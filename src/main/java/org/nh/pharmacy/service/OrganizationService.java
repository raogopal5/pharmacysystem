package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Organization.
 */
public interface OrganizationService {

    /**
     * Save a organization.
     *
     * @param organization the entity to save
     * @return the persisted entity
     */
    Organization save(Organization organization);

    /**
     * Get all the organizations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Organization> findAll(Pageable pageable);

    /**
     * Get the "id" organization.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Organization findOne(Long id);

    /**
     * Delete the "id" organization.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the organization corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Organization> search(String query, Pageable pageable);

    /**
     * Organization subscriber
     */
    void consume(Organization organization);
}
