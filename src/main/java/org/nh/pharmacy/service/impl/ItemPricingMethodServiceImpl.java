package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.ItemPricingMethod;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.ItemPricingMethodRepository;
import org.nh.pharmacy.repository.search.ItemPricingMethodSearchRepository;
import org.nh.pharmacy.service.ItemPricingMethodService;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing ItemPricingMethod.
 */
@Service
@Transactional
public class ItemPricingMethodServiceImpl implements ItemPricingMethodService {

    private final Logger log = LoggerFactory.getLogger(ItemPricingMethodServiceImpl.class);

    private final ItemPricingMethodRepository itemPricingMethodRepository;

    private final ItemPricingMethodSearchRepository itemPricingMethodSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;

    public ItemPricingMethodServiceImpl(ItemPricingMethodRepository itemPricingMethodRepository, ItemPricingMethodSearchRepository itemPricingMethodSearchRepository) {
        this.itemPricingMethodRepository = itemPricingMethodRepository;
        this.itemPricingMethodSearchRepository = itemPricingMethodSearchRepository;
    }

    /**
     * Save a itemPricingMethod.
     *
     * @param itemPricingMethod the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemPricingMethod save(ItemPricingMethod itemPricingMethod) {
        log.debug("Request to save ItemPricingMethod : {}", itemPricingMethod);
        ItemPricingMethod result = itemPricingMethodRepository.save(itemPricingMethod);
        itemPricingMethodSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the itemPricingMethods.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemPricingMethod> findAll(Pageable pageable) {
        log.debug("Request to get all ItemPricingMethods");
        Page<ItemPricingMethod> result = itemPricingMethodRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one itemPricingMethod by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemPricingMethod findOne(Long id) {
        log.debug("Request to get ItemPricingMethod : {}", id);
        ItemPricingMethod itemPricingMethod = itemPricingMethodRepository.findById(id).get();
        return itemPricingMethod;
    }

    /**
     * Delete the  itemPricingMethod by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemPricingMethod : {}", id);
        itemPricingMethodRepository.deleteById(id);
        itemPricingMethodSearchRepository.deleteById(id);
    }

    /**
     * Search for the itemPricingMethod corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemPricingMethod> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ItemPricingMethods for query {}", query);
        Page<ItemPricingMethod> result = itemPricingMethodSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save ItemPricingMethod
     *
     * @param itemPricingMethod
     */
    @ServiceActivator(inputChannel = Channels.ITEMPRICINGMETHOD_INPUT)
    @Override
    public void consume(ItemPricingMethod itemPricingMethod) {
        try {
            log.debug("Request to consume ItemPricingMethod Id : {}", itemPricingMethod.getId());
            itemPricingMethodRepository.save(itemPricingMethod);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
