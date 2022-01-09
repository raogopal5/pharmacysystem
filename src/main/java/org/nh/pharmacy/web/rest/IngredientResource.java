package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.domain.Ingredient;
import org.nh.pharmacy.service.IngredientService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Ingredient.
 */
@RestController
@RequestMapping("/api")
public class IngredientResource {

    private final Logger log = LoggerFactory.getLogger(IngredientResource.class);

    private static final String ENTITY_NAME = "ingredient";
        
    private final IngredientService ingredientService;

    public IngredientResource(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /**
     * POST  /ingredients : Create a new ingredient.
     *
     * @param ingredient the ingredient to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ingredient, or with status 400 (Bad Request) if the ingredient has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ingredients")
    //@Timed
    public ResponseEntity<Ingredient> createIngredient(@Valid @RequestBody Ingredient ingredient) throws URISyntaxException {
        log.debug("REST request to save Ingredient : {}", ingredient);
        if (ingredient.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new ingredient cannot already have an ID")).body(null);
        }
        Ingredient result = ingredientService.save(ingredient);
        return ResponseEntity.created(new URI("/api/ingredients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ingredients : Updates an existing ingredient.
     *
     * @param ingredient the ingredient to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ingredient,
     * or with status 400 (Bad Request) if the ingredient is not valid,
     * or with status 500 (Internal Server Error) if the ingredient couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ingredients")
    //@Timed
    public ResponseEntity<Ingredient> updateIngredient(@Valid @RequestBody Ingredient ingredient) throws URISyntaxException {
        log.debug("REST request to update Ingredient : {}", ingredient);
        if (ingredient.getId() == null) {
            return createIngredient(ingredient);
        }
        Ingredient result = ingredientService.save(ingredient);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ingredient.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ingredients : get all the ingredients.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ingredients in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/ingredients")
    //@Timed
    public ResponseEntity<List<Ingredient>> getAllIngredients(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Ingredients");
        Page<Ingredient> page = ingredientService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ingredients");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ingredients/:id : get the "id" ingredient.
     *
     * @param id the id of the ingredient to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ingredient, or with status 404 (Not Found)
     */
    @GetMapping("/ingredients/{id}")
    //@Timed
    public ResponseEntity<Ingredient> getIngredient(@PathVariable Long id) {
        log.debug("REST request to get Ingredient : {}", id);
        Ingredient ingredient = ingredientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ingredient));
    }

    /**
     * DELETE  /ingredients/:id : delete the "id" ingredient.
     *
     * @param id the id of the ingredient to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ingredients/{id}")
    //@Timed
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        log.debug("REST request to delete Ingredient : {}", id);
        ingredientService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/ingredients?query=:query : search for the ingredient corresponding
     * to the query.
     *
     * @param query the query of the ingredient search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/ingredients")
    //@Timed
    public ResponseEntity<List<Ingredient>> searchIngredients(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Ingredients for query {}", query);
        Page<Ingredient> page = ingredientService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/ingredients");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
