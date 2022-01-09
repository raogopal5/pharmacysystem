package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.DispenseReturn;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Spring Data Elasticsearch repository for the DispenseReturn entity.
 */
public interface DispenseReturnSearchRepository extends ElasticsearchRepository<DispenseReturn, Long> {

    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    DispenseReturn findByDocumentNumber(String documentNumber);
    @Query("{\"bool\": {\"must\": [{\"match\": {\"document.dispenseRef.referenceNumber.raw\": \"?0\"}}]}}")
    List<DispenseReturn> findByDispenseNumber(String dispenseNumber);
    @Query("{\"bool\": {\"must\": [{\"match\": {\"documentNumber.raw\": \"?0\"}}]}}")
    DispenseReturn findByDispnseReturnDocumentNumber(String documentNumber);
    @Query("{\"bool\": {\"must\": [{\"match\": {\"id\": \"?0\"}}]}}")
    DispenseReturn find(Long id);
    @Query("{\"bool\": {\"must\": [{\"match\": {\"document.invoiceRef.referenceNumber.raw\": \"?0\"}}]}}")
    List<DispenseReturn> findByInvoiceNumber(String dispenseNumber);


}
