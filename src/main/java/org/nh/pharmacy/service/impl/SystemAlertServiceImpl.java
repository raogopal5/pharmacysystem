package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.SystemAlertRepository;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing SystemAlert.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SystemAlertServiceImpl implements SystemAlertService {

    private final Logger log = LoggerFactory.getLogger(SystemAlertServiceImpl.class);

    private final SystemAlertRepository systemAlertRepository;


    public SystemAlertServiceImpl(SystemAlertRepository systemAlertRepository) {
        this.systemAlertRepository = systemAlertRepository;
    }

    /**
     * Save a systemAlert.
     *
     * @param systemAlert the entity to save
     * @return the persisted entity
     */
    @Override
    public SystemAlert save(SystemAlert systemAlert) {
        log.debug("Request to save SystemAlert : {}", systemAlert);
        SystemAlert result = systemAlertRepository.save(systemAlert);
        return result;
    }

    /**
     * Get all the systemAlerts.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SystemAlert> findAll(Pageable pageable) {
        log.debug("Request to get all SystemAlerts");
        Page<SystemAlert> result = systemAlertRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one systemAlert by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public SystemAlert findOne(Long id) {
        log.debug("Request to get SystemAlert : {}", id);
        SystemAlert systemAlert = systemAlertRepository.findById(id).get();
        return systemAlert;
    }

    /**
     * Delete the  systemAlert by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete SystemAlert : {}", id);
        systemAlertRepository.deleteById(id);
    }

}
