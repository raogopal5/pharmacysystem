package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.ItemStoreStockView;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the ItemStoreStockView entity.
 */
public interface ItemStoreStockViewSearchRepository extends ElasticsearchRepository<ItemStoreStockView, Long> {
}
