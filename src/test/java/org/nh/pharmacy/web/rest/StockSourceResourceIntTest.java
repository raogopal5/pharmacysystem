package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.domain.enumeration.FormatType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.dto.BarcodeConfiguration;
import org.nh.pharmacy.repository.HealthcareServiceCenterRepository;
import org.nh.pharmacy.repository.OrganizationRepository;
import org.nh.pharmacy.repository.StockSourceRepository;
import org.nh.pharmacy.service.StockSourceService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockSourceResource REST controller.
 *
 * @see StockSourceResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockSourceResourceIntTest {

    private static final Long DEFAULT_ITEM_ID = 1L;
    private static final Long UPDATED_ITEM_ID = 2L;

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
    private static final BigDecimal UPDATED_COST = getBigDecimal(2F);;

    private static final BigDecimal DEFAULT_MRP = BigDecimal.ONE;
    private static final BigDecimal UPDATED_MRP = getBigDecimal(2F);

    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.GRN;
    private static final TransactionType UPDATED_TRANSACTION_TYPE = TransactionType.GRN;

    private static final String DEFAULT_TRANSACTION_REF_NO = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_REF_NO = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_FIRST_STOCK_IN_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FIRST_STOCK_IN_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_LAST_STOCK_OUT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_LAST_STOCK_OUT_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_CONSIGNMENT = false;
    private static final Boolean UPDATED_CONSIGNMENT = true;

    private static final Float DEFAULT_QUANTITY = 1F;
    private static final Float UPDATED_QUANTITY = 2F;

    private static final Float DEFAULT_AVAILABLE_QUANTITY = 1F;
    private static final Float UPDATED_AVAILABLE_QUANTITY = 2F;

    private static final String DEFAULT_SUPPLIER = "AAAAAAAAAA";
    private static final String UPDATED_SUPPLIER = "BBBBBBBBBB";

    private static final String DEFAULT_TAX_NAME = "AAAAAAAAAA";
    private static final String UPDATED_TAX_NAME = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TAX_PER_UNIT = BigDecimal.ONE;
    private static final BigDecimal UPDATED_TAX_PER_UNIT = getBigDecimal(2F);

    private static final String DEFAULT_TAX_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TAX_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_BAR_CODE = "AAAAAAAAAA";
    private static final String UPDATED_BAR_CODE = "BBBBBBBBBB";

    String barcodeFormat = "CT~~CD,~CC^~CT~\n" +
        "^XA~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR2,2~SD30^JUS^LRN^CI0^XZ\n" +
        "^XA\n" +
        "^MMT\n" +
        "^PW1200\n" +
        "^LL0177\n" +
        "^LS0$#\n" +
        "^FT129,55^A0N,21,21^FH\\^FD{ITEMNAME}^FS\n" +
        "^FT131,80^A0N,21,21^FH\\^FD{BATCH_CODE}^FS\n" +
        "^FT133,105^A0N,21,21^FH\\^FD{EXPIRY_DATE}^FS\n" +
        "^BY112,96^FT20,123^BXN,6,200,0,0,1,_\n" +
        "^FH\\^FD{BARCODE}^FS\n" +
        "^FT429,55^A0N,21,21^FH\\^FD{ITEMNAME}^FS\n" +
        "^FT431,80^A0N,21,21^FH\\^FD{BATCH_CODE}^FS\n" +
        "^FT433,105^A0N,21,21^FH\\^FD{EXPIRY_DATE}^FS\n" +
        "^BY112,96^FT320,123^BXN,6,200,0,0,1,_\n" +
        "^FH\\^FD{BARCODE}^FS\n" +
        "^FT729,55^A0N,21,21^FH\\^FD{ITEMNAME}^FS\n" +
        "^FT731,80^A0N,21,21^FH\\^FD{BATCH_CODE}^FS\n" +
        "^FT733,105^A0N,21,21^FH\\^FD{EXPIRY_DATE}^FS\n" +
        "^BY112,96^FT620,123^BXN,6,200,0,0,1,_\n" +
        "^FH\\^FD{BARCODE}^FS\n" +
        "^FT1029,55^A0N,21,21^FH\\^FD{ITEMNAME}^FS\n" +
        "^FT1031,80^A0N,21,21^FH\\^FD{BATCH_CODE}^FS\n" +
        "^FT1033,105^A0N,21,21^FH\\^FD{EXPIRY_DATE}^FS\n" +
        "^BY112,96^FT920,123^BXN,6,200,0,0,1,_\n" +
        "^FH\\^FD{BARCODE}^FS$#\n" +
        "^PQ1,0,1,Y^XZ";


    @Autowired
    private StockSourceRepository stockSourceRepository;

    @Autowired
    private StockSourceService stockSourceService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private EntityManager em;

    private MockMvc restStockSourceMockMvc;

    private StockSource stockSource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockSourceResource stockSourceResource = new StockSourceResource(stockSourceService);
        this.restStockSourceMockMvc = MockMvcBuilders.standaloneSetup(stockSourceResource)
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
    public static StockSource createEntity(EntityManager em) {
        StockSource stockSource = new StockSource()
            .itemId(DEFAULT_ITEM_ID)
            .uomId(DEFAULT_UOM_ID)
            .sku(DEFAULT_SKU)
            .batchNo(DEFAULT_BATCH_NO)
            .expiryDate(DEFAULT_EXPIRY_DATE)
            .owner(DEFAULT_OWNER)
            .cost(DEFAULT_COST)
            .mrp(DEFAULT_MRP)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionRefNo(DEFAULT_TRANSACTION_REF_NO)
            .firstStockInDate(DEFAULT_FIRST_STOCK_IN_DATE)
            .lastStockOutDate(DEFAULT_LAST_STOCK_OUT_DATE)
            .consignment(DEFAULT_CONSIGNMENT)
            .quantity(DEFAULT_QUANTITY)
            .availableQuantity(DEFAULT_AVAILABLE_QUANTITY)
            .supplier(DEFAULT_SUPPLIER)
            .taxName(DEFAULT_TAX_NAME)
            .taxPerUnit(DEFAULT_TAX_PER_UNIT)
            .taxType(DEFAULT_TAX_TYPE)
            .barCode(DEFAULT_BAR_CODE);
        return stockSource;
    }

    @Before
    public void initTest() {
        stockSource = createEntity(em);
    }

    @Test
    @Transactional
    public void createStockSource() throws Exception {
        int databaseSizeBeforeCreate = stockSourceRepository.findAll().size();

        // Create the StockSource

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isCreated());

        // Validate the StockSource in the database
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeCreate + 1);
        StockSource testStockSource = stockSourceList.get(stockSourceList.size() - 1);
        assertThat(testStockSource.getItemId()).isEqualTo(DEFAULT_ITEM_ID);
        assertThat(testStockSource.getUomId()).isEqualTo(DEFAULT_UOM_ID);
        assertThat(testStockSource.getSku()).isEqualTo(DEFAULT_SKU);
        assertThat(testStockSource.getBatchNo()).isEqualTo(DEFAULT_BATCH_NO);
        assertThat(testStockSource.getExpiryDate()).isEqualTo(DEFAULT_EXPIRY_DATE);
        assertThat(testStockSource.getOwner()).isEqualTo(DEFAULT_OWNER);
        assertThat(testStockSource.getCost()).isEqualTo(DEFAULT_COST);
        assertThat(testStockSource.getMrp()).isEqualTo(DEFAULT_MRP);
        assertThat(testStockSource.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testStockSource.getTransactionRefNo()).isEqualTo(DEFAULT_TRANSACTION_REF_NO);
        assertThat(testStockSource.getFirstStockInDate()).isEqualTo(DEFAULT_FIRST_STOCK_IN_DATE);
        assertThat(testStockSource.getLastStockOutDate()).isEqualTo(DEFAULT_LAST_STOCK_OUT_DATE);
        assertThat(testStockSource.isConsignment()).isEqualTo(DEFAULT_CONSIGNMENT);
        assertThat(testStockSource.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testStockSource.getAvailableQuantity()).isEqualTo(DEFAULT_AVAILABLE_QUANTITY);
        assertThat(testStockSource.getSupplier()).isEqualTo(DEFAULT_SUPPLIER);
        assertThat(testStockSource.getTaxName()).isEqualTo(DEFAULT_TAX_NAME);
        assertThat(testStockSource.getTaxPerUnit()).isEqualTo(DEFAULT_TAX_PER_UNIT);
        assertThat(testStockSource.getTaxType()).isEqualTo(DEFAULT_TAX_TYPE);
        assertThat(testStockSource.getBarCode()).isEqualTo(DEFAULT_BAR_CODE);
    }

    @Test
    @Transactional
    public void createStockSourceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockSourceRepository.findAll().size();

        // Create the StockSource with an existing ID
        StockSource existingStockSource = new StockSource();
        existingStockSource.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockSource)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkItemIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setItemId(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUomIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setUomId(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSkuIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setSku(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOwnerIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setOwner(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCostIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setCost(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setTransactionType(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTransactionRefNoIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setTransactionRefNo(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkFirstStockInDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setFirstStockInDate(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setQuantity(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAvailableQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setAvailableQuantity(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSupplierIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockSourceRepository.findAll().size();
        // set the field null
        stockSource.setSupplier(null);

        // Create the StockSource, which fails.

        restStockSourceMockMvc.perform(post("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isBadRequest());

        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockSources() throws Exception {
        // Initialize the database
        //  stockSourceRepository.saveAndFlush(stockSource);

        // Get all the stockSourceList
        restStockSourceMockMvc.perform(get("/api/stock-sources?transactionRefNo=MDR8001-8001&toDate=2020-09-11&fromDate=2016-09-11"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].transactionRefNo").value(hasItem("MDR8001-8001".toString())));
          /*  .andExpect(jsonPath("$.[*].id").value(hasItem(stockSource.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(DEFAULT_ITEM_ID.intValue())))
            .andExpect(jsonPath("$.[*].uomId").value(hasItem(DEFAULT_UOM_ID.intValue())))
            .andExpect(jsonPath("$.[*].sku").value(hasItem(DEFAULT_SKU.toString())))
            .andExpect(jsonPath("$.[*].batchNo").value(hasItem(DEFAULT_BATCH_NO.toString())))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].owner").value(hasItem(DEFAULT_OWNER.toString())))
            .andExpect(jsonPath("$.[*].cost").value(hasItem(DEFAULT_COST.doubleValue())))
            .andExpect(jsonPath("$.[*].mrp").value(hasItem(DEFAULT_MRP.doubleValue())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].transactionRefNo").value(hasItem(DEFAULT_TRANSACTION_REF_NO.toString())))
            .andExpect(jsonPath("$.[*].firstStockInDate").value(hasItem(DEFAULT_FIRST_STOCK_IN_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastStockOutDate").value(hasItem(DEFAULT_LAST_STOCK_OUT_DATE.toString())))
            .andExpect(jsonPath("$.[*].consignment").value(hasItem(DEFAULT_CONSIGNMENT.booleanValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].availableQuantity").value(hasItem(DEFAULT_AVAILABLE_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].supplier").value(hasItem(DEFAULT_SUPPLIER.toString())))
            .andExpect(jsonPath("$.[*].taxName").value(hasItem(DEFAULT_TAX_NAME.toString())))
            .andExpect(jsonPath("$.[*].taxPerUnit").value(hasItem(DEFAULT_TAX_PER_UNIT.doubleValue())))
            .andExpect(jsonPath("$.[*].taxType").value(hasItem(DEFAULT_TAX_TYPE.toString())))
            .andExpect(jsonPath("$.[*].barCode").value(hasItem(DEFAULT_BAR_CODE.toString())));*/
    }


    @Test
    @Transactional
    public void getStockSource() throws Exception {
        // Initialize the database
        stockSourceRepository.saveAndFlush(stockSource);

        // Get the stockSource
        restStockSourceMockMvc.perform(get("/api/stock-sources/{id}", stockSource.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockSource.getId().intValue()))
            .andExpect(jsonPath("$.itemId").value(DEFAULT_ITEM_ID.intValue()))
            .andExpect(jsonPath("$.uomId").value(DEFAULT_UOM_ID.intValue()))
            .andExpect(jsonPath("$.sku").value(DEFAULT_SKU.toString()))
            .andExpect(jsonPath("$.batchNo").value(DEFAULT_BATCH_NO.toString()))
            .andExpect(jsonPath("$.expiryDate").value(DEFAULT_EXPIRY_DATE.toString()))
            .andExpect(jsonPath("$.owner").value(DEFAULT_OWNER.toString()))
            .andExpect(jsonPath("$.cost").value(DEFAULT_COST.doubleValue()))
            .andExpect(jsonPath("$.mrp").value(DEFAULT_MRP.doubleValue()))
            .andExpect(jsonPath("$.transactionType").value(DEFAULT_TRANSACTION_TYPE.toString()))
            .andExpect(jsonPath("$.transactionRefNo").value(DEFAULT_TRANSACTION_REF_NO.toString()))
            .andExpect(jsonPath("$.firstStockInDate").value(DEFAULT_FIRST_STOCK_IN_DATE.toString()))
            .andExpect(jsonPath("$.lastStockOutDate").value(DEFAULT_LAST_STOCK_OUT_DATE.toString()))
            .andExpect(jsonPath("$.consignment").value(DEFAULT_CONSIGNMENT.booleanValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.availableQuantity").value(DEFAULT_AVAILABLE_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.supplier").value(DEFAULT_SUPPLIER.toString()))
            .andExpect(jsonPath("$.taxName").value(DEFAULT_TAX_NAME.toString()))
            .andExpect(jsonPath("$.taxPerUnit").value(DEFAULT_TAX_PER_UNIT.doubleValue()))
            .andExpect(jsonPath("$.taxType").value(DEFAULT_TAX_TYPE.toString()))
            .andExpect(jsonPath("$.barCode").value(DEFAULT_BAR_CODE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingStockSource() throws Exception {
        // Get the stockSource
        restStockSourceMockMvc.perform(get("/api/stock-sources/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStockSource() throws Exception {
        // Initialize the database
        stockSourceService.save(stockSource);

        int databaseSizeBeforeUpdate = stockSourceRepository.findAll().size();

        // Update the stockSource
        StockSource updatedStockSource = stockSourceRepository.findById(stockSource.getId()).get();
        updatedStockSource
            .itemId(UPDATED_ITEM_ID)
            .uomId(UPDATED_UOM_ID)
            .sku(UPDATED_SKU)
            .batchNo(UPDATED_BATCH_NO)
            .expiryDate(UPDATED_EXPIRY_DATE)
            .owner(UPDATED_OWNER)
            .cost(UPDATED_COST)
            .mrp(UPDATED_MRP)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .transactionRefNo(UPDATED_TRANSACTION_REF_NO)
            .firstStockInDate(UPDATED_FIRST_STOCK_IN_DATE)
            .lastStockOutDate(UPDATED_LAST_STOCK_OUT_DATE)
            .consignment(UPDATED_CONSIGNMENT)
            .quantity(UPDATED_QUANTITY)
            .availableQuantity(UPDATED_AVAILABLE_QUANTITY)
            .supplier(UPDATED_SUPPLIER)
            .taxName(UPDATED_TAX_NAME)
            .taxPerUnit(UPDATED_TAX_PER_UNIT)
            .taxType(UPDATED_TAX_TYPE)
            .barCode(UPDATED_BAR_CODE);

        restStockSourceMockMvc.perform(put("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockSource)))
            .andExpect(status().isOk());

        // Validate the StockSource in the database
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeUpdate);
        StockSource testStockSource = stockSourceList.get(stockSourceList.size() - 1);
        assertThat(testStockSource.getItemId()).isEqualTo(UPDATED_ITEM_ID);
        assertThat(testStockSource.getUomId()).isEqualTo(UPDATED_UOM_ID);
        assertThat(testStockSource.getSku()).isEqualTo(UPDATED_SKU);
        assertThat(testStockSource.getBatchNo()).isEqualTo(UPDATED_BATCH_NO);
        assertThat(testStockSource.getExpiryDate()).isEqualTo(UPDATED_EXPIRY_DATE);
        assertThat(testStockSource.getOwner()).isEqualTo(UPDATED_OWNER);
        assertThat(testStockSource.getCost()).isEqualTo(UPDATED_COST);
        assertThat(testStockSource.getMrp()).isEqualTo(UPDATED_MRP);
        assertThat(testStockSource.getTransactionType()).isEqualTo(UPDATED_TRANSACTION_TYPE);
        assertThat(testStockSource.getTransactionRefNo()).isEqualTo(UPDATED_TRANSACTION_REF_NO);
        assertThat(testStockSource.getFirstStockInDate()).isEqualTo(UPDATED_FIRST_STOCK_IN_DATE);
        assertThat(testStockSource.getLastStockOutDate()).isEqualTo(UPDATED_LAST_STOCK_OUT_DATE);
        assertThat(testStockSource.isConsignment()).isEqualTo(UPDATED_CONSIGNMENT);
        assertThat(testStockSource.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testStockSource.getAvailableQuantity()).isEqualTo(UPDATED_AVAILABLE_QUANTITY);
        assertThat(testStockSource.getSupplier()).isEqualTo(UPDATED_SUPPLIER);
        assertThat(testStockSource.getTaxName()).isEqualTo(UPDATED_TAX_NAME);
        assertThat(testStockSource.getTaxPerUnit()).isEqualTo(UPDATED_TAX_PER_UNIT);
        assertThat(testStockSource.getTaxType()).isEqualTo(UPDATED_TAX_TYPE);
        assertThat(testStockSource.getBarCode()).isEqualTo(UPDATED_BAR_CODE);
    }

    @Test
    @Transactional
    public void updateNonExistingStockSource() throws Exception {
        int databaseSizeBeforeUpdate = stockSourceRepository.findAll().size();

        // Create the StockSource

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockSourceMockMvc.perform(put("/api/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockSource)))
            .andExpect(status().isCreated());

        // Validate the StockSource in the database
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockSource() throws Exception {
        // Initialize the database
        stockSourceService.save(stockSource);

        int databaseSizeBeforeDelete = stockSourceRepository.findAll().size();

        // Get the stockSource
        restStockSourceMockMvc.perform(delete("/api/stock-sources/{id}", stockSource.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<StockSource> stockSourceList = stockSourceRepository.findAll();
        assertThat(stockSourceList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void generateStockSourceBarcode() throws Exception {
        // Update the stockSource
        StockSource updatedStockSource = stockSourceRepository.findById(129l).get();
        // updatedStockSource.setBarCode(null);

        restStockSourceMockMvc.perform(put("/api/_generate/stock-sources")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockSource)))
            .andExpect(status().isOk());
    }


    @Test
    public void aCreateIndex() {
        if (elasticsearchTemplate.indexExists("barcodeconfiguration"))
            elasticsearchTemplate.deleteIndex("barcodeconfiguration");
        elasticsearchTemplate.createIndex("barcodeconfiguration");

        org.nh.pharmacy.dto.BarcodeConfiguration barcodeConfiguration = new org.nh.pharmacy.dto.BarcodeConfiguration();
        barcodeConfiguration.setId(1l);
        barcodeConfiguration.setCode("NH_Test");
        barcodeConfiguration.setPrinterName("NH_Printer");
        barcodeConfiguration.setFormat(barcodeFormat);
        barcodeConfiguration.setFormatType(FormatType.STOCK_BARCODE);
        barcodeConfiguration.setColumnCount(1);
        barcodeConfiguration.setPrintNewLine(true);
        org.nh.pharmacy.domain.HealthcareServiceCenter healthcareServiceCenter = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO healthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(healthcareServiceCenter, healthcareServiceCenterDTO);
        barcodeConfiguration.setHsc(healthcareServiceCenterDTO);
        org.nh.pharmacy.domain.Organization unit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyProperties(unit, organizationDTO);
        barcodeConfiguration.setUnit(organizationDTO);

        IndexQuery indexQuery = new IndexQueryBuilder().withId("1").withObject(barcodeConfiguration).build();
        elasticsearchTemplate.index(indexQuery, IndexCoordinates.of("barcodeconfiguration"));

        elasticsearchTemplate.refresh(BarcodeConfiguration.class);
    }

    @Test
    @Transactional
    public void getBarcodeFormatDetail() throws Exception {
        aCreateIndex();
        StockSource updatedStockSource = stockSourceRepository.findById(129l).get();

        restStockSourceMockMvc.perform(get("/api/_print/stock-sources/{stockSourceId}/{unitId}", 129, 1)
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockSource)))
            .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print());

    }


    @Test
    @Transactional
    public void sortStockSources() throws Exception {
        // Initialize the database
        //  stockSourceRepository.saveAndFlush(stockSource);

        // Get all the stockSourceList
        restStockSourceMockMvc.perform(get("/api/stock-sources?transactionRefNo=MDR800&sort=transactionRefNo,id"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].transactionRefNo").value(hasItem("MDR8001-8001".toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockSource.class);
    }
}
