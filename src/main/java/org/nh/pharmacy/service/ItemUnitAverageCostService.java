package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ItemUnitAverageCost;

import java.util.List;

/**
 * Service Interface for managing ItemUnitAverageCost.
 */
public interface ItemUnitAverageCostService {

    /**
     * Save a itemUnitAverageCost.
     *
     * @param itemUnitAverageCost the entity to save
     * @return the persisted entity
     */
    ItemUnitAverageCost save(ItemUnitAverageCost itemUnitAverageCost);

    /**
     * Save a itemUnitAverageCost using new transaction.
     *
     * @param itemUnitAverageCost the entity to save
     * @return the persisted entity
     */
    ItemUnitAverageCost saveInNewTransaction(ItemUnitAverageCost itemUnitAverageCost);

    /**
     * Get all the itemUnitAverageCosts.
     *
     * @return the list of entities
     */
    List<ItemUnitAverageCost> findAll();

    /**
     * Get the "id" itemUnitAverageCost.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemUnitAverageCost findOne(Long id);

    /**
     * Delete the "id" itemUnitAverageCost.
     *
     * @param id the id of the entity
     */
    void delete(Long id);
}
