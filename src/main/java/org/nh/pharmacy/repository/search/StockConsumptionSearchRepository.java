package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.StockConsumption;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the StockConsumption entity.
 */
public interface StockConsumptionSearchRepository extends ElasticsearchRepository<StockConsumption, Long> {

    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    StockConsumption findByDocumentNumber(String documentNumber);
}
