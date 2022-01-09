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
import org.nh.common.dto.*;
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
import org.nh.pharmacy.domain.InventoryAdjustment;
import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.UOM;
import org.nh.pharmacy.domain.dto.Configuration;
import org.nh.pharmacy.domain.dto.InventoryAdjustmentDocument;
import org.nh.pharmacy.domain.dto.InventoryAdjustmentDocumentLine;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.enumeration.AdjustmentType;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.InventoryAdjustmentSearchRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.InventoryAdjustmentService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.repository.hibernate.type.JacksonUtil;
import org.nh.security.AuthenticatedUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the InventoryAdjustmentResource REST controller.
 *
 * @see InventoryAdjustmentResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class InventoryAdjustmentResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private InventoryAdjustmentDocument DEFAULT_DOCUMENT = new InventoryAdjustmentDocument();
    private static final String UPDATED_DOCUMENT = "BBBBBBBBBB";

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;

    @Autowired
    private InventoryAdjustmentRepository inventoryAdjustmentRepository;

    @Autowired
    private InventoryAdjustmentService inventoryAdjustmentService;

    @Autowired
    private InventoryAdjustmentSearchRepository inventoryAdjustmentSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private LocatorRepository locatorRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private UOMRepository uomRepository;

    @Autowired
    private SpringSecurityIdentityProvider identityProvider;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private GroupService groupService;

    private MockMvc restInventoryAdjustmentMockMvc;

    private MockMvc restWorkflowMockMvc;

    private InventoryAdjustment inventoryAdjustment;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InventoryAdjustmentResource inventoryAdjustmentResource = new InventoryAdjustmentResource(inventoryAdjustmentService, applicationProperties,inventoryAdjustmentRepository,inventoryAdjustmentSearchRepository);
        this.restInventoryAdjustmentMockMvc = MockMvcBuilders.standaloneSetup(inventoryAdjustmentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
        WorkflowResource workflowResource = new WorkflowResource(workflowService, identityProvider, jbpmTaskSearchRepository, jbpmProperties);
        this.restWorkflowMockMvc = MockMvcBuilders.standaloneSetup(workflowResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }


    private InventoryAdjustmentDocument createAdjustmentDocument() {

        InventoryAdjustmentDocument inventoryAdjustmentDocument = new InventoryAdjustmentDocument();
        inventoryAdjustmentDocument.setCreatedBy(createUser(1l, "Nitesh", "empl"));
        inventoryAdjustmentDocument.setDocumentType(TransactionType.Inventory_Adjustment);
        inventoryAdjustmentDocument.setDocumentDate(LocalDateTime.now());
        inventoryAdjustmentDocument.setModifiedDate(LocalDateTime.now());
        inventoryAdjustmentDocument.setStore(createStore());
        inventoryAdjustmentDocument.setUnit(createUnit());
        inventoryAdjustmentDocument.setLines(createAdjustmentLines());
        inventoryAdjustmentDocument.setStatus(Status.DRAFT);
        inventoryAdjustmentDocument.setApprovedDate(LocalDateTime.now());
        inventoryAdjustmentDocument.setCreatedDate(LocalDateTime.now());
        inventoryAdjustmentDocument.setStoreContact(createUser(1l, "Test", "empl"));
        return inventoryAdjustmentDocument;
    }

    private static UserDTO createUser(Long id, String name, String empNo) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setLogin(name);
        user.setEmployeeNo(empNo);
        user.setDisplayName(name);
        return user;
    }

    public HealthcareServiceCenterDTO createStore() {
        org.nh.pharmacy.domain.HealthcareServiceCenter store = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO storeDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(store, storeDTO);
        return storeDTO;
    }

    private OrganizationDTO createUnit() {
        org.nh.pharmacy.domain.Organization unit = organizationRepository.findById(1l).get();
        OrganizationDTO unitDTO = new OrganizationDTO();
        BeanUtils.copyProperties(unit, unitDTO);
        return unitDTO;
    }

    private List<InventoryAdjustmentDocumentLine> createAdjustmentLines() {
        Stock stock = stockRepository.findById(51l).get();
        List<InventoryAdjustmentDocumentLine> lines = new ArrayList<>();
        InventoryAdjustmentDocumentLine positiveAdjustmentDocumentLine = new InventoryAdjustmentDocumentLine();
        positiveAdjustmentDocumentLine.setStockId(stock.getId());
        positiveAdjustmentDocumentLine.setBatchNumber(stock.getBatchNo());
        positiveAdjustmentDocumentLine.setConsignment(stock.isConsignment());
        positiveAdjustmentDocumentLine.setCost(stock.getCost());
        positiveAdjustmentDocumentLine.setExpiryDate(LocalDate.now());
        positiveAdjustmentDocumentLine.setItem(createItem(1l));
        positiveAdjustmentDocumentLine.setLocator(createLocator());
        positiveAdjustmentDocumentLine.setMrp(stock.getMrp());
        positiveAdjustmentDocumentLine.setOwner(stock.getOwner());
        positiveAdjustmentDocumentLine.setSku(stock.getSku());
        positiveAdjustmentDocumentLine.setAdjustmentType(AdjustmentType.POSITIVE_ADJUSTMENT);
        positiveAdjustmentDocumentLine.setAdjustQuantity(createQuantity(10f, uomRepository.findById(stock.getUomId()).get()));
        positiveAdjustmentDocumentLine.setStockQuantity(createQuantity(stock.getQuantity(), uomRepository.findById(stock.getUomId()).get()));
        lines.add(positiveAdjustmentDocumentLine);
        InventoryAdjustmentDocumentLine negativeAdjustmentDocumentLine = new InventoryAdjustmentDocumentLine();
        negativeAdjustmentDocumentLine.setStockId(stock.getId());
        negativeAdjustmentDocumentLine.setBatchNumber(stock.getBatchNo());
        negativeAdjustmentDocumentLine.setConsignment(stock.isConsignment());
        negativeAdjustmentDocumentLine.setCost(stock.getCost());
        negativeAdjustmentDocumentLine.setExpiryDate(LocalDate.now());
        negativeAdjustmentDocumentLine.setItem(createItem(1l));
        negativeAdjustmentDocumentLine.setLocator(createLocator());
        negativeAdjustmentDocumentLine.setMrp(stock.getMrp());
        negativeAdjustmentDocumentLine.setOwner(stock.getOwner());
        negativeAdjustmentDocumentLine.setSku(stock.getSku());
        negativeAdjustmentDocumentLine.setAdjustmentType(AdjustmentType.NEGATIVE_ADJUSTMENT);
        negativeAdjustmentDocumentLine.setAdjustQuantity(createQuantity(10f, uomRepository.findById(stock.getUomId()).get()));
        negativeAdjustmentDocumentLine.setStockQuantity(createQuantity(stock.getQuantity(), uomRepository.findById(stock.getUomId()).get()));
        lines.add(negativeAdjustmentDocumentLine);
        return lines;
    }

    private LocatorDTO createLocator() {
        org.nh.pharmacy.domain.Locator locator = locatorRepository.findById(1l).get();
        LocatorDTO locatorDto = new LocatorDTO();
        BeanUtils.copyProperties(locator, locatorDto);
        return locatorDto;
    }

    private static Quantity createQuantity(Float value, UOM uom) {
        Quantity quantity = new Quantity();
        quantity.setValue(value);
        quantity.setUom(uom.getUOMDTO());
        return quantity;
    }

    private ItemDTO createItem(Long id) {
        org.nh.pharmacy.domain.Item item = itemRepository.findById(id).get();
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);
        return itemDto;
    }

    public void createIndex() {
        if (elasticsearchTemplate.indexExists("configuration"))
            elasticsearchTemplate.deleteIndex("configuration");
        elasticsearchTemplate.createIndex("configuration");

        if (!elasticsearchTemplate.indexExists("organization"))
            elasticsearchTemplate.createIndex("organization");

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(1l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_inventoryadjustment_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(1l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_inventoryadjustment_workflow_definition");
        configuration2.setValue("inventory_adjustment_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(1l);
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
    public InventoryAdjustment createEntity(EntityManager em) {
        DEFAULT_DOCUMENT = createAdjustmentDocument();
        InventoryAdjustment inventoryAdjustment = new InventoryAdjustment()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_DOCUMENT);
        return inventoryAdjustment;
    }

    @Before
    public void initTest() {
        inventoryAdjustmentSearchRepository.deleteAll();
        inventoryAdjustment = createEntity(em);
        groupService.doIndex();
        addUserToSecurityContext(1l, "admin", "admin");
    }

    public static void addUserToSecurityContext(Long userId, String userName, String password) {
        AuthenticatedUser user = new AuthenticatedUser(userName, password, Collections.emptyList());
        user.setPreferences(new org.nh.security.dto.Preferences());
        user.getPreferences().setHospital(new org.nh.security.dto.Organization().code("1001"));
        user.getPreferences().getHospital().setId(1L);
        user.getPreferences().setUser(new org.nh.security.dto.User());
        user.getPreferences().getUser().setId(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null, Collections.emptyList());
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void inventoryAdjustmentWithActionApproved() throws Exception {
        createIndex();
        int reserveStockDatabaseSizeBefore = reserveStockRepository.findAll().size();
        Stock stockBefore = stockRepository.findById(51l).get();

        MvcResult result = restInventoryAdjustmentMockMvc.perform(post("/api/inventory-adjustments?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isCreated()).andReturn();

        inventoryAdjustment = JacksonUtil.fromString(result.getResponse().getContentAsString(), InventoryAdjustment.class);

        result = restInventoryAdjustmentMockMvc.perform(put("/api/inventory-adjustments?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isOk()).andReturn();

        inventoryAdjustment = JacksonUtil.fromString(result.getResponse().getContentAsString(), InventoryAdjustment.class);

        restInventoryAdjustmentMockMvc.perform(put("/api/inventory-adjustments?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isOk());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();
        Stock stockAfter = stockRepository.findById(51l).get();

        assertThat(reserveStockList).hasSize(reserveStockDatabaseSizeBefore + 1);
        assertThat(stockAfter.getQuantity()).isEqualTo(stockBefore.getQuantity() + 10);

    }

    @Test
    public void inventoryAdjustmentWithActionRejected() throws Exception {
        createIndex();
        int reserveStockDatabaseSizeBefore = reserveStockRepository.findAll().size();

        MvcResult result = restInventoryAdjustmentMockMvc.perform(post("/api/inventory-adjustments?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isCreated()).andReturn();

        inventoryAdjustment = JacksonUtil.fromString(result.getResponse().getContentAsString(), InventoryAdjustment.class);

        restInventoryAdjustmentMockMvc.perform(put("/api/inventory-adjustments?action=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isOk());

        List<ReserveStock> reserveStockList = reserveStockRepository.findAll();

        assertThat(reserveStockList).hasSize(reserveStockDatabaseSizeBefore);
    }

    @Test
    @Transactional
    public void createInventoryAdjustmentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = inventoryAdjustmentRepository.findAll().size();

        // Create the InventoryAdjustment with an existing ID
        InventoryAdjustment existingInventoryAdjustment = new InventoryAdjustment();
        existingInventoryAdjustment.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restInventoryAdjustmentMockMvc.perform(post("/api/inventory-adjustments")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingInventoryAdjustment)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<InventoryAdjustment> inventoryAdjustmentList = inventoryAdjustmentRepository.findAll();
        assertThat(inventoryAdjustmentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = inventoryAdjustmentRepository.findAll().size();
        // set the field null
        inventoryAdjustment.setDocument(null);

        // Create the InventoryAdjustment, which fails.

        restInventoryAdjustmentMockMvc.perform(post("/api/inventory-adjustments")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isBadRequest());

        List<InventoryAdjustment> inventoryAdjustmentList = inventoryAdjustmentRepository.findAll();
        assertThat(inventoryAdjustmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllInventoryAdjustments() throws Exception {
        // Initialize the database
        inventoryAdjustmentService.save(inventoryAdjustment);

        // Get all the inventoryAdjustmentList
        restInventoryAdjustmentMockMvc.perform(get("/api/inventory-adjustments?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(inventoryAdjustment.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.createdBy.displayName").value(hasItem(DEFAULT_DOCUMENT.getCreatedBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getInventoryAdjustment() throws Exception {
        // Initialize the database
        inventoryAdjustmentService.save(inventoryAdjustment);

        // Get the inventoryAdjustment
        restInventoryAdjustmentMockMvc.perform(get("/api/inventory-adjustments/{id}", inventoryAdjustment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(inventoryAdjustment.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.document.createdBy.displayName").value(DEFAULT_DOCUMENT.getCreatedBy().getDisplayName()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingInventoryAdjustment() throws Exception {
        // Get the inventoryAdjustment
        restInventoryAdjustmentMockMvc.perform(get("/api/inventory-adjustments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInventoryAdjustment() throws Exception {
        // Initialize the database
        inventoryAdjustmentService.save(inventoryAdjustment);

        int databaseSizeBeforeUpdate = inventoryAdjustmentRepository.findAll().size();

        // Update the inventoryAdjustment
        InventoryAdjustment updatedInventoryAdjustment = inventoryAdjustmentRepository.findOne(inventoryAdjustment.getId());
        updatedInventoryAdjustment
            .documentNumber(UPDATED_DOCUMENT_NUMBER);
                /*.document(UPDATED_DOCUMENT)
                .version(UPDATED_VERSION)
                .latest(UPDATED_LATEST);*/

        restInventoryAdjustmentMockMvc.perform(put("/api/inventory-adjustments")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedInventoryAdjustment)))
            .andExpect(status().isOk());

        // Validate the InventoryAdjustment in the database
        List<InventoryAdjustment> inventoryAdjustmentList = inventoryAdjustmentRepository.findAll(Sort.by("id", "version"));
        assertThat(inventoryAdjustmentList).hasSize(databaseSizeBeforeUpdate + 1);
        InventoryAdjustment testInventoryAdjustment = inventoryAdjustmentList.get(inventoryAdjustmentList.size() - 1);
        assertThat(testInventoryAdjustment.getDocumentNumber()).isNotEmpty();
        assertThat(testInventoryAdjustment.getVersion()).isEqualTo(UPDATED_VERSION);

        // Validate the InventoryAdjustment in Elasticsearch
        InventoryAdjustment inventoryAdjustmentEs = inventoryAdjustmentSearchRepository.findById(testInventoryAdjustment.getId()).get();
        assertThat(inventoryAdjustmentEs.getId()).isEqualToComparingFieldByField(testInventoryAdjustment.getId());
        assertThat(inventoryAdjustmentEs.getDocument().getCreatedBy()).isEqualToComparingFieldByField(testInventoryAdjustment.getDocument().getCreatedBy());
    }

    @Test
    @Transactional
    public void updateNonExistingInventoryAdjustment() throws Exception {
        int databaseSizeBeforeUpdate = inventoryAdjustmentRepository.findAll().size();

        // Create the InventoryAdjustment

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restInventoryAdjustmentMockMvc.perform(put("/api/inventory-adjustments")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isCreated());

        // Validate the InventoryAdjustment in the database
        List<InventoryAdjustment> inventoryAdjustmentList = inventoryAdjustmentRepository.findAll();
        assertThat(inventoryAdjustmentList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteInventoryAdjustment() throws Exception {
        // Initialize the database
        inventoryAdjustmentService.save(inventoryAdjustment);

        int databaseSizeBeforeDelete = inventoryAdjustmentRepository.findAll().size();

        // Get the inventoryAdjustment
        restInventoryAdjustmentMockMvc.perform(delete("/api/inventory-adjustments/{id}", inventoryAdjustment.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean inventoryAdjustmentExistsInEs = inventoryAdjustmentSearchRepository.existsById(inventoryAdjustment.getId());
        assertThat(inventoryAdjustmentExistsInEs).isFalse();

        // Validate the database is empty
        List<InventoryAdjustment> inventoryAdjustmentList = inventoryAdjustmentRepository.findAll();
        assertThat(inventoryAdjustmentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchInventoryAdjustment() throws Exception {
        // Initialize the database
        inventoryAdjustmentService.save(inventoryAdjustment);

        // Search the inventoryAdjustment
        restInventoryAdjustmentMockMvc.perform(get("/api/_search/inventory-adjustments?query=id:" + inventoryAdjustment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(inventoryAdjustment.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.createdBy.displayName").value(hasItem(DEFAULT_DOCUMENT.getCreatedBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getInventoryAdjustmentStatusCount() throws Exception {
        // Initialize the database
        inventoryAdjustmentService.save(inventoryAdjustment);

        restInventoryAdjustmentMockMvc.perform(get("/api/status-count/inventory-adjustments?query=id:" + inventoryAdjustment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.DRAFT").value(1));

    }

    @Test
    public void verifyWorkflow() throws Exception {

        createIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");

        MvcResult result = restInventoryAdjustmentMockMvc.perform(post("/api/inventory-adjustments?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isCreated()).andReturn();

        inventoryAdjustment = JacksonUtil.fromString(result.getResponse().getContentAsString(), InventoryAdjustment.class);

        result = restInventoryAdjustmentMockMvc.perform(put("/api/inventory-adjustments?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isOk()).andReturn();

        addUserToSecurityContext(3L, "90011X", "Approver");

        inventoryAdjustment = JacksonUtil.fromString(result.getResponse().getContentAsString(), InventoryAdjustment.class);

        MvcResult taskResult = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(taskResult.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        taskResult = restInventoryAdjustmentMockMvc.perform(get("/api/_workflow/inventory-adjustments?documentNumber=" + inventoryAdjustment.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(taskResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        result = restInventoryAdjustmentMockMvc.perform(put("/api/_workflow/inventory-adjustments?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isOk()).andReturn();

        addUserToSecurityContext(3L, "90011Y", "Manager");

        inventoryAdjustment = JacksonUtil.fromString(result.getResponse().getContentAsString(), InventoryAdjustment.class);

        taskResult = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        taskList = new ObjectMapper().readValue(taskResult.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        taskResult = restInventoryAdjustmentMockMvc.perform(get("/api/_workflow/inventory-adjustments?documentNumber=" + inventoryAdjustment.getDocumentNumber() + "&userId=90011Y"))
            .andExpect(status().isOk()).andReturn();
        taskDetails = new ObjectMapper().readValue(taskResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restInventoryAdjustmentMockMvc.perform(put("/api/_workflow/inventory-adjustments?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(inventoryAdjustment)))
            .andExpect(status().isOk());

        inventoryAdjustmentRepository.deleteAll();
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InventoryAdjustment.class);
    }
}
