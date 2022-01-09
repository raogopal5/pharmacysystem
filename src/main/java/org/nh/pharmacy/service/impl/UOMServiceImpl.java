package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.domain.UOM;
import org.nh.pharmacy.repository.UOMRepository;
import org.nh.pharmacy.repository.search.UOMSearchRepository;
import org.nh.pharmacy.service.SystemAlertService;
import org.nh.pharmacy.service.UOMService;
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
 * Service Implementation for managing UOM.
 */
@Service
@Transactional
public class UOMServiceImpl implements UOMService {

    private final Logger log = LoggerFactory.getLogger(UOMServiceImpl.class);

    private final UOMRepository uOMRepository;

    private final UOMSearchRepository uOMSearchRepository;

    @Autowired
    SystemAlertService systemAlertService;

    public UOMServiceImpl(UOMRepository uOMRepository, UOMSearchRepository uOMSearchRepository) {
        this.uOMRepository = uOMRepository;
        this.uOMSearchRepository = uOMSearchRepository;
    }

    /**
     * Save a uOM.
     *
     * @param uOM the entity to save
     * @return the persisted entity
     */
    @Override
    public UOM save(UOM uOM) {
        log.debug("Request to save UOM : {}", uOM);
        UOM result = uOMRepository.save(uOM);
        uOMSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the uOMS.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UOM> findAll(Pageable pageable) {
        log.debug("Request to get all UOMS");
        Page<UOM> result = uOMRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one uOM by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public UOM findOne(Long id) {
        log.debug("Request to get UOM : {}", id);
        UOM uOM = uOMRepository.findById(id).get();
        return uOM;
    }

    /**
     * Delete the  uOM by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete UOM : {}", id);
        uOMRepository.deleteById(id);
        uOMSearchRepository.deleteById(id);
    }

    /**
     * Search for the uOM corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UOM> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of UOMS for query {}", query);
        Page<UOM> result = uOMSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save UOM
     *
     * @param uom
     */
    @ServiceActivator(inputChannel = Channels.UOM_INPUT)
    @Override
    public void consume(UOM uom) {
        try {
            log.debug("Request to consume UOM code : {}", uom.getCode());
            uOMRepository.save(uom);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }
}
