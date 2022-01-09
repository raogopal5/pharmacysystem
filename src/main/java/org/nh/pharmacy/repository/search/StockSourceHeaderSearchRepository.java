package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockSourceHeader;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the StockSourceHeader entity.
 */
public interface StockSourceHeaderSearchRepository extends ElasticsearchRepository<StockSourceHeader, Long> {
}
