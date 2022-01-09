package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Item.
 */
public interface ItemService {

    /**
     * Save a item.
     *
     * @param item the entity to save
     * @return the persisted entity
     */
    Item save(Item item);

    /**
     * Get all the items.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Item> findAll(Pageable pageable);

    /**
     * Get the "id" item.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Item findOne(Long id);

    /**
     * Delete the "id" item.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the item corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Item> search(String query, Pageable pageable);

    /**
     * Item subscriber
     */
    void consume(Item item);
}
