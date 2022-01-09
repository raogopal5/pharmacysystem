package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the IPDispenseReturnRequest entity.
 */
public interface IPDispenseReturnRequestSearchRepository extends ElasticsearchRepository<IPDispenseReturnRequest, Long> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    IPDispenseReturnRequest findByDocumentNumber(String documentNumber);
}
