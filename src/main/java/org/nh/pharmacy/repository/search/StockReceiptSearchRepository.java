package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockReceipt;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the StockReceipt entity.
 */
public interface StockReceiptSearchRepository extends ElasticsearchRepository<StockReceipt, Long> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"id\": \"?0\"}}]}}")
    StockReceipt findByStockReceiptId(Long id);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    StockReceipt findByStockReceiptDocumentNumber(String documentNumber);
}
