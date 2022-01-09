package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Ingredient.
 */
public interface IngredientService {

    /**
     * Save a ingredient.
     *
     * @param ingredient the entity to save
     * @return the persisted entity
     */
    Ingredient save(Ingredient ingredient);

    /**
     * Get all the ingredients.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Ingredient> findAll(Pageable pageable);

    /**
     * Get the "id" ingredient.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Ingredient findOne(Long id);

    /**
     * Delete the "id" ingredient.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the ingredient corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Ingredient> search(String query, Pageable pageable);
}
