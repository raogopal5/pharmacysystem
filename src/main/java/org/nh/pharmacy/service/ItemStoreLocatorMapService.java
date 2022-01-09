package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ItemStoreLocatorMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing ItemStoreLocatorMap.
 */
public interface ItemStoreLocatorMapService {

    /**
     * Save a itemStoreLocatorMap.
     *
     * @param itemStoreLocatorMap the entity to save
     * @return the persisted entity
     */
    ItemStoreLocatorMap save(ItemStoreLocatorMap itemStoreLocatorMap);

    /**
     * Get all the itemStoreLocatorMaps.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemStoreLocatorMap> findAll(Pageable pageable);

    /**
     * Get the "id" itemStoreLocatorMap.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemStoreLocatorMap findOne(Long id);

    /**
     * Delete the "id" itemStoreLocatorMap.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the itemStoreLocatorMap corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemStoreLocatorMap> search(String query, Pageable pageable);

    /**
     * ItemStoreLocatorMap subscriber
     */
    void consume(ItemStoreLocatorMap itemStoreLocatorMap);

    /**
     * Do elastic index for ItemStoreLocatorMap
     */
    void doIndex();
}
