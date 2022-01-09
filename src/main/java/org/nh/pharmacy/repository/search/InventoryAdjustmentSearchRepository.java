package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.InventoryAdjustment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the InventoryAdjustment entity.
 */
public interface InventoryAdjustmentSearchRepository extends ElasticsearchRepository<InventoryAdjustment, Long> {

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"must\": [{\"match\": {\"id\": \"?0\"}}]}}")
    InventoryAdjustment findByAdjustmentId(Long id);

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    InventoryAdjustment findByAdjustmentDocumentNumber(String documentNumber);
}
