package org.nh.pharmacy.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.jbpm.config.JbpmProperties;
import org.nh.jbpm.domain.dto.TaskInfo;
import org.nh.jbpm.repository.search.JBPMTaskSearchRepository;
import org.nh.jbpm.security.SpringSecurityIdentityProvider;
import org.nh.jbpm.service.WorkflowService;
import org.nh.jbpm.web.rest.WorkflowResource;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.ItemSearchRepository;
import org.nh.pharmacy.repository.search.LocatorSearchRepository;
import org.nh.pharmacy.repository.search.StockAuditSearchRepository;
import org.nh.pharmacy.repository.search.UOMSearchRepository;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.repository.hibernate.type.JacksonUtil;
import org.nh.security.AuthenticatedUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.pharmacy.domain.enumeration.AdjustmentType.NEGATIVE_ADJUSTMENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockAuditResource REST controller.
 *
 * @see StockAuditResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockAuditResourceIntTest {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final AuditDocument DEFAULT_DOCUMENT = new AuditDocument();
    private static final AuditDocument UPDATED_DOCUMENT = new AuditDocument();

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAA";
    private static final String UPDATED_EMPLOYEE_NO = "BBBBBBBBBB";

    private static final UserDTO DEFAULT_USER = createUser(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DISPLAY_NAME, DEFAULT_EMPLOYEE_NO);
    private static final UserDTO UPDATED_USER = createUser(UPDATED_ID, UPDATED_NAME, UPDATED_DISPLAY_NAME, UPDATED_EMPLOYEE_NO);

    private static final String DEFAULT_REMARKS = "AAAAAAAAAA";
    private static final String UPDATED_REMARKS = "BBBBBBBBBB";


    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    @Autowired
    private StockAuditRepository stockAuditRepository;

    @Autowired
    private StockAuditPlanRepository stockAuditPlanRepository;

    @Autowired
    private InventoryAdjustmentRepository inventoryAdjustmentRepository;

    @Autowired
    private StockAuditService stockAuditService;

    @Autowired
    private InventoryAdjustmentService inventoryAdjustmentService;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private StockAuditSearchRepository stockAuditSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    SpringSecurityIdentityProvider identityProvider;

    @Autowired
    OrganizationRepository organizationRepository;

    private MockMvc restStockAuditMockMvc;

    private MockMvc restWorkflowMockMvc;

    private StockAuditPlan stockAuditPlan;

    private StockAudit stockAudit;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemSearchRepository itemSearchRepository;
    @Autowired
    private LocatorService locatorService;
    @Autowired
    private LocatorSearchRepository locatorSearchRepository;

    @Autowired
    private UOMService uomService;

    @Autowired
    private UOMSearchRepository uomSearchRepository;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockAuditResource stockAuditResource = new StockAuditResource(stockAuditService, inventoryAdjustmentService, applicationProperties,stockAuditRepository,stockAuditSearchRepository);
        this.restStockAuditMockMvc = MockMvcBuilders.standaloneSetup(stockAuditResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
        WorkflowResource workflowResource = new WorkflowResource(workflowService, identityProvider, jbpmTaskSearchRepository, jbpmProperties);
        this.restWorkflowMockMvc = MockMvcBuilders.standaloneSetup(workflowResource)
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


    public List<AuditDocumentLine> createAuditDocumentLines() {
        Stock stock = stockRepository.findById(51l).get();
        org.nh.pharmacy.domain.Item item = itemRepository.findById(stock.getItemId()).get();
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);

        AuditDocumentLine auditDocumentLine1 = new AuditDocumentLine();
        auditDocumentLine1.setItem(itemDto);
        auditDocumentLine1.setBatchNumber(stock.getBatchNo());
        auditDocumentLine1.setConsignment(stock.isConsignment());
        auditDocumentLine1.setMrp(stock.getMrp());
        auditDocumentLine1.setCost(stock.getCost());
        auditDocumentLine1.setOwner(stock.getOwner());
        auditDocumentLine1.setSku(stock.getSku());
        auditDocumentLine1.setStockId(stock.getId());
        auditDocumentLine1.setDiscrepantReason(new ValueSetCode(1l, "Damaged", "Damaged", true));
        auditDocumentLine1.setStockQuantity(new Quantity(stock.getQuantity(), uomService.findOne(stock.getUomId()).getUOMDTO()));
        auditDocumentLine1.setDiscrepantQuantity(new Quantity(0.0f, uomService.findOne(stock.getUomId()).getUOMDTO()));
        auditDocumentLine1.setHasDiscrepancy(false);
        auditDocumentLine1.setAuditingUser(createUser(3l, "90011X", "Approver", "90011X"));

        AuditDocumentLine auditDocumentLine2 = new AuditDocumentLine();
        auditDocumentLine2.setItem(itemDto);
        auditDocumentLine2.setBatchNumber(stock.getBatchNo());
        auditDocumentLine2.setConsignment(stock.isConsignment());
        auditDocumentLine2.setMrp(stock.getMrp());
        auditDocumentLine2.setCost(stock.getCost());
        auditDocumentLine2.setOwner(stock.getOwner());
        auditDocumentLine2.setSku(stock.getSku());
        auditDocumentLine2.setStockId(stock.getId());
        auditDocumentLine2.setDiscrepantReason(new ValueSetCode(1l, "Damaged", "Damaged", true));
        auditDocumentLine2.setStockQuantity(new Quantity(stock.getQuantity(), uomService.findOne(stock.getUomId()).getUOMDTO()));
        auditDocumentLine2.setDiscrepantQuantity(new Quantity(0.0f, uomService.findOne(stock.getUomId()).getUOMDTO()));
        auditDocumentLine2.setHasDiscrepancy(false);
        auditDocumentLine2.setAuditingUser(createUser(4l, "admin", "admin", "admin"));
        List<AuditDocumentLine> auditDocumentLineList = Arrays.asList(auditDocumentLine1, auditDocumentLine2);
        return auditDocumentLineList;
    }

    public static StockAuditPlan createStockAuditPlan() {
        AuditDocument auditPlanDocument = StockAuditPlanResourceIntTest.createAuditPlanDocument();
        return new StockAuditPlan().document(auditPlanDocument);
    }

    public static void addUserToSecurityContext(Long userId, String userName, String password) {
        AuthenticatedUser user = new AuthenticatedUser(userName, password, Collections.emptyList());
        user.setPreferences(new org.nh.security.dto.Preferences());
        user.getPreferences().setHospital(new org.nh.security.dto.Organization().code("1001"));
        user.getPreferences().getHospital().setId(3L);
        user.getPreferences().setUser(new org.nh.security.dto.User());
        user.getPreferences().getUser().setId(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null, Collections.emptyList());
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public HealthcareServiceCenterDTO createStore() {
        org.nh.pharmacy.domain.HealthcareServiceCenter store = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO storeDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(store, storeDTO);
        return storeDTO;
    }

    private OrganizationDTO createUnit() {
        org.nh.pharmacy.domain.Organization unit = organizationRepository.findById(3l).get();
        OrganizationDTO unitDTO = new OrganizationDTO();
        BeanUtils.copyProperties(unit, unitDTO);
        return unitDTO;
    }

    public void createIndex() {
        if (elasticsearchTemplate.indexExists("configuration"))
            elasticsearchTemplate.deleteIndex("configuration");
        elasticsearchTemplate.createIndex("configuration");

        if (!elasticsearchTemplate.indexExists("organization"))
            elasticsearchTemplate.createIndex("organization");

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(3l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockaudit_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(3l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_stockaudit_workflow_definition");
        configuration2.setValue("stock_audit_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(3l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_inventoryadjustment_enable_workflow");
        configuration3.setValue("Yes");
        configuration3.setLevel(2);

        Configuration configuration4 = new Configuration();
        configuration4.setApplicableCode("1001");
        configuration4.setApplicableTo(3l);
        configuration4.setApplicableType(ConfigurationLevel.Unit);
        configuration4.setKey("athma_inventoryadjustment_workflow_definition");
        configuration4.setValue("inventory_adjustment_document_process");
        configuration4.setLevel(2);

        Configuration configuration5 = new Configuration();
        configuration5.setApplicableCode("1001");
        configuration5.setApplicableTo(3l);
        configuration5.setApplicableType(ConfigurationLevel.Unit);
        configuration5.setKey("athma_date_format");
        configuration5.setValue("dd/MM/yy");
        configuration5.setLevel(2);

        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration1).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration2).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration3).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("4").withObject(configuration4).build();
        IndexQuery indexQuery5 = new IndexQueryBuilder().withId("5").withObject(configuration5).build();
        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);
        elasticsearchTemplate.index(indexQuery3, coordinates);
        elasticsearchTemplate.index(indexQuery4, coordinates);
        elasticsearchTemplate.index(indexQuery5, coordinates);


        elasticsearchTemplate.refresh(Configuration.class);
    }

    public void createIndexWithoutWorkflow() {
        if (elasticsearchTemplate.indexExists("configuration"))
            elasticsearchTemplate.deleteIndex("configuration");
        elasticsearchTemplate.createIndex("configuration");

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(3l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockaudit_enable_workflow");
        configuration1.setValue("No");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(3l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_inventoryadjustment_enable_workflow");
        configuration2.setValue("No");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(3l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_date_format");
        configuration3.setValue("dd/MM/yy");
        configuration3.setLevel(2);

        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration1).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration2).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration3).build();

        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);
        elasticsearchTemplate.index(indexQuery3, coordinates);

        elasticsearchTemplate.refresh(Configuration.class);

    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public StockAudit createEntity(EntityManager em) {
        DEFAULT_DOCUMENT.setCreatedBy(DEFAULT_USER);
        DEFAULT_DOCUMENT.setLines(createAuditDocumentLines());
        DEFAULT_DOCUMENT.setDocumentDate(LocalDateTime.now());
        DEFAULT_DOCUMENT.setUnit(createUnit());
        DEFAULT_DOCUMENT.setStore(createStore());
        DEFAULT_DOCUMENT.setStatus(Status.DRAFT);
        DEFAULT_DOCUMENT.setRemarks(DEFAULT_REMARKS);
        DEFAULT_DOCUMENT.setStoreContact(DEFAULT_USER);
        StockAudit stockAudit = new StockAudit()
            .document(DEFAULT_DOCUMENT);
        return stockAudit;
    }

    @Before
    public void initTest() {
        stockAuditSearchRepository.deleteAll();
        stockAudit = createEntity(em);
        stockAuditPlan = createStockAuditPlan();
        groupService.doIndex();
        addUserToSecurityContext(1l, "admin", "admin");
        itemSearchRepository.saveAll(itemService.findAll(PageRequest.of(0, 200)));
        locatorSearchRepository.saveAll(locatorService.findAll(PageRequest.of(0, 200)));
        uomSearchRepository.saveAll(uomService.findAll(PageRequest.of(0, 200)));
        addConfigurations();
    }

    @Test
    @Transactional
    public void createStockAudit() throws Exception {
        int databaseSizeBeforeCreate = stockAuditRepository.findAll().size();

        // Create the StockAudit

        stockAuditService.save(stockAudit, "DRAFT");

        // Validate the StockAudit in the database
        List<StockAudit> stockAuditList = stockAuditRepository.findAll();
        assertThat(stockAuditList).hasSize(databaseSizeBeforeCreate + 1);
        StockAudit testStockAudit = stockAuditList.get(stockAuditList.size() - 1);
        assertThat(testStockAudit.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAudit.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockAudit.isLatest()).isEqualTo(DEFAULT_LATEST);

        // Validate the StockAudit in Elasticsearch
        StockAudit stockAuditEs = stockAuditSearchRepository.findById(testStockAudit.getId()).get();
        assertThat(stockAuditEs.getId()).isEqualToComparingFieldByField(testStockAudit.getId());
    }

    @Test
    @Transactional
    public void createStockAuditWithNullId() throws Exception {
        int databaseSizeBeforeCreate = stockAuditRepository.findAll().size();

        // Create the StockAudit with an existing ID
        StockAudit existingStockAudit = new StockAudit();
        existingStockAudit.setId(null);

        // An entity with an null ID cannot be created, so this API call must fail
        restStockAuditMockMvc.perform(post("/api/stock-audits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockAudit)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockAudit> stockAuditList = stockAuditRepository.findAll();
        assertThat(stockAuditList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockAuditRepository.findAll().size();

        stockAuditService.save(stockAudit);
        // set the field null
        stockAudit.setDocument(null);

        // Update the StockAudit, which fails.

        restStockAuditMockMvc.perform(put("/api/stock-audits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isBadRequest());

//        List<StockAudit> stockAuditList = stockAuditRepository.findAll();
//        assertThat(stockAuditList).hasSize(databaseSizeBeforeTest + 1);
    }

    @Test
    @Transactional
    public void getAllStockAudits() throws Exception {
        // Initialize the database
        stockAuditService.save(stockAudit);

        // Get all the stockAuditList
        restStockAuditMockMvc.perform(get("/api/stock-audits?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockAudit.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockAudit() throws Exception {
        // Initialize the database
        stockAuditService.save(stockAudit);

        // Get the stockAudit
        restStockAuditMockMvc.perform(get("/api/stock-audits/{id}", stockAudit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockAudit.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void updateStockAudit() throws Exception {
        // Initialize the database
        stockAuditService.save(stockAudit);

        int databaseSizeBeforeUpdate = stockAuditRepository.findAll().size();

        // Update the stockAudit
        StockAudit updatedStockAudit = stockAuditRepository.findOne(stockAudit.getId());
        updatedStockAudit.getDocument().setRemarks(UPDATED_REMARKS);

        restStockAuditMockMvc.perform(put("/api/stock-audits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockAudit)))
            .andExpect(status().isOk());

        // Validate the StockAudit in the database
        List<StockAudit> stockAuditList = stockAuditRepository.findAll();
        assertThat(stockAuditList).hasSize(databaseSizeBeforeUpdate + 1);
        StockAudit testStockAudit = stockAuditList.get(stockAuditList.size() - 1);
        assertThat(testStockAudit.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAudit.getDocument().getRemarks()).isEqualTo(UPDATED_REMARKS);
        assertThat(testStockAudit.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testStockAudit.isLatest()).isEqualTo(DEFAULT_LATEST);

        // Validate the StockAudit in Elasticsearch
        StockAudit stockAuditEs = stockAuditSearchRepository.findById(testStockAudit.getId()).get();
        assertThat(stockAuditEs.getId()).isEqualToComparingFieldByField(testStockAudit.getId());
    }

    @Test
    @Transactional
    public void deleteStockAudit() throws Exception {
        // Initialize the database
        stockAuditService.save(stockAudit);

        int databaseSizeBeforeDelete = stockAuditRepository.findAll().size();

        // Get the stockAudit
        restStockAuditMockMvc.perform(delete("/api/stock-audits/{id}", stockAudit.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockAuditExistsInEs = stockAuditSearchRepository.existsById(stockAudit.getId());
        assertThat(stockAuditExistsInEs).isFalse();

        // Validate the database is empty
        List<StockAudit> stockAuditList = stockAuditRepository.findAll();
        assertThat(stockAuditList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockAudit() throws Exception {
        // Initialize the database
        stockAuditService.save(stockAudit);

        // Search the stockAudit
        restStockAuditMockMvc.perform(get("/api/_search/stock-audits?query=id:" + stockAudit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockAudit.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockAuditStatusCount() throws Exception {
        // Initialize the database
        stockAuditService.save(stockAudit);

        restStockAuditMockMvc.perform(get("/api/status-count/stock-audits?query=id:" + stockAudit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.DRAFT").value(1));

    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockAudit.class);
    }

    @Test
    public void verifyAuditApproval() throws Exception {

        createIndexWithoutWorkflow();

        MvcResult result = restStockAuditMockMvc.perform(post("/api/stock-audits?action=START")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isCreated()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restStockAuditMockMvc.perform(get("/api/stock-audits/" + stockAudit.getId())).andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        stockAudit.getDocument().getLines().get(0).setAuditQuantity(new Quantity(999f, uomService.findOne(1l).getUOMDTO()));

        result = restStockAuditMockMvc.perform(put("/api/stock-audits?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        addUserToSecurityContext(3L, "admin", "admin");

        result = restStockAuditMockMvc.perform(get("/api/stock-audits/" + stockAudit.getId())).andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        stockAudit.getDocument().getLines().get(0).setAuditQuantity(new Quantity(1005f, uomService.findOne(1l).getUOMDTO()));

        result = restStockAuditMockMvc.perform(put("/api/stock-audits?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        addUserToSecurityContext(3L, "90011X", "Approver");

        restStockAuditMockMvc.perform(put("/api/stock-audits?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk()).andReturn();

        //Check Inventory Adjustment
        List<InventoryAdjustment> inventoryAdjustmentList = inventoryAdjustmentRepository.findAll(Sort.by("id", "version"));

        for (InventoryAdjustment inventoryAdjustment : inventoryAdjustmentList) {
            assertThat(inventoryAdjustment.getDocumentNumber()).isNotEmpty();
            assertThat(inventoryAdjustment.getDocument().getStatus()).isEqualTo(Status.WAITING_FOR_APPROVAL);
            assertThat(inventoryAdjustment.getVersion()).isEqualTo(DEFAULT_VERSION);
            assertThat(inventoryAdjustment.isLatest()).isEqualTo(DEFAULT_LATEST);
            for (InventoryAdjustmentDocumentLine documentLine : inventoryAdjustment.getDocument().getLines()) {
                if (documentLine.getAdjustQuantity().getValue() == 1f) {
                    assertThat(documentLine.getAdjustmentType()).isEqualTo(NEGATIVE_ADJUSTMENT);
                } else {
//                     assertThat(documentLine.getAdjustmentType()).isEqualTo(POSITIVE_ADJUSTMENT);
                }
            }
        }
        stockAuditRepository.deleteAll();
        inventoryAdjustmentRepository.deleteAll();
    }

    @Test
    public void verifyWorkflow() throws Exception {

        createIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");

        MvcResult result = restStockAuditMockMvc.perform(post("/api/stock-audits?action=START")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isCreated()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restStockAuditMockMvc.perform(get("/api/stock-audits/" + stockAudit.getId())).andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        stockAudit.getDocument().getLines().stream().filter(auditDocumentLine -> "90011X".equals(auditDocumentLine.getAuditingUser().getLogin())).forEach(
            auditDocumentLine -> auditDocumentLine.setAuditQuantity(new Quantity(1000f, uomService.findOne(1l).getUOMDTO())));

        result = restStockAuditMockMvc.perform(put("/api/stock-audits?")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        result = restStockAuditMockMvc.perform(get("/api/_workflow/stock-audits?documentNumber=" + stockAudit.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockAuditMockMvc.perform(put("/api/_workflow/stock-audits?transition=Send for Approval&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "admin", "admin");

        result = restStockAuditMockMvc.perform(get("/api/stock-audits/" + stockAudit.getId())).andExpect(status().isOk()).andReturn();

        stockAudit = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockAudit.class);

        stockAudit.getDocument().getLines().stream().filter(auditDocumentLine -> "admin".equals(auditDocumentLine.getAuditingUser().getLogin())).forEach(
            auditDocumentLine -> auditDocumentLine.setAuditQuantity(new Quantity(1005f, uomService.findOne(1l).getUOMDTO())));

        result = restStockAuditMockMvc.perform(get("/api/_workflow/stock-audits?documentNumber=" + stockAudit.getDocumentNumber() + "&userId=admin"))
            .andExpect(status().isOk()).andReturn();
        taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockAuditMockMvc.perform(put("/api/_workflow/stock-audits?transition=Send for Approval&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=3&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        stockAudit = stockAuditService.findOne(stockAudit.getId());

        result = restStockAuditMockMvc.perform(get("/api/_workflow/stock-audits?documentNumber=" + stockAudit.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockAuditMockMvc.perform(put("/api/_workflow/stock-audits?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk());

        stockAuditRepository.deleteAll();
        inventoryAdjustmentRepository.deleteAll();
    }

    @Test
    public void zDestroy() {
        stockAuditRepository.deleteAll();
        stockAuditPlanRepository.deleteAll();
        inventoryAdjustmentRepository.deleteAll();
    }

    private void addConfigurations() {
        if (!elasticsearchTemplate.indexExists("configuration"))
            elasticsearchTemplate.createIndex("configuration");

        Configuration configuration = new Configuration();
        configuration.setApplicableCode("1001");
        configuration.setApplicableTo(3l);
        configuration.setApplicableType(ConfigurationLevel.Unit);
        configuration.setKey("athma_stockaudit_enable_workflow");
        configuration.setValue("No");
        configuration.setLevel(2);

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(3l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockaudit_workflow_definition");
        configuration1.setValue("stock_atock_document_process");
        configuration1.setLevel(2);

        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration1).build();
        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);

        elasticsearchTemplate.refresh(Configuration.class);
    }

    @Test
    public void createStockAuditForCriteria() throws Exception {
        StockAudit stockAudit = new StockAudit();
        stockAudit.setLatest(Boolean.TRUE);
        stockAudit.setDocument(new AuditDocument());
        stockAudit.getDocument().setLines(new ArrayList<>());
        stockAudit.getDocument().setLines(createAuditDocumentLines());
        stockAudit.getDocument().setStore(new HealthcareServiceCenterDTO());
        stockAudit.getDocument().getStore().setId(Long.valueOf(1));
        stockAudit.getDocument().setUnit(new OrganizationDTO());
        stockAudit.getDocument().getUnit().setId(Long.valueOf(3));
        stockAudit.getDocument().getUnit().setCode("1001");

        List<AuditCriteria> auditCriterias = new ArrayList<>();
        AuditCriteria auditCriteria = new AuditCriteria();
        auditCriteria.setAuditFilter(new ArrayList<>());
        AuditCriteriaFilter valueFilter = new AuditCriteriaFilter();
        valueFilter.setField("VALUE");
        valueFilter.setOperator("EQUALTO");
        valueFilter.setValue("3000");
        auditCriteria.getAuditFilter().add(valueFilter);

        AuditCriteriaFilter costFilter = new AuditCriteriaFilter();
        costFilter.setField("COST");
        costFilter.setOperator("EQUALTO");
        costFilter.setValue("1");
        auditCriteria.getAuditFilter().add(costFilter);

        AuditCriteriaFilter consignmentFilter = new AuditCriteriaFilter();
        consignmentFilter.setField("CONSIGNMENT");
        consignmentFilter.setValue("false");
        //auditCriteria.getAuditFilter().add(consignmentFilter);

        AuditCriteriaFilter vedCategoryFilter = new AuditCriteriaFilter();
        vedCategoryFilter.setField("VED_CATEGORY");
        vedCategoryFilter.setValue("V");
        //auditCriteria.getAuditFilter().add(vedCategoryFilter);

        AuditCriteriaFilter locatorFilter = new AuditCriteriaFilter();
        locatorFilter.setField("LOCATOR");
        locatorFilter.setEntity(new HashMap<>());
        locatorFilter.getEntity().put("id", "1");
        auditCriteria.getAuditFilter().add(locatorFilter);

        AuditCriteriaFilter itemCategoryFilter = new AuditCriteriaFilter();
        itemCategoryFilter.setField("ITEM_CATEGORY");
        itemCategoryFilter.setEntity(new HashMap<>());
        itemCategoryFilter.getEntity().put("id", "1");
        auditCriteria.getAuditFilter().add(itemCategoryFilter);

        AuditCriteriaFilter itemTypeFilter = new AuditCriteriaFilter();
        itemTypeFilter.setField("ITEM_TYPE");
        itemTypeFilter.setEntity(new HashMap<>());
        itemTypeFilter.getEntity().put("id", "181");
        auditCriteria.getAuditFilter().add(itemTypeFilter);

        AuditCriteriaFilter itemNameFilter = new AuditCriteriaFilter();
        itemNameFilter.setField("ITEM_NAME");
        itemNameFilter.setValue("650");
        auditCriteria.getAuditFilter().add(itemNameFilter);

        AuditCriteriaFilter itemCodeFilter = new AuditCriteriaFilter();
        itemCodeFilter.setField("ITEM_CODE");
        itemCodeFilter.setValue("M-N-IM-A001-MD8-996");
        //auditCriteria.getAuditFilter().add(itemCodeFilter);

        auditCriterias.add(auditCriteria);
        stockAudit.setAuditCriterias(auditCriterias);
        restStockAuditMockMvc.perform(post("/api/_create-for-criteria/stock-audits")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAudit)))
            .andExpect(status().isOk());
    }

}
