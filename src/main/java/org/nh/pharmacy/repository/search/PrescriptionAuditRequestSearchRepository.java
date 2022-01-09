package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PrescriptionAuditRequestSearchRepository extends ElasticsearchRepository<PrescriptionAuditRequest, Long> {
}

