package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Locator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Locator.
 */
public interface LocatorService {

    /**
     * Save a locator.
     *
     * @param locator the entity to save
     * @return the persisted entity
     */
    Locator save(Locator locator);

    /**
     * Get all the locators.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Locator> findAll(Pageable pageable);

    /**
     * Get the "id" locator.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Locator findOne(Long id);

    /**
     * Delete the "id" locator.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the locator corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Locator> search(String query, Pageable pageable);

    /**
     * Locator subscriber
     */
    void consume(Locator locator);
}
