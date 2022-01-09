package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.UOM;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the UOM entity.
 */
public interface UOMSearchRepository extends ElasticsearchRepository<UOM, Long> {
}
