package org.nh.pharmacy.repository.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.nh.pharmacy.domain.StockAudit;

/**
 * Spring Data Elasticsearch repository for the StockAudit entity.
 */
public interface StockAuditSearchRepository extends ElasticsearchRepository<StockAudit, Long> {
}
