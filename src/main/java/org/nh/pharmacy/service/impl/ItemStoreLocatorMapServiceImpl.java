package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.ItemStoreLocatorMap;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.ItemStoreLocatorMapRepository;
import org.nh.pharmacy.repository.search.ItemStoreLocatorMapSearchRepository;
import org.nh.pharmacy.service.ItemStoreLocatorMapService;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing ItemStoreLocatorMap.
 */
@Service
@Transactional
public class ItemStoreLocatorMapServiceImpl implements ItemStoreLocatorMapService {

    private final Logger log = LoggerFactory.getLogger(ItemStoreLocatorMapServiceImpl.class);

    private final ItemStoreLocatorMapRepository itemStoreLocatorMapRepository;

    private final ItemStoreLocatorMapSearchRepository itemStoreLocatorMapSearchRepository;

    private final ApplicationProperties applicationProperties;

    @Autowired
    SystemAlertService systemAlertService;

    public ItemStoreLocatorMapServiceImpl(ItemStoreLocatorMapRepository itemStoreLocatorMapRepository, ItemStoreLocatorMapSearchRepository itemStoreLocatorMapSearchRepository,
                                          ApplicationProperties applicationProperties) {
        this.itemStoreLocatorMapRepository = itemStoreLocatorMapRepository;
        this.itemStoreLocatorMapSearchRepository = itemStoreLocatorMapSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Save a itemStoreLocatorMap.
     *
     * @param itemStoreLocatorMap the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemStoreLocatorMap save(ItemStoreLocatorMap itemStoreLocatorMap) {
        log.debug("Request to save ItemStoreLocatorMap : {}", itemStoreLocatorMap);
        ItemStoreLocatorMap result = itemStoreLocatorMapRepository.save(itemStoreLocatorMap);
        itemStoreLocatorMapSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the itemStoreLocatorMaps.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemStoreLocatorMap> findAll(Pageable pageable) {
        log.debug("Request to get all ItemStoreLocatorMaps");
        Page<ItemStoreLocatorMap> result = itemStoreLocatorMapRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one itemStoreLocatorMap by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemStoreLocatorMap findOne(Long id) {
        log.debug("Request to get ItemStoreLocatorMap : {}", id);
        ItemStoreLocatorMap itemStoreLocatorMap = itemStoreLocatorMapRepository.findById(id).get();
        return itemStoreLocatorMap;
    }

    /**
     * Delete the  itemStoreLocatorMap by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemStoreLocatorMap : {}", id);
        itemStoreLocatorMapRepository.deleteById(id);
        itemStoreLocatorMapSearchRepository.deleteById(id);
    }

    /**
     * Search for the itemStoreLocatorMap corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemStoreLocatorMap> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ItemStoreLocatorMaps for query {}", query);
        Page<ItemStoreLocatorMap> result = itemStoreLocatorMapSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save ItemStoreLocatorMap
     *
     * @param itemStoreLocatorMap
     */
    @ServiceActivator(inputChannel = Channels.ITEMSTORELOCATORMAP_INPUT)
    @Override
    public void consume(ItemStoreLocatorMap itemStoreLocatorMap) {
        try {
            log.debug("Request to consume ItemStoreLocatorMap Id : {}", itemStoreLocatorMap.getId());
            itemStoreLocatorMapRepository.save(itemStoreLocatorMap);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }

    @Override
    public void doIndex() {
        log.debug("Request to do elastic index on ItemStoreLocator");
        long resultCount = itemStoreLocatorMapRepository.findAll().size();
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            List<ItemStoreLocatorMap> data = itemStoreLocatorMapRepository.findAllSortById(PageRequest.of(i, pageSize)).getContent();
            if (!data.isEmpty()) {
                itemStoreLocatorMapSearchRepository.saveAll(data);
            }
        }
        itemStoreLocatorMapSearchRepository.refresh();
    }
}
