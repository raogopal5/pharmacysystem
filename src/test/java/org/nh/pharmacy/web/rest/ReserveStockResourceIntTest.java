package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.ReserveStockRepository;
import org.nh.pharmacy.service.ReserveStockService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ReserveStockResource REST controller.
 *
 * @see ReserveStockResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ReserveStockResourceIntTest {

    private static final Long DEFAULT_STOCK_ID = 1L;
    private static final Long UPDATED_STOCK_ID = 2L;

    private static final Float DEFAULT_QUANTITY = 1F;
    private static final Float UPDATED_QUANTITY = 2F;

    private static final LocalDateTime DEFAULT_RESERVED_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime UPDATED_RESERVED_DATE = LocalDateTime.now();

    private static final Long DEFAULT_TRANSACTION_ID = 1L;
    private static final Long UPDATED_TRANSACTION_ID = 2L;

    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.GRN;
    private static final TransactionType UPDATED_TRANSACTION_TYPE = TransactionType.GRN;

    private static final String DEFAULT_TRANSACTION_NO = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_NO = "BBBBBBBBBB";

    private static final Long DEFAULT_TRANSACTION_LINE_ID = 1L;
    private static final Long UPDATED_TRANSACTION_LINE_ID = 2L;

    private static final LocalDateTime DEFAULT_TRANSACTION_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime UPDATED_TRANSACTION_DATE = LocalDateTime.now();

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private ReserveStockService reserveStockService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restReserveStockMockMvc;

    private ReserveStock reserveStock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReserveStockResource reserveStockResource = new ReserveStockResource(reserveStockService);
        this.restReserveStockMockMvc = MockMvcBuilders.standaloneSetup(reserveStockResource)
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
    public static ReserveStock createEntity(EntityManager em) {
        ReserveStock reserveStock = new ReserveStock()
            .stockId(DEFAULT_STOCK_ID)
            .quantity(DEFAULT_QUANTITY)
            .reservedDate(DEFAULT_RESERVED_DATE)
            .transactionId(DEFAULT_TRANSACTION_ID)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionNo(DEFAULT_TRANSACTION_NO)
            .transactionLineId(DEFAULT_TRANSACTION_LINE_ID)
            .transactionDate(DEFAULT_TRANSACTION_DATE);
        return reserveStock;
    }

    @Before
    public void initTest() {
        reserveStock = createEntity(em);
    }

    @Test
    @Transactional
    public void createReserveStock() throws Exception {
        int databaseSizeBeforeCreate = reserveStockRepository.findAll().size();

        // Create the ReserveStock

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isCreated());

        // Validate the ReserveStock in the database
        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeCreate + 1);
        ReserveStock testReserveStock = reserveStockList.get(reserveStockList.size() - 1);
        assertThat(testReserveStock.getStockId()).isEqualTo(DEFAULT_STOCK_ID);
        assertThat(testReserveStock.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testReserveStock.getReservedDate()).isEqualTo(DEFAULT_RESERVED_DATE);
        assertThat(testReserveStock.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
        assertThat(testReserveStock.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testReserveStock.getTransactionNo()).isEqualTo(DEFAULT_TRANSACTION_NO);
        assertThat(testReserveStock.getTransactionLineId()).isEqualTo(DEFAULT_TRANSACTION_LINE_ID);
        assertThat(testReserveStock.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
    }

    @Test
    @Transactional
    public void createReserveStockWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = reserveStockRepository.findAll().size();

        // Create the ReserveStock with an existing ID
        ReserveStock existingReserveStock = new ReserveStock();
        existingReserveStock.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingReserveStock)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkStockIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setStockId(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setQuantity(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkReservedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setReservedDate(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setTransactionId(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setTransactionType(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionNoIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setTransactionNo(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionLineIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setTransactionLineId(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = reserveStockRepository.findAll().size();
        // set the field null
        reserveStock.setTransactionDate(null);

        // Create the ReserveStock, which fails.

        restReserveStockMockMvc.perform(post("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isBadRequest());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllReserveStocks() throws Exception {
        // Initialize the database
        reserveStockRepository.saveAndFlush(reserveStock);

        // Get all the reserveStockList
        restReserveStockMockMvc.perform(get("/api/reserve-stocks?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reserveStock.getId().intValue())))
            .andExpect(jsonPath("$.[*].stockId").value(hasItem(DEFAULT_STOCK_ID.intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].reservedDate").value(hasItem(DEFAULT_RESERVED_DATE.toString())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].transactionNo").value(hasItem(DEFAULT_TRANSACTION_NO.toString())))
            .andExpect(jsonPath("$.[*].transactionLineId").value(hasItem(DEFAULT_TRANSACTION_LINE_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())));
    }

    @Test
    @Transactional
    public void getReserveStock() throws Exception {
        // Initialize the database
        reserveStockRepository.saveAndFlush(reserveStock);

        // Get the reserveStock
        restReserveStockMockMvc.perform(get("/api/reserve-stocks/{id}", reserveStock.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reserveStock.getId().intValue()))
            .andExpect(jsonPath("$.stockId").value(DEFAULT_STOCK_ID.intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.reservedDate").value(DEFAULT_RESERVED_DATE.toString()))
            .andExpect(jsonPath("$.transactionId").value(DEFAULT_TRANSACTION_ID.intValue()))
            .andExpect(jsonPath("$.transactionType").value(DEFAULT_TRANSACTION_TYPE.toString()))
            .andExpect(jsonPath("$.transactionNo").value(DEFAULT_TRANSACTION_NO.toString()))
            .andExpect(jsonPath("$.transactionLineId").value(DEFAULT_TRANSACTION_LINE_ID.intValue()))
            .andExpect(jsonPath("$.transactionDate").value(DEFAULT_TRANSACTION_DATE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingReserveStock() throws Exception {
        // Get the reserveStock
        restReserveStockMockMvc.perform(get("/api/reserve-stocks/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReserveStock() throws Exception {
        // Initialize the database
        reserveStockService.save(reserveStock);

        int databaseSizeBeforeUpdate = reserveStockRepository.findAll().size();

        // Update the reserveStock
        ReserveStock updatedReserveStock = reserveStockRepository.findById(reserveStock.getId()).get();
        updatedReserveStock
            .stockId(UPDATED_STOCK_ID)
            .quantity(UPDATED_QUANTITY)
            .reservedDate(UPDATED_RESERVED_DATE)
            .transactionId(UPDATED_TRANSACTION_ID)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .transactionNo(UPDATED_TRANSACTION_NO)
            .transactionLineId(UPDATED_TRANSACTION_LINE_ID)
            .transactionDate(UPDATED_TRANSACTION_DATE);

        restReserveStockMockMvc.perform(put("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedReserveStock)))
            .andExpect(status().isOk());

        // Validate the ReserveStock in the database
        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeUpdate);
        ReserveStock testReserveStock = reserveStockList.get(reserveStockList.size() - 1);
        assertThat(testReserveStock.getStockId()).isEqualTo(UPDATED_STOCK_ID);
        assertThat(testReserveStock.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testReserveStock.getReservedDate()).isEqualTo(UPDATED_RESERVED_DATE);
        assertThat(testReserveStock.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
        assertThat(testReserveStock.getTransactionType()).isEqualTo(UPDATED_TRANSACTION_TYPE);
        assertThat(testReserveStock.getTransactionNo()).isEqualTo(UPDATED_TRANSACTION_NO);
        assertThat(testReserveStock.getTransactionLineId()).isEqualTo(UPDATED_TRANSACTION_LINE_ID);
        assertThat(testReserveStock.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingReserveStock() throws Exception {
        int databaseSizeBeforeUpdate = reserveStockRepository.findAll().size();

        // Create the ReserveStock

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restReserveStockMockMvc.perform(put("/api/reserve-stocks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(reserveStock)))
            .andExpect(status().isCreated());

        // Validate the ReserveStock in the database
        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteReserveStock() throws Exception {
        // Initialize the database
        reserveStockService.save(reserveStock);

        int databaseSizeBeforeDelete = reserveStockRepository.findAll().size();

        // Get the reserveStock
        restReserveStockMockMvc.perform(delete("/api/reserve-stocks/{id}", reserveStock.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        assertThat(reserveStockList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReserveStock.class);
    }
}
