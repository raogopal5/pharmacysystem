package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.FlowType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.service.ItemBatchInfoService;
import org.nh.pharmacy.service.StockService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.nh.common.util.BigDecimalUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockResource REST controller.
 *
 * @see StockResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockResourceIntTest {

    private static final String DEFAULT_BATCH_NO = "BatchNo";
    private static final String UPDATED_BATCH_NO = "UpdatedBatchNo";

    private static final LocalDate DEFAULT_EXPIRY_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXPIRY_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_OWNER = "Owner";
    private static final String UPDATED_OWNER = "UpdatedOwner";

    private static final BigDecimal DEFAULT_COST = BigDecimal.ONE;
    private static final BigDecimal UPDATED_COST = getBigDecimal(2F);

    private static final BigDecimal DEFAULT_MRP = BigDecimal.ONE;
    private static final BigDecimal UPDATED_MRP = getBigDecimal(2F);

    private static final Float DEFAULT_QUANTITY = 100F;
    private static final Float DEFAULT_QUANTITY_STOCK_IN = 10F;
    private static final Float UPDATED_QUANTITY = 2F;

    private static final BigDecimal DEFAULT_STOCK_VALUE = BigDecimal.ONE;
    private static final BigDecimal UPDATED_STOCK_VALUE = getBigDecimal(2F);

    private static final String DEFAULT_SUPPLIER = "Supplier";
    private static final String UPDATED_SUPPLIER = "UpdatedSupplier";

    private static final String DEFAULT_SKU = "AAAAAAAAAA";
    private static final String UPDATED_SKU = "BBBBBBBBBB";

    private static final Boolean DEFAULT_CONSIGNMENT = false;
    private static final Boolean UPDATED_CONSIGNMENT = true;

    private static final Long DEFAULT_STOCK_ID = 1L;

    private static final Float DEFAULT_AVAILABLE_QUANTITY = 1F;

    private static final String DEFAULT_TAX_NAME = "AAAAAAAAAA";

    private static final BigDecimal DEFAULT_TAX_PER_UNIT = getBigDecimal(2F);

    private static final String DEFAULT_TAX_TYPE = "AAAAAAAAAA";

    private static final String DEFAULT_BAR_CODE = "AAAAAAAAAA";

    private static final LocalDate DEFAULT_FIRST_STOCK_IN_DATE = LocalDate.ofEpochDay(0L);

    private static final LocalDate DEFAULT_LAST_STOCK_OUT_DATE = LocalDate.ofEpochDay(0L);

    private static final LocalDateTime DEFAULT_TRANSACTION_DATE = LocalDateTime.now();

    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.GRN;

    private static final String DEFAULT_TRANSACTION_NUMBER = "AAAAAAAAAA";

    private static final Long DEFAULT_TRANSACTION_LINE_ID = 1L;

    private static final String DEFAULT_TRANSACTION_REF_NO = "AAAAAAAAAA";

    private static final String DEFAULT_TRANSACTION_NO = "AAAAAAAAAA";

    private static final Long DEFAULT_TRANSACTION_ID = 1L;

    private static final LocalDateTime DEFAULT_RESERVED_DATE = LocalDateTime.now().minusDays(1l);

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockFlowRepository stockFlowRepository;

    @Autowired
    private StockSourceRepository stockSourceRepository;

    @Autowired
    private ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    @Autowired
    private ItemStoreStockViewRepository itemStoreStockViewRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restStockMockMvc;

    private Stock stock;

    @Autowired
    private StockService stockService;

    @Autowired
    private ItemBatchInfoService itemBatchInfoService;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    private StockEntry stockEntry;

    private Long userId =1L;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockResource stockResource = new StockResource(stockService, itemBatchInfoService);
        this.restStockMockMvc = MockMvcBuilders.standaloneSetup(stockResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Stock createEntity(EntityManager em) {
        Stock stock = new Stock()
            .batchNo(DEFAULT_BATCH_NO)
            .expiryDate(DEFAULT_EXPIRY_DATE)
            .owner(DEFAULT_OWNER)
            .cost(DEFAULT_COST)
            .mrp(DEFAULT_MRP)
            .quantity(DEFAULT_QUANTITY)
            .stockValue(DEFAULT_STOCK_VALUE)
            .supplier(DEFAULT_SUPPLIER)
            .sku(DEFAULT_SKU)
            .consignment(DEFAULT_CONSIGNMENT);

        stock.setItemId(ItemResourceIntTest.createEntityIfNotExist(em).getId());
        stock.setStoreId(HealthcareServiceCenterResourceIntTest.createEntityIfNotExist(em).getId());
        stock.setLocatorId(LocatorResourceIntTest.createEntityIfNotExist(em).getId());
        stock.setUomId(UOMResourceIntTest.createEntityIfNotExist(em).getId());
        stock.setUnitId(OrganizationResourceIntTest.createEntityIfNotExist(em).getId());

        return stock;
    }

    public static StockEntry createStockEntry(EntityManager em) {
        StockEntry stockEntry = new StockEntry()
            .batchNo(DEFAULT_BATCH_NO)
            .owner(DEFAULT_OWNER)
            .supplier(DEFAULT_SUPPLIER)
            .taxPerUnit(DEFAULT_TAX_PER_UNIT)
            .taxName(DEFAULT_TAX_NAME)
            .taxType(DEFAULT_TAX_TYPE)
            .firstStockInDate(DEFAULT_FIRST_STOCK_IN_DATE)
            .lastStockOutDate(DEFAULT_LAST_STOCK_OUT_DATE)
            .consignment(DEFAULT_CONSIGNMENT)
            .cost(DEFAULT_COST).mrp(DEFAULT_MRP)
            .sku(DEFAULT_SKU)
            .quantity(DEFAULT_QUANTITY_STOCK_IN)
            .availableQuantity(DEFAULT_AVAILABLE_QUANTITY)
            .expiryDate(DEFAULT_EXPIRY_DATE)
            .transactionDate(DEFAULT_TRANSACTION_DATE)
            .transactionId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionNumber(DEFAULT_TRANSACTION_NUMBER)
            .transactionRefNo(DEFAULT_TRANSACTION_REF_NO)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .barCode(DEFAULT_BAR_CODE);

        stockEntry.itemId(ItemResourceIntTest.createEntityIfNotExist(em).getId());
        stockEntry.storeId(HealthcareServiceCenterResourceIntTest.createEntityIfNotExist(em).getId());
        stockEntry.locatorId(LocatorResourceIntTest.createEntityIfNotExist(em).getId());
        stockEntry.uomId(UOMResourceIntTest.createEntityIfNotExist(em).getId());
        stockEntry.unitId(OrganizationResourceIntTest.createEntityIfNotExist(em).getId());

        return stockEntry;
    }

    @Before
    public void initTest() {
        stock = createEntity(em);
        stockEntry = createStockEntry(em);
    }

    public Item getNewItem() {
        Item newItem = ItemResourceIntTest.createEntity(em);
        newItem.setCode(String.valueOf((Math.random())));
        newItem.getCategory().setCode(String.valueOf((Math.random())));
        newItem = itemRepository.saveAndFlush(newItem);
        return newItem;
    }

    @Test
    @Transactional
    public void createStock() throws Exception {
        int databaseSizeBeforeCreate = stockRepository.findAll().size();
        // Create the Stock

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isCreated());

        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeCreate + 1);
        /*Stock testStock = stockList.get(stockList.size() - 1);
        assertThat(testStock.getItemId()).isEqualTo(stock.getItemId());
        assertThat(testStock.getBatchNo()).isEqualTo(DEFAULT_BATCH_NO);
        assertThat(testStock.getExpiryDate()).isEqualTo(DEFAULT_EXPIRY_DATE);
        assertThat(testStock.getOwner()).isEqualTo(DEFAULT_OWNER);
        assertThat(testStock.getCost()).isEqualTo(DEFAULT_COST);
        assertThat(testStock.getMrp()).isEqualTo(DEFAULT_MRP);
        assertThat(testStock.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testStock.getStockValue()).isEqualTo(DEFAULT_STOCK_VALUE);
        assertThat(testStock.getStoreId()).isEqualTo(stock.getStoreId());
        assertThat(testStock.getLocatorId()).isEqualTo(stock.getLocatorId());
        assertThat(testStock.getSupplier()).isEqualTo(DEFAULT_SUPPLIER);
        assertThat(testStock.getUomId()).isEqualTo(stock.getUomId());
        assertThat(testStock.getSku()).isEqualTo(DEFAULT_SKU);
        assertThat(testStock.getUnitId()).isEqualTo(stock.getUnitId());
        assertThat(testStock.isConsignment()).isEqualTo(DEFAULT_CONSIGNMENT);*/
    }

    @Test
    @Transactional
    public void createStockWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockRepository.findAll().size();

        // Create the Stock with an existing ID
        Stock existingStock = new Stock();
        existingStock.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStock)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkItemIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setItemId(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOwnerIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setOwner(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCostIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setCost(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setQuantity(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStockValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setStockValue(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStoreIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setStoreId(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLocatorIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setLocatorId(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSupplierIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setSupplier(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUomIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setUomId(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSkuIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setSku(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUnitIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockRepository.findAll().size();
        // set the field null
        stock.setUnitId(null);

        // Create the Stock, which fails.

        restStockMockMvc.perform(post("/api/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stock)))
            .andExpect(status().isBadRequest());

        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStocks() throws Exception {
        // Initialize the database
        stockRepository.saveAndFlush(stock);

        // Get all the stockList
        restStockMockMvc.perform(get("/api/stocks?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stock.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(stock.getItemId().intValue())))
            .andExpect(jsonPath("$.[*].batchNo").value(hasItem(DEFAULT_BATCH_NO.toString())))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].owner").value(hasItem(DEFAULT_OWNER.toString())))
            .andExpect(jsonPath("$.[*].cost").value(hasItem(DEFAULT_COST.doubleValue())))
            .andExpect(jsonPath("$.[*].mrp").value(hasItem(DEFAULT_MRP.doubleValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].stockValue").value(hasItem(DEFAULT_STOCK_VALUE.doubleValue())))
            .andExpect(jsonPath("$.[*].storeId").value(hasItem(stock.getStoreId().intValue())))
            .andExpect(jsonPath("$.[*].locatorId").value(hasItem(stock.getLocatorId().intValue())))
            .andExpect(jsonPath("$.[*].supplier").value(hasItem(DEFAULT_SUPPLIER.toString())))
            .andExpect(jsonPath("$.[*].uomId").value(hasItem(stock.getUomId().intValue())))
            .andExpect(jsonPath("$.[*].sku").value(hasItem(DEFAULT_SKU.toString())))
            .andExpect(jsonPath("$.[*].unitId").value(hasItem(stock.getUnitId().intValue())))
            .andExpect(jsonPath("$.[*].consignment").value(hasItem(DEFAULT_CONSIGNMENT.booleanValue())));
    }

    @Test
    @Transactional
    public void getStock() throws Exception {
        // Initialize the database
        stockRepository.saveAndFlush(stock);

        // Get the stock
        restStockMockMvc.perform(get("/api/stocks/{id}", stock.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stock.getId().intValue()))
            .andExpect(jsonPath("$.itemId").value(stock.getItemId().intValue()))
            .andExpect(jsonPath("$.batchNo").value(DEFAULT_BATCH_NO.toString()))
            .andExpect(jsonPath("$.expiryDate").value(DEFAULT_EXPIRY_DATE.toString()))
            .andExpect(jsonPath("$.owner").value(DEFAULT_OWNER.toString()))
            .andExpect(jsonPath("$.cost").value(DEFAULT_COST.doubleValue()))
            .andExpect(jsonPath("$.mrp").value(DEFAULT_MRP.doubleValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.stockValue").value(DEFAULT_STOCK_VALUE.doubleValue()))
            .andExpect(jsonPath("$.storeId").value(stock.getStoreId().intValue()))
            .andExpect(jsonPath("$.locatorId").value(stock.getLocatorId().intValue()))
            .andExpect(jsonPath("$.supplier").value(DEFAULT_SUPPLIER.toString()))
            .andExpect(jsonPath("$.uomId").value(stock.getUomId().intValue()))
            .andExpect(jsonPath("$.sku").value(DEFAULT_SKU.toString()))
            .andExpect(jsonPath("$.unitId").value(stock.getUnitId().intValue()))
            .andExpect(jsonPath("$.consignment").value(DEFAULT_CONSIGNMENT.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStock() throws Exception {
        // Get the stock
        restStockMockMvc.perform(get("/api/stocks/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStock() throws Exception {
        // Initialize the database
        stockService.save(stock);

        int databaseSizeBeforeUpdate = stockRepository.findAll().size();

        // Update the stock
        Stock updatedStock = stockRepository.findById(stock.getId()).get();
        updatedStock
            .batchNo(UPDATED_BATCH_NO)
            .expiryDate(UPDATED_EXPIRY_DATE)
            .owner(UPDATED_OWNER)
            .cost(UPDATED_COST)
            .mrp(UPDATED_MRP)
            .quantity(UPDATED_QUANTITY)
            .stockValue(UPDATED_STOCK_VALUE)
            .supplier(UPDATED_SUPPLIER)
            .sku(UPDATED_SKU)
            .consignment(UPDATED_CONSIGNMENT);

        stockService.save(updatedStock);

        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeUpdate);
        Stock testStock = stockRepository.findById(stock.getId()).get();
        ;
        assertThat(testStock.getBatchNo()).isEqualTo(UPDATED_BATCH_NO);
        assertThat(testStock.getExpiryDate()).isEqualTo(UPDATED_EXPIRY_DATE);
        assertThat(testStock.getOwner()).isEqualTo(UPDATED_OWNER);
        assertThat(testStock.getCost()).isEqualTo(UPDATED_COST);
        assertThat(testStock.getMrp()).isEqualTo(UPDATED_MRP);
        assertThat(testStock.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testStock.getStockValue()).isEqualTo(UPDATED_STOCK_VALUE);
        assertThat(testStock.getSupplier()).isEqualTo(UPDATED_SUPPLIER);
        assertThat(testStock.getSku()).isEqualTo(UPDATED_SKU);
        assertThat(testStock.isConsignment()).isEqualTo(UPDATED_CONSIGNMENT);
    }

    @Test
    @Transactional
    public void deleteStock() throws Exception {
        // Initialize the database
        stockService.save(stock);
        int databaseSizeBeforeDelete = stockRepository.findAll().size();
        stockService.delete(stock.getId());
        // Validate the database is empty
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Stock.class);
    }

    public static Stock createEntityIfNotExist(EntityManager em) {
        List<Stock> stocks = em.createQuery("from " + Stock.class.getName()).getResultList();
        Stock stock = null;
        if (stocks != null && !stocks.isEmpty()) {
            stock = stocks.get(0);
        } else {
            stock = createEntity(em);
            em.persist(stock);
            em.flush();
            stock = (Stock) em.createQuery("from " + Stock.class.getName()).getResultList().get(0);
        }
        return stock;
    }

    /**
     * Creating reserve stock but stock not found, OUTPUT : StockException "Insufficient stock for given combination itemId, batchNo, storeId" : FAILURE
     */
    @Test(expected = StockException.class)
    @Transactional
    public void checkRequestedStockIsNotFound() throws StockException {
        Item item = ItemResourceIntTest.createEntityIfNotExist(em);
        stockService.reserveStock(null, item.getId(), "new batch", stock.getStoreId(), 10f, 1l, TransactionType.GRN,
            "TxNo_1", 1l, LocalDateTime.now(), userId);
    }

    /**
     * Creating reserve stock but no stock available, OUTPUT : StockException "Insufficient stock for given ID" : FAILURE
     */
    @Test(expected = StockException.class)
    @Transactional
    public void checkRequestedStockIsNotAvailable() throws StockException {
        List<Stock> stockList = stockRepository.findAll();
        stock = stockList.get(stockList.size() - 1);
        stockService.reserveStock(stock.getId(), stock.getItemId(), stock.getBatchNo(), stock.getStoreId(), 10000f, 2l, TransactionType.GRN,
            "TxNo_2", 2l, LocalDateTime.now(), userId);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void aCreateNewStock() {
        int databaseSizeBeforeCreate = stockRepository.findAll().size();
        stock = createEntity(em);
        stock = stockRepository.saveAndFlush(stock);
        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeCreate + 1);
        Stock testStock = stockRepository.findById(stock.getId()).get();
        assertThat(testStock.getItemId()).isEqualTo(stock.getItemId());
        assertThat(testStock.getBatchNo()).isEqualTo(DEFAULT_BATCH_NO);
        assertThat(testStock.getOwner()).isEqualTo(DEFAULT_OWNER);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void bCreateNewReservedStock() {
        int databaseSizeBeforeCreate = reserveStockRepository.findAll().size();
        stock = stockRepository.findAll().get(0);
        ReserveStock reserveStock = ReserveStockResourceIntTest.createEntity(em).stockId(stock.getId());
        reserveStock = reserveStockRepository.saveAndFlush(reserveStock);

        // Validate the Stock in the database
        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeCreate + 1);
        ReserveStock testReserveStock = reserveStockList.get(reserveStockList.size() - 1);
        assertThat(testReserveStock.getStockId()).isEqualTo(reserveStock.getStockId());
        assertThat(testReserveStock.getTransactionId()).isEqualTo(reserveStock.getTransactionId());
        assertThat(testReserveStock.getTransactionLineId()).isEqualTo(reserveStock.getTransactionLineId());
    }

    /**
     * Create reserve stock and stock available with existing reserve stock, OUTPUT : SUCCESS
     */
    @Test
    @Transactional
    public void checkRequestedStockIsAvailable() throws StockException {
        int databaseSizeBeforeCreate = reserveStockRepository.findAll().size();
        stock = stockRepository.findAll().get(0);
        stockService.reserveStock(stock.getId(), stock.getItemId(), stock.getBatchNo(), stock.getStoreId(), 4f, 3l, TransactionType.GRN,
            "TxNo_3", 3l, LocalDateTime.now(), userId);

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(databaseSizeBeforeCreate + 1).isEqualTo(reserveStockList.size());
        ReserveStock testReserveStock = reserveStockList.get(reserveStockList.size() - 1);
        assertThat(testReserveStock.getStockId()).isEqualTo(stock.getId());
        assertThat(testReserveStock.getTransactionId()).isEqualTo(3l);
        assertThat(testReserveStock.getTransactionLineId()).isEqualTo(3l);
    }

    /**
     * Creating reserve stock and stock available with no existing reserve stock, OUTPUT : SUCCESS
     */
    @Test
    @Transactional
    public void checkRequestedStockHasNoReservedStock() throws StockException {
        int databaseSizeBeforeCreate = reserveStockRepository.findAll().size();
        stock = stockRepository.findAll().get(0);
        stockService.reserveStock(stock.getId(), stock.getItemId(), stock.getBatchNo(), stock.getStoreId(), 10f, 4l, TransactionType.GRN,
            "TxNo_4", 4l, LocalDateTime.now(),userId);

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(databaseSizeBeforeCreate + 1).isEqualTo(reserveStockList.size());
        ReserveStock testReserveStock = reserveStockList.get(reserveStockList.size() - 1);
        assertThat(testReserveStock.getStockId()).isEqualTo(stock.getId());
        assertThat(testReserveStock.getTransactionId()).isEqualTo(4l);
        assertThat(testReserveStock.getTransactionLineId()).isEqualTo(4l);
    }

    /**
     * delete reserved stock by transactionId and transactionType, OUTPUT : SUCCESS
     */
    @Test
//    @Transactional
    public void deleteReservedStock() {
        int databaseSizeBeforeDelete = reserveStockRepository.findAll().size();
        ReserveStock reserveStock = reserveStockRepository.findAll().get(0);
        stockService.deleteReservedStock(reserveStock.getTransactionId(), reserveStock.getTransactionType());
        int databaseSizeAfterDelete = reserveStockRepository.findAll().size();
        assertThat(databaseSizeBeforeDelete - 1).isEqualTo(databaseSizeAfterDelete);
    }

    // @Test
    public void destroy() {
        //    zDestroy();
    }

    public StockEntry getStockEntry() throws Exception {
        return new StockEntry().itemId(stockEntry.getItemId()).locatorId(stockEntry.getLocatorId()).stockId(DEFAULT_STOCK_ID).storeId(stockEntry.getStoreId()).batchNo(DEFAULT_BATCH_NO).owner(DEFAULT_OWNER).supplier(DEFAULT_SUPPLIER).unitId(stockEntry.getUnitId()).uomId(stockEntry.getUomId()).taxPerUnit(DEFAULT_TAX_PER_UNIT).taxName(DEFAULT_TAX_NAME).taxType(DEFAULT_TAX_TYPE).firstStockInDate(DEFAULT_FIRST_STOCK_IN_DATE).lastStockOutDate(DEFAULT_LAST_STOCK_OUT_DATE).consignment(DEFAULT_CONSIGNMENT).cost(DEFAULT_COST).mrp(DEFAULT_MRP).sku(DEFAULT_SKU).quantity(DEFAULT_QUANTITY_STOCK_IN).availableQuantity(DEFAULT_AVAILABLE_QUANTITY).expiryDate(DEFAULT_EXPIRY_DATE).transactionDate(DEFAULT_TRANSACTION_DATE).transactionId(DEFAULT_TRANSACTION_LINE_ID).transactionLineId(DEFAULT_TRANSACTION_LINE_ID).transactionNumber(DEFAULT_TRANSACTION_NUMBER).transactionRefNo(DEFAULT_TRANSACTION_REF_NO).transactionType(DEFAULT_TRANSACTION_TYPE).barCode(DEFAULT_BAR_CODE);
    }

    //  @Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockInWith1MultipleEntry() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int stockSourceTableSizeBeforeSave = stockSourceRepository.findAll().size();
        int itemUnitAverageCostTableSizeBeforeSave = itemUnitAverageCostRepository.findAll().size();


        StockEntry stockEntryWithStockIdNull = getStockEntry().stockId(null);
        StockEntry stockEntryWithSameStockId = getStockEntry().stockId(1L);
        StockEntry stockEntryWithDiffBatchNo = getStockEntry().batchNo("BatchNo2");

        stockService.stockIn(new ArrayList<StockEntry>() {{
            add(stockEntryWithStockIdNull);
            add(stockEntryWithSameStockId);
            add(stockEntryWithDiffBatchNo);
        }});

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave + 2);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 3);
        assertThat(stockSourceList).hasSize(stockSourceTableSizeBeforeSave + 3);
        assertThat(itemUnitAverageCostList).hasSize(itemUnitAverageCostTableSizeBeforeSave + 1);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);

    }

    //  @Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockInWith2ExistingStockId() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int stockSourceTableSizeBeforeSave = stockSourceRepository.findAll().size();
        int itemUnitAverageCostTableSizeBeforeSave = itemUnitAverageCostRepository.findAll().size();

        StockEntry stockEntryWithExistingStockId = getStockEntry().stockId(stockRepository.findAll().get(stockTableSizeBeforeSave - 1).getId());

        stockService.stockIn(new ArrayList<StockEntry>() {{
            add(stockEntryWithExistingStockId);
        }});

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 1);
        assertThat(stockSourceList).hasSize(stockSourceTableSizeBeforeSave + 1);
        assertThat(itemUnitAverageCostList).hasSize(itemUnitAverageCostTableSizeBeforeSave);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);
    }

    //  @Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockInWith3NewItem() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int stockSourceTableSizeBeforeSave = stockSourceRepository.findAll().size();
        int itemUnitAverageCostTableSizeBeforeSave = itemUnitAverageCostRepository.findAll().size();

        StockEntry stockEntryWithNewItemId = getStockEntry().itemId(getNewItem().getId()).stockId(null).cost(getBigDecimal(5F)).quantity(100F);
        stockService.stockIn(new ArrayList<StockEntry>() {{
            add(stockEntryWithNewItemId);
        }});

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave + 1);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 1);
        assertThat(stockSourceList).hasSize(stockSourceTableSizeBeforeSave + 1);
        assertThat(itemUnitAverageCostList).hasSize(itemUnitAverageCostTableSizeBeforeSave + 1);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);
    }

    // @Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockInWith4DifferentCosts() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int stockSourceTableSizeBeforeSave = stockSourceRepository.findAll().size();
        int itemUnitAverageCostTableSizeBeforeSave = itemUnitAverageCostRepository.findAll().size();

        StockEntry stockEntryWithDifferentCost1 = getStockEntry().itemId(stockEntry.getItemId()).stockId(null).cost(BigDecimal.TEN).quantity(200F);
        StockEntry stockEntryWithDifferentCost2 = getStockEntry().itemId(stockEntry.getItemId()).stockId(null).cost(getBigDecimal(15F)).quantity(300F);

        stockService.stockIn(new ArrayList<StockEntry>() {{
            add(stockEntryWithDifferentCost1);
            add(stockEntryWithDifferentCost2);
        }});

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave + 2);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 2);
        assertThat(stockSourceList).hasSize(stockSourceTableSizeBeforeSave + 2);
        assertThat(itemUnitAverageCostList).hasSize(itemUnitAverageCostTableSizeBeforeSave);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);
    }

    //  @Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockInWith5Duplicate() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int stockSourceTableSizeBeforeSave = stockSourceRepository.findAll().size();
        int itemUnitAverageCostTableSizeBeforeSave = itemUnitAverageCostRepository.findAll().size();

        Item item = getNewItem();
        StockEntry stockEntryWithNewItemIdOne = getStockEntry().itemId(item.getId()).stockId(null).sku(null).quantity(10F);
        StockEntry stockEntryWithNewItemIdTwo = getStockEntry().itemId(item.getId()).stockId(null).sku(null).quantity(20F);
        StockEntry stockEntryWithNewItemIdThree = getStockEntry().itemId(item.getId()).stockId(null).sku(null).quantity(30F);

        stockService.stockIn(new ArrayList<StockEntry>() {{
            add(stockEntryWithNewItemIdOne);
            add(stockEntryWithNewItemIdTwo);
            add(stockEntryWithNewItemIdThree);
        }});

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave + 1);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 3);
        assertThat(stockSourceList).hasSize(stockSourceTableSizeBeforeSave + 3);
        assertThat(itemUnitAverageCostList).hasSize(itemUnitAverageCostTableSizeBeforeSave + 1);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);
    }

    //   @Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockInWith6DiffTransactionType() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int stockSourceTableSizeBeforeSave = stockSourceRepository.findAll().size();
        int itemUnitAverageCostTableSizeBeforeSave = itemUnitAverageCostRepository.findAll().size();

        StockEntry stockEntryWithDiffTransactionType = getStockEntry().stockId(stockRepository.findAll().get(2).getId()).transactionType(TransactionType.Stock_Receipt);

        stockService.stockIn(new ArrayList<StockEntry>() {{
            add(stockEntryWithDiffTransactionType);
        }});

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 1);
        assertThat(stockSourceList).hasSize(stockSourceTableSizeBeforeSave);
        assertThat(itemUnitAverageCostList).hasSize(itemUnitAverageCostTableSizeBeforeSave);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);
    }

    //@Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockOutCreateReserveStock() {
        ReserveStock reserveStockWithDiffQuantity = new ReserveStock()
            .stockId(stockRepository.findAll().get(0).getId())
            .quantity(2F)
            .reservedDate(DEFAULT_RESERVED_DATE)
            .transactionId(DEFAULT_TRANSACTION_ID)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionNo(DEFAULT_TRANSACTION_NO)
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionDate(DEFAULT_TRANSACTION_DATE);
        ReserveStock reserveStockWithDiffStockId = new ReserveStock()
            .stockId(stockRepository.findAll().get(1).getId())
            .quantity(4F)
            .reservedDate(DEFAULT_RESERVED_DATE)
            .transactionId(DEFAULT_TRANSACTION_ID)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionNo(DEFAULT_TRANSACTION_NO)
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionDate(DEFAULT_TRANSACTION_DATE);

        ReserveStock reserveStockWithDiffTransactionNo = new ReserveStock()
            .stockId(stockRepository.findAll().get(1).getId())
            .quantity(4F)
            .reservedDate(DEFAULT_RESERVED_DATE)
            .transactionId(DEFAULT_TRANSACTION_ID)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionNo("BBBBBBBB")
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionDate(DEFAULT_TRANSACTION_DATE);

        reserveStockRepository.saveAll(new ArrayList<ReserveStock>() {{
            add(reserveStockWithDiffQuantity);
            add(reserveStockWithDiffStockId);
            add(reserveStockWithDiffTransactionNo);
        }});
    }

    //@Test
    @Transactional
    @Rollback(value = false)
    public void verifyStockOutMain() throws Exception {

        int stockTableSizeBeforeSave = stockRepository.findAll().size();
        int stockFlowTableSizeBeforeSave = stockFlowRepository.findAll().size();
        int reserveStockTableSizeBeforeSave = reserveStockRepository.findAll().size();


        stockService.stockOut(DEFAULT_TRANSACTION_NO);

        List<Stock> stockList = stockRepository.findAll();
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        List<ReserveStock> stockReserveList = reserveStockRepository.findAll();

        assertThat(stockList).hasSize(stockTableSizeBeforeSave);
        assertThat(stockFlowList).hasSize(stockFlowTableSizeBeforeSave + 2);
        assertThat(stockReserveList).hasSize(reserveStockTableSizeBeforeSave - 2);

        final float[] stockQuantity = new float[1];
        final float[] stockFlowQuantity = new float[1];
        stockList.forEach(stock -> stockQuantity[0] += stock.getQuantity());
        stockFlowList.forEach(stockFlow -> {
            if (stockFlow.getFlowType().equals(FlowType.StockIn))
                stockFlowQuantity[0] += stockFlow.getQuantity();
            else
                stockFlowQuantity[0] -= stockFlow.getQuantity();
        });

        assertThat(stockQuantity[0]).isEqualTo(stockFlowQuantity[0]);

    }

    // @Test
    public void zDestroy() {
        stockRepository.deleteAll();
        stockFlowRepository.deleteAll();
        stockSourceRepository.deleteAll();
        itemUnitAverageCostRepository.deleteAll();
        reserveStockRepository.deleteAll();
    }

    // @Test
    public void yVerifyDelete() throws Exception {

        stockRepository.deleteAll();
        stockFlowRepository.deleteAll();
        stockSourceRepository.deleteAll();
        itemUnitAverageCostRepository.deleteAll();
        reserveStockRepository.deleteAll();
    }

    @Test
    @Transactional
    @Rollback(value = false)
    public void yVerifyStockIn() throws Exception {

        restStockMockMvc.perform(post("/api/external/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockEntry)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.stockFlowId").value(stockFlowRepository.findAll(Sort.by("entryDate")).get(stockFlowRepository.findAll().size() - 1).getId()));
    }

    //  @Test
    @Transactional
    public void yVerifyStockOut() throws Exception {
        Long stockId = stockRepository.findAll().get(0).getId();
        stockEntry.setStockId(stockId);
        stockEntry.setTransactionType(TransactionType.Stock_Receipt);

        restStockMockMvc.perform(post("/api/external/stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockEntry)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.stockFlowId").value(stockFlowRepository.findAll().get(stockFlowRepository.findAll().size() - 1).getId()));
    }

    @Test
    @Transactional
    public void getCurrentAvailableStock() throws Exception {

        Stock stock = stockRepository.findById(51l).get();

        restStockMockMvc.perform(get("/api/stock-value/{unitId}?consignment=" + stock.isConsignment() + "&storeId=" + stock.getStoreId() + "&itemId=" + stock.getItemId(), stock.getUnitId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].batchCode").value(hasItem(stock.getBatchNo().toString())))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(stock.getExpiryDate().toString())))
            .andExpect(jsonPath("$.[*].unitRate").value(hasItem(stock.getCost().doubleValue())))
            .andExpect(jsonPath("$.[*].unitMrp").value(hasItem(stock.getMrp().doubleValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(stock.getQuantity().doubleValue())))
            .andExpect(jsonPath("$.[*].stockValue").value(hasItem((stock.getQuantity().doubleValue()) * (stock.getCost().doubleValue()))));
    }

    @Test
    @Transactional
    public void exportStocks() throws Exception {

        Stock stock = stockRepository.findById(51l).get();

        //Response is OutputStream. Not able to validate response,So validating status and content type.
        restStockMockMvc.perform(get("/api/stock-value/_export/{unitId}?consignment=" + stock.isConsignment() + "&storeId=" + stock.getStoreId() + "&itemId=" + stock.getItemId(), stock.getUnitId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void verifyGetBatchDetails() throws Exception {

        Stock stock = stockRepository.findById(51l).get();

        restStockMockMvc.perform(get("/api/stocks/{id}/{docNumber}?batchNo=" + stock.getBatchNo() + "&code=" + itemRepository.findById(stock.getItemId()).get().getCode(), stock.getStoreId(), " "))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
