package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.SystemAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing SystemAlert.
 */
public interface SystemAlertService {

    /**
     * Save a systemAlert.
     *
     * @param systemAlert the entity to save
     * @return the persisted entity
     */
    SystemAlert save(SystemAlert systemAlert);

    /**
     * Get all the systemAlerts.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<SystemAlert> findAll(Pageable pageable);

    /**
     * Get the "id" systemAlert.
     *
     * @param id the id of the entity
     * @return the entity
     */
    SystemAlert findOne(Long id);

    /**
     * Delete the "id" systemAlert.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

}
