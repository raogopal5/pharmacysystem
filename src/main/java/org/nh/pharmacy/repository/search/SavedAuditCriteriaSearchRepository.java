package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.SavedAuditCriteria;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the SavedAuditCriteria entity.
 */
public interface SavedAuditCriteriaSearchRepository extends ElasticsearchRepository<SavedAuditCriteria, Long> {
}
