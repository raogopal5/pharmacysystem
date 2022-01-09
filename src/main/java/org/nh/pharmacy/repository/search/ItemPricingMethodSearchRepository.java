package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.ItemPricingMethod;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the ItemPricingMethod entity.
 */
public interface ItemPricingMethodSearchRepository extends ElasticsearchRepository<ItemPricingMethod, Long> {
}
