package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing ItemCategory.
 */
public interface ItemCategoryService {

    /**
     * Save a itemCategory.
     *
     * @param itemCategory the entity to save
     * @return the persisted entity
     */
    ItemCategory save(ItemCategory itemCategory);

    /**
     * Get all the itemCategories.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemCategory> findAll(Pageable pageable);

    /**
     * Get the "id" itemCategory.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemCategory findOne(Long id);

    /**
     * Delete the "id" itemCategory.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the itemCategory corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemCategory> search(String query, Pageable pageable);

    /**
     * ItemCategory subscriber
     */
    void consume(ItemCategory itemCategory);
}
