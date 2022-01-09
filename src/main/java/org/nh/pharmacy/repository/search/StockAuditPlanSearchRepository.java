package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockAuditPlan;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the StockAuditPlan entity.
 */
public interface StockAuditPlanSearchRepository extends ElasticsearchRepository<StockAuditPlan, Long> {
}
