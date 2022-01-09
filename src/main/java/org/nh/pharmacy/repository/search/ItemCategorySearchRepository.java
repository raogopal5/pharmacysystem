package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.ItemCategory;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the ItemCategory entity.
 */
public interface ItemCategorySearchRepository extends ElasticsearchRepository<ItemCategory, Long> {
}
