package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.StockAuditPlan;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockAuditPlanRepository;
import org.nh.pharmacy.repository.search.StockAuditPlanSearchRepository;
import org.nh.pharmacy.service.StockAuditPlanService;
import org.nh.pharmacy.service.StockAuditService;
import org.nh.pharmacy.web.rest.mapper.AuditMapper;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing StockAuditPlan.
 */
@Service
@Transactional
public class StockAuditPlanServiceImpl implements StockAuditPlanService {

    private final Logger log = LoggerFactory.getLogger(StockAuditPlanServiceImpl.class);

    private final StockAuditPlanRepository stockAuditPlanRepository;

    private final StockAuditPlanSearchRepository stockAuditPlanSearchRepository;

    private final StockAuditService stockAuditService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final AuditMapper auditMapper;

    private final ApplicationProperties applicationProperties;

    public StockAuditPlanServiceImpl(StockAuditPlanRepository stockAuditPlanRepository, StockAuditPlanSearchRepository stockAuditPlanSearchRepository
        , StockAuditService stockAuditService, SequenceGeneratorService sequenceGeneratorService, AuditMapper auditMapper, ApplicationProperties applicationProperties) {
        this.stockAuditPlanRepository = stockAuditPlanRepository;
        this.stockAuditPlanSearchRepository = stockAuditPlanSearchRepository;
        this.stockAuditService = stockAuditService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.auditMapper = auditMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Save a stockAuditPlan.
     *
     * @param stockAuditPlan the entity to save
     * @return the persisted entity
     * @throws SequenceGenerateException
     */
    @Override
    public StockAuditPlan save(StockAuditPlan stockAuditPlan) throws SequenceGenerateException {
        log.debug("Request to save StockAuditPlan : {}", stockAuditPlan);
        if (stockAuditPlan.getId() == null) {
            stockAuditPlan.id(stockAuditPlanRepository.getId());
            if ((Status.DRAFT).equals(stockAuditPlan.getDocument().getStatus())) {
                stockAuditPlan.documentNumber(StringUtils.join(new Object[]{Status.DRAFT, stockAuditPlan.getId()}, "-"));
            }
            stockAuditPlan.version(0);
            stockAuditPlan.getDocument().setId(stockAuditPlan.getId());
        } else {
            stockAuditPlanRepository.updateLatest(stockAuditPlan.getId());
            int version = stockAuditPlan.getVersion() + 1;
            stockAuditPlan.version(version);
        }
        stockAuditPlan.latest(true);
        if (CollectionUtils.isNotEmpty(stockAuditPlan.getDocument().getLines())) {
            stockAuditPlan.getDocument().getLines().stream()
                .filter(stockAuditPlanLine -> stockAuditPlanLine.getId() == null)
                .forEach(stockAuditPlanLine -> stockAuditPlanLine.setId(stockAuditPlanRepository.getId()));
        }
        StockAuditPlan result = stockAuditPlanRepository.save(stockAuditPlan);
        stockAuditPlanSearchRepository.save(result);
        return result;
    }

    /**
     * Save a stockAuditPlan.
     *
     * @param stockAuditPlan the entity to save
     * @param action         to be performed
     * @return the persisted entity
     * @throws SequenceGenerateException
     */
    @Override
    public StockAuditPlan save(StockAuditPlan stockAuditPlan, String action) throws SequenceGenerateException {
        log.debug("Request to save StockAuditPlan with action : {}", stockAuditPlan);
        StockAuditPlan result;
        switch (action) {
            case "SENDFORAPPROVAL":
                log.debug("Request to send approval StockAuditPlan : {}", stockAuditPlan);
                stockAuditPlan.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
                stockAuditPlan.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Audit_Plan.name(), "NH", stockAuditPlan));
                result = save(stockAuditPlan);
                break;
            case "APPROVED":
                log.debug("Request to approve StockAuditPlan : {}", stockAuditPlan);
                stockAuditPlan.getDocument().setStatus(Status.APPROVED);
                result = save(stockAuditPlan);
                createStockAudit(stockAuditPlan);
                break;
            case "REJECTED":
                log.debug("Request to reject StockAuditPlan : {}", stockAuditPlan);
                stockAuditPlan.getDocument().setStatus(Status.REJECTED);
                result = save(stockAuditPlan);
                break;
            default:
                log.debug("Request to save as draft StockAuditPlan : {}", stockAuditPlan);
                if (stockAuditPlan.getDocument().getStatus() == null) {
                    stockAuditPlan.getDocument().setStatus(Status.DRAFT);
                }
                result = save(stockAuditPlan);
        }
        return result;
    }

    /**
     * Create stock audit.
     *
     * @param stockAuditPlan the entity
     * @return stockAudit the entity
     */
    public void createStockAudit(StockAuditPlan stockAuditPlan) throws SequenceGenerateException {
        log.debug("Request to create StockAudit for StockAuditPlain : {}", stockAuditPlan);
        StockAudit stockAudit = auditMapper.convertFromAuditPlan(stockAuditPlan);
        stockAuditService.save(stockAudit);
    }

    /**
     * Get all the stockAuditPlans.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockAuditPlan> findAll(Pageable pageable) {
        log.debug("Request to get all StockAuditPlans");
        Page<StockAuditPlan> result = stockAuditPlanRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockAuditPlan by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockAuditPlan findOne(Long id) {
        log.debug("Request to get StockAuditPlan : {}", id);
        StockAuditPlan stockAuditPlan = stockAuditPlanRepository.findOne(id);
        return stockAuditPlan;
    }

    /**
     * Get one stockAuditPlan by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockAuditPlan findOne(Long id, Integer version) {

        log.debug("Request to get StockAuditPlan : {}", id, version);
        StockAuditPlan stockAuditPlan = stockAuditPlanRepository.findById(new DocumentId(id, version)).get();
        return stockAuditPlan;
    }

    /**
     * Delete the stockAuditPlan by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockAuditPlan : {}", id);
        stockAuditPlanRepository.delete(id);
        stockAuditPlanSearchRepository.deleteById(id);
    }

    /**
     * Delete the stockAuditPlan by id,version.
     *
     * @param id,version the id of the entity
     */
    @Override
    public void delete(Long id, Integer version) {
        log.debug("Request to delete StockAuditPlan : {}", id, version);
        stockAuditPlanRepository.deleteById(new DocumentId(id, version));
        stockAuditPlanSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockAuditPlan corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockAuditPlan> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockAuditPlans for query {}", query);
        return stockAuditPlanSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on Dispense latest=true");
        List<StockAuditPlan> data = stockAuditPlanRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockAuditPlanSearchRepository.saveAll(data);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of StockAuditPlan");
        stockAuditPlanSearchRepository.deleteAll();
    }
}
