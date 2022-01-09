package org.nh.pharmacy.repository.search;


import org.nh.pharmacy.domain.dto.ItemStoreStock;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository.
 */
public interface ItemStoreStockSearchRepository extends ElasticsearchRepository<ItemStoreStock, String> {
}
