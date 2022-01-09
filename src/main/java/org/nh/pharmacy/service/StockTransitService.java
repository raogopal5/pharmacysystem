package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.StockTransit;

import java.util.List;

/**
 * Service Interface for managing StockTransit.
 */
public interface StockTransitService {

    /**
     * Save a stockTransit.
     *
     * @param stockTransit the entity to save
     * @return the persisted entity
     */
    StockTransit save(StockTransit stockTransit);

    /**
     *  Get all the stockTransits.
     *
     *  @return the list of entities
     */
    List<StockTransit> findAll();

    /**
     *  Get the "id" stockTransit.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    StockTransit findOne(Long id);

    /**
     *  Delete the "id" stockTransit.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
