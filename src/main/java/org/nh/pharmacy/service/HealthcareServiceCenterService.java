package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing HealthcareServiceCenter.
 */
public interface HealthcareServiceCenterService {

    /**
     * Save a healthcareServiceCenter.
     *
     * @param healthcareServiceCenter the entity to save
     * @return the persisted entity
     */
    HealthcareServiceCenter save(HealthcareServiceCenter healthcareServiceCenter);

    /**
     * Get all the healthcareServiceCenters.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<HealthcareServiceCenter> findAll(Pageable pageable);

    /**
     * Get the "id" healthcareServiceCenter.
     *
     * @param id the id of the entity
     * @return the entity
     */
    HealthcareServiceCenter findOne(Long id);

    /**
     * Delete the "id" healthcareServiceCenter.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the healthcareServiceCenter corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<HealthcareServiceCenter> search(String query, Pageable pageable);

    /**
     * HealthcareServiceCenter subscriber
     */
    void consume(HealthcareServiceCenter healthcareServiceCenter);
}
