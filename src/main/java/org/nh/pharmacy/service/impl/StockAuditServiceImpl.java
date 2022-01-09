package org.nh.pharmacy.service.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jbpm.services.api.model.DeployedUnit;
import org.nh.billing.web.rest.util.ConfigurationUtility;
import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.LocatorDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.dto.ValueSetCodeDTO;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.AuditCriteriaFilter;
import org.nh.pharmacy.domain.dto.AuditDocumentLine;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.dto.RelatedDocument;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.domain.enumeration.VEDCategory;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.repository.StockAuditRepository;
import org.nh.pharmacy.repository.StockRepository;
import org.nh.pharmacy.repository.search.HealthcareServiceCenterSearchRepository;
import org.nh.pharmacy.repository.search.ItemSearchRepository;
import org.nh.pharmacy.repository.search.StockAuditSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.mapper.InventoryAdjustmentMapper;
import org.nh.pharmacy.web.rest.util.DateUtil;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.common.util.BigDecimalUtil.multiply;
import static org.nh.pharmacy.domain.enumeration.Context.Audit_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.Status.*;
import static org.nh.pharmacy.exception.constants.PharmacyErrorCodes.*;
import static org.nh.pharmacy.util.PharmacyConstants.MAX_STATUS_COUNT;

/**
 * Service Implementation for managing StockAudit.
 */
@Service("stockAuditService")
@Transactional
public class StockAuditServiceImpl implements StockAuditService {

    private final Logger log = LoggerFactory.getLogger(StockAuditServiceImpl.class);

    private final StockAuditRepository stockAuditRepository;

    private final StockAuditSearchRepository stockAuditSearchRepository;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final InventoryAdjustmentMapper inventoryAdjustmentMapper;

    private final InventoryAdjustmentService inventoryAdjustmentService;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final StockRepository stockRepository;

    private final ItemSearchRepository itemSearchRepository;

    private final WorkflowService workflowService;

    private final EntityManager entityManager;

    private final UserService userService;

    private final GroupService groupService;

    private final ApplicationProperties applicationProperties;

    private final HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository;

    private final RuleExecutorService ruleExecutorService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    public StockAuditServiceImpl(StockAuditRepository stockAuditRepository, StockAuditSearchRepository stockAuditSearchRepository,
                                 SequenceGeneratorService sequenceGeneratorService, InventoryAdjustmentMapper inventoryAdjustmentMapper,
                                 InventoryAdjustmentService inventoryAdjustmentService, ElasticsearchOperations elasticsearchTemplate,
                                 StockRepository stockRepository, ItemSearchRepository itemSearchRepository, WorkflowService workflowService,
                                 EntityManager entityManager, UserService userService, GroupService groupService, ApplicationProperties applicationProperties,
                                 HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository, RuleExecutorService ruleExecutorService, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.stockAuditRepository = stockAuditRepository;
        this.stockAuditSearchRepository = stockAuditSearchRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.inventoryAdjustmentMapper = inventoryAdjustmentMapper;
        this.inventoryAdjustmentService = inventoryAdjustmentService;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.stockRepository = stockRepository;
        this.itemSearchRepository = itemSearchRepository;
        this.workflowService = workflowService;
        this.entityManager = entityManager;
        this.userService = userService;
        this.groupService = groupService;
        this.applicationProperties = applicationProperties;
        this.healthcareServiceCenterSearchRepository = healthcareServiceCenterSearchRepository;
        this.ruleExecutorService = ruleExecutorService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a stockAudit.
     *
     * @param stockAudit the entity to save
     * @return the persisted entity
     * @throws SequenceGenerateException
     */
    @Override
    public StockAudit save(StockAudit stockAudit) throws SequenceGenerateException {
        log.debug("Request to save StockAudit : {} starts: {}", stockAudit, LocalTime.now());
        if (stockAudit.getId() == null) {
            stockAudit.id(stockAuditRepository.getId());
            if ((DRAFT).equals(stockAudit.getDocument().getStatus())) {
                stockAudit.documentNumber(join(new Object[]{DRAFT, stockAudit.getId()}, "-"));
            }
            stockAudit.version(0);
            stockAudit.getDocument().setId(stockAudit.getId());
        } else {
            stockAuditRepository.updateLatest(stockAudit.getId());
            int version = stockAudit.getVersion() + 1;
            stockAudit.version(version);
        }
        if (isNotEmpty(stockAudit.getDocument().getLines())) {
            for (AuditDocumentLine auditDocumentLine : stockAudit.getDocument().getLines()) {
                if (auditDocumentLine.getId() == null) {
                    auditDocumentLine.setId(stockAuditRepository.getId());
                }
                if (auditDocumentLine.getAuditQuantity() != null) {
                    auditDocumentLine.setDiscrepantQuantity(new Quantity((auditDocumentLine.getStockQuantity().getValue() - auditDocumentLine.getAuditQuantity().getValue()), auditDocumentLine.getAuditQuantity().getUom()));
                    auditDocumentLine.setHasDiscrepancy(auditDocumentLine.getDiscrepantQuantity().getValue() == 0 ? false : true);
                    auditDocumentLine.setDiscrepantValue(multiply(auditDocumentLine.getDiscrepantQuantity().getValue(), auditDocumentLine.getCost()));
                }
            }
        }
        stockAudit.getDocument().setModifiedDate(stockAudit.getDocument().getModifiedDate() == null ? LocalDateTime.now() : stockAudit.getDocument().getModifiedDate());
        stockAudit.latest(true);

        StockAudit result = stockAuditRepository.save(stockAudit);
        stockAuditSearchRepository.save(result);
        log.debug("Request to save StockAudit : {} ends: {}", stockAudit, LocalTime.now());
        return result;
    }

    /**
     * @param stockAudit the entity to save
     * @param action     the activity needs to be performed
     * @return the persisted entity
     * @throws Exception
     */

    public StockAudit save(StockAudit stockAudit, String action) throws Exception {
        log.debug("Request to save StockAudit with action : {} starts : {}", stockAudit, LocalTime.now());
        StockAudit result;
        switch (action) {
            case "SENDFORAPPROVAL":
                log.debug("Request to send approval StockAudit : {}", stockAudit);
                filterStockAuditLines(stockAudit);
                validateStockAuditLines(stockAudit);
                stockAudit = lockDocumentAndUpdate(stockAudit);
                stockAudit.getDocument().setStatus(IN_PROGRESS);
                result = save(stockAudit);
                break;
            case "APPROVED":
                log.debug("Request to approve StockAudit : {}", stockAudit);
                //validateDocumentApprover(stockAudit);
                stockAudit.getDocument().setStatus(APPROVED);
                result = save(stockAudit);
                createInventoryAdjustment(stockAudit);
                break;
            case "REJECTED":
                log.debug("Request to reject StockAudit : {}", stockAudit);
                stockAudit.getDocument().setStatus(REJECTED);
                result = save(stockAudit);
                break;
            case "START":
                log.debug("Request to start StockAudit : {}", stockAudit);
                validateInProgress(stockAudit);
                stockAudit.documentNumber(sequenceGeneratorService.generateSequence(TransactionType.Stock_Audit.name(), "NH", stockAudit));
                stockAudit.getDocument().setStatus(IN_PROGRESS);
                result = save(stockAudit);
                //Get workflow configurations
                Map<String, Object> configurations = retrieveWorkflowConfigurations(result, true);
                //Start workflow if workflow enabled
                if ((Boolean) configurations.get("enableWorkflow")) {
                    startWorkflow(result, action, configurations);
                }
                break;
            default:
                log.debug("Request to save as draft StockAudit : {}", stockAudit);
                if (stockAudit.getDocument().getStatus() == null) {
                    stockAudit.getDocument().setStatus(DRAFT);
                }
                if (IN_PROGRESS.equals(stockAudit.getDocument().getStatus())) {
                    filterStockAuditLines(stockAudit);
                    stockAudit = lockDocumentAndUpdate(stockAudit);
                }
                result = save(stockAudit);
        }
        log.debug("Request to save StockAudit with action : {} starts : {}", stockAudit, LocalTime.now());
        return result;
    }

    public void validateDocumentApprover(StockAudit stockAudit) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(stockAudit, "stock_audit_document_approver_validation");

    }

