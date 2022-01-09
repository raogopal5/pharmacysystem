package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.StockTransit;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockTransitRepository;
import org.nh.pharmacy.service.StockTransitService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.pharmacy.web.rest.TestUtil.createFormattingConversionService;
import static org.nh.pharmacy.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockTransitResource REST controller.
 *
 * @see StockTransitResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockTransitResourceIntTest {

    private static final Long DEFAULT_STOCK_ID = 1L;
    private static final Long UPDATED_STOCK_ID = 2L;

    private static final Float DEFAULT_QUANTITY = 1F;
    private static final Float UPDATED_QUANTITY = 2F;

    private static final LocalDateTime DEFAULT_TRANSIT_DATE = LocalDateTime.now();
    private static final LocalDateTime UPDATED_TRANSIT_DATE = LocalDateTime.now();

    private static final Long DEFAULT_TRANSACTION_ID = 1L;
    private static final Long UPDATED_TRANSACTION_ID = 2L;

    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.GRN;
    private static final TransactionType UPDATED_TRANSACTION_TYPE = TransactionType.GRN;

    private static final String DEFAULT_TRANSACTION_NO = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_NO = "BBBBBBBBBB";

    private static final Long DEFAULT_TRANSACTION_LINE_ID = 1L;
    private static final Long UPDATED_TRANSACTION_LINE_ID = 2L;

    private static final LocalDateTime DEFAULT_TRANSACTION_DATE = LocalDateTime.now();
    private static final LocalDateTime UPDATED_TRANSACTION_DATE =LocalDateTime.now();

    private static final Float DEFAULT_TRANSIT_QUANTITY = 1F;
    private static final Float UPDATED_TRANSIT_QUANTITY = 2F;

    private static final Float DEFAULT_PENDING_QUANTITY = 1F;
    private static final Float UPDATED_PENDING_QUANTITY = 2F;

    @Autowired
    private StockTransitRepository stockTransitRepository;

    @Autowired
    private StockTransitService stockTransitService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restStockTransitMockMvc;

    private StockTransit stockTransit;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final StockTransitResource stockTransitResource = new StockTransitResource(stockTransitService);
        this.restStockTransitMockMvc = MockMvcBuilders.standaloneSetup(stockTransitResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StockTransit createEntity(EntityManager em) {
        StockTransit stockTransit = new StockTransit()
            .stockId(DEFAULT_STOCK_ID)
            .quantity(DEFAULT_QUANTITY)
            .transitDate(DEFAULT_TRANSIT_DATE)
            .transactionId(DEFAULT_TRANSACTION_ID)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionNo(DEFAULT_TRANSACTION_NO)
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionDate(DEFAULT_TRANSACTION_DATE)
            .pendingQuantity(DEFAULT_PENDING_QUANTITY);
        return stockTransit;
    }

    @Before
    public void initTest() {
        stockTransit = createEntity(em);
    }

    @Test
    @Transactional
    public void createStockTransit() throws Exception {
        int databaseSizeBeforeCreate = stockTransitRepository.findAll().size();

        // Create the StockTransit
        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isCreated());

        // Validate the StockTransit in the database
        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeCreate + 1);
        StockTransit testStockTransit = stockTransitList.get(stockTransitList.size() - 1);
        assertThat(testStockTransit.getStockId()).isEqualTo(DEFAULT_STOCK_ID);
        assertThat(testStockTransit.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testStockTransit.getTransitDate()).isEqualTo(DEFAULT_TRANSIT_DATE);
        assertThat(testStockTransit.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
        assertThat(testStockTransit.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testStockTransit.getTransactionNo()).isEqualTo(DEFAULT_TRANSACTION_NO);
        assertThat(testStockTransit.getTransactionLineId()).isEqualTo(DEFAULT_TRANSACTION_LINE_ID);
        assertThat(testStockTransit.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testStockTransit.getPendingQuantity()).isEqualTo(DEFAULT_PENDING_QUANTITY);
    }

    @Test
    @Transactional
    public void createStockTransitWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockTransitRepository.findAll().size();

        // Create the StockTransit with an existing ID
        stockTransit.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        // Validate the StockTransit in the database
        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkStockIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setStockId(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setQuantity(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransitDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setTransitDate(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setTransactionId(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setTransactionType(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionNoIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setTransactionNo(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionLineIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setTransactionLineId(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setTransactionDate(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransitQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPendingQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockTransitRepository.findAll().size();
        // set the field null
        stockTransit.setPendingQuantity(null);

        // Create the StockTransit, which fails.

        restStockTransitMockMvc.perform(post("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isBadRequest());

        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockTransits() throws Exception {
        // Initialize the database
        stockTransitRepository.saveAndFlush(stockTransit);

        // Get all the stockTransitList
        restStockTransitMockMvc.perform(get("/api/stock-transits?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockTransit.getId().intValue())))
            .andExpect(jsonPath("$.[*].stockId").value(hasItem(DEFAULT_STOCK_ID.intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].transitDate").value(hasItem(sameInstant(DEFAULT_TRANSIT_DATE))))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].transactionNo").value(hasItem(DEFAULT_TRANSACTION_NO.toString())))
            .andExpect(jsonPath("$.[*].transactionLineId").value(hasItem(DEFAULT_TRANSACTION_LINE_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(sameInstant(DEFAULT_TRANSACTION_DATE))))
            .andExpect(jsonPath("$.[*].transitQuantity").value(hasItem(DEFAULT_TRANSIT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].pendingQuantity").value(hasItem(DEFAULT_PENDING_QUANTITY.doubleValue())));
    }

    @Test
    @Transactional
    public void getStockTransit() throws Exception {
        // Initialize the database
        stockTransitRepository.saveAndFlush(stockTransit);

        // Get the stockTransit
        restStockTransitMockMvc.perform(get("/api/stock-transits/{id}", stockTransit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockTransit.getId().intValue()))
            .andExpect(jsonPath("$.stockId").value(DEFAULT_STOCK_ID.intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.transitDate").value(sameInstant(DEFAULT_TRANSIT_DATE)))
            .andExpect(jsonPath("$.transactionId").value(DEFAULT_TRANSACTION_ID.intValue()))
            .andExpect(jsonPath("$.transactionType").value(DEFAULT_TRANSACTION_TYPE.toString()))
            .andExpect(jsonPath("$.transactionNo").value(DEFAULT_TRANSACTION_NO.toString()))
            .andExpect(jsonPath("$.transactionLineId").value(DEFAULT_TRANSACTION_LINE_ID.intValue()))
            .andExpect(jsonPath("$.transactionDate").value(sameInstant(DEFAULT_TRANSACTION_DATE)))
            .andExpect(jsonPath("$.transitQuantity").value(DEFAULT_TRANSIT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.pendingQuantity").value(DEFAULT_PENDING_QUANTITY.doubleValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStockTransit() throws Exception {
        // Get the stockTransit
        restStockTransitMockMvc.perform(get("/api/stock-transits/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStockTransit() throws Exception {
        // Initialize the database
        stockTransitService.save(stockTransit);

        int databaseSizeBeforeUpdate = stockTransitRepository.findAll().size();

        // Update the stockTransit
        StockTransit updatedStockTransit = stockTransitRepository.findById(stockTransit.getId()).get();
        updatedStockTransit
            .stockId(UPDATED_STOCK_ID)
            .quantity(UPDATED_QUANTITY)
            .transitDate(UPDATED_TRANSIT_DATE)
            .transactionId(UPDATED_TRANSACTION_ID)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .transactionNo(UPDATED_TRANSACTION_NO)
            .transactionLineId(UPDATED_TRANSACTION_LINE_ID)
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .pendingQuantity(UPDATED_PENDING_QUANTITY);

        restStockTransitMockMvc.perform(put("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockTransit)))
            .andExpect(status().isOk());

        // Validate the StockTransit in the database
        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeUpdate);
        StockTransit testStockTransit = stockTransitList.get(stockTransitList.size() - 1);
        assertThat(testStockTransit.getStockId()).isEqualTo(UPDATED_STOCK_ID);
        assertThat(testStockTransit.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testStockTransit.getTransitDate()).isEqualTo(UPDATED_TRANSIT_DATE);
        assertThat(testStockTransit.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
        assertThat(testStockTransit.getTransactionType()).isEqualTo(UPDATED_TRANSACTION_TYPE);
        assertThat(testStockTransit.getTransactionNo()).isEqualTo(UPDATED_TRANSACTION_NO);
        assertThat(testStockTransit.getTransactionLineId()).isEqualTo(UPDATED_TRANSACTION_LINE_ID);
        assertThat(testStockTransit.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
        assertThat(testStockTransit.getPendingQuantity()).isEqualTo(UPDATED_PENDING_QUANTITY);
    }

    @Test
    @Transactional
    public void updateNonExistingStockTransit() throws Exception {
        int databaseSizeBeforeUpdate = stockTransitRepository.findAll().size();

        // Create the StockTransit

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockTransitMockMvc.perform(put("/api/stock-transits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockTransit)))
            .andExpect(status().isCreated());

        // Validate the StockTransit in the database
        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockTransit() throws Exception {
        // Initialize the database
        stockTransitService.save(stockTransit);

        int databaseSizeBeforeDelete = stockTransitRepository.findAll().size();

        // Get the stockTransit
        restStockTransitMockMvc.perform(delete("/api/stock-transits/{id}", stockTransit.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<StockTransit> stockTransitList = stockTransitRepository.findAll();
        assertThat(stockTransitList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockTransit.class);
        StockTransit stockTransit1 = new StockTransit();
        stockTransit1.setId(1L);
        StockTransit stockTransit2 = new StockTransit();
        stockTransit2.setId(stockTransit1.getId());
        assertThat(stockTransit1).isEqualTo(stockTransit2);
        stockTransit2.setId(2L);
        assertThat(stockTransit1).isNotEqualTo(stockTransit2);
        stockTransit1.setId(null);
        assertThat(stockTransit1).isNotEqualTo(stockTransit2);
    }
}
