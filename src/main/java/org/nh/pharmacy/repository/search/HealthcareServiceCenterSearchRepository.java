package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the HealthcareServiceCenter entity.
 */
public interface HealthcareServiceCenterSearchRepository extends ElasticsearchRepository<HealthcareServiceCenter, Long> {
}
