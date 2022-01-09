package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.ItemCategory;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.ItemCategoryRepository;
import org.nh.pharmacy.repository.search.ItemCategorySearchRepository;
import org.nh.pharmacy.service.ItemCategoryService;
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
 * Service Implementation for managing ItemCategory.
 */
@Service
@Transactional
public class ItemCategoryServiceImpl implements ItemCategoryService {

    private final Logger log = LoggerFactory.getLogger(ItemCategoryServiceImpl.class);

    private final ItemCategoryRepository itemCategoryRepository;

    private final ItemCategorySearchRepository itemCategorySearchRepository;

    @Autowired
    SystemAlertService systemAlertService;


    public ItemCategoryServiceImpl(ItemCategoryRepository itemCategoryRepository, ItemCategorySearchRepository itemCategorySearchRepository) {
        this.itemCategoryRepository = itemCategoryRepository;
        this.itemCategorySearchRepository = itemCategorySearchRepository;
    }

    /**
     * Save a itemCategory.
     *
     * @param itemCategory the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemCategory save(ItemCategory itemCategory) {
        log.debug("Request to save ItemCategory : {}", itemCategory);
        ItemCategory result = itemCategoryRepository.save(itemCategory);
        itemCategorySearchRepository.save(result);
        return result;
    }

    /**
     * Get all the itemCategories.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemCategory> findAll(Pageable pageable) {
        log.debug("Request to get all ItemCategories");
        Page<ItemCategory> result = itemCategoryRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one itemCategory by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemCategory findOne(Long id) {
        log.debug("Request to get ItemCategory : {}", id);
        ItemCategory itemCategory = itemCategoryRepository.findById(id).get();
        return itemCategory;
    }

    /**
     * Delete the  itemCategory by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemCategory : {}", id);
        itemCategoryRepository.deleteById(id);
        itemCategorySearchRepository.deleteById(id);
    }

    /**
     * Search for the itemCategory corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemCategory> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ItemCategories for query {}", query);
        Page<ItemCategory> result = itemCategorySearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save itemCategory
     *
     * @param itemCategory
     */
    @ServiceActivator(inputChannel = Channels.ITEMCATEGORY_INPUT)
    @Override
    public void consume(ItemCategory itemCategory) {
        try {
            log.debug("Request to consume ItemCategory code : {}", itemCategory.getCode());
            itemCategoryRepository.save(itemCategory);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
