package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.MedicationRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the MedicationRequest entity.
 */
public interface MedicationRequestSearchRepository extends ElasticsearchRepository<MedicationRequest, Long> {
}
