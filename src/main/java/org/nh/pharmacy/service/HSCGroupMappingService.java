package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.HSCGroupMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing HSCGroupMapping.
 */
public interface HSCGroupMappingService {

    /**
     * Save a hSCGroupMapping.
     *
     * @param hSCGroupMapping the entity to save
     * @return the persisted entity
     */
    HSCGroupMapping save(HSCGroupMapping hSCGroupMapping);

    /**
     * Get all the hSCGroupMappings.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<HSCGroupMapping> findAll(Pageable pageable);

    /**
     * Get the "id" hSCGroupMapping.
     *
     * @param id the id of the entity
     * @return the entity
     */
    HSCGroupMapping findOne(Long id);

    /**
     * Delete the "id" hSCGroupMapping.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the hSCGroupMapping corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<HSCGroupMapping> search(String query, Pageable pageable);

    /**
     * hscGroupMapping subscriber
     */
    void consume(HSCGroupMapping hscGroupMapping);


    /**
     * Delete set of removed object from given entity
     */
    void consume(String removedIdsWithEntity) throws Exception;
}
