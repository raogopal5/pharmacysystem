package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockIssue;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the StockIssue entity.
 */
public interface StockIssueSearchRepository extends ElasticsearchRepository<StockIssue, Long> {

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    StockIssue findByStockIssueDocumentNumber(String documentNumber);
}
