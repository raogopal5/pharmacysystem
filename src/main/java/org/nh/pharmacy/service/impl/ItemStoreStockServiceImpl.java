package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.domain.dto.ItemStoreStock;
import org.nh.pharmacy.repository.search.ItemStoreStockSearchRepository;
import org.nh.pharmacy.service.ItemStoreStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
@Transactional
public class ItemStoreStockServiceImpl implements ItemStoreStockService {

    private final Logger log = LoggerFactory.getLogger(ItemStoreStockServiceImpl.class);

    private final ItemStoreStockSearchRepository itemStoreStockSearchRepository;

    public ItemStoreStockServiceImpl(ItemStoreStockSearchRepository itemStoreStockSearchRepository) {
        this.itemStoreStockSearchRepository = itemStoreStockSearchRepository;
    }


    /**
     * Save a itemStoreStock.
     *
     * @param itemStoreStock the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemStoreStock save(ItemStoreStock itemStoreStock) {
        log.debug("Request to save itemStoreStock : {}", itemStoreStock);
        return itemStoreStockSearchRepository.save(itemStoreStock);
    }

    /**
     * Search for the ItemStoreStock corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemStoreStock> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ItemStoreStock for query {}", query);
        Page<ItemStoreStock> result = itemStoreStockSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }
}
