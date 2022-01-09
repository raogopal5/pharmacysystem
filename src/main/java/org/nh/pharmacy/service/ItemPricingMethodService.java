package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ItemPricingMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing ItemPricingMethod.
 */
public interface ItemPricingMethodService {

    /**
     * Save a itemPricingMethod.
     *
     * @param itemPricingMethod the entity to save
     * @return the persisted entity
     */
    ItemPricingMethod save(ItemPricingMethod itemPricingMethod);

    /**
     * Get all the itemPricingMethods.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemPricingMethod> findAll(Pageable pageable);

    /**
     * Get the "id" itemPricingMethod.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemPricingMethod findOne(Long id);

    /**
     * Delete the "id" itemPricingMethod.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the itemPricingMethod corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemPricingMethod> search(String query, Pageable pageable);

    /**
     * ItemPricingMethod subscriber
     */
    void consume(ItemPricingMethod itemPricingMethod);
}
