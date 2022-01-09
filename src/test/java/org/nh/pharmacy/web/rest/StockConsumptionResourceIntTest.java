package org.nh.pharmacy.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
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
import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.StockConsumption;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.StockConsumptionSearchRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.ItemService;
import org.nh.pharmacy.service.StockConsumptionService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.repository.hibernate.type.JacksonUtil;
import org.nh.security.AuthenticatedUser;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockConsumptionResource REST controller.
 *
 * @see StockConsumptionResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockConsumptionResourceIntTest {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;
    private int pageNo=1;
    private int pageSize=10;

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final ConsumptionDocument DEFAULT_DOCUMENT = new ConsumptionDocument();
    private static final ConsumptionDocument DEFAULT_CONSUMPTION_DOCUMENT = createConsumptionDocument();
    private static final ConsumptionDocument UPDATED_DOCUMENT = createConsumptionDocument();

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAA";
    private static final String UPDATED_EMPLOYEE_NO = "BBBBBBBBBB";

    private static final UserDTO DEFAULT_USER = createUser(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DISPLAY_NAME, DEFAULT_EMPLOYEE_NO);
    private static final UserDTO UPDATED_USER = createUser(UPDATED_ID, UPDATED_NAME, UPDATED_DISPLAY_NAME, UPDATED_EMPLOYEE_NO);

    private static List<ConsumptionDocumentLine> DEFAULT_LINE = new ArrayList<>();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    @Autowired
    private StockConsumptionRepository stockConsumptionRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private StockConsumptionService stockConsumptionService;

    @Autowired
    private StockConsumptionSearchRepository stockConsumptionSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private SpringSecurityIdentityProvider identityProvider;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UOMRepository uomRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc restStockConsumptionMockMvc;

    private StockConsumption stockConsumption;

    private MockMvc restWorkflowMockMvc;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockConsumptionResource stockConsumptionResource = new StockConsumptionResource(stockConsumptionService,stockConsumptionRepository,stockConsumptionSearchRepository,applicationProperties);
        this.restStockConsumptionMockMvc = MockMvcBuilders.standaloneSetup(stockConsumptionResource)
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

    public static UOMDTO createUom() {
        UOMDTO uom = new UOMDTO();
        uom.setId(1l);
        uom.setCode("DEFAULT_UOM");
        uom.setName("DEFAULT_NAME");
        return uom;
    }

    /**
     * Returns object of ConsumptionDocumentLine
     *
     * @return
     */
    public static ConsumptionDocumentLine createDocumentLine() {
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        quantity.setUom(createUom());
        ItemDTO item = new ItemDTO();
        item.setId(12l);
        item.setCode("code");
        ConsumptionDocumentLine consumptionDocumentLine = new ConsumptionDocumentLine();
        consumptionDocumentLine.setQuantity(quantity);
        consumptionDocumentLine.setBatchNumber("batch");
        consumptionDocumentLine.setConsignment(false);
        consumptionDocumentLine.setCost(BigDecimal.TEN);
        consumptionDocumentLine.setItem(item);
        return consumptionDocumentLine;
    }

    /**
     * Creates ConsumptionDocument
     *
     * @return
     */
    public static ConsumptionDocument createConsumptionDocument() {

        ConsumptionDocument consumptionDocument = new ConsumptionDocument();
        consumptionDocument.setForDepartment(createOrganization());
        consumptionDocument.setConsumptionUnit(createOrganization());
        consumptionDocument.setDocumentType(TransactionType.Stock_Consumption);
        consumptionDocument.setConsumedBy(createUser(1l, "AAAAAAAAAA", "AAAAAAAAAA", "AAAAAAAAAA"));
        consumptionDocument.setCreatedBy(createUser(2l, "AAAAAAAAAA", "AAAAAAAAAA", "AAAAAAAAAA"));
        consumptionDocument.setRequestedBy(createUser(3l, "AAAAAAAAAA", "AAAAAAAAAA", "AAAAAAAAAA"));
        consumptionDocument.setConsumptionStore(createStore());
        consumptionDocument.setLines(Arrays.asList(createDocumentLine()));
        consumptionDocument.setConsumedDate(LocalDateTime.now());
        consumptionDocument.setCreatedDate(LocalDateTime.now());
        consumptionDocument.setDocumentType(TransactionType.Stock_Consumption);
        consumptionDocument.setConsumptionStore(createStore());
        consumptionDocument.setForHSC(createStore());
        consumptionDocument.setStatus(Status.DRAFT);
        return consumptionDocument;
    }

    /**
     * Returns Object of HealthcareServiceCenter
     *
     * @return
     */
    public static HealthcareServiceCenterDTO createStore() {
        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(2l);
        hsc.setCode("NH");
        hsc.setName("Narayana HealthCare");
        hsc.setPartOf(createOrganization());
        return hsc;
    }

    /**
     * Returns Object of Organization
     *
     * @return
     */
    public static OrganizationDTO createOrganization() {
        OrganizationDTO organization = new OrganizationDTO();
        organization.setId(1l);
        organization.setName("HSR");
        organization.setCode("HSR");
        return organization;
    }

    /**
     * Create Entity of Stock
     *
     * @return
     */
    public static Stock createEntityStock() {
        Stock stock = new Stock()
            .itemId(12l)
            .batchNo("batch")
            .expiryDate(LocalDate.now())
            .owner("owner")
            .cost(BigDecimal.TEN)
            .mrp(getBigDecimal(1000f))
            .quantity(100f)
            .stockValue(getBigDecimal(1000f))
            .storeId(1l)
            .locatorId(1l)
            .supplier("cipla")
            .uomId(1l)
            .sku("sku1")
            .unitId(1l)
            .consignment(false);
        return stock;
    }


    public ConsumptionDocumentLine createDefaultDocumentLine() {
        org.nh.pharmacy.domain.Item item = itemService.findOne(1l);
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);
        ConsumptionDocumentLine consumptionDocumentLine = new ConsumptionDocumentLine();
        consumptionDocumentLine.setQuantity(createDefaultQuantity());
        consumptionDocumentLine.setBatchNumber("BTH12345");
        consumptionDocumentLine.setConsignment(false);
        consumptionDocumentLine.setCost(BigDecimal.TEN);
        consumptionDocumentLine.setItem(itemDto);
        consumptionDocumentLine.setStockId(51l);
        return consumptionDocumentLine;
    }

    private Quantity createDefaultQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(20f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }


    public ConsumptionDocument createDefaultConsumptionDocument() {

        ConsumptionDocument consumptionDocument = new ConsumptionDocument();
        org.nh.pharmacy.domain.Organization ConsumptionUnit = organizationRepository.findById(1l).get();
        OrganizationDTO ConsumptionUnitDTO = new OrganizationDTO();
        BeanUtils.copyProperties(ConsumptionUnit, ConsumptionUnitDTO);
        consumptionDocument.setForDepartment(ConsumptionUnitDTO);
        consumptionDocument.setConsumptionUnit(ConsumptionUnitDTO);
        consumptionDocument.setDocumentType(TransactionType.Stock_Consumption);
        consumptionDocument.setConsumedBy(createUser(1l, "AAAAAAAAAA", "AAAAAAAAAA", "AAAAAAAAAA"));
        consumptionDocument.setLines(Arrays.asList(createDefaultDocumentLine()));
        consumptionDocument.setConsumedDate(LocalDateTime.now());
        consumptionDocument.setCreatedDate(LocalDateTime.now());
        consumptionDocument.setCreatedBy(createUser(2l, "AAAAAAAAAA", "AAAAAAAAAA", "AAAAAAAAAA"));
        consumptionDocument.setRequestedBy(createUser(3l, "AAAAAAAAAA", "AAAAAAAAAA", "AAAAAAAAAA"));
        org.nh.pharmacy.domain.HealthcareServiceCenter consumptionHealthcareServiceCenter = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO consumptionHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(consumptionHealthcareServiceCenter, consumptionHealthcareServiceCenterDTO);
        consumptionDocument.setConsumptionStore(consumptionHealthcareServiceCenterDTO);
        consumptionDocument.setForHSC(consumptionHealthcareServiceCenterDTO);
        consumptionDocument.setStatus(Status.DRAFT);
        return consumptionDocument;
    }


    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StockConsumption createEntity() {
        DEFAULT_DOCUMENT.setConsumedBy(DEFAULT_USER);
        DEFAULT_DOCUMENT.setLines(DEFAULT_LINE);
        DEFAULT_DOCUMENT.setStatus(Status.DRAFT);
        DEFAULT_DOCUMENT.setDraft(true);
        DEFAULT_DOCUMENT.setConsumedDate(LocalDateTime.now());
        DEFAULT_DOCUMENT.setConsumptionStore(createStore());
        DEFAULT_DOCUMENT.setDocumentType(TransactionType.Stock_Consumption);
        StockConsumption stockConsumption = new StockConsumption()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(createConsumptionDocument())
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockConsumption;
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

    @Before
    public void initTest() {
        stockConsumptionSearchRepository.deleteAll();
        stockConsumption = createEntity();
        groupService.doIndex();
        addUserToSecurityContext(1L, "admin", "admin");
    }

    @Test
    public void aCreateIndex() {
        Map<String, Object> objectMap = new HashMap<>();
        if (elasticsearchTemplate.indexExists("configurations"))
            elasticsearchTemplate.deleteIndex("configurations");
        elasticsearchTemplate.createIndex("configurations");
        objectMap.put("athma_stockconsumption_enable_workflow", "Yes");
        org.nh.pharmacy.dto.Configurations conf = new org.nh.pharmacy.dto.Configurations();
        conf.setId(1l);
        conf.setApplicableTo(1l);
        conf.setApplicableType("Unit");
        conf.setConfiguration(objectMap);
        IndexQuery indexQuery = new IndexQueryBuilder().withId("1").withObject(conf).build();
        elasticsearchTemplate.index(indexQuery, IndexCoordinates.of("configurations"));
        elasticsearchTemplate.refresh(Configurations.class);

        //Config for workflow test

        if (elasticsearchTemplate.indexExists("configuration"))
            elasticsearchTemplate.deleteIndex("configuration");
        elasticsearchTemplate.createIndex("configuration");

        Configuration configuration = new Configuration();
        configuration.setApplicableCode("1001");
        configuration.setApplicableTo(1l);
        configuration.setApplicableType(ConfigurationLevel.Global);
        configuration.setKey("athma_stockconsumption_enable_workflow");
        configuration.setValue("Yes");
        configuration.setLevel(1);

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(1l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockconsumption_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(1l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_stockconsumption_workflow_definition");
        configuration2.setValue("stock_consumption_document_process");
        configuration2.setLevel(2);


        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(1l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_date_format");
        configuration3.setValue("dd/MM/yy");
        configuration3.setLevel(2);

        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration1).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration2).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("4").withObject(configuration3).build();

        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);
        elasticsearchTemplate.index(indexQuery3, coordinates);
        elasticsearchTemplate.index(indexQuery4, coordinates);

        elasticsearchTemplate.refresh(Configuration.class);
    }


    @Test
    public void createStockConsumption_Draft() throws Exception {
        int databaseSizeBeforeCreate = stockConsumptionRepository.findAll().size();

        // Create the StockConsumption
        stockConsumption.setDocumentNumber(null);
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        restStockConsumptionMockMvc.perform(post("/api/stock-consumptions?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isCreated());

        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionList = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionList).hasSize(databaseSizeBeforeCreate + 1);
        StockConsumption testStockConsumption = stockConsumptionList.get(stockConsumptionList.size() - 1);
        assertThat(testStockConsumption.getDocumentNumber()).isNotEmpty();
        assertThat(testStockConsumption.getDocument().getConsumedBy().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockConsumption.getDocument().getConsumedBy().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockConsumption.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);
        assertThat(testStockConsumption.getDocument().getStatus().toString()).isEqualTo("DRAFT");

        // Validate the StockConsumption in Elasticsearch
        StockConsumption stockConsumptionEs = stockConsumptionSearchRepository.findById(testStockConsumption.getId()).get();
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getLogin()).isEqualTo(testStockConsumption.getDocument().getConsumedBy().getLogin());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getDisplayName()).isEqualTo(testStockConsumption.getDocument().getConsumedBy().getDisplayName());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(testStockConsumption.getDocument().getConsumedBy().getEmployeeNo());
        assertThat(stockConsumptionEs.getDocumentNumber()).isEqualTo(testStockConsumption.getDocumentNumber());
    }


    @Test
    public void createStockConsumption_Draft_SFA_Reject() throws Exception {
        int databaseSizeBeforeCreate = stockConsumptionRepository.findAll().size();

        // Create the StockConsumption
        stockConsumption.setDocumentNumber(null);
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        restStockConsumptionMockMvc.perform(post("/api/stock-consumptions?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isCreated());

        List<StockConsumption> stockConsumptionListA = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListA).hasSize(databaseSizeBeforeCreate + 1);
        StockConsumption testStockConsumptionA = stockConsumptionListA.get(stockConsumptionListA.size() - 1);

        stockConsumption = stockConsumptionService.findOne(testStockConsumptionA.getId());

        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());
        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionListB = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListB).hasSize(databaseSizeBeforeCreate + 2);
        StockConsumption testStockConsumptionB = stockConsumptionListB.get(stockConsumptionListB.size() - 1);

        assertThat(testStockConsumptionB.getDocumentNumber()).isNotEmpty();
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);
        assertThat(testStockConsumptionB.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockConsumption = stockConsumptionService.findOne(testStockConsumptionA.getId());

        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions?action=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());
        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionListC = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListC).hasSize(databaseSizeBeforeCreate + 3);
        StockConsumption testStockConsumptionC = stockConsumptionListC.get(stockConsumptionListC.size() - 1);

        assertThat(testStockConsumptionC.getDocumentNumber()).isNotEmpty();
        assertThat(testStockConsumptionC.getDocument().getConsumedBy().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockConsumptionC.getDocument().getConsumedBy().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockConsumptionC.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);
        assertThat(testStockConsumptionC.getDocument().getStatus().toString()).isEqualTo("REJECTED");

        // Validate the StockConsumption in Elasticsearch
        StockConsumption stockConsumptionEs = stockConsumptionSearchRepository.findById(testStockConsumptionB.getId()).get();
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getLogin()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getLogin());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getDisplayName()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getDisplayName());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getEmployeeNo());
        assertThat(stockConsumptionEs.getDocumentNumber()).isEqualTo(testStockConsumptionC.getDocumentNumber());
    }


    @Test
    public void createStockConsumption_Draft_SFA() throws Exception {
        int databaseSizeBeforeCreate = stockConsumptionRepository.findAll().size();

        // Create the StockConsumption
        stockConsumption.setDocumentNumber(null);
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        restStockConsumptionMockMvc.perform(post("/api/stock-consumptions?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isCreated());

        List<StockConsumption> stockConsumptionListA = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListA).hasSize(databaseSizeBeforeCreate + 1);
        StockConsumption testStockConsumptionA = stockConsumptionListA.get(stockConsumptionListA.size() - 1);

        stockConsumption = stockConsumptionService.findOne(testStockConsumptionA.getId());

        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());
        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionListB = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListB).hasSize(databaseSizeBeforeCreate + 2);
        StockConsumption testStockConsumptionB = stockConsumptionListB.get(stockConsumptionListB.size() - 1);

        assertThat(testStockConsumptionB.getDocumentNumber()).isNotEmpty();
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);
        assertThat(testStockConsumptionB.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        // Validate the StockConsumption in Elasticsearch
        StockConsumption stockConsumptionEs = stockConsumptionSearchRepository.findById(testStockConsumptionB.getId()).get();
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getLogin()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getLogin());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getDisplayName()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getDisplayName());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getEmployeeNo());
        assertThat(stockConsumptionEs.getDocumentNumber()).isEqualTo(testStockConsumptionB.getDocumentNumber());
    }


    @Test
    public void createStockConsumption_Draft_SFA_Approved() throws Exception {
        int databaseSizeBeforeCreate = stockConsumptionRepository.findAll().size();

        // Create the StockConsumption
        stockConsumption.setDocumentNumber(null);
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        restStockConsumptionMockMvc.perform(post("/api/stock-consumptions?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isCreated());

        List<StockConsumption> stockConsumptionListA = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListA).hasSize(databaseSizeBeforeCreate + 1);
        StockConsumption testStockConsumptionA = stockConsumptionListA.get(stockConsumptionListA.size() - 1);

        stockConsumption = stockConsumptionService.findOne(testStockConsumptionA.getId());

        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());
        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionListB = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListB).hasSize(databaseSizeBeforeCreate + 2);
        StockConsumption testStockConsumptionB = stockConsumptionListB.get(stockConsumptionListB.size() - 1);

        assertThat(testStockConsumptionB.getDocumentNumber()).isNotEmpty();
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockConsumptionB.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);
        assertThat(testStockConsumptionB.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockConsumption = stockConsumptionService.findOne(testStockConsumptionA.getId());

        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());
        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionListC = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionListC).hasSize(databaseSizeBeforeCreate + 3);
        StockConsumption testStockConsumptionC = stockConsumptionListC.get(stockConsumptionListC.size() - 1);

        assertThat(testStockConsumptionC.getDocumentNumber()).isNotEmpty();
        assertThat(testStockConsumptionC.getDocument().getConsumedBy().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockConsumptionC.getDocument().getConsumedBy().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockConsumptionC.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);
        assertThat(testStockConsumptionC.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        // Validate the StockConsumption in Elasticsearch
        StockConsumption stockConsumptionEs = stockConsumptionSearchRepository.findById(testStockConsumptionB.getId()).get();
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getLogin()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getLogin());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getDisplayName()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getDisplayName());
        assertThat(stockConsumptionEs.getDocument().getConsumedBy().getEmployeeNo()).isEqualTo(testStockConsumptionB.getDocument().getConsumedBy().getEmployeeNo());
        assertThat(stockConsumptionEs.getDocumentNumber()).isEqualTo(testStockConsumptionC.getDocumentNumber());
    }

    @Test
    @Transactional
    public void createStockConsumptionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockConsumptionRepository.findAll().size();

        // Create the StockConsumption with an existing ID
        StockConsumption existingStockConsumption = new StockConsumption();
        existingStockConsumption.setId(1L);
        existingStockConsumption.setVersion(0);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockConsumptionMockMvc.perform(post("/api/stock-consumptions")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockConsumption)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockConsumption> stockConsumptionList = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockConsumptionRepository.findAll().size();
        // set the field null
        stockConsumption.setDocument(null);

        // Create the StockConsumption, which fails.

        restStockConsumptionMockMvc.perform(post("/api/stock-consumptions")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isBadRequest());

        List<StockConsumption> stockConsumptionList = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockConsumptions() throws Exception {
        // Initialize the database
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        stockConsumptionService.save(stockConsumption);

        // Get all the stockConsumptionList
        restStockConsumptionMockMvc.perform(get("/api/stock-consumptions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockConsumption.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.consumedBy.login").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].document.consumedBy.displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].document.consumedBy.employeeNo").value(hasItem(DEFAULT_EMPLOYEE_NO)));
    }

    @Test
    @Transactional
    public void getStockConsumption() throws Exception {
        // Initialize the database
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        stockConsumptionService.save(stockConsumption);
        // Get the stockConsumption
        restStockConsumptionMockMvc.perform(get("/api/stock-consumptions/{id}", stockConsumption.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockConsumption.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.document.consumedBy.login").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.document.consumedBy.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.document.consumedBy.employeeNo").value(DEFAULT_EMPLOYEE_NO));
    }

    @Test
    @Transactional
    public void getNonExistingStockConsumption() throws Exception {
        // Get the stockConsumption
        restStockConsumptionMockMvc.perform(get("/api/stock-consumptions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNonExistingStockConsumption() throws Exception {
        int databaseSizeBeforeUpdate = stockConsumptionRepository.findAll().size();

        // Create the StockConsumption
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isCreated());

        // Validate the StockConsumption in the database
        List<StockConsumption> stockConsumptionList = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockConsumption() throws Exception {
        // Initialize the database
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        stockConsumptionService.save(stockConsumption);

        int databaseSizeBeforeDelete = stockConsumptionRepository.findAll().size();

        // Get the stockConsumption
        restStockConsumptionMockMvc.perform(delete("/api/stock-consumptions/{id}", stockConsumption.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockConsumptionExistsInEs = stockConsumptionSearchRepository.existsById(stockConsumption.getId());
        assertThat(stockConsumptionExistsInEs).isFalse();

        // Validate the database is empty
        List<StockConsumption> stockConsumptionList = stockConsumptionRepository.findAll();
        assertThat(stockConsumptionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockConsumption() throws Exception {
        // Initialize the database
        stockConsumption.setDocument(createDefaultConsumptionDocument());
        stockConsumptionService.save(stockConsumption);
        stockConsumptionService.doIndex(pageNo,pageSize, LocalDate.now(), LocalDate.now());
        // Search the stockConsumption
        restStockConsumptionMockMvc.perform(get("/api/_search/stock-consumptions?query=id:" + stockConsumption.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockConsumption.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.consumedBy.login").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].document.consumedBy.displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].document.consumedBy.employeeNo").value(hasItem(DEFAULT_EMPLOYEE_NO)));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockConsumption.class);
    }

    @Test
    public void verifyWorkflow() throws Exception {

        aCreateIndex();

        stockConsumption.setDocument(createDefaultConsumptionDocument());

        addUserToSecurityContext(4L, "90011Z", "creator");

        MvcResult result = restStockConsumptionMockMvc.perform(post("/api/stock-consumptions?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isCreated()).andReturn();

        stockConsumption = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockConsumption.class);

        restStockConsumptionMockMvc.perform(put("/api/stock-consumptions?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        //Retrieve task content
        result = restWorkflowMockMvc.perform(get("/api/jbpm/process/process-variable?taskId=" + taskList.get(0).getTaskId() + "&variableName=content")).andExpect(status().isOk()).andReturn();
        Map taskContent = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Assert.assertEquals(String.valueOf(stockConsumption.getId()), String.valueOf(taskContent.get("document_id")));
        Assert.assertEquals(stockConsumption.getDocument().getDocumentType().name(), String.valueOf(taskContent.get("document_type")));


        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        stockConsumption = stockConsumptionService.findOne(stockConsumption.getId());

        result = restStockConsumptionMockMvc.perform(get("/api/_workflow/stock-consumptions?documentNumber=" + stockConsumption.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());


        restStockConsumptionMockMvc.perform(put("/api/_workflow/stock-consumptions?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockConsumption)))
            .andExpect(status().isOk());

        stockConsumptionRepository.deleteAll();
    }
}
