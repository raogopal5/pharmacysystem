package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.Dispense;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Dispense entity.
 */
public interface DispenseSearchRepository extends ElasticsearchRepository<Dispense, Long> {

    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    Dispense findByDocumentNumber(String documentNumber);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"id\": \"?0\"}}]}}")
    Dispense findOne(Long id);


}

