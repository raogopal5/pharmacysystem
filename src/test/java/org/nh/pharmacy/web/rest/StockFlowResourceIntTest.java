package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.domain.Locator;
import org.nh.pharmacy.domain.StockFlow;
import org.nh.pharmacy.domain.enumeration.FlowType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockFlowRepository;
import org.nh.pharmacy.service.StockFlowService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.nh.pharmacy.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockFlowResource REST controller.
 *
 * @see StockFlowResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockFlowResourceIntTest {

    private static final Long DEFAULT_ITEM_ID = 1L;
    private static final Long UPDATED_ITEM_ID = 2L;

    private static final Long DEFAULT_STOCK_ID = 1L;
    private static final Long UPDATED_STOCK_ID = 2L;

    private static final Long DEFAULT_STORE_ID = 1L;
    private static final Long UPDATED_STORE_ID = 2L;

    private static final Long DEFAULT_TRANSACTION_ID = 1L;
    private static final Long UPDATED_TRANSACTION_ID = 2L;

    private static final Long DEFAULT_LOCATOR_ID = 1L;
    private static final Long UPDATED_LOCATOR_ID = 2L;

    private static final Long DEFAULT_TRANSACTION_LINE_ID = 1L;
    private static final Long UPDATED_TRANSACTION_LINE_ID = 2L;

    private static final Long DEFAULT_UOM_ID = 1L;
    private static final Long UPDATED_UOM_ID = 2L;

    private static final String DEFAULT_SKU = "AAAAAAAAAA";
    private static final String UPDATED_SKU = "BBBBBBBBBB";

    private static final String DEFAULT_BATCH_NO = "AAAAAAAAAA";
    private static final String UPDATED_BATCH_NO = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EXPIRY_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXPIRY_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_OWNER = "AAAAAAAAAA";
    private static final String UPDATED_OWNER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_COST = BigDecimal.ONE;
    private static final BigDecimal UPDATED_COST = getBigDecimal(2F);

    private static final BigDecimal DEFAULT_MRP = BigDecimal.ONE;
    private static final BigDecimal UPDATED_MRP = getBigDecimal(2F);

    private static final FlowType DEFAULT_FLOW_TYPE = FlowType.StockIn;
    private static final FlowType UPDATED_FLOW_TYPE = FlowType.StockOut;

    private static final Float DEFAULT_QUANTITY = 1F;
    private static final Float UPDATED_QUANTITY = 2F;

    private static final LocalDateTime DEFAULT_TRANSACTION_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime UPDATED_TRANSACTION_DATE = LocalDateTime.now().minusDays(1l);


    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.GRN;
    private static final TransactionType UPDATED_TRANSACTION_TYPE = TransactionType.GRN;

    private static final Boolean DEFAULT_CONSIGNMENT = false;
    private static final Boolean UPDATED_CONSIGNMENT = true;

    private static final String DEFAULT_TRANSACTION_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AVERAGE_COST = BigDecimal.ONE;
    private static final BigDecimal UPDATED_AVERAGE_COST = getBigDecimal(2F);

    private static final BigDecimal DEFAULT_AVERAGE_COST_VALUE = BigDecimal.ONE;
    private static final BigDecimal UPDATED_AVERAGE_COST_VALUE = getBigDecimal(2F);

    private static final BigDecimal DEFAULT_COST_VALUE = BigDecimal.ONE;
    private static final BigDecimal UPDATED_COST_VALUE = getBigDecimal(2F);

    private static final LocalDateTime DEFAULT_ENTRY_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime UPDATED_ENTRY_DATE = LocalDateTime.now().minusDays(1l);

    private static final String DEFAULT_BAR_CODE = "AAAAAAAAAA";
    private static final String UPDATED_BAR_CODE = "BBBBBBBBBB";

    @Autowired
    private StockFlowRepository stockFlowRepository;

    @Autowired
    private StockFlowService stockFlowService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restStockFlowMockMvc;

    private StockFlow stockFlow;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockFlowResource stockFlowResource = new StockFlowResource(stockFlowService);
        this.restStockFlowMockMvc = MockMvcBuilders.standaloneSetup(stockFlowResource)
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
    public static StockFlow createEntity(EntityManager em) {
        StockFlow stockFlow = new StockFlow()
            .itemId(DEFAULT_ITEM_ID)
            .stockId(DEFAULT_STOCK_ID)
            .storeId(DEFAULT_STORE_ID)
            .transactionId(DEFAULT_TRANSACTION_ID)
            .locatorId(DEFAULT_LOCATOR_ID)
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .uomId(DEFAULT_UOM_ID)
            .sku(DEFAULT_SKU)
            .batchNo(DEFAULT_BATCH_NO)
            .expiryDate(DEFAULT_EXPIRY_DATE)
            .owner(DEFAULT_OWNER)
            .cost(DEFAULT_COST)
            .mrp(DEFAULT_MRP)
            .flowType(DEFAULT_FLOW_TYPE)
            .quantity(DEFAULT_QUANTITY)
            .transactionDate(DEFAULT_TRANSACTION_DATE)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .consignment(DEFAULT_CONSIGNMENT)
            .transactionNumber(DEFAULT_TRANSACTION_NUMBER)
            .averageCost(DEFAULT_AVERAGE_COST)
            .averageCostValue(DEFAULT_AVERAGE_COST_VALUE)
            .costValue(DEFAULT_COST_VALUE)
            .entryDate(DEFAULT_ENTRY_DATE)
            .barCode(DEFAULT_BAR_CODE);
        return stockFlow;
    }

    public static StockFlow createEntityIfNotExist(EntityManager em) {
        List<StockFlow> stockFlows = em.createQuery("from " + StockFlow.class.getName()).getResultList();
        StockFlow stockFlow = null;
        if (stockFlows != null && !stockFlows.isEmpty()) {
            stockFlow = stockFlows.get(0);
        } else {
            stockFlow = createEntity(em);
            em.persist(stockFlow);
            em.flush();
            stockFlow = (StockFlow) em.createQuery("from " + StockFlow.class.getName()).getResultList().get(0);
        }
        return stockFlow;
    }

    @Before
    public void initTest() {
        stockFlow = createEntity(em);
    }

    @Test
    @Transactional
    public void createStockFlow() throws Exception {
        int databaseSizeBeforeCreate = stockFlowRepository.findAll().size();

        // Create the StockFlow

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isCreated());

        // Validate the StockFlow in the database
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeCreate + 1);
        StockFlow testStockFlow = stockFlowList.get(stockFlowList.size() - 1);
        assertThat(testStockFlow.getItemId()).isEqualTo(DEFAULT_ITEM_ID);
        assertThat(testStockFlow.getStockId()).isEqualTo(DEFAULT_STOCK_ID);
        assertThat(testStockFlow.getStoreId()).isEqualTo(DEFAULT_STORE_ID);
        assertThat(testStockFlow.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
        assertThat(testStockFlow.getLocatorId()).isEqualTo(DEFAULT_LOCATOR_ID);
        assertThat(testStockFlow.getTransactionLineId()).isEqualTo(DEFAULT_TRANSACTION_LINE_ID);
        assertThat(testStockFlow.getUomId()).isEqualTo(DEFAULT_UOM_ID);
        assertThat(testStockFlow.getSku()).isEqualTo(DEFAULT_SKU);
        assertThat(testStockFlow.getBatchNo()).isEqualTo(DEFAULT_BATCH_NO);
        assertThat(testStockFlow.getExpiryDate()).isEqualTo(DEFAULT_EXPIRY_DATE);
        assertThat(testStockFlow.getOwner()).isEqualTo(DEFAULT_OWNER);
        assertThat(testStockFlow.getCost()).isEqualTo(DEFAULT_COST);
        assertThat(testStockFlow.getMrp()).isEqualTo(DEFAULT_MRP);
        assertThat(testStockFlow.getFlowType()).isEqualTo(DEFAULT_FLOW_TYPE);
        assertThat(testStockFlow.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testStockFlow.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testStockFlow.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testStockFlow.isConsignment()).isEqualTo(DEFAULT_CONSIGNMENT);
        assertThat(testStockFlow.getTransactionNumber()).isEqualTo(DEFAULT_TRANSACTION_NUMBER);
        assertThat(testStockFlow.getAverageCost()).isEqualTo(DEFAULT_AVERAGE_COST);
        assertThat(testStockFlow.getAverageCostValue()).isEqualTo(DEFAULT_AVERAGE_COST_VALUE);
        assertThat(testStockFlow.getCostValue()).isEqualTo(DEFAULT_COST_VALUE);
        assertThat(testStockFlow.getEntryDate()).isEqualTo(DEFAULT_ENTRY_DATE);
        assertThat(testStockFlow.getBarCode()).isEqualTo(DEFAULT_BAR_CODE);
    }

    @Test
    @Transactional
    public void createStockFlowWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockFlowRepository.findAll().size();

        // Create the StockFlow with an existing ID
        StockFlow existingStockFlow = new StockFlow();
        existingStockFlow.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockFlow)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkItemIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setItemId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStockIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setStockId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStoreIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setStoreId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setTransactionId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLocatorIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setLocatorId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionLineIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setTransactionLineId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUomIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setUomId(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSkuIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setSku(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOwnerIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setOwner(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCostIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setCost(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkFlowTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setFlowType(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setQuantity(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setTransactionDate(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setTransactionType(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setTransactionNumber(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAverageCostIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setAverageCost(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAverageCostValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setAverageCostValue(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCostValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockFlowRepository.findAll().size();
        // set the field null
        stockFlow.setCostValue(null);

        // Create the StockFlow, which fails.

        restStockFlowMockMvc.perform(post("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isBadRequest());

        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockFlows() throws Exception {
        // Initialize the database
        stockFlowRepository.saveAndFlush(stockFlow);

        // Get all the stockFlowList
        restStockFlowMockMvc.perform(get("/api/stock-flows?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockFlow.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(DEFAULT_ITEM_ID.intValue())))
            .andExpect(jsonPath("$.[*].stockId").value(hasItem(DEFAULT_STOCK_ID.intValue())))
            .andExpect(jsonPath("$.[*].storeId").value(hasItem(DEFAULT_STORE_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].locatorId").value(hasItem(DEFAULT_LOCATOR_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionLineId").value(hasItem(DEFAULT_TRANSACTION_LINE_ID.intValue())))
            .andExpect(jsonPath("$.[*].uomId").value(hasItem(DEFAULT_UOM_ID.intValue())))
            .andExpect(jsonPath("$.[*].sku").value(hasItem(DEFAULT_SKU.toString())))
            .andExpect(jsonPath("$.[*].batchNo").value(hasItem(DEFAULT_BATCH_NO.toString())))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].owner").value(hasItem(DEFAULT_OWNER.toString())))
            .andExpect(jsonPath("$.[*].cost").value(hasItem(DEFAULT_COST.doubleValue())))
            .andExpect(jsonPath("$.[*].mrp").value(hasItem(DEFAULT_MRP.doubleValue())))
            .andExpect(jsonPath("$.[*].flowType").value(hasItem(DEFAULT_FLOW_TYPE.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(sameInstant(DEFAULT_TRANSACTION_DATE))))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].consignment").value(hasItem(DEFAULT_CONSIGNMENT.booleanValue())))
            .andExpect(jsonPath("$.[*].transactionNumber").value(hasItem(DEFAULT_TRANSACTION_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].averageCost").value(hasItem(DEFAULT_AVERAGE_COST.doubleValue())))
            .andExpect(jsonPath("$.[*].averageCostValue").value(hasItem(DEFAULT_AVERAGE_COST_VALUE.doubleValue())))
            .andExpect(jsonPath("$.[*].costValue").value(hasItem(DEFAULT_COST_VALUE.doubleValue())))
            .andExpect(jsonPath("$.[*].entryDate").value(hasItem(DEFAULT_ENTRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].barCode").value(hasItem(DEFAULT_BAR_CODE.toString())));
    }

    @Test
    @Transactional
    public void getStockFlow() throws Exception {
        // Initialize the database
        stockFlowRepository.saveAndFlush(stockFlow);

        // Get the stockFlow
        restStockFlowMockMvc.perform(get("/api/stock-flows/{id}", stockFlow.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockFlow.getId().intValue()))
            .andExpect(jsonPath("$.itemId").value(DEFAULT_ITEM_ID.intValue()))
            .andExpect(jsonPath("$.stockId").value(DEFAULT_STOCK_ID.intValue()))
            .andExpect(jsonPath("$.storeId").value(DEFAULT_STORE_ID.intValue()))
            .andExpect(jsonPath("$.transactionId").value(DEFAULT_TRANSACTION_ID.intValue()))
            .andExpect(jsonPath("$.locatorId").value(DEFAULT_LOCATOR_ID.intValue()))
            .andExpect(jsonPath("$.transactionLineId").value(DEFAULT_TRANSACTION_LINE_ID.intValue()))
            .andExpect(jsonPath("$.uomId").value(DEFAULT_UOM_ID.intValue()))
            .andExpect(jsonPath("$.sku").value(DEFAULT_SKU.toString()))
            .andExpect(jsonPath("$.batchNo").value(DEFAULT_BATCH_NO.toString()))
            .andExpect(jsonPath("$.expiryDate").value(DEFAULT_EXPIRY_DATE.toString()))
            .andExpect(jsonPath("$.owner").value(DEFAULT_OWNER.toString()))
            .andExpect(jsonPath("$.cost").value(DEFAULT_COST.doubleValue()))
            .andExpect(jsonPath("$.mrp").value(DEFAULT_MRP.doubleValue()))
            .andExpect(jsonPath("$.flowType").value(DEFAULT_FLOW_TYPE.toString()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.transactionDate").value(sameInstant(DEFAULT_TRANSACTION_DATE)))
            .andExpect(jsonPath("$.transactionType").value(DEFAULT_TRANSACTION_TYPE.toString()))
            .andExpect(jsonPath("$.consignment").value(DEFAULT_CONSIGNMENT.booleanValue()))
            .andExpect(jsonPath("$.transactionNumber").value(DEFAULT_TRANSACTION_NUMBER.toString()))
            .andExpect(jsonPath("$.averageCost").value(DEFAULT_AVERAGE_COST.doubleValue()))
            .andExpect(jsonPath("$.averageCostValue").value(DEFAULT_AVERAGE_COST_VALUE.doubleValue()))
            .andExpect(jsonPath("$.costValue").value(DEFAULT_COST_VALUE.doubleValue()))
            .andExpect(jsonPath("$.entryDate").value(DEFAULT_ENTRY_DATE.toString()))
            .andExpect(jsonPath("$.barCode").value(DEFAULT_BAR_CODE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingStockFlow() throws Exception {
        // Get the stockFlow
        restStockFlowMockMvc.perform(get("/api/stock-flows/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStockFlow() throws Exception {
        // Initialize the database
        stockFlowService.save(stockFlow);

        int databaseSizeBeforeUpdate = stockFlowRepository.findAll().size();

        // Update the stockFlow
        StockFlow updatedStockFlow = stockFlowRepository.findById(stockFlow.getId()).get();
        updatedStockFlow
            .itemId(UPDATED_ITEM_ID)
            .stockId(UPDATED_STOCK_ID)
            .storeId(UPDATED_STORE_ID)
            .transactionId(UPDATED_TRANSACTION_ID)
            .locatorId(UPDATED_LOCATOR_ID)
            .transactionLineId(UPDATED_TRANSACTION_LINE_ID)
            .uomId(UPDATED_UOM_ID)
            .sku(UPDATED_SKU)
            .batchNo(UPDATED_BATCH_NO)
            .expiryDate(UPDATED_EXPIRY_DATE)
            .owner(UPDATED_OWNER)
            .cost(UPDATED_COST)
            .mrp(UPDATED_MRP)
            .flowType(UPDATED_FLOW_TYPE)
            .quantity(UPDATED_QUANTITY)
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .consignment(UPDATED_CONSIGNMENT)
            .transactionNumber(UPDATED_TRANSACTION_NUMBER)
            .averageCost(UPDATED_AVERAGE_COST)
            .averageCostValue(UPDATED_AVERAGE_COST_VALUE)
            .costValue(UPDATED_COST_VALUE)
            .entryDate(UPDATED_ENTRY_DATE)
            .barCode(UPDATED_BAR_CODE);

        restStockFlowMockMvc.perform(put("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockFlow)))
            .andExpect(status().isOk());

        // Validate the StockFlow in the database
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeUpdate);
        StockFlow testStockFlow = stockFlowList.get(stockFlowList.size() - 1);
        assertThat(testStockFlow.getItemId()).isEqualTo(UPDATED_ITEM_ID);
        assertThat(testStockFlow.getStockId()).isEqualTo(UPDATED_STOCK_ID);
        assertThat(testStockFlow.getStoreId()).isEqualTo(UPDATED_STORE_ID);
        assertThat(testStockFlow.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
        assertThat(testStockFlow.getLocatorId()).isEqualTo(UPDATED_LOCATOR_ID);
        assertThat(testStockFlow.getTransactionLineId()).isEqualTo(UPDATED_TRANSACTION_LINE_ID);
        assertThat(testStockFlow.getUomId()).isEqualTo(UPDATED_UOM_ID);
        assertThat(testStockFlow.getSku()).isEqualTo(UPDATED_SKU);
        assertThat(testStockFlow.getBatchNo()).isEqualTo(UPDATED_BATCH_NO);
        assertThat(testStockFlow.getExpiryDate()).isEqualTo(UPDATED_EXPIRY_DATE);
        assertThat(testStockFlow.getOwner()).isEqualTo(UPDATED_OWNER);
        assertThat(testStockFlow.getCost()).isEqualTo(UPDATED_COST);
        assertThat(testStockFlow.getMrp()).isEqualTo(UPDATED_MRP);
        assertThat(testStockFlow.getFlowType()).isEqualTo(UPDATED_FLOW_TYPE);
        assertThat(testStockFlow.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testStockFlow.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
        assertThat(testStockFlow.getTransactionType()).isEqualTo(UPDATED_TRANSACTION_TYPE);
        assertThat(testStockFlow.isConsignment()).isEqualTo(UPDATED_CONSIGNMENT);
        assertThat(testStockFlow.getTransactionNumber()).isEqualTo(UPDATED_TRANSACTION_NUMBER);
        assertThat(testStockFlow.getAverageCost()).isEqualTo(UPDATED_AVERAGE_COST);
        assertThat(testStockFlow.getAverageCostValue()).isEqualTo(UPDATED_AVERAGE_COST_VALUE);
        assertThat(testStockFlow.getCostValue()).isEqualTo(UPDATED_COST_VALUE);
        assertThat(testStockFlow.getEntryDate()).isEqualTo(UPDATED_ENTRY_DATE);
        assertThat(testStockFlow.getBarCode()).isEqualTo(UPDATED_BAR_CODE);
    }

    @Test
    @Transactional
    public void updateNonExistingStockFlow() throws Exception {
        int databaseSizeBeforeUpdate = stockFlowRepository.findAll().size();

        // Create the StockFlow

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockFlowMockMvc.perform(put("/api/stock-flows")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockFlow)))
            .andExpect(status().isCreated());

        // Validate the StockFlow in the database
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockFlow() throws Exception {
        // Initialize the database
        stockFlowService.save(stockFlow);

        int databaseSizeBeforeDelete = stockFlowRepository.findAll().size();

        // Get the stockFlow
        restStockFlowMockMvc.perform(delete("/api/stock-flows/{id}", stockFlow.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<StockFlow> stockFlowList = stockFlowRepository.findAll();
        assertThat(stockFlowList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockFlow.class);
    }

    @Test
    @Transactional
    public void getStockTransactions() throws Exception {

        HealthcareServiceCenter hsc = HealthcareServiceCenterResourceIntTest.createEntityIfNotExist(em);
        Item item = ItemResourceIntTest.createEntityIfNotExist(em);
        Locator locator = LocatorResourceIntTest.createEntityIfNotExist(em);

        StockFlow stockFlow = createEntity(em);
        stockFlow.setTransactionDate(LocalDateTime.parse("2017-08-29 11:13:44.94", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS")));
        stockFlow.setStoreId(hsc.getId());
        stockFlow.setItemId(item.getId());
        stockFlow.setLocatorId(locator.getId());
        stockFlowRepository.saveAndFlush(stockFlow);

        restStockFlowMockMvc.perform(get("/api/stock-ledger/{entryDate}/{consignment}/{unitId}/{storeId}?itemId=" + stockFlow.getItemId(), "2017-09-05", stockFlow.isConsignment(), hsc.getPartOf().getId(), stockFlow.getStoreId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].batchCode").value(hasItem(DEFAULT_BATCH_NO.toString())))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].mrp").value(hasItem(DEFAULT_MRP.doubleValue())))
            .andExpect(jsonPath("$.[*].cost").value(hasItem(DEFAULT_COST.doubleValue())))
            .andExpect(jsonPath("$.[*].averageCost").value(hasItem(DEFAULT_AVERAGE_COST.doubleValue())))
            .andExpect(jsonPath("$.[*].costValue").value(hasItem(DEFAULT_COST_VALUE.doubleValue())))
            .andExpect(jsonPath("$.[*].averageCostValue").value(hasItem(DEFAULT_AVERAGE_COST_VALUE.doubleValue())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_FLOW_TYPE.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem("2017-08-29 11:13:44")))
            .andExpect(jsonPath("$.[*].documentType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].refDocNo").value(hasItem(DEFAULT_TRANSACTION_NUMBER.toString())));
    }

    @Test
    @Transactional
    public void exportStocks() throws Exception {
        HealthcareServiceCenter hsc = HealthcareServiceCenterResourceIntTest.createEntityIfNotExist(em);
        Item item = ItemResourceIntTest.createEntityIfNotExist(em);
        Locator locator = LocatorResourceIntTest.createEntityIfNotExist(em);

        StockFlow stockFlow = createEntity(em);
        stockFlow.setTransactionDate(LocalDateTime.parse("2017-08-29 11:13:44.94", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS")));
        stockFlow.setStoreId(hsc.getId());
        stockFlow.setItemId(item.getId());
        stockFlow.setLocatorId(locator.getId());
        stockFlowRepository.saveAndFlush(stockFlow);


        //Response is OutputStream. Not able to validate response,So validating status and content type.
        restStockFlowMockMvc.perform(get("/api/stock-ledger/_export/{entryDate}/{consignment}/{unitId}/{storeId}?itemId=" + stockFlow.getItemId(), "2017-09-05", stockFlow.isConsignment(), hsc.getPartOf().getId(), stockFlow.getStoreId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
