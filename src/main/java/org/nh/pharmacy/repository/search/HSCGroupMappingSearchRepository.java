package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.HSCGroupMapping;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the HSCGroupMapping entity.
 */
public interface HSCGroupMappingSearchRepository extends ElasticsearchRepository<HSCGroupMapping, Long> {
}
