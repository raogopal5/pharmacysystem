package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.domain.ItemUnitAverageCost;
import org.nh.pharmacy.service.ItemUnitAverageCostService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ItemUnitAverageCost.
 */
@RestController
@RequestMapping("/api")
public class ItemUnitAverageCostResource {

    private final Logger log = LoggerFactory.getLogger(ItemUnitAverageCostResource.class);

    private static final String ENTITY_NAME = "itemUnitAverageCost";
        
    private final ItemUnitAverageCostService itemUnitAverageCostService;

    public ItemUnitAverageCostResource(ItemUnitAverageCostService itemUnitAverageCostService) {
        this.itemUnitAverageCostService = itemUnitAverageCostService;
    }

    /**
     * POST  /item-unit-average-costs : Create a new itemUnitAverageCost.
     *
     * @param itemUnitAverageCost the itemUnitAverageCost to create
     * @return the ResponseEntity with status 201 (Created) and with body the new itemUnitAverageCost, or with status 400 (Bad Request) if the itemUnitAverageCost has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/item-unit-average-costs")
    //@Timed
    public ResponseEntity<ItemUnitAverageCost> createItemUnitAverageCost(@Valid @RequestBody ItemUnitAverageCost itemUnitAverageCost) throws URISyntaxException {
        log.debug("REST request to save ItemUnitAverageCost : {}", itemUnitAverageCost);
        if (itemUnitAverageCost.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new itemUnitAverageCost cannot already have an ID")).body(null);
        }
        ItemUnitAverageCost result = itemUnitAverageCostService.save(itemUnitAverageCost);
        return ResponseEntity.created(new URI("/api/item-unit-average-costs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /item-unit-average-costs : Updates an existing itemUnitAverageCost.
     *
     * @param itemUnitAverageCost the itemUnitAverageCost to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated itemUnitAverageCost,
     * or with status 400 (Bad Request) if the itemUnitAverageCost is not valid,
     * or with status 500 (Internal Server Error) if the itemUnitAverageCost couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/item-unit-average-costs")
    //@Timed
    public ResponseEntity<ItemUnitAverageCost> updateItemUnitAverageCost(@Valid @RequestBody ItemUnitAverageCost itemUnitAverageCost) throws URISyntaxException {
        log.debug("REST request to update ItemUnitAverageCost : {}", itemUnitAverageCost);
        if (itemUnitAverageCost.getId() == null) {
            return createItemUnitAverageCost(itemUnitAverageCost);
        }
        ItemUnitAverageCost result = itemUnitAverageCostService.save(itemUnitAverageCost);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, itemUnitAverageCost.getId().toString()))
            .body(result);
    }

    /**
     * GET  /item-unit-average-costs : get all the itemUnitAverageCosts.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of itemUnitAverageCosts in body
     */
    @GetMapping("/item-unit-average-costs")
    //@Timed
    public List<ItemUnitAverageCost> getAllItemUnitAverageCosts() {
        log.debug("REST request to get all ItemUnitAverageCosts");
        return itemUnitAverageCostService.findAll();
    }

    /**
     * GET  /item-unit-average-costs/:id : get the "id" itemUnitAverageCost.
     *
     * @param id the id of the itemUnitAverageCost to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the itemUnitAverageCost, or with status 404 (Not Found)
     */
    @GetMapping("/item-unit-average-costs/{id}")
    //@Timed
    public ResponseEntity<ItemUnitAverageCost> getItemUnitAverageCost(@PathVariable Long id) {
        log.debug("REST request to get ItemUnitAverageCost : {}", id);
        ItemUnitAverageCost itemUnitAverageCost = itemUnitAverageCostService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(itemUnitAverageCost));
    }

    /**
     * DELETE  /item-unit-average-costs/:id : delete the "id" itemUnitAverageCost.
     *
     * @param id the id of the itemUnitAverageCost to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/item-unit-average-costs/{id}")
    //@Timed
    public ResponseEntity<Void> deleteItemUnitAverageCost(@PathVariable Long id) {
        log.debug("REST request to delete ItemUnitAverageCost : {}", id);
        itemUnitAverageCostService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
