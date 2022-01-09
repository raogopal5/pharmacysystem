package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.nh.common.dto.*;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.dto.ReversalDocument;
import org.nh.pharmacy.domain.dto.ReversalDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.StockReversalSearchRepository;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.pharmacy.web.rest.mapper.ReversalMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockReversalResource REST controller.
 *
 * @see StockReversalResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockReversalResourceIntTest {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAA";
    private static final String UPDATED_EMPLOYEE_NO = "BBBBBBBBBB";

    private static final ReversalDocument DEFAULT_DOCUMENT = new ReversalDocument();
    private static final ReversalDocument DEFAULT_REVERSAL_DOCUMENT = new ReversalDocument();

    private static final ReversalDocumentLine DEFAULT_REVERSAL_DOCUMENT_LINE = new ReversalDocumentLine();
    private static final ReversalDocumentLine DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE = new ReversalDocumentLine();

    private static final List<SourceDocument> DEFAULT_SOURCE_DOCUMENT = Arrays.asList(new SourceDocument());

    private static final float value = 1.0f;

    private static final Quantity DEFAULT_QUANTITY = new Quantity();
    private static final Quantity UPDATED_QUANTITY = new Quantity();


    private static List<ReversalDocumentLine> DEFAULT_LINE = Arrays.asList(new ReversalDocumentLine());

    private static final UserDTO DEFAULT_USER = createUser(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DISPLAY_NAME, DEFAULT_EMPLOYEE_NO);
    private static final UserDTO UPDATED_USER = createUser(UPDATED_ID, UPDATED_NAME, UPDATED_DISPLAY_NAME, UPDATED_EMPLOYEE_NO);

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Float DEFAULT_QUANTITY_VALUE = 1.0f;
    private static final Float UPDATED_QUANTITY_VALUE = 2.0f;


    private static final Boolean DEFAULT_LATEST = true;

    @Autowired
    private StockReversalRepository stockReversalRepository;

    @Autowired
    private StockReversalService stockReversalService;

    @Autowired
    private StockReversalSearchRepository stockReversalSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private StockService stockService;

    @Autowired
    private ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private ReversalMapper reversalMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UOMRepository uomRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private ItemStoreLocatorMapService itemStoreLocatorMapService;

    @Autowired
    private LocatorService locatorService;

    @Autowired
    private EntityManager em;

    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc restStockReversalMockMvc;

    private StockReversal stockReversal;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockReversalResource stockReversalResource = new StockReversalResource(stockReversalService,stockReversalRepository,stockReversalSearchRepository,applicationProperties);
        this.restStockReversalMockMvc = MockMvcBuilders.standaloneSetup(stockReversalResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    public static UserDTO createUser(Long id, String name, String displayName, String employeeNo) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setLogin(name);
        user.setDisplayName(displayName);
        user.setEmployeeNo(employeeNo);
        return user;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StockReversal createEntity(EntityManager em) {
        DEFAULT_DOCUMENT.setReversalMadeBy(DEFAULT_USER);
        DEFAULT_DOCUMENT.setSourceType(TransactionType.Stock_Issue);
        DEFAULT_QUANTITY.setValue(DEFAULT_QUANTITY_VALUE);
        DEFAULT_SOURCE_DOCUMENT.get(0).setQuantity(DEFAULT_QUANTITY);
        DEFAULT_REVERSAL_DOCUMENT_LINE.setSourceDocument(DEFAULT_SOURCE_DOCUMENT);
        DEFAULT_LINE = Arrays.asList(DEFAULT_REVERSAL_DOCUMENT_LINE);
        DEFAULT_DOCUMENT.setLines(DEFAULT_LINE);
        StockReversal stockReversal = new StockReversal()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockReversal;
    }

    private Quantity rejectQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }

    public ReversalDocument createReversalDocument() {
        org.nh.pharmacy.domain.Item item = itemService.findOne(1l);
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);
        org.nh.pharmacy.domain.Locator locator = locatorService.findOne(1l);
        LocatorDTO locatorDto = new LocatorDTO();
        BeanUtils.copyProperties(locator, locatorDto);
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setItem(itemDto);
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setLocator(locatorDto);
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setStockId(51l);
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setBatchNumber("BTH12345");
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setOwner("HUL");
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setRejectedQuantity(rejectQuantity());
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setCost(getBigDecimal(80f));
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setConsignment(true);
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setBarCode("BAR_Code");
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setId(1L);
        DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE.setSupplier("supplier");
        List<ReversalDocumentLine> list = new ArrayList();
        list.add(DEFAULT_REVERSAL_DOCUMENT_LINE_FOR_APPROVE);
        DEFAULT_REVERSAL_DOCUMENT.setReversalMadeBy(DEFAULT_USER);
        DEFAULT_REVERSAL_DOCUMENT.setLines(list);
        org.nh.pharmacy.domain.HealthcareServiceCenter issueHealthcareServiceCenter = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO issueHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(issueHealthcareServiceCenter, issueHealthcareServiceCenterDTO);

        org.nh.pharmacy.domain.HealthcareServiceCenter indentHealthcareServiceCenter = healthcareServiceCenterRepository.findById(2l).get();
        HealthcareServiceCenterDTO indentHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(indentHealthcareServiceCenter, indentHealthcareServiceCenterDTO);
        DEFAULT_REVERSAL_DOCUMENT.setIndentStore(issueHealthcareServiceCenterDTO);
        DEFAULT_REVERSAL_DOCUMENT.setIssueStore(indentHealthcareServiceCenterDTO);

        org.nh.pharmacy.domain.Organization indentUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIndentDTO = new OrganizationDTO();
        BeanUtils.copyProperties(indentUnit, organizationIndentDTO);
        org.nh.pharmacy.domain.Organization issueUnit = organizationRepository.findById(2l).get();
        OrganizationDTO organizationIssueDTO = new OrganizationDTO();
        BeanUtils.copyProperties(issueUnit, organizationIssueDTO);
        DEFAULT_REVERSAL_DOCUMENT.setIndentUnit(organizationIndentDTO);
        DEFAULT_REVERSAL_DOCUMENT.setIssueUnit(organizationIssueDTO);
        DEFAULT_REVERSAL_DOCUMENT.setApprovedDate(LocalDateTime.now());
        DEFAULT_REVERSAL_DOCUMENT.setDocumentType(TransactionType.Stock_Reversal);
        return DEFAULT_REVERSAL_DOCUMENT;
    }

    @Before
    public void initTest() {
        stockReversalSearchRepository.deleteAll();
        stockReversal = createEntity(em);
        StockIndentResourceIntTest.addUserToSecurityContext(1L, "admin", "admin");
    }

    @Test
    @Transactional
    public void createStockReversal() throws Exception {
        int databaseSizeBeforeCreate = stockReversalRepository.findAll().size();
        stockReversal.setDocument(createReversalDocument());

        // Create the StockReversal

        restStockReversalMockMvc.perform(post("/api/stock-reversals?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReversal)))
            .andExpect(status().isCreated());

        // Validate the StockReversal in the database
        List<StockReversal> stockReversalList = stockReversalRepository.findAll();
        assertThat(stockReversalList).hasSize(databaseSizeBeforeCreate + 1);
        StockReversal testStockReversal = stockReversalList.get(stockReversalList.size() - 1);
        assertThat(testStockReversal.getDocumentNumber()).isNotEmpty();
        assertThat(testStockReversal.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockReversal.isLatest()).isEqualTo(DEFAULT_LATEST);
        assertThat(testStockReversal.getDocument().getStatus()).isEqualTo(Status.DRAFT);

        // Validate the StockReversal in ElasticSearch
        StockReversal stockReversalEs = stockReversalSearchRepository.findById(testStockReversal.getId()).get();
        assertThat(stockReversalEs.getDocument().getReversalMadeBy().getLogin()).isEqualTo(testStockReversal.getDocument().getReversalMadeBy().getLogin());
        assertThat(stockReversalEs.getDocument().getReversalMadeBy().getDisplayName()).isEqualTo(testStockReversal.getDocument().getReversalMadeBy().getDisplayName());
        assertThat(stockReversalEs.getDocument().getReversalMadeBy().getEmployeeNo()).isEqualTo(testStockReversal.getDocument().getReversalMadeBy().getEmployeeNo());
    }

    @Test
    @Transactional
    public void createStockReversalWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockReversalRepository.findAll().size();

        // Create the StockReversal with an existing ID
        StockReversal existingStockReversal = new StockReversal();
        existingStockReversal.setId(1L);
        existingStockReversal.document(DEFAULT_DOCUMENT);
        existingStockReversal.setVersion(0);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockReversalMockMvc.perform(post("/api/stock-reversals?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockReversal)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockReversal> stockReversalList = stockReversalRepository.findAll();
        assertThat(stockReversalList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkLatestIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockReversalRepository.findAll().size();
        // set the field null
        stockReversal.setLatest(null);

        // Create the StockReversal, which fails.

        restStockReversalMockMvc.perform(post("/api/stock-reversals?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReversal)))
            .andExpect(status().isBadRequest());

        List<StockReversal> stockReversalList = stockReversalRepository.findAll();
        assertThat(stockReversalList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockReversals() throws Exception {
        // Initialize the database
        stockReversalService.save(stockReversal);

        // Get all the stockReversalList
        restStockReversalMockMvc.perform(get("/api/stock-reversals?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockReversal.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.reversalMadeBy.login").value(hasItem(DEFAULT_DOCUMENT.getReversalMadeBy().getLogin())))
            .andExpect(jsonPath("$.[*].document.reversalMadeBy.displayName").value(hasItem(DEFAULT_DOCUMENT.getReversalMadeBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].document.reversalMadeBy.employeeNo").value(hasItem(DEFAULT_DOCUMENT.getReversalMadeBy().getEmployeeNo())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockReversal() throws Exception {
        // Initialize the database
        stockReversalService.save(stockReversal);

        // Get the stockReversal
        restStockReversalMockMvc.perform(get("/api/stock-reversals/{id}", stockReversal.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockReversal.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.document.reversalMadeBy.login").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.document.reversalMadeBy.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.document.reversalMadeBy.employeeNo").value(DEFAULT_EMPLOYEE_NO))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStockReversal() throws Exception {
        // Get the stockReversal
        restStockReversalMockMvc.perform(get("/api/stock-reversals/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStockReversal() throws Exception {
        // Initialize the database
        stockReversalService.save(stockReversal);

        int databaseSizeBeforeUpdate = stockReversalRepository.findAll().size();

        // Update the stockReversal
        StockReversal updatedStockReversal = stockReversalRepository.findOne(stockReversal.getId());
        updatedStockReversal.getDocument().setReversalMadeBy(UPDATED_USER);
        DEFAULT_QUANTITY.setValue(UPDATED_QUANTITY_VALUE);
        DEFAULT_SOURCE_DOCUMENT.get(0).setQuantity(UPDATED_QUANTITY);
        DEFAULT_REVERSAL_DOCUMENT_LINE.setSourceDocument(DEFAULT_SOURCE_DOCUMENT);
        DEFAULT_LINE = Arrays.asList(DEFAULT_REVERSAL_DOCUMENT_LINE);
        updatedStockReversal.getDocument().setLines(DEFAULT_LINE);
        updatedStockReversal.setLatest(DEFAULT_LATEST);

        restStockReversalMockMvc.perform(put("/api/stock-reversals?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockReversal)))
            .andExpect(status().isOk());

        // Validate the StockReversal in the database
        List<StockReversal> stockReversalList = stockReversalRepository.findAll(Sort.by("id","version"));
        assertThat(stockReversalList).hasSize(databaseSizeBeforeUpdate + 1);
        StockReversal testStockReversal = stockReversalList.get(stockReversalList.size() - 1);
        assertThat(testStockReversal.getDocumentNumber()).isNotEmpty();
        assertThat(testStockReversal.getDocument().getReversalMadeBy().getLogin()).isEqualTo(updatedStockReversal.getDocument().getReversalMadeBy().getLogin());
        assertThat(testStockReversal.getDocument().getReversalMadeBy().getDisplayName()).isEqualTo(updatedStockReversal.getDocument().getReversalMadeBy().getDisplayName());
        assertThat(testStockReversal.getDocument().getReversalMadeBy().getEmployeeNo()).isEqualTo(updatedStockReversal.getDocument().getReversalMadeBy().getEmployeeNo());
        assertThat(testStockReversal.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testStockReversal.isLatest()).isEqualTo(DEFAULT_LATEST);
        // Validate the StockReversal in ElasticSearch
        StockReversal stockReversalEs = stockReversalSearchRepository.findById(testStockReversal.getId()).get();
        assertThat(stockReversalEs.getDocument().getReversalMadeBy().getLogin()).isEqualTo(testStockReversal.getDocument().getReversalMadeBy().getLogin());
        assertThat(stockReversalEs.getDocument().getReversalMadeBy().getDisplayName()).isEqualTo(testStockReversal.getDocument().getReversalMadeBy().getDisplayName());
        assertThat(stockReversalEs.getDocument().getReversalMadeBy().getEmployeeNo()).isEqualTo(testStockReversal.getDocument().getReversalMadeBy().getEmployeeNo());
    }

    @Test
    @Transactional
    public void updateNonExistingStockReversal() throws Exception {
        int databaseSizeBeforeUpdate = stockReversalRepository.findAll().size();

        // Create the StockReversal

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockReversalMockMvc.perform(put("/api/stock-reversals?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReversal)))
            .andExpect(status().isCreated());

        // Validate the StockReversal in the database
        List<StockReversal> stockReversalList = stockReversalRepository.findAll();
        assertThat(stockReversalList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockReversal() throws Exception {
        // Initialize the database
        stockReversalService.save(stockReversal);

        int databaseSizeBeforeDelete = stockReversalRepository.findAll().size();

        // Get the stockReversal
        restStockReversalMockMvc.perform(delete("/api/stock-reversals/{id}", stockReversal.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean stockReversalExistsInEs = stockReversalSearchRepository.existsById(stockReversal.getId());
        assertThat(stockReversalExistsInEs).isFalse();

        // Validate the database is empty
        List<StockReversal> stockReversalList = stockReversalRepository.findAll();
        assertThat(stockReversalList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockReversal() throws Exception {
        // Initialize the database
        stockReversalService.save(stockReversal);

        // Search the stockReversal
        restStockReversalMockMvc.perform(get("/api/_search/stock-reversals?query=id:" + stockReversal.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockReversal.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.reversalMadeBy.login").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].document.reversalMadeBy.displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].document.reversalMadeBy.employeeNo").value(hasItem(DEFAULT_EMPLOYEE_NO)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockReversalStatusCount() throws Exception {
        // Initialize the database
        stockReversalService.save(stockReversal, "DRAFT");

        // Get the stockIssue
        restStockReversalMockMvc.perform(get("/api/status-count/stock-reversals?query=id:" + stockReversal.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockReversal.class);
    }
}
