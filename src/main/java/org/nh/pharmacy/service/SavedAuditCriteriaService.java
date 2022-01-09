package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.SavedAuditCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service Interface for managing SavedAuditCriteria.
 */
public interface SavedAuditCriteriaService {

    /**
     * Save a savedAuditCriteria.
     *
     * @param savedAuditCriteria the entity to save
     * @return the persisted entity
     */
    SavedAuditCriteria save(SavedAuditCriteria savedAuditCriteria);

    /**
     * Get all the savedAuditCriteria.
     *
     * @return the list of entities
     */
    List<SavedAuditCriteria> findAll();

    /**
     * Get the "id" savedAuditCriteria.
     *
     * @param id the id of the entity
     * @return the entity
     */
    SavedAuditCriteria findOne(Long id);

    /**
     * Delete the "id" savedAuditCriteria.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the savedAuditCriteria corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    List<SavedAuditCriteria> search(String query);

    Page<SavedAuditCriteria> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields);

    void deleteIndex();

    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

}
