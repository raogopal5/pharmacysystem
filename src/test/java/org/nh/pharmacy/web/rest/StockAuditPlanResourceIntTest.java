package org.nh.pharmacy.web.rest;

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
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.StockAuditPlan;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.repository.StockAuditPlanRepository;
import org.nh.pharmacy.repository.StockAuditRepository;
import org.nh.pharmacy.repository.search.StockAuditPlanSearchRepository;
import org.nh.pharmacy.service.StockAuditPlanService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.nh.common.util.BigDecimalUtil.*;
/**
 * Test class for the StockAuditPlanResource REST controller.
 *
 * @see StockAuditPlanResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockAuditPlanResourceIntTest {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final AuditDocument DEFAULT_DOCUMENT = new AuditDocument();
    private static final AuditDocument DEFAULT_AUDIT_PLAN_DOCUMENT = createAuditPlanDocument();

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAA";
    private static final String UPDATED_EMPLOYEE_NO = "BBBBBBBBBB";

    private static final UserDTO DEFAULT_USER = createUser(DEFAULT_ID,DEFAULT_NAME, DEFAULT_DISPLAY_NAME, DEFAULT_EMPLOYEE_NO);
    private static final UserDTO UPDATED_USER = createUser(UPDATED_ID,UPDATED_NAME, UPDATED_DISPLAY_NAME, UPDATED_EMPLOYEE_NO);

    private static final String DEFAULT_REMARKS = "AAAAAAAAAA";
    private static final String UPDATED_REMARKS = "BBBBBBBBBB";

    private static List<AuditDocumentLine> DEFAULT_LINE = new ArrayList<>();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    @Autowired
    private StockAuditPlanRepository stockAuditPlanRepository;

    @Autowired
    private StockAuditRepository stockAuditRepository;

    @Autowired
    private StockAuditPlanService stockAuditPlanService;

    @Autowired
    private StockAuditPlanSearchRepository stockAuditPlanSearchRepository;

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
    private ApplicationProperties applicationProperties;

    private MockMvc restStockAuditPlanMockMvc;

    private StockAuditPlan stockAuditPlan;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockAuditPlanResource stockAuditPlanResource = new StockAuditPlanResource(stockAuditPlanService,stockAuditPlanRepository,stockAuditPlanSearchRepository,applicationProperties);
        this.restStockAuditPlanMockMvc = MockMvcBuilders.standaloneSetup(stockAuditPlanResource)
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

    public static AuditDocumentLine createDocumentLine1() {
        ItemDTO item = new ItemDTO();
        item.setId(1l);
        item.setCode("code");
        AuditDocumentLine stockAuditPlanDocumentLine = new AuditDocumentLine();
        stockAuditPlanDocumentLine.setStockId(1l);
        stockAuditPlanDocumentLine.setStockQuantity(new Quantity(10f,null));
        stockAuditPlanDocumentLine.setBatchNumber("batch");
        stockAuditPlanDocumentLine.setConsignment(false);
        stockAuditPlanDocumentLine.setCost(BigDecimal.TEN);
        stockAuditPlanDocumentLine.setItem(item);
        return stockAuditPlanDocumentLine;
    }

    public static AuditDocumentLine createDocumentLine2()
    {
        ItemDTO item = new ItemDTO();
        item.setId(2l);
        item.setCode("code1");
        AuditDocumentLine stockAuditPlanDocumentLine = new AuditDocumentLine();
        stockAuditPlanDocumentLine.setStockId(2l);
        stockAuditPlanDocumentLine.setStockQuantity(new Quantity(5f,null));
        stockAuditPlanDocumentLine.setBatchNumber("batch");
        stockAuditPlanDocumentLine.setConsignment(false);
        stockAuditPlanDocumentLine.setCost(getBigDecimal(5f));
        stockAuditPlanDocumentLine.setItem(item);
        return stockAuditPlanDocumentLine;
    }

    public static AuditDocumentLine createDocumentLine3()
    {
        ItemDTO item = new ItemDTO();
        item.setId(3l);
        item.setCode("code2");
        AuditDocumentLine stockAuditPlanDocumentLine = new AuditDocumentLine();
        stockAuditPlanDocumentLine.setStockId(2l);
        stockAuditPlanDocumentLine.setStockQuantity(new Quantity(8f,null));
        stockAuditPlanDocumentLine.setBatchNumber("batch2");
        stockAuditPlanDocumentLine.setConsignment(false);
        stockAuditPlanDocumentLine.setCost(getBigDecimal(5f));
        stockAuditPlanDocumentLine.setItem(item);
        return stockAuditPlanDocumentLine;
    }

    public static AuditDocument createAuditPlanDocument(){

        AuditDocument stockAuditPlanDocument = new AuditDocument();
        stockAuditPlanDocument.setCreatedBy(createUser(2l,"User02","emp02","User2"));
        stockAuditPlanDocument.setUnit(createOrganization());
        stockAuditPlanDocument.setStore(createStore());
        stockAuditPlanDocument.setUseBarCode(false);
        List<AuditDocumentLine> lines = new ArrayList<>();
        lines.add(createDocumentLine1()); lines.add(createDocumentLine2());lines.add(createDocumentLine3());
        stockAuditPlanDocument.setLines(lines);
        stockAuditPlanDocument.setDocumentDate(LocalDateTime.now());
        stockAuditPlanDocument.setModifiedDate(LocalDateTime.now());
        return stockAuditPlanDocument;
    }

    public static HealthcareServiceCenterDTO createStore(){
        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(2l);
        hsc.setCode("NH");
        hsc.setName("Narayana HealthCare");
        hsc.setPartOf(createOrganization());
        return hsc;
    }

    public static OrganizationDTO createOrganization(){
        OrganizationDTO organization = new OrganizationDTO();
        organization.setId(1l);
        organization.setName("HSR");
        organization.setCode("HSR");
        return organization;
    }

    public static void addUserToSecurityContext(Long userId) {
        AuthenticatedUser user = new AuthenticatedUser("admin", "admin", Collections.emptyList());
        user.setPreferences(new org.nh.security.dto.Preferences());
        user.getPreferences().setHospital(new org.nh.security.dto.Organization().code("1001"));
        user.getPreferences().getHospital().setId(1l);
        user.getPreferences().setUser(new org.nh.security.dto.User());
        user.getPreferences().getUser().setId(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null, Collections.emptyList());
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StockAuditPlan createEntity(EntityManager em) {
        DEFAULT_DOCUMENT.setCreatedBy(DEFAULT_USER);
        DEFAULT_DOCUMENT.setLines(DEFAULT_LINE);
        DEFAULT_DOCUMENT.setRemarks(DEFAULT_REMARKS);
        DEFAULT_DOCUMENT.setStatus(Status.DRAFT);
        StockAuditPlan stockAuditPlan = new StockAuditPlan()
            .document(DEFAULT_DOCUMENT);
        return stockAuditPlan;
    }

    @Before
    public void initTest() {
        if(!elasticsearchTemplate.indexExists(StockAuditPlan.class))
            elasticsearchTemplate.createIndex(StockAuditPlan.class);
        stockAuditPlanSearchRepository.deleteAll();
        stockAuditPlan = createEntity(em);
        addUserToSecurityContext(1l);
    }

    @Test
    @Transactional
    public void createStockAuditPlan() throws Exception {
        int databaseSizeBeforeCreate = stockAuditPlanRepository.findAll().size();

        // Create the StockAuditPlan

        restStockAuditPlanMockMvc.perform(post("/api/stock-audit-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isCreated());

        // Validate the StockAuditPlan in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll(Sort.by("id","version"));
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeCreate + 1);
        StockAuditPlan testStockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        assertThat(testStockAuditPlan.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAuditPlan.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockAuditPlan.isLatest()).isEqualTo(DEFAULT_LATEST);

        // Validate the StockAuditPlan in Elasticsearch
        StockAuditPlan stockAuditPlanEs = stockAuditPlanSearchRepository.findById(testStockAuditPlan.getId()).get();
        assertThat(stockAuditPlanEs).isEqualToComparingOnlyGivenFields(testStockAuditPlan, "id","version","documentNumber","document","latest");
    }

    @Test
    @Transactional
    public void createStockAuditPlanWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockAuditPlanRepository.findAll().size();

        // Create the StockAuditPlan with an existing ID
        StockAuditPlan existingStockAuditPlan = new StockAuditPlan();
        existingStockAuditPlan.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockAuditPlanMockMvc.perform(post("/api/stock-audit-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockAuditPlan)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockAuditPlanRepository.findAll().size();
        // set the field null
        stockAuditPlan.setDocument(null);

        // Create the StockAuditPlan, which fails.

        restStockAuditPlanMockMvc.perform(post("/api/stock-audit-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isBadRequest());

        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockAuditPlans() throws Exception {
        // Initialize the database
        stockAuditPlanService.save(stockAuditPlan);

        // Get all the stockAuditPlanList
        restStockAuditPlanMockMvc.perform(get("/api/stock-audit-plans?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockAuditPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockAuditPlan() throws Exception {
        // Initialize the database
        stockAuditPlanService.save(stockAuditPlan);

        // Get the stockAuditPlan
        restStockAuditPlanMockMvc.perform(get("/api/stock-audit-plans/{id}", stockAuditPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockAuditPlan.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStockAuditPlan() throws Exception {
        // Get the stockAuditPlan
        restStockAuditPlanMockMvc.perform(get("/api/stock-audit-plans/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStockAuditPlan() throws Exception {
        // Initialize the database
        stockAuditPlanService.save(stockAuditPlan);

        int databaseSizeBeforeUpdate = stockAuditPlanRepository.findAll().size();

        // Update the stockAuditPlan
        StockAuditPlan updatedStockAuditPlan = stockAuditPlanRepository.findOne(stockAuditPlan.getId());
        updatedStockAuditPlan.getDocument().setRemarks(UPDATED_REMARKS);

        restStockAuditPlanMockMvc.perform(put("/api/stock-audit-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockAuditPlan)))
            .andExpect(status().isOk());

        // Validate the StockAuditPlan in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeUpdate + 1);
        StockAuditPlan testStockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        assertThat(testStockAuditPlan.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAuditPlan.getDocument().getRemarks()).isEqualTo(UPDATED_REMARKS);
        assertThat(testStockAuditPlan.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testStockAuditPlan.isLatest()).isEqualTo(DEFAULT_LATEST);

        // Validate the StockAuditPlan in Elasticsearch
        StockAuditPlan stockAuditPlanEs = stockAuditPlanSearchRepository.findById(testStockAuditPlan.getId()).get();
        assertThat(stockAuditPlanEs).isEqualToComparingOnlyGivenFields(testStockAuditPlan, "id","version","documentNumber","document","latest");
    }

    @Test
    @Transactional
    public void updateNonExistingStockAuditPlan() throws Exception {
        int databaseSizeBeforeUpdate = stockAuditPlanRepository.findAll().size();

        // Create the StockAuditPlan

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockAuditPlanMockMvc.perform(put("/api/stock-audit-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isCreated());

        // Validate the StockAuditPlan in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockAuditPlan() throws Exception {
        // Initialize the database
        stockAuditPlanService.save(stockAuditPlan);

        int databaseSizeBeforeDelete = stockAuditPlanRepository.findAll().size();

        // Get the stockAuditPlan
        restStockAuditPlanMockMvc.perform(delete("/api/stock-audit-plans/{id}", stockAuditPlan.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockAuditPlanExistsInEs = stockAuditPlanSearchRepository.existsById(stockAuditPlan.getId());
        assertThat(stockAuditPlanExistsInEs).isFalse();

        // Validate the database is empty
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockAuditPlan() throws Exception {
        // Initialize the database
        stockAuditPlanService.save(stockAuditPlan);

        // Search the stockAuditPlan
        restStockAuditPlanMockMvc.perform(get("/api/_search/stock-audit-plans?query=id:" + stockAuditPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockAuditPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    public void  xAuditPlan4Destroy() {

        stockAuditRepository.deleteAll();
        stockAuditPlanRepository.deleteAll();
    }

    @Test
    @Transactional
    @Rollback(false)
    public void xAuditPlan5ActionSendForApproval() throws Exception {
        int databaseSizeBeforeCreate = stockAuditPlanRepository.findAll().size();
        // Create the StockAuditPlan
        stockAuditPlan.setDocument(DEFAULT_AUDIT_PLAN_DOCUMENT);

        restStockAuditPlanMockMvc.perform(post("/api/stock-audit-plans?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isCreated());

        // Validate the StockAuditPlan in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeCreate + 1);
        StockAuditPlan testStockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        assertThat(testStockAuditPlan.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAuditPlan.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockAuditPlan.isLatest()).isEqualTo(DEFAULT_LATEST);
        assertThat(testStockAuditPlan.getDocument().getStatus()).isEqualTo(Status.WAITING_FOR_APPROVAL);
        // Validate the StockAuditPlan in Elasticsearch
        StockAuditPlan stockAuditPlanEs = stockAuditPlanSearchRepository.findById(testStockAuditPlan.getId()).get();
        assertThat(stockAuditPlanEs.getId()).isEqualToComparingFieldByField(testStockAuditPlan.getId());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void xAuditPlan6ActionApproved() throws Exception {
        int databaseSizeBeforeCreate = stockAuditPlanRepository.findAll().size();
        int databaseSizeBeforeStockAudit = stockAuditRepository.findAll().size();

        stockAuditPlan = stockAuditPlanRepository.findAll().get(0);

        restStockAuditPlanMockMvc.perform(put("/api/stock-audit-plans?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isOk());

        // Validate the StockAuditPlan in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll();
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeCreate + 1);
        StockAuditPlan testStockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        assertThat(testStockAuditPlan.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAuditPlan.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testStockAuditPlan.isLatest()).isEqualTo(DEFAULT_LATEST);
        assertThat(testStockAuditPlan.getDocument().getStatus()).isEqualTo(Status.APPROVED);

        //Check Stock Audit
        List<StockAudit> stockAuditList = stockAuditRepository.findAll();
        assertThat(stockAuditList).hasSize(databaseSizeBeforeStockAudit + 1);
        StockAudit testStockAudit = stockAuditList.get(stockAuditList.size() - 1);
        assertThat(testStockAudit.getDocument().getStatus()).isEqualTo(Status.DRAFT);
        assertThat(testStockAudit.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockAudit.isLatest()).isEqualTo(DEFAULT_LATEST);

    }

    @Test
    @Transactional
    @Rollback(false)
    public void xAuditPlan7ActionAsDraft() throws Exception {
        List<StockAuditPlan> stockAuditPlanListBefore = stockAuditPlanRepository.findAll();
        int databaseSizeBeforeCreate = stockAuditPlanListBefore.size();
        // Create the StockAuditPlan
        stockAuditPlan.setDocumentNumber(null);
        stockAuditPlan.setDocument(DEFAULT_AUDIT_PLAN_DOCUMENT);

        restStockAuditPlanMockMvc.perform(post("/api/stock-audit-plans?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isCreated());

        // Validate the StockAuditPlan in the database
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll(Sort.by("id","version"));
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeCreate + 1);
        stockAuditPlanList.removeAll(stockAuditPlanListBefore);
        StockAuditPlan testStockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        assertThat(testStockAuditPlan.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAuditPlan.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockAuditPlan.isLatest()).isEqualTo(DEFAULT_LATEST);
        assertThat(testStockAuditPlan.getDocument().getStatus()).isEqualTo(Status.DRAFT);

    }

    @Test
    @Transactional
    @Rollback(false)
    public void xAuditPlan8ActionSendForApproval() throws Exception {
        List<StockAuditPlan> stockAuditPlanListBefore = stockAuditPlanRepository.findAll();
        int databaseSizeBeforeCreate = stockAuditPlanListBefore.size();
        // Create the StockAuditPlan
        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll(Sort.by("id","version"));
        stockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        restStockAuditPlanMockMvc.perform(put("/api/stock-audit-plans?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isOk());

        // Validate the StockAuditPlan in the database
        stockAuditPlanList = stockAuditPlanRepository.findAll(Sort.by("id","version"));
        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeCreate + 1);
        stockAuditPlanList.removeAll(stockAuditPlanListBefore);
        StockAuditPlan testStockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        assertThat(testStockAuditPlan.getDocumentNumber()).isNotEmpty();
        assertThat(testStockAuditPlan.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testStockAuditPlan.isLatest()).isEqualTo(DEFAULT_LATEST);
        assertThat(testStockAuditPlan.getDocument().getStatus()).isEqualTo(Status.WAITING_FOR_APPROVAL);

    }

    @Test
    @Transactional
    @Rollback(false)
    public void xAuditPlan9ActionRejected() throws Exception {
        int databaseSizeBeforeUpdate = stockAuditPlanRepository.findAll().size();

        List<StockAuditPlan> stockAuditPlanList = stockAuditPlanRepository.findAll(Sort.by("id","version"));
        stockAuditPlan = stockAuditPlanList.get(stockAuditPlanList.size() - 1);
        restStockAuditPlanMockMvc.perform(put("/api/stock-audit-plans?action=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockAuditPlan)))
            .andExpect(status().isOk());

        // Validate the StockAuditPlan in the database and validate reserveStock in the database
        stockAuditPlanList = stockAuditPlanRepository.findAll();

        assertThat(stockAuditPlanList).hasSize(databaseSizeBeforeUpdate + 1);
        assertThat(stockAuditPlanList.get(stockAuditPlanList.size() - 1).getDocument().getStatus()).isEqualTo(Status.REJECTED);

    }
    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockAuditPlan.class);
    }

    @Test
    public void zDestroy() {
        stockAuditPlanRepository.deleteAll();
        stockAuditRepository.deleteAll();
    }
}
