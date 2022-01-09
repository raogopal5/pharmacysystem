package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockIndent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the StockIndent entity.
 */
public interface StockIndentSearchRepository extends ElasticsearchRepository<StockIndent, Long> {

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    StockIndent findByStockIndentDocumentNumber(String documentNumber);

}
