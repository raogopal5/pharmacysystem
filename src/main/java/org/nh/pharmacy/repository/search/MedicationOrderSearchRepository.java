package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.MedicationOrder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the MedicationOrder entity.
 */
public interface MedicationOrderSearchRepository extends ElasticsearchRepository<MedicationOrder, Long> {
}
