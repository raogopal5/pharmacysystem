package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.domain.ItemUnitAverageCost;
import org.nh.pharmacy.repository.ItemUnitAverageCostRepository;
import org.nh.pharmacy.service.ItemUnitAverageCostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing ItemUnitAverageCost.
 */
@Service
@Transactional
public class ItemUnitAverageCostServiceImpl implements ItemUnitAverageCostService {

    private final Logger log = LoggerFactory.getLogger(ItemUnitAverageCostServiceImpl.class);

    private final ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    public ItemUnitAverageCostServiceImpl(ItemUnitAverageCostRepository itemUnitAverageCostRepository) {
        this.itemUnitAverageCostRepository = itemUnitAverageCostRepository;
    }

    /**
     * Save a itemUnitAverageCost.
     *
     * @param itemUnitAverageCost the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemUnitAverageCost save(ItemUnitAverageCost itemUnitAverageCost) {
        log.debug("Request to save ItemUnitAverageCost : {}", itemUnitAverageCost);
        ItemUnitAverageCost result = itemUnitAverageCostRepository.save(itemUnitAverageCost);
        return result;
    }

    /**
     * Save a itemUnitAverageCost.
     *
     * @param itemUnitAverageCost the entity to save
     * @return the persisted entity
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ItemUnitAverageCost saveInNewTransaction(ItemUnitAverageCost itemUnitAverageCost) {
        log.debug("Request to save ItemUnitAverageCost : {}", itemUnitAverageCost);
        ItemUnitAverageCost result = itemUnitAverageCostRepository.save(itemUnitAverageCost);
        return result;
    }

    /**
     * Get all the itemUnitAverageCosts.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemUnitAverageCost> findAll() {
        log.debug("Request to get all ItemUnitAverageCosts");
        List<ItemUnitAverageCost> result = itemUnitAverageCostRepository.findAll();

        return result;
    }

    /**
     * Get one itemUnitAverageCost by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemUnitAverageCost findOne(Long id) {
        log.debug("Request to get ItemUnitAverageCost : {}", id);
        ItemUnitAverageCost itemUnitAverageCost = itemUnitAverageCostRepository.findById(id).get();
        return itemUnitAverageCost;
    }

    /**
     * Delete the  itemUnitAverageCost by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemUnitAverageCost : {}", id);
        itemUnitAverageCostRepository.deleteById(id);
    }
}
