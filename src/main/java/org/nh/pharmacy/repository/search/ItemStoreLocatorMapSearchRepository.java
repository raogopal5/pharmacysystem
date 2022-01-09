package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.ItemStoreLocatorMap;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the ItemStoreLocatorMap entity.
 */
public interface ItemStoreLocatorMapSearchRepository extends ElasticsearchRepository<ItemStoreLocatorMap, Long> {
}
