package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.Locator;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Locator entity.
 */
public interface LocatorSearchRepository extends ElasticsearchRepository<Locator, Long> {
}
