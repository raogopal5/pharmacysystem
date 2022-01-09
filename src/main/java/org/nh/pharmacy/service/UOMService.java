package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.UOM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing UOM.
 */
public interface UOMService {

    /**
     * Save a uOM.
     *
     * @param uOM the entity to save
     * @return the persisted entity
     */
    UOM save(UOM uOM);

    /**
     * Get all the uOMS.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<UOM> findAll(Pageable pageable);

    /**
     * Get the "id" uOM.
     *
     * @param id the id of the entity
     * @return the entity
     */
    UOM findOne(Long id);

    /**
     * Delete the "id" uOM.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the uOM corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<UOM> search(String query, Pageable pageable);

    /**
     * UOM subscriber
     */
    void consume(UOM uom);
}
