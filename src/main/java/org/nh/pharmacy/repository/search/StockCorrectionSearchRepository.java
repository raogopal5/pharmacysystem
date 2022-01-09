package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockCorrection;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the StockCorrection entity.
 */
public interface StockCorrectionSearchRepository extends ElasticsearchRepository<StockCorrection, Long> {

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"must\": [{\"match\": {\"id\": \"?0\"}}]}}")
    StockCorrection findByStockCorrectionId(Long id);

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    StockCorrection findByStockCorrectionDocumentNumber(String documentNumber);
}
