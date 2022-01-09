package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.Medication;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Medication entity.
 */
public interface MedicationSearchRepository extends ElasticsearchRepository<Medication, Long> {
}
