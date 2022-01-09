package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.domain.Ingredient;
import org.nh.pharmacy.repository.IngredientRepository;
import org.nh.pharmacy.repository.search.IngredientSearchRepository;
import org.nh.pharmacy.service.IngredientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Ingredient.
 */
@Service
@Transactional
public class IngredientServiceImpl implements IngredientService {

    private final Logger log = LoggerFactory.getLogger(IngredientServiceImpl.class);

    private final IngredientRepository ingredientRepository;

    private final IngredientSearchRepository ingredientSearchRepository;

    public IngredientServiceImpl(IngredientRepository ingredientRepository, IngredientSearchRepository ingredientSearchRepository) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientSearchRepository = ingredientSearchRepository;
    }

    /**
     * Save a ingredient.
     *
     * @param ingredient the entity to save
     * @return the persisted entity
     */
    @Override
    public Ingredient save(Ingredient ingredient) {
        log.debug("Request to save Ingredient : {}", ingredient);
        Ingredient result = ingredientRepository.save(ingredient);
        ingredientSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the ingredients.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Ingredient> findAll(Pageable pageable) {
        log.debug("Request to get all Ingredients");
        Page<Ingredient> result = ingredientRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one ingredient by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Ingredient findOne(Long id) {
        log.debug("Request to get Ingredient : {}", id);
        Ingredient ingredient = ingredientRepository.findById(id).get();
        return ingredient;
    }

    /**
     * Delete the  ingredient by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Ingredient : {}", id);
        ingredientRepository.deleteById(id);
        ingredientSearchRepository.deleteById(id);
    }

    /**
     * Search for the ingredient corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Ingredient> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Ingredients for query {}", query);
        Page<Ingredient> result = ingredientSearchRepository.search(queryStringQuery(query)
            .defaultOperator(Operator.AND), pageable);
        return result;
    }
}
