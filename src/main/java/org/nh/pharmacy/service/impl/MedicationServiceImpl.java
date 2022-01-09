package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.MedicationRepository;
import org.nh.pharmacy.repository.search.MedicationSearchRepository;
import org.nh.pharmacy.service.MedicationService;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Medication.
 */
@Service
@Transactional
public class MedicationServiceImpl implements MedicationService {

    private final Logger log = LoggerFactory.getLogger(MedicationServiceImpl.class);

    private final MedicationRepository medicationRepository;

    private final MedicationSearchRepository medicationSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;

    public MedicationServiceImpl(MedicationRepository medicationRepository, MedicationSearchRepository medicationSearchRepository) {
        this.medicationRepository = medicationRepository;
        this.medicationSearchRepository = medicationSearchRepository;
    }

    /**
     * Save a medication.
     *
     * @param medication the entity to save
     * @return the persisted entity
     */
    @Override
    public Medication save(Medication medication) {
        log.debug("Request to save Medication : {}", medication);
        Medication result = medicationRepository.save(medication);
        medicationSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the medications.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Medication> findAll(Pageable pageable) {
        log.debug("Request to get all Medications");
        Page<Medication> result = medicationRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one medication by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Medication findOne(Long id) {
        log.debug("Request to get Medication : {}", id);
        Medication medication = medicationRepository.findOneWithEagerRelationships(id);
        return medication;
    }

    /**
     * Delete the  medication by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Medication : {}", id);
        medicationRepository.deleteById(id);
        medicationSearchRepository.deleteById(id);
    }

    /**
     * Search for the medication corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Medication> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Medications for query {}", query);
        Page<Medication> result = medicationSearchRepository.search(queryStringQuery(query)
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save medication
     *
     * @param medicationData
     */
    @ServiceActivator(inputChannel = Channels.MEDICATION_INPUT)
    @Override
    public void consume(String medicationData) {
        try {
            log.debug("Request to consume Medication code : {}", medicationData);
            //medicationRepository.save(medication);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
