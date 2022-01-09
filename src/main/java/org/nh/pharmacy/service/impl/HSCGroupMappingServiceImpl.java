package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.HSCGroupMapping;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.HSCGroupMappingRepository;
import org.nh.pharmacy.repository.search.HSCGroupMappingSearchRepository;
import org.nh.pharmacy.service.HSCGroupMappingService;
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
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing HSCGroupMapping.
 */
@Service
@Transactional
public class HSCGroupMappingServiceImpl implements HSCGroupMappingService {

    private final Logger log = LoggerFactory.getLogger(HSCGroupMappingServiceImpl.class);

    private final HSCGroupMappingRepository hSCGroupMappingRepository;

    private final HSCGroupMappingSearchRepository hSCGroupMappingSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;

    public HSCGroupMappingServiceImpl(HSCGroupMappingRepository hSCGroupMappingRepository, HSCGroupMappingSearchRepository hSCGroupMappingSearchRepository) {
        this.hSCGroupMappingRepository = hSCGroupMappingRepository;
        this.hSCGroupMappingSearchRepository = hSCGroupMappingSearchRepository;
    }

    /**
     * Save a hSCGroupMapping.
     *
     * @param hSCGroupMapping the entity to save
     * @return the persisted entity
     */
    @Override
    public HSCGroupMapping save(HSCGroupMapping hSCGroupMapping) {
        log.debug("Request to save HSCGroupMapping : {}", hSCGroupMapping);
        HSCGroupMapping result = hSCGroupMappingRepository.save(hSCGroupMapping);
        hSCGroupMappingSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the hSCGroupMappings.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HSCGroupMapping> findAll(Pageable pageable) {
        log.debug("Request to get all HSCGroupMappings");
        Page<HSCGroupMapping> result = hSCGroupMappingRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one hSCGroupMapping by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public HSCGroupMapping findOne(Long id) {
        log.debug("Request to get HSCGroupMapping : {}", id);
        HSCGroupMapping hSCGroupMapping = hSCGroupMappingRepository.findById(id).get();
        return hSCGroupMapping;
    }

    /**
     * Delete the  hSCGroupMapping by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete HSCGroupMapping : {}", id);
        hSCGroupMappingRepository.deleteById(id);
        hSCGroupMappingSearchRepository.deleteById(id);
    }

    /**
     * Search for the hSCGroupMapping corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HSCGroupMapping> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of HSCGroupMappings for query {}", query);
        Page<HSCGroupMapping> result = hSCGroupMappingSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save hSCGroupMapping
     *
     * @param hscGroupMapping
     */
    @ServiceActivator(inputChannel = Channels.HSCGROUPMAPPING_INPUT)
    @Override
    public void consume(HSCGroupMapping hscGroupMapping) {
        try {
            log.debug("Request to consume HSCGroupMappings id : {}", hscGroupMapping.getId());
            hSCGroupMappingRepository.save(hscGroupMapping);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }

    /**
     * subscriber and save hSCGroupMapping
     *
     * @param removedIdsWithEntityJson
     */
    @ServiceActivator(inputChannel = Channels.REMOVE_HSC_GROUP_MAPPING_INPUT)
    @Override
    public void consume(String removedIdsWithEntityJson) throws Exception {
        Map<String, Set<Long>> removedIdsWithEntity = new ObjectMapper().readValue(removedIdsWithEntityJson, new TypeReference<Map<String, Set<Long>>>() {
        });
        log.debug("Request to consume removedIdsWithEntity : {}", removedIdsWithEntity);
        for (Map.Entry<String, Set<Long>> entry : removedIdsWithEntity.entrySet()) {
            Set<Long> ids = entry.getValue();
            ids.forEach(id -> {
                try {
                    hSCGroupMappingRepository.deleteById(id);
                } catch (Exception ex) {
                    log.error("Consumer is not able to delete hSCGroupMappingRepository for id: {}", id);
                    systemAlertService.save(new SystemAlert()
                        .fromClass(this.getClass().getName())
                        .onDate(ZonedDateTime.now())
                        .message("Error while processing message ")
                        .addDescription(ex));
                }
            });
        }
    }
}
