package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.StockSourceHeader;
import org.nh.pharmacy.repository.StockSourceHeaderRepository;
import org.nh.pharmacy.repository.search.StockSourceHeaderSearchRepository;
import org.nh.pharmacy.service.StockSourceHeaderService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
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
import static org.nh.pharmacy.web.rest.TestUtil.createFormattingConversionService;
import static org.nh.pharmacy.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockSourceHeaderResource REST controller.
 *
 * @see StockSourceHeaderResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockSourceHeaderResourceIntTest {

    private static final String DEFAULT_UNIT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_UNIT_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final LocalDateTime DEFAULT_TRANSACTION_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime UPDATED_TRANSACTION_DATE = LocalDateTime.now().minusDays(1l);

    @Autowired
    private StockSourceHeaderRepository stockSourceHeaderRepository;

    @Autowired
    private StockSourceHeaderService stockSourceHeaderService;

    @Autowired
    private StockSourceHeaderSearchRepository stockSourceHeaderSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restStockSourceHeaderMockMvc;

    private StockSourceHeader stockSourceHeader;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final StockSourceHeaderResource stockSourceHeaderResource = new StockSourceHeaderResource(stockSourceHeaderService,stockSourceHeaderRepository,stockSourceHeaderSearchRepository,applicationProperties);
        this.restStockSourceHeaderMockMvc = MockMvcBuilders.standaloneSetup(stockSourceHeaderResource)
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
    public static StockSourceHeader createEntity(EntityManager em) {
        StockSourceHeader stockSourceHeader = new StockSourceHeader()
            .unitCode(DEFAULT_UNIT_CODE)
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .transactionDate(DEFAULT_TRANSACTION_DATE);
        return stockSourceHeader;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(StockSourceHeader.class))
            elasticsearchTemplate.createIndex(StockSourceHeader.class);
        stockSourceHeaderSearchRepository.deleteAll();
        stockSourceHeader = createEntity(em);
    }

    @Test
    @Transactional
    public void createStockSourceHeader() throws Exception {
        int databaseSizeBeforeCreate = stockSourceHeaderRepository.findAll().size();

        // Create the StockSourceHeader
        restStockSourceHeaderMockMvc.perform(post("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSourceHeader)))
            .andExpect(status().isCreated());

        // Validate the StockSourceHeader in the database
        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeCreate + 1);
        StockSourceHeader testStockSourceHeader = stockSourceHeaderList.get(stockSourceHeaderList.size() - 1);
        assertThat(testStockSourceHeader.getUnitCode()).isEqualTo(DEFAULT_UNIT_CODE);
        assertThat(testStockSourceHeader.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT_NUMBER);
        assertThat(testStockSourceHeader.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);

        // Validate the StockSourceHeader in Elasticsearch
        StockSourceHeader stockSourceHeaderEs = stockSourceHeaderSearchRepository.findById(testStockSourceHeader.getId()).get();
        assertThat(testStockSourceHeader.getTransactionDate()).isEqualTo(testStockSourceHeader.getTransactionDate());
        assertThat(stockSourceHeaderEs).isEqualToIgnoringGivenFields(testStockSourceHeader, "transactionDate");
    }

    @Test
    @Transactional
    public void createStockSourceHeaderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockSourceHeaderRepository.findAll().size();

        // Create the StockSourceHeader with an existing ID
        stockSourceHeader.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockSourceHeaderMockMvc.perform(post("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSourceHeader)))
            .andExpect(status().isBadRequest());

        // Validate the StockSourceHeader in the database
        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkUnitCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceHeaderRepository.findAll().size();
        // set the field null
        stockSourceHeader.setUnitCode(null);

        // Create the StockSourceHeader, which fails.

        restStockSourceHeaderMockMvc.perform(post("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSourceHeader)))
            .andExpect(status().isBadRequest());

        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDocumentNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceHeaderRepository.findAll().size();
        // set the field null
        stockSourceHeader.setDocumentNumber(null);

        // Create the StockSourceHeader, which fails.

        restStockSourceHeaderMockMvc.perform(post("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSourceHeader)))
            .andExpect(status().isBadRequest());

        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceHeaderRepository.findAll().size();
        // set the field null
        stockSourceHeader.setTransactionDate(null);

        // Create the StockSourceHeader, which fails.

        restStockSourceHeaderMockMvc.perform(post("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSourceHeader)))
            .andExpect(status().isBadRequest());

        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockSourceHeaders() throws Exception {
        // Initialize the database
        stockSourceHeaderRepository.saveAndFlush(stockSourceHeader);

        // Get all the stockSourceHeaderList
        restStockSourceHeaderMockMvc.perform(get("/api/stock-source-headers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockSourceHeader.getId().intValue())))
            .andExpect(jsonPath("$.[*].unitCode").value(hasItem(DEFAULT_UNIT_CODE.toString())))
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(DEFAULT_DOCUMENT_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(sameInstant(DEFAULT_TRANSACTION_DATE))));
    }

    @Test
    @Transactional
    public void getStockSourceHeader() throws Exception {
        // Initialize the database
        stockSourceHeaderRepository.saveAndFlush(stockSourceHeader);

        // Get the stockSourceHeader
        restStockSourceHeaderMockMvc.perform(get("/api/stock-source-headers/{id}", stockSourceHeader.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockSourceHeader.getId().intValue()))
            .andExpect(jsonPath("$.unitCode").value(DEFAULT_UNIT_CODE.toString()))
            .andExpect(jsonPath("$.documentNumber").value(DEFAULT_DOCUMENT_NUMBER.toString()))
            .andExpect(jsonPath("$.transactionDate").value(sameInstant(DEFAULT_TRANSACTION_DATE)));
    }

    @Test
    @Transactional
    public void getNonExistingStockSourceHeader() throws Exception {
        // Get the stockSourceHeader
        restStockSourceHeaderMockMvc.perform(get("/api/stock-source-headers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStockSourceHeader() throws Exception {
        // Initialize the database
        stockSourceHeaderService.save(stockSourceHeader);

        int databaseSizeBeforeUpdate = stockSourceHeaderRepository.findAll().size();

        // Update the stockSourceHeader
        StockSourceHeader updatedStockSourceHeader = stockSourceHeaderRepository.findById(stockSourceHeader.getId()).get();
        // Disconnect from session so that the updates on updatedStockSourceHeader are not directly saved in db
        em.detach(updatedStockSourceHeader);
        updatedStockSourceHeader
            .unitCode(UPDATED_UNIT_CODE)
            .documentNumber(UPDATED_DOCUMENT_NUMBER)
            .transactionDate(UPDATED_TRANSACTION_DATE);

        restStockSourceHeaderMockMvc.perform(put("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockSourceHeader)))
            .andExpect(status().isOk());

        // Validate the StockSourceHeader in the database
        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeUpdate);
        StockSourceHeader testStockSourceHeader = stockSourceHeaderList.get(stockSourceHeaderList.size() - 1);
        assertThat(testStockSourceHeader.getUnitCode()).isEqualTo(UPDATED_UNIT_CODE);
        assertThat(testStockSourceHeader.getDocumentNumber()).isEqualTo(UPDATED_DOCUMENT_NUMBER);
        assertThat(testStockSourceHeader.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);

        // Validate the StockSourceHeader in Elasticsearch
        StockSourceHeader stockSourceHeaderEs = stockSourceHeaderSearchRepository.findById(testStockSourceHeader.getId()).get();
        assertThat(testStockSourceHeader.getTransactionDate()).isEqualTo(testStockSourceHeader.getTransactionDate());
        assertThat(stockSourceHeaderEs).isEqualToIgnoringGivenFields(testStockSourceHeader, "transactionDate");
    }

    @Test
    @Transactional
    public void updateNonExistingStockSourceHeader() throws Exception {
        int databaseSizeBeforeUpdate = stockSourceHeaderRepository.findAll().size();

        // Create the StockSourceHeader

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockSourceHeaderMockMvc.perform(put("/api/stock-source-headers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSourceHeader)))
            .andExpect(status().isCreated());

        // Validate the StockSourceHeader in the database
        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockSourceHeader() throws Exception {
        // Initialize the database
        stockSourceHeaderService.save(stockSourceHeader);

        int databaseSizeBeforeDelete = stockSourceHeaderRepository.findAll().size();

        // Get the stockSourceHeader
        restStockSourceHeaderMockMvc.perform(delete("/api/stock-source-headers/{id}", stockSourceHeader.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockSourceHeaderExistsInEs = stockSourceHeaderSearchRepository.existsById(stockSourceHeader.getId());
        assertThat(stockSourceHeaderExistsInEs).isFalse();

        // Validate the database is empty
        List<StockSourceHeader> stockSourceHeaderList = stockSourceHeaderRepository.findAll();
        assertThat(stockSourceHeaderList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockSourceHeader() throws Exception {
        // Initialize the database
        stockSourceHeaderService.save(stockSourceHeader);

        // Search the stockSourceHeader
        restStockSourceHeaderMockMvc.perform(get("/api/_search/stock-source-headers?query=id:" + stockSourceHeader.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockSourceHeader.getId().intValue())))
            .andExpect(jsonPath("$.[*].unitCode").value(hasItem(DEFAULT_UNIT_CODE.toString())))
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(DEFAULT_DOCUMENT_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(sameInstant(DEFAULT_TRANSACTION_DATE))));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockSourceHeader.class);
        StockSourceHeader stockSourceHeader1 = new StockSourceHeader();
        stockSourceHeader1.setId(1L);
        StockSourceHeader stockSourceHeader2 = new StockSourceHeader();
        stockSourceHeader2.setId(stockSourceHeader1.getId());
        assertThat(stockSourceHeader1).isEqualTo(stockSourceHeader2);
        stockSourceHeader2.setId(2L);
        assertThat(stockSourceHeader1).isNotEqualTo(stockSourceHeader2);
        stockSourceHeader1.setId(null);
        assertThat(stockSourceHeader1).isNotEqualTo(stockSourceHeader2);
    }
}
