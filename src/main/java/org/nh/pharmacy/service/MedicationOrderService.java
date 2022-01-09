package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.MedicationOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service Interface for managing MedicationOrder.
 */
public interface MedicationOrderService {

    /**
     * Save a medicationOrder.
     *
     * @param medicationOrder the entity to save
     * @return the persisted entity
     */
    MedicationOrder save(MedicationOrder medicationOrder);

    /**
     *  Get all the medicationOrders.
     *
     *  @return the list of entities
     */
    List<MedicationOrder> findAll();

    /**
     *  Get the "id" medicationOrder.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    MedicationOrder findOne(Long id);

    /**
     *  Delete the "id" medicationOrder.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the medicationOrder corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable
     * @return the list of entities
     */
    Page<MedicationOrder> search(String query, Pageable pageable);

    void reIndexWithMedicationRequestId(Long medicationRequestId);

    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    void deleteIndex();

    void refreshIndex();
}
