package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Medication.
 */
public interface MedicationService {

    /**
     * Save a medication.
     *
     * @param medication the entity to save
     * @return the persisted entity
     */
    Medication save(Medication medication);

    /**
     * Get all the medications.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Medication> findAll(Pageable pageable);

    /**
     * Get the "id" medication.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Medication findOne(Long id);

    /**
     * Delete the "id" medication.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the medication corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Medication> search(String query, Pageable pageable);

    /**
     * Medication subscriber
     */
    void consume(String medicationData);
}
