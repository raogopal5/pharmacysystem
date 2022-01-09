package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.domain.MedicationOrder;
import org.nh.pharmacy.domain.dto.MedicationOrderDocumentLine;
import org.nh.pharmacy.repository.MedicationOrderRepository;
import org.nh.pharmacy.repository.search.MedicationOrderSearchRepository;
import org.nh.pharmacy.service.MedicationOrderService;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing MedicationOrder.
 */
@Service
@Transactional
public class MedicationOrderServiceImpl implements MedicationOrderService {

    private final Logger log = LoggerFactory.getLogger(MedicationOrderServiceImpl.class);

    private final MedicationOrderRepository medicationOrderRepository;

    private final MedicationOrderSearchRepository medicationOrderSearchRepository;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final ObjectMapper objectMapper;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final String moduleName;

    public MedicationOrderServiceImpl(MedicationOrderRepository medicationOrderRepository, MedicationOrderSearchRepository medicationOrderSearchRepository,
                                      SequenceGeneratorService sequenceGeneratorService, ObjectMapper objectMapper, ElasticsearchOperations elasticsearchTemplate,
                                      @Qualifier("moduleName") String moduleName) {
        this.medicationOrderRepository = medicationOrderRepository;
        this.medicationOrderSearchRepository = medicationOrderSearchRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.objectMapper = objectMapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.moduleName = moduleName;
    }

    /**
     * Save a medicationOrder.
     *
     * @param medicationOrder the entity to save
     * @return the persisted entity
     */
    public MedicationOrder save(MedicationOrder medicationOrder) {
        log.debug("Request to save MedicationOrder : {}", medicationOrder);
        if(null == medicationOrder.getId()){
            medicationOrder.setId(medicationOrderRepository.getId());
        }
        MedicationOrder result = medicationOrderRepository.save(medicationOrder);
        result.setDocumentLines(medicationOrder.getDocumentLines());
        medicationOrderSearchRepository.indexWithoutRefresh(result);
        return result;
    }

    /**
     *  Get all the medicationOrders.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<MedicationOrder> findAll() {
        log.debug("Request to get all MedicationOrders");
        List<MedicationOrder> result = medicationOrderRepository.findAll();

        return result;
    }

    /**
     *  Get one medicationOrder by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public MedicationOrder findOne(Long id) {
        log.debug("Request to get MedicationOrder : {}", id);
        MedicationOrder medicationOrder = medicationOrderRepository.findById(id).get();
        if(null!= medicationOrder && null != medicationOrder.getDocumentLines()) {
            MedicationOrderDocumentLine documentLines = objectMapper.convertValue(medicationOrder.getDocumentLines(), new TypeReference<MedicationOrderDocumentLine>() {
            });
            medicationOrder.setDocumentLines(documentLines);
        }
        return medicationOrder;
    }

    /**
     *  Delete the  medicationOrder by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete MedicationOrder : {}", id);
        medicationOrderRepository.deleteById(id);
        medicationOrderSearchRepository.deleteById(id);
    }

    /**
     * Search for the medicationOrder corresponding to the query.
     *
     *  @param query the query of the search
     *  @param pageable
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MedicationOrder> search(String query, Pageable pageable) {
        log.debug("Request to search MedicationOrders for query {}", query);
        Page<MedicationOrder> result = medicationOrderSearchRepository.search(queryStringQuery(query)
            .field("medicationRequestNumber").field("patient.displayName")
            .field("patient.mrn").field("medicationOrderStatus")
            .field("renderingHSC.name").field("renderingHSC.code")
            .field("createdBy.login").field("createdBy.displayName")
            .field("consultant.name").field("consultant.displayName")
            .field("documentLines.medication.name").field("medicationOrderNumber")
            .field("encounter.visitNumber")
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    @Override
    public void reIndexWithMedicationRequestId(Long medicationRequestId) {
        log.debug("Request to do re-index medication order with medication request id: {}", medicationRequestId);
        if (null != medicationRequestId) {
            List<MedicationOrder>  medicationOrders = search("medicationRequestId:"+medicationRequestId, PageRequest.of(0,500)).getContent();
            medicationOrders.forEach( medicationOrder -> {
                MedicationOrder repositoryOne = medicationOrderRepository.findById(medicationOrder.getId()).orElse(null);
                if (repositoryOne == null) {
                    if (medicationOrderSearchRepository.existsById(medicationOrder.getId())) {
                        medicationOrderSearchRepository.deleteById(medicationOrder.getId());
                    }
                } else {
                    medicationOrderSearchRepository.save(repositoryOne);
                }
            });

        }
        log.debug("medication order re index ends : {}", LocalTime.now());
    }

    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to index elastic index of registrationForm");
        List<MedicationOrder> data = medicationOrderRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
            data.stream().forEach(medicationOrder -> {
                if(null !=medicationOrder.getDocumentLines()) {
                    MedicationOrderDocumentLine documentLines = objectMapper.convertValue(medicationOrder.getDocumentLines(), new TypeReference<MedicationOrderDocumentLine>() {
                    });
                    medicationOrder.setDocumentLines(documentLines);
                }
            });
            if (!data.isEmpty()) {
                medicationOrderSearchRepository.saveAll(data);
            }

    }

    @Override
    public void deleteIndex() {
        log.debug("Request to delete elastic index of medication order");
        medicationOrderSearchRepository.deleteAll();
    }

    @Override
    public void refreshIndex() {
        elasticsearchTemplate.getIndexOperations().refresh(IndexCoordinates.of(moduleName+"_medicationorder"));
    }
}
