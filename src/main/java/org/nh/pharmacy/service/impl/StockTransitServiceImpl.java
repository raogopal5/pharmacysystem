package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.domain.StockTransit;
import org.nh.pharmacy.repository.StockTransitRepository;
import org.nh.pharmacy.service.StockTransitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing StockTransit.
 */
@Service
@Transactional
public class StockTransitServiceImpl implements StockTransitService {

    private final Logger log = LoggerFactory.getLogger(StockTransitServiceImpl.class);

    private final StockTransitRepository stockTransitRepository;

    public StockTransitServiceImpl(StockTransitRepository stockTransitRepository) {
        this.stockTransitRepository = stockTransitRepository;
    }

    /**
     * Save a stockTransit.
     *
     * @param stockTransit the entity to save
     * @return the persisted entity
     */
    @Override
    public StockTransit save(StockTransit stockTransit) {
        log.debug("Request to save StockTransit : {}", stockTransit);
        return stockTransitRepository.save(stockTransit);
    }

    /**
     *  Get all the stockTransits.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<StockTransit> findAll() {
        log.debug("Request to get all StockTransits");
        return stockTransitRepository.findAll();
    }

    /**
     *  Get one stockTransit by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockTransit findOne(Long id) {
        log.debug("Request to get StockTransit : {}", id);
        return stockTransitRepository.findById(id).get();
    }

    /**
     *  Delete the  stockTransit by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockTransit : {}", id);
        stockTransitRepository.deleteById(id);
    }
}
