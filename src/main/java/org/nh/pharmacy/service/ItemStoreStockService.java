package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.dto.ItemStoreStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ItemStoreStockService {

    /**
     * Save a ItemStoreStock.
     *
     * @param itemStoreStock the entity to save
     * @return the persisted entity
     */
    ItemStoreStock save(ItemStoreStock itemStoreStock);

    /**
     * Search for the ItemStoreStock corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemStoreStock> search(String query, Pageable pageable);
}
