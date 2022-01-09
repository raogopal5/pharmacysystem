package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockReversal;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the StockReversal entity.
 */
public interface StockReversalSearchRepository extends ElasticsearchRepository<StockReversal, Long> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"id\": \"?0\"}}]}}")
    StockReversal findByStockReversalId(Long id);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    StockReversal findByStockReversalDocumentNumber(String documentNumber);
}