    /**
     * Change document status from workflow
     *
     * @param documentNumber
     */
    @Override
    public void changeDocumentStatus(String documentNumber) {
        StockAudit stockAudit = stockAuditRepository.findOneByDocumentNumber(documentNumber);
        stockAudit.getDocument().setStatus(WAITING_FOR_APPROVAL);
        stockAuditRepository.save(stockAudit);
        stockAuditSearchRepository.save(stockAudit);
    }


    /**
     * Filter document lines for logged in user
     *
     * @param stockAudit
     */
    public void filterStockAuditLines(StockAudit stockAudit) {
        if (stockAudit.getDocument().getStatus().equals(IN_PROGRESS)) {
            List<AuditDocumentLine> newAuditDocumentLine = new ArrayList<>();
            stockAudit.getDocument().getLines().stream()
                .filter(auditDocumentLine -> auditDocumentLine.getAuditingUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().get()))
                .forEach(auditDocumentLine -> newAuditDocumentLine.add(auditDocumentLine));
            stockAudit.getDocument().setLines(newAuditDocumentLine);
        }
    }

    /**
     * Create inventory adjustment(s).
     *
     * @param stockAudit the entity
     */
    public void createInventoryAdjustment(StockAudit stockAudit) throws Exception {
        log.debug("Request to create InventoryAdjustment for StockAudit : {} starts : {}", stockAudit, LocalTime.now());
        if (containsDiscrepancyLines(stockAudit)) {
            inventoryAdjustmentService.save(inventoryAdjustmentMapper.convertToAdjustmentFromAudit(stockAudit), "SENDFORAPPROVAL");
        }
        log.debug("Request to create InventoryAdjustment for StockAudit : {} ends : {}", stockAudit, LocalTime.now());
    }

    /**
     * Check for discrepancy lines
     *
     * @param stockAudit
     * @return boolean
     */
    public Boolean containsDiscrepancyLines(StockAudit stockAudit) {
        log.debug("Request to check for DiscrepancyLines starts {}", LocalTime.now());
        Boolean hasDiscrepancy = false;
        List<AuditDocumentLine> auditDocumentLineList = stockAudit.getDocument().getLines();
        if (isNotEmpty(auditDocumentLineList)) {
            for (AuditDocumentLine auditDocumentLine : auditDocumentLineList) {
                if (auditDocumentLine.getHasDiscrepancy()) {
                    hasDiscrepancy = true;
                    break;
                }
            }
        }
        log.debug("Request to check for DiscrepancyLines ends {}", LocalTime.now());
        return hasDiscrepancy;
    }

    /**
     * Get all the stockAudits.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockAudit> findAll(Pageable pageable) {
        log.debug("Request to get all StockAudits starts : {}", LocalTime.now());
        Page<StockAudit> result = stockAuditRepository.findAll(pageable);
        log.debug("Request to get all StockAudits ends : {}", LocalTime.now());
        return result;
    }

    /**
     * Get one stockAudit by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockAudit findOne(Long id) {
        log.debug("Request to get StockAudit : {} starts : {}", id, LocalTime.now());
        StockAudit stockAudit = stockAuditRepository.findOne(id);
        log.debug("Request to get StockAudit : {} ends : {}", id, LocalTime.now());
        return stockAudit;
    }

    /**
     * Get one Detached stockAudit by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public StockAudit findDetachedOne(Long id) {
        log.debug("Request to get StockAudit : {} starts : {}", id, LocalTime.now());
        StockAudit stockAudit = stockAuditRepository.findOne(id);
        log.debug("Request to get StockAudit : {} ends : {}", id, LocalTime.now());
        return stockAudit;
    }

    /**
     * Get one stockAudit by id,version.
     *
     * @param id,version the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockAudit findOne(Long id, Integer version) {

        log.debug("Request to get StockAudit : {} version : {} starts : {}", id, version, LocalTime.now());
        StockAudit stockAudit = stockAuditRepository.findById(new DocumentId(id, version)).get();
        log.debug("Request to get StockAudit : {} version : {} ends : {}", id, version, LocalTime.now());
        return stockAudit;
    }

    /**
     * Delete the  stockAudit by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockAudit : {} starts : {}", id, LocalTime.now());
        stockAuditRepository.delete(id);
        stockAuditSearchRepository.deleteById(id);
        log.debug("Request to delete StockAudit : {} ends : {}", id, LocalTime.now());
    }

    /**
     * Delete the stockAudit by id,version.
     *
     * @param id,version the id of the entity
     */
    @Override
    public void delete(Long id, Integer version) {
        log.debug("Request to delete StockAudit : {} version : {} starts : {}", id, version, LocalTime.now());
        stockAuditRepository.deleteById(new DocumentId(id, version));
        stockAuditSearchRepository.deleteById(id);
        log.debug("Request to delete StockAudit : {} version : {} ends : {}", id, version, LocalTime.now());
    }

    /**
     * Search for the stockAudit corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockAudit> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockAudits for query {} starts {}", query, LocalTime.now());
        Page<StockAudit> result = stockAuditSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND)
            .field("documentNumber").field("document.store.name")
            .field("document.storeContact.name").field("document.status").field("document.storeContact.displayName"), pageable);
        log.debug("Request to search for a page of StockAudits for query {} ends {}", query, LocalTime.now());
        return result;
    }

    /**
     * @param query
     * @param pageable
     * @param includeFields
     * @param excludeFields
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockAudit> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of StockAudits for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.store.name")
                .field("document.storeContact.name").field("document.status").field("document.storeContact.displayName")).
            withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return stockAuditSearchRepository.search(searchQuery);
    }

    @Override
    public Map<String, Long> getStatusCount(String query) {
        Map<String, Long> statusCount = new HashMap<>();
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.store.name")
                .field("document.storeContact.name").field("document.status")
                .field("document.storeContact.displayName")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.status.raw").size(MAX_STATUS_COUNT))
            .build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "stockaudit");
        Terms terms = aggregations.get("status_count");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    @Override
    public List<AuditDocumentLine> getAllDocumentLines(Long itemId, Long storeId) throws StockException {
        log.debug("Request to get all document lines starts {}", LocalTime.now());
        return mapStockToAuditDocLine(itemId, storeId);
    }

    private List<AuditDocumentLine> mapStockToAuditDocLine(Long itemId, Long storeId) throws StockException {
        log.debug("Request to find items with positive quantity starts {}", LocalTime.now());
        List<Stock> stocks = stockRepository.findByItemIdAndStoreIdWithPositiveQty(itemId, storeId);
        log.debug("Request to find items with positive quantity ends {}", LocalTime.now());
        if (stocks.isEmpty()) {
            log.debug("Request to find items with zero quantity starts {}", LocalTime.now());
            stocks = stockRepository.findByItemIdAndStoreIdWithZeroQty(itemId, storeId);
            log.debug("Request to find items with zero quantity ends {}", LocalTime.now());
            if (stocks.isEmpty()) {
                org.nh.pharmacy.domain.Item item = itemSearchRepository.findById(itemId).get();
                HealthcareServiceCenter healthcareServiceCenter = healthcareServiceCenterSearchRepository.findById(storeId).get();
                throw new StockException(itemId, null, storeId, item.getName(), healthcareServiceCenter.getName(), "stock details not available for given item").errorCode(STOCK_DETAIL_NOT_FOUND);
            }
        }
        List<AuditDocumentLine> auditDocumentLines = new ArrayList<>();
        mapStocksToAuditLines(stocks, auditDocumentLines, null);
        return auditDocumentLines;
    }

    private void mapStocksToAuditLines(List<Stock> stocks, List<AuditDocumentLine> auditDocumentLines, UserDTO user) {
        for (Stock stock : stocks) {
            if (auditDocumentLines.stream().anyMatch(line -> stock.getId().equals(line.getStockId()))) {
                continue;// already the stock is added to current audit document
            }
            AuditDocumentLine auditDocLine = new AuditDocumentLine();
            auditDocLine.setStockId(stock.getId());
            auditDocLine.setBatchNumber(stock.getBatchNo());
            auditDocLine.setItem(mapItem(stock.getItemId()));
            auditDocLine.setLocator(mapLocator(stock.getLocatorId()));
            auditDocLine.setStockQuantity(mapStockQuantity(stock.getQuantity(), stock.getUomId()));
            auditDocLine.setCost(stock.getCost());
            auditDocLine.setExpiryDate(stock.getExpiryDate());
            auditDocLine.setAuditingUser(user);
            auditDocLine.setMrp(stock.getMrp());
            auditDocLine.setConsignment(stock.isConsignment());
            auditDocumentLines.add(auditDocLine);
        }
    }

    private ItemDTO mapItem(Long itemId) {
        log.debug("Request to mapItem starts: {}", LocalTime.now());
        Query query = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("id:" + itemId).defaultOperator(Operator.AND)).build();
        List<org.nh.pharmacy.domain.Item> items = ElasticSearchUtil.getRecords(query, org.nh.pharmacy.domain.Item.class, elasticsearchTemplate, "item");
        org.nh.pharmacy.domain.Item item = items.get(0);
        log.debug("Request to mapItem ends: {}", LocalTime.now());
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(item.getId());
        itemDTO.setCode(item.getCode());
        itemDTO.setName(item.getName());
        itemDTO.setSaleUOM(item.getSaleUOM().getUOMDTO());
        itemDTO.setPurchaseUOM(item.getPurchaseUOM().getUOMDTO());
        itemDTO.setCategory(item.getCategory().getItemCategoryDTO());
        itemDTO.setTrackUOM(item.getTrackUOM().getUOMDTO());
        itemDTO.setType(new ValueSetCodeDTO(item.getType().getId(), item.getType().getCode(), item.getType().getDisplay()));
        itemDTO.setDispensableGenericName(item.getDispensableGenericName());
        return itemDTO;
    }

    private LocatorDTO mapLocator(Long locatorId) {
        log.debug("Request to map locator starts: {}", LocalTime.now());
        Query query = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("id:" + locatorId).defaultOperator(Operator.AND)).build();
        List<org.nh.pharmacy.domain.Locator> locators = ElasticSearchUtil.getRecords(query, org.nh.pharmacy.domain.Locator.class, elasticsearchTemplate, "locator");
        org.nh.pharmacy.domain.Locator locator = locators.get(0);
        log.debug("Request to map locator ends: {}", LocalTime.now());
        return locator.getLocatorDTO();
    }

    private Quantity mapStockQuantity(Float quantity, Long uomId) {
        log.debug("Request to map stock quantity starts: {}", LocalTime.now());
        Query query = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("id:" + uomId).defaultOperator(Operator.AND)).build();
        List<UOM> uomList = ElasticSearchUtil.getRecords(query, UOM.class, elasticsearchTemplate, "uom");

        Quantity tempQuantity = new Quantity();
        tempQuantity.setUom(uomList.get(0).getUOMDTO());
        tempQuantity.setValue(quantity);
        log.debug("Request to map stock quantity ends: {}", LocalTime.now());
        return tempQuantity;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Stock Audit starts : {}", LocalTime.now());
        stockAuditSearchRepository.deleteAll();
        log.debug("Request to delete elastic index of Stock Audit ends : {}", LocalTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on stockAudit latest=true");
        List<StockAudit> data = stockAuditRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            stockAuditSearchRepository.saveAll(data);
        }
        stockAuditSearchRepository.refresh();
    }

    /**
     * Do index for Stock Audit
     *
     * @param stockAudit
     */
    @Override
    public void index(StockAudit stockAudit) {
        log.debug("Request to do index Stock Audit starts : {}", LocalTime.now());
        stockAuditSearchRepository.save(stockAudit);
        log.debug("Request to do index Stock Audit ends : {}", LocalTime.now());
    }

    /**
     * Reindex audit elasticsearch for given id
     *
     * @param id
     */
    @Override
    public void reIndex(Long id) {
        log.debug("Request to do reIndex Stock Audit starts : {}", LocalTime.now());
        if (id != null) {
            StockAudit stockAudit = stockAuditRepository.findOne(id);
            if (stockAudit == null) {
                if (stockAuditSearchRepository.existsById(id)) {
                    stockAuditSearchRepository.deleteById(id);
                }
            } else {
                stockAuditSearchRepository.save(stockAudit);
            }
        }
        log.debug("Request to do reIndex Stock Audit ends : {}", LocalTime.now());
    }

    /**
     * Start workflow
     *
     * @param stockAudit
     * @param action
     * @Param configurations
     */
    public void startWorkflow(StockAudit stockAudit, String action, Map configurations) {
        log.debug("Request to start workflow starts : {}", LocalTime.now());
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set content
            content.put("document_id", stockAudit.getId());
            content.put("document_type", TransactionType.Stock_Audit);
            content.put("group_id", configurations.get("groupIds"));
            //Set params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_number", stockAudit.getDocumentNumber());
            params.put("document_date", stockAudit.getDocument().getDocumentDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("for_store", stockAudit.getDocument().getStore().getName());
            params.put("unit_id", String.valueOf(stockAudit.getDocument().getUnit().getId()));
            params.put("auditors", getAuditors(stockAudit));
            params.put("content", content);
            //Start the process
            Long processInstanceId = workflowService.startProcess(deployedUnit, (String) configurations.get("processId"), params);
            //Set result
            results.put("action_out", action);
            results.put("user_out", getAuditors(stockAudit).iterator().next());
            //Complete the document creation task
            workflowService.completeUserTaskForProcessInstance(processInstanceId, userId, results);
        }
        log.debug("Request to start workflow ends : {}", LocalTime.now());
    }

    /**
     * Get distinct auditors from audit document line list
     *
     * @param stockAudit
     * @return auditorsList
     */
    public List<String> getAuditors(StockAudit stockAudit) {
        log.debug("Request to get auditors starts : {}", LocalTime.now());
        Set<String> auditorList = null;
        List<AuditDocumentLine> documentLineList = stockAudit.getDocument().getLines();
        if (isNotEmpty(documentLineList)) {
            auditorList = documentLineList.stream().map(documentLine -> documentLine.getAuditingUser().getLogin()).collect(Collectors.toCollection(TreeSet::new));
        }
        log.debug("Request to get auditors ends : {}", LocalTime.now());
        return new ArrayList<>(auditorList);
    }

    /**
     * Execute workflow
     *
     * @param stockAudit the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return stockAudit object
     * @throws Exception
     */
    @Override
    @Transactional
    public StockAudit executeWorkflow(StockAudit stockAudit, String transition, Long taskId) throws Exception {
        log.debug("Request to execute workflow starts : {}", LocalTime.now());
        StockAudit result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Send for Approval":
                action = "SENDFORAPPROVAL";
                break;
            case "Approved":
                action = "APPROVED";
                break;
            case "Rejected":
                action = "REJECTED";
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }

        result = save(stockAudit, action);
        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("action_out", action);
        workflowService.completeUserTask(taskId, userId, results);
        log.debug("Request to execute workflow ends : {}", LocalTime.now());
        return result;
    }

    /**
     * Get task constraints
     *
     * @param documentNumber
     * @param userId
     * @param taskId
     * @return taskId, constraints
     */
    @Override
    public Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId) {
        log.debug("Request to get TaskConstraints starts : {}", LocalTime.now());
        Map<String, Object> configurations, taskDetails;
        log.debug("Request to find audit by document number starts : {}", LocalTime.now());
        StockAudit stockAudit = stockAuditRepository.findOneByDocumentNumber(documentNumber);
        log.debug("Request to find audit by document number ends : {}", LocalTime.now());
        configurations = retrieveWorkflowConfigurations(stockAudit, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) : workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,stockAudit.getDocument().getCreatedBy().getLogin());
            if ((Boolean) taskDetails.get("isGroupTask")) {
                List<String> workflowGroupIdList = (List<String>) taskDetails.get("groupIdList");
                List<String> userGroupIdList = groupService.groupsForUser(userId);
                if (disjoint(workflowGroupIdList, userGroupIdList)) {
                    taskDetails.put("taskId", null);
                    taskDetails.put("isGroupTask", false);
                }
            }
            taskDetails.remove("groupIdList");
        } else {
            taskDetails = new HashMap<String, Object>() {{
                put("taskId", null);
                put("constraints", new HashSet<String>());
                put("isGroupTask", false);
            }};
        }
        log.debug("Request to get TaskConstraints ends : {}", LocalTime.now());
        return taskDetails;
    }

    /**
     * Get workflow configurations
     *
     * @param stockAudit
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(StockAudit stockAudit, boolean isStartWorkflow) {
        log.debug("Request to retrieveWorkflowConfigurations starts : {}", LocalTime.now());
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_stockaudit_enable_workflow", stockAudit.getDocument().getStore().getId(), stockAudit.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_stockaudit_workflow_definition", stockAudit.getDocument().getStore().getId(), stockAudit.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(Audit_Approval_Committee, stockAudit.getDocument().getUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", stockAudit.getDocument().getStore().getId(), stockAudit.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        log.debug("Request to retrieveWorkflowConfigurations ends : {}", LocalTime.now());
        return configurations;
    }

    @Override
    public void generateExcel(String auditDocNumber, File file) throws IOException {
        log.debug("Request to generate Excel starts : {}", LocalTime.now());
        Page<StockAudit> auditPage = search("documentNumber.raw:" + auditDocNumber, PageRequest.of(0,1));
        if (auditPage.hasContent()) {
            StockAudit stockAudit = auditPage.getContent().iterator().next();
            List<AuditDocumentLine> auditDocumentLines = stockAudit.getDocument().getLines();
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("audit_document_lines");

            createAuditXlsHeader(sheet, stockAudit.getDocument().getStatus());
            if (stockAudit.getDocument().getStatus().equals(IN_PROGRESS)) {
                List<AuditDocumentLine> auditLinesPerUser = new ArrayList<>();
                auditDocumentLines.stream()
                    .filter(auditDocumentLine -> auditDocumentLine.getAuditingUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().get()))
                    .forEach(auditDocumentLine -> auditLinesPerUser.add(auditDocumentLine));
                if (!auditLinesPerUser.isEmpty())
                    insertValueToExcel(auditLinesPerUser, sheet, stockAudit.getDocument().getStatus());
            } else {
                insertValueToExcel(auditDocumentLines, sheet, stockAudit.getDocument().getStatus());
            }
            sheet.setColumnHidden(10, true);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
//            workbook.close();
        }
        log.debug("Request to generate Excel ends : {}", LocalTime.now());
    }

    private void insertValueToExcel(List<AuditDocumentLine> auditDocumentLines, XSSFSheet sheet, Status status) {
        log.debug("Request to insert value to  Excel starts : {}", LocalTime.now());
        Query queryValueSetCode = new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, 9999))
            .withQuery(queryStringQuery("valueSet.code.raw:AUDIT_DISCREPANCY_REASON")).build();
        List<ValueSetCode> valueSetCodeList = ElasticSearchUtil.getRecords(queryValueSetCode, ValueSetCode.class, elasticsearchTemplate, "valuesetcode");

        int rowIndex = 1;
        int cellIndex;
        for (AuditDocumentLine auditDocLine : auditDocumentLines) {
            cellIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            row.createCell(cellIndex++).setCellValue(auditDocLine.getItem().getCode());
            row.createCell(cellIndex++).setCellValue(auditDocLine.getItem().getName());
            row.createCell(cellIndex++).setCellValue(auditDocLine.getStockQuantity().getUom().getName());
            row.createCell(cellIndex++).setCellValue(auditDocLine.getBatchNumber());
            row.createCell(cellIndex++).setCellValue(auditDocLine.getMrp() != null ? auditDocLine.getMrp().floatValue() : 0);
            row.createCell(cellIndex++).setCellValue(auditDocLine.getExpiryDate() != null ? auditDocLine.getExpiryDate().format(ISO_LOCAL_DATE) : null);
            row.createCell(cellIndex++).setCellValue(auditDocLine.getStockQuantity().getValue());
            if (auditDocLine.getAuditQuantity() != null) {
                row.createCell(cellIndex++).setCellValue(auditDocLine.getAuditQuantity().getValue());
            } else {
                row.createCell(cellIndex++);
            }
            row.createCell(cellIndex++).setCellValue(auditDocLine.getDiscrepantReason() != null ? auditDocLine.getDiscrepantReason().getDisplay() : null);
            row.createCell(cellIndex++).setCellValue(auditDocLine.getAuditingUser().getDisplayName());
            row.createCell(cellIndex++).setCellValue(auditDocLine.getStockId());
        }

        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        CellRangeAddressList addressList = new CellRangeAddressList(1, auditDocumentLines.size(), 8, 8);
        if (valueSetCodeList == null || valueSetCodeList.isEmpty()) {
            throw new CustomParameterizedException(NO_DISCREPENCY_REASONS);
        }
        String[] valueSetCodeArr = new String[valueSetCodeList.size()];
        for (int i = 0; i < valueSetCodeList.size(); i++) {
            valueSetCodeArr[i] = valueSetCodeList.get(i).getDisplay();
        }
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(valueSetCodeList == null ? new String[]{} : valueSetCodeArr);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
        log.debug("Request to insert value to  Excel ends : {}", LocalTime.now());
    }

    private void createAuditXlsHeader(XSSFSheet sheet, Status status) {
        int cellIndex = 0;
        Row row = sheet.createRow(0);
        row.createCell(cellIndex++).setCellValue("ITEM CODE");
        row.createCell(cellIndex++).setCellValue("ITEM NAME");
        row.createCell(cellIndex++).setCellValue("UOM NAME");
        row.createCell(cellIndex++).setCellValue("BATCH NO");
        row.createCell(cellIndex++).setCellValue("MRP");
        row.createCell(cellIndex++).setCellValue("EXPIRY DATE");
        row.createCell(cellIndex++).setCellValue("SYSTEM QUANTITY");
        row.createCell(cellIndex++).setCellValue("ACTUAL QUANTITY");
        row.createCell(cellIndex++).setCellValue("REASON");
        row.createCell(cellIndex++).setCellValue("ASSIGNEE");

    }

    public StockAudit lockDocumentAndUpdate(StockAudit stockAudit) throws Exception {
        log.debug("Request to lock document and save starts : {}", LocalTime.now());
        stockAuditRepository.findOneWithLock(stockAudit.getId());
        StockAudit lockedStockAudit = stockAuditSearchRepository.findById(stockAudit.getId()).get();
        for (AuditDocumentLine auditLines : stockAudit.getDocument().getLines()) {
            AuditDocumentLine line = lockedStockAudit.getDocument().getLines()
                .stream().filter(auditDocumentLine -> auditDocumentLine.getId().equals(auditLines.getId())).findAny().orElse(null);
            if (line != null) {
                line.setAuditQuantity(auditLines.getAuditQuantity());
                line.setDiscrepantReason(auditLines.getDiscrepantReason());
                line.setDiscrepantQuantity(auditLines.getDiscrepantQuantity());
                line.setDiscrepantValue(auditLines.getDiscrepantValue());
                line.setHasDiscrepancy(auditLines.getHasDiscrepancy());
            }
        }
        log.debug("Request to lock document and save ends : {}", LocalTime.now());
        return lockedStockAudit;
    }

    @Override
    public StockAudit createStockAuditForCriteria(StockAudit stockAudit) throws Exception {
        log.debug("Request to create stock audit for criteria starts : {}", LocalTime.now());
        stockAudit.getAuditCriterias().forEach(auditCriteria -> {
            Map<String, Object> params = new HashMap<>();
            StringBuilder query = new StringBuilder("select s from Stock s where s.storeId =:storeId");
            params.put("storeId", stockAudit.getDocument().getStore().getId());
            auditCriteria.auditFilter.forEach(filter -> {
                switch (filter.getField()) {
                    case "VALUE":
                        query.append(" and s.stockValue");
                        appendAuditFilterCriteriaOperator(query, filter);
                        query.append(":stockValue");
                        params.put("stockValue", BigDecimal.valueOf(Double.valueOf(filter.getValue())));
                        break;
                    case "COST":
                        query.append(" and s.cost");
                        appendAuditFilterCriteriaOperator(query, filter);
                        query.append(":cost");
                        params.put("cost", BigDecimal.valueOf(Double.valueOf(filter.getValue())));
                        break;
                    case "CONSIGNMENT":
                        query.append(" and s.consignment").append(" = ").append(":consignment");
                        params.put("consignment", Boolean.valueOf(filter.getValue()));
                        break;
                    case "VED_CATEGORY":
                        query.append(" and s.itemId").append(" in ").append("(select i.id from Item i where i.vedCategory = :vedCategory)");
                        params.put("vedCategory", VEDCategory.valueOf(filter.getValue()));
                        break;
                    case "LOCATOR":
                        query.append(" and s.locatorId").append(" = ").append(":locatorId");
                        params.put("locatorId", Long.valueOf(filter.getEntity().get("id").toString()));
                        break;
                    case "ITEM_CATEGORY":
                        query.append(" and s.itemId").append(" in ").append("(select i.id from Item i where i.category.id = :categoryId)");
                        params.put("categoryId", Long.valueOf(filter.getEntity().get("id").toString()));
                        break;
                    case "ITEM_TYPE":
                        query.append(" and s.itemId").append(" in ").append("(select i.id from Item i where jsonb_extract_path_text(i.type, 'id') = :itemTypeId)");
                        params.put("itemTypeId", filter.getEntity().get("id").toString());
                        break;
                    case "ITEM_NAME":
                        query.append(" and s.itemId").append(" in ").append("(select i.id from Item i where lower(i.name) like :itemName)");
                        params.put("itemName", new StringBuilder("%").append(filter.getValue()).append("%").toString().toLowerCase());
                        break;
                    case "ITEM_CODE":
                        query.append(" and s.itemId").append(" in ").append("(select i.id from Item i where lower(i.code) = :itemCode)");
                        params.put("itemCode", filter.getValue().toLowerCase());
                        break;
                }
            });
            TypedQuery<Stock> typedQuery = entityManager.createQuery(query.toString(), Stock.class);
            params.forEach((key, value) -> {
                typedQuery.setParameter(key, value);
            });
            List<Stock> stocks = typedQuery.getResultList();
            if (!stocks.isEmpty())
                mapStocksToAuditLines(stocks, stockAudit.getDocument().getLines(), auditCriteria.getAuditingUser());
        });
        if (stockAudit.getDocument().getLines().isEmpty()) {
            throw new CustomParameterizedException(ITEM_NOT_FOUND_FOR_GIVEN_CRITERIA);
        }
        log.debug("Request to create stock audit for criteria starts : {}", LocalTime.now());
        return stockAudit;

    }

    private void appendAuditFilterCriteriaOperator(StringBuilder query, AuditCriteriaFilter filter) {
        if ("LESS_THAN".equals(filter.getOperator()))
            query.append(" < ");
        else if ("LESS_THAN_OR_EQUAL_TO".equals(filter.getOperator()))
            query.append(" <= ");
        else if ("GREATER_THAN".equals(filter.getOperator()))
            query.append(" > ");
        else if ("GREATER_THAN_OR_EQUAL_TO".equals(filter.getOperator()))
            query.append(" >= ");
        else if ("EQUALTO".equals(filter.getOperator()))
            query.append(" = ");
    }

    @Override
    public StockAudit uploadAuditExcel(MultipartFile file, Long docId) throws Exception {
        log.debug("Request to upload audit excel starts : {}", LocalTime.now());
        StockAudit stockAudit = stockAuditRepository.findDetachedOne(docId);
        Sheet sheet;
        if (!file.isEmpty()) {
            if (file.getOriginalFilename().toLowerCase().endsWith("xlsx")) {
                XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
                sheet = workbook.getSheetAt(0);
            } else if (file.getOriginalFilename().toLowerCase().endsWith("xls")) {
                HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
                sheet = workbook.getSheetAt(0);
            } else {
                throw new CustomParameterizedException(INVALID_DOCUMENT);
            }

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) break;
                if (row.getCell(10) != null) {
                    AuditDocumentLine auditLine = stockAudit.getDocument().getLines().stream()
                        .filter(auditDocumentLine -> auditDocumentLine.getStockId().longValue() == (long) row.getCell(10).getNumericCellValue())
                        .filter(auditDocumentLine -> auditDocumentLine.getAuditingUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().get()))
                        .findAny().orElse(null);
                    if (auditLine != null && row.getCell(7) != null) {
                        float auditValue = 0f;
                        boolean isInvalid = false;
                        try {
                            auditValue = (float) row.getCell(7).getNumericCellValue();
                            if (auditValue < 0f || auditValue % 1 != 0.0f) {
                                isInvalid = true;
                            }
                        } catch (Exception ex) {
                            isInvalid = true;
                        }
                        if (isInvalid) {
                            Map<String, Object> source = new HashMap<>();
                            source.put("rowNumber", i + 1);
                            throw new FieldValidationException(singletonList(new ErrorMessage(INVALID_AUDIT_QUANTITY, source)), "Excel Validation Exception");
                        } else {
                            auditLine.setAuditQuantity(new Quantity(auditValue, auditLine.getStockQuantity().getUom()));
                            float discrepantValue = auditLine.getStockQuantity().getValue() - auditLine.getAuditQuantity().getValue();
                            if (discrepantValue != 0f) {
                                if (row.getCell(8) != null) {
                                    String cacheKey = "PHR:"+"display.raw:\"" + row.getCell(8).getStringCellValue() + "\""+"valueSet.code.raw:AUDIT_DISCREPANCY_REASON";
                                    List<ValueSetCode> valueSetCodeList = getValueSetCodes(row.getCell(8).getStringCellValue(),"AUDIT_DISCREPANCY_REASON",cacheKey);
                                    auditLine.setDiscrepantReason(isNotEmpty(valueSetCodeList) ? valueSetCodeList.get(0) : null);
                                }
                                auditLine.setDiscrepantQuantity(new Quantity(discrepantValue, auditLine.getStockQuantity().getUom()));
                                auditLine.setDiscrepantValue(multiply(discrepantValue, auditLine.getCost()));
                                auditLine.setHasDiscrepancy(true);
                            } else {
                                auditLine.setDiscrepantReason(null);
                                auditLine.setDiscrepantQuantity(null);
                                auditLine.setDiscrepantValue(null);
                                auditLine.setHasDiscrepancy(false);
                            }
                        }
                    }
                } else {
                    Map<String, Object> source = new HashMap<>();
                    source.put("rowNumber", i + 1);
                    throw new FieldValidationException(singletonList(new ErrorMessage(EXTRA_ITEM_FOUND, source)), "Excel Validation Exception");
                }
            }
        }
        log.debug("Request to upload audit excel ends : {}", LocalTime.now());
        return stockAudit;
    }

    private List<ValueSetCode> getValueSetCodes(String displayCode, String valueSetCode, String cacheKey) {
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return  pharmacyRedisCacheService.getValueSetCodes(displayCode,valueSetCode,elasticsearchTemplate,cacheKey);
        }else {
            Query searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery()
                .must(queryStringQuery("display.raw:\"" + displayCode + "\""))
                .must(queryStringQuery("valueSet.code.raw:" + valueSetCode))).build();
            return ElasticSearchUtil.getRecords(searchQuery, ValueSetCode.class, elasticsearchTemplate, "valuesetcode");
        }
    }

    private void mapRejectedItem(String errorCode, String itemName, String itemCode, int lineNo, Map<String, Object> rejectedItem) {
        rejectedItem.put("errorCode", errorCode);
        rejectedItem.put("itemName", itemName);
        rejectedItem.put("itemCode", itemCode);
        rejectedItem.put("lineNo", (lineNo));
    }

    @Override
    public StockAudit addItemLines(String itemCode, String batchNo, StockAudit stockAudit) {
        log.debug("Request to add item lines starts : {}", LocalTime.now());
        List<Stock> stocks = stockRepository.getStockWithItemCodeAndBatch(itemCode, batchNo);
        if (!stocks.isEmpty()) {
            List<AuditDocumentLine> auditDocumentLines = new ArrayList<>();
            UserDTO user = stockAudit.getDocument().getLines().get(0).getAuditingUser();
            mapStocksToAuditLines(stocks, auditDocumentLines, user);
            auditDocumentLines.stream().forEach(auditDocumentLine -> stockAudit.getDocument().getLines().add(auditDocumentLine));
        }
        log.debug("Request to add item lines ends : {}", LocalTime.now());
        return stockAudit;
    }


    private void validateStockAuditLines(StockAudit stockAudit) throws FieldValidationException {
        List<ErrorMessage> errorMessages = new ArrayList<>();
        for (AuditDocumentLine line : stockAudit.getDocument().getLines()) {
            if (line.getAuditQuantity() != null) {
                if (line.getAuditQuantity().getValue() == null) {
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put("itemName", line.getItem().getName());
                    errorMessages.add(new ErrorMessage(NULL_AUDIT_QUANTITY, source));
                }
            }
        }
        try {
            validateInProgress(stockAudit);
            if (!errorMessages.isEmpty()) {
                throw new FieldValidationException(errorMessages, "Validation exception");
            }
        } catch (FieldValidationException exception) {
            exception.getErrorMessages().addAll(errorMessages);
            throw exception;
        }
    }

    private void validateInProgress(StockAudit stockAudit) {

        List<ErrorMessage> errorMessages = new ArrayList<>();
        for (AuditDocumentLine line : stockAudit.getDocument().getLines()) {
            if (line.getAuditingUser() == null || line.getAuditingUser().getLogin() == null) {
                Map<String, Object> source = new HashMap<String, Object>();
                source.put("itemName", line.getItem().getName());
                errorMessages.add(new ErrorMessage(NULL_AUDITING_USER, source));
            }
        }
        if (!errorMessages.isEmpty())
            throw new FieldValidationException(errorMessages, "Validation exception");
    }

    @Override
    public void generateStockAuditList(File file, String query, Pageable pageable) throws IOException {
        log.debug("Request to generate stock audit list starts : {} ", LocalTime.now());

        FileWriter stockAuditFileWriter = new FileWriter(file);
        final String[] stockAuditFileHeader = {"Audit No", "Creation Date", "Approval Date", "Audit Store", "Store Contact", "Status"};
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);

        String dateFormat = null;

        try (CSVPrinter csvFilePrinter = new CSVPrinter(stockAuditFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(stockAuditFileHeader);

            Iterator<StockAudit> stockAuditIterator = search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();

            while (stockAuditIterator.hasNext()) {

                StockAudit stockAudit = stockAuditIterator.next();

                if (dateFormat == null)
                    dateFormat = ConfigurationUtility.getConfiguration("athma_date_format", null, stockAudit.getDocument().getUnit().getId(), null, elasticsearchTemplate);

                List stockAuditData = new ArrayList();
                stockAuditData.add(stockAudit.getDocumentNumber());
                stockAuditData.add(DateUtil.getFormattedDateAsFunctionForCSVExport(stockAudit.getDocument().getCreatedDate(), dateFormat));

                if (stockAudit.getDocument().getApprovedDate() == null) {
                    stockAuditData.add("");
                } else {
                    stockAuditData.add(DateUtil.getFormattedDateAsFunctionForCSVExport(stockAudit.getDocument().getApprovedDate(), dateFormat));
                }
                stockAuditData.add(stockAudit.getDocument().getStore().getName());
                stockAuditData.add(stockAudit.getDocument().getStoreContact() != null ? stockAudit.getDocument().getStoreContact().getDisplayName() : " ");
                stockAuditData.add(stockAudit.getDocument().getStatus().getStatusDisplay());
                csvFilePrinter.printRecord(stockAuditData);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (stockAuditFileWriter != null)
                stockAuditFileWriter.close();
        }
        log.debug("Request to generate stock audit list ends : {} ", LocalTime.now());
    }

    @Override
    public Map<String, Object> addAuditLinesFromExcel(MultipartFile file, Long storeId, Long userId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<AuditDocumentLine> auditDocumentLines = new ArrayList<>();
        List<Map<String, Object>> rejectedItemList = new ArrayList<>();
        Sheet sheet;
        String itemCode;
        if (file.getOriginalFilename().toLowerCase().endsWith("xls")) {
            sheet = new HSSFWorkbook(file.getInputStream()).getSheetAt(0);
        } else if (file.getOriginalFilename().toLowerCase().endsWith("xlsx")) {
            sheet = new XSSFWorkbook(file.getInputStream()).getSheetAt(0);
        } else {
            throw new CustomParameterizedException(INVALID_DOCUMENT);
        }
        UserDTO user = getUserDto(userId);
        try {
            for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                Map rejectedItemMap = new HashMap();
                if (row == null) continue;
                if (row.getCell(0) != null && !(itemCode = row.getCell(0).getStringCellValue().trim()).isEmpty()) {
                    long count = itemSearchRepository.search(queryStringQuery("code.raw:\"" + itemCode + "\""), PageRequest.of(0,1)).getTotalElements();
                    if (count == 0) {
                        mapRejectedItem(INVALID_ITEM_CODE, null, null, i + 1, rejectedItemMap);
                    } else {
                        List<Stock> stocks = stockRepository.findStocksByStoreIdAndItemCode(storeId, itemCode);
                        if (stocks.isEmpty()) {
                            mapRejectedItem(ITEM_NOT_MAPPED_TO_STORE, null, null, i + 1, rejectedItemMap);
                        } else {
                            List<Stock> stockList = stocks.stream().filter(stock -> stock.getQuantity() > 0).collect(Collectors.toList());
                            if (stockList.isEmpty()) {
                                mapRejectedItem(STOCK_NOT_AVAILABLE_FOR_ITEM_AND_STORE, null, null, i + 1, rejectedItemMap);
                            } else {
                                mapStocksToAuditLines(stockList, auditDocumentLines, user);
                            }
                        }
                    }
                }
                if (!rejectedItemMap.isEmpty()) {
                    rejectedItemList.add(rejectedItemMap);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new CustomParameterizedException(INVALID_DOCUMENT);
        }
        result.put("AUDIT_DOC_LINES", auditDocumentLines);
        result.put("WARNING", rejectedItemList);
        return result;
    }

    private UserDTO getUserDto(Long id) {
        org.nh.pharmacy.domain.User user = userService.findOne(id);
        UserDTO userDto = new UserDTO();
        userDto.setId(user.getId());
        userDto.setLogin(user.getLogin());
        userDto.setDisplayName(user.getDisplayName());
        userDto.setEmployeeNo(user.getEmployeeNo());

        return userDto;
    }

    @Override
    public Map<String, Set<RelatedDocument>> getRelatedDocuments(String documentNumber) {
        log.debug("Get all documents which is related to given stockAudit document:-" + documentNumber);

        Set<RelatedDocument> relatedDocumentList = new LinkedHashSet<>();
        Map<String, Set<RelatedDocument>> finalList = new LinkedHashMap<>();
        //  finalList.put(TransactionType.Stock_Audit_Plan.getTransactionTypeDisplay(), new LinkedHashSet<>());
        Iterable<StockAudit> stockAudits = stockAuditSearchRepository.search(queryStringQuery("documentNumber.raw:" + documentNumber));
        stockAudits.forEach(stockAudit -> {
            Query queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(queryStringQuery("document.referenceDocumentNumber.raw:" + stockAudit.getDocumentNumber())).build();
            List<InventoryAdjustment> inventoryAdjustments = ElasticSearchUtil.getRecords(queryBuilder, InventoryAdjustment.class, elasticsearchTemplate, "inventoryadjustment");
            inventoryAdjustments.forEach(inventoryAdjustment -> {
                RelatedDocument relDoc = new RelatedDocument();
                relDoc.setId(inventoryAdjustment.getId().toString());
                relDoc.setDocumentType(TransactionType.Inventory_Adjustment);
                relDoc.setDocumentNumber(inventoryAdjustment.getDocumentNumber());
                relDoc.setStatus(Status.valueOf(inventoryAdjustment.getDocument().getStatus().toString()));
                relDoc.setCreatedDate(LocalDateTime.parse(inventoryAdjustment.getDocument().getCreatedDate().toString()));
                relatedDocumentList.add(relDoc);
                finalList.put(TransactionType.Inventory_Adjustment.getTransactionTypeDisplay(), relatedDocumentList);
                /*if (stockAudit.getDocument().getReferenceDocumentNumber() != null) {
                    populateAuditPlanRelatedDocument(stockAudit.getDocument().getReferenceDocumentNumber(), finalList);
                }*/
            });
        });
        return finalList;
    }

    private void populateAuditPlanRelatedDocument(String documentNumber, Map<String, Set<RelatedDocument>> finalList) {
        Set<RelatedDocument> relatedDocumentList = finalList.get(TransactionType.Stock_Audit_Plan.getTransactionTypeDisplay());
        Query queryBuilder = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("documentNumber:" + documentNumber)).build();
        List<StockAuditPlan> stockAuditPlans = ElasticSearchUtil.getRecords(queryBuilder, StockAuditPlan.class, elasticsearchTemplate, "stockauditplan");
        stockAuditPlans.forEach(stockAuditPlan -> {
            RelatedDocument relDoc = new RelatedDocument();
            relDoc.setId(stockAuditPlan.getId().toString());
            relDoc.setDocumentType(TransactionType.Stock_Audit_Plan);
            relDoc.setDocumentNumber(stockAuditPlan.getDocumentNumber());
            relDoc.setStatus(Status.valueOf(stockAuditPlan.getDocument().getStatus().toString()));
            relDoc.setCreatedDate(LocalDateTime.parse(stockAuditPlan.getDocument().getCreatedDate().toString()));
            relatedDocumentList.add(relDoc);
            finalList.put(TransactionType.Stock_Audit_Plan.getTransactionTypeDisplay(), relatedDocumentList);
        });
    }

    private String getGroupData(Context context, Long unitId) {
        String cacheKey = "PHR: context:"+context.name()+" AND active:true AND partOf.id:"+unitId+" !_exists_:partOf";
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return pharmacyRedisCacheService.getCommaSeparatedGroupCodes(context,unitId,elasticsearchTemplate,cacheKey);
        }else {
            return ConfigurationUtil.getCommaSeparatedGroupCodes(context, unitId, elasticsearchTemplate);
        }
    }

}
