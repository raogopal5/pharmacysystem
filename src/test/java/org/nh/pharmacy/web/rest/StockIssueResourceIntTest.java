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
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.StockIssueSearchRepository;
import org.nh.pharmacy.service.*;
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
 * Test class for the StockIssueResource REST controller.
 *
 * @see StockIssueResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockIssueResourceIntTest {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final IssueDocument DEFAULT_ISSUE_DOCUMENT = createIssueDocument();
    private static final IssueDocument UPDATED_ISSUE_DOCUMENT = new IssueDocument();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAA";

    private static final UserDTO DEFAULT_USER = createUser(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DISPLAY_NAME, DEFAULT_EMPLOYEE_NO);


    @Autowired
    private StockIssueRepository stockIssueRepository;

    @Autowired
    private StockIssueService stockIssueService;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockIssueSearchRepository stockIssueSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private StockIndentService stockIndentService;

    @Autowired
    private StockIndentRepository stockIndentRepository;

    @Autowired
    private GroupService groupService;

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
    private ApplicationProperties applicationProperties;

    private ItemUnitAverageCost itemUnitAverageCost;

    private MockMvc restWorkflowMockMvc;

    private MockMvc restStockIssueMockMvc;

    private StockIssue stockIssue;

    private Stock stock;

    private Stock stock1;

    private Stock stock2;

    private Stock stock3;

    private StockIndent stockIndent, stockIndent1;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        UPDATED_ISSUE_DOCUMENT.setIssuedBy(createUser(1l, "User001", "emp01", "User1"));
        MockitoAnnotations.initMocks(this);
        StockIssueResource stockIssueResource = new StockIssueResource(stockIssueService,stockIssueRepository,stockIssueSearchRepository,applicationProperties);
        this.restStockIssueMockMvc = MockMvcBuilders.standaloneSetup(stockIssueResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
        WorkflowResource workflowResource = new WorkflowResource(workflowService, identityProvider, jbpmTaskSearchRepository, jbpmProperties);
        this.restWorkflowMockMvc = MockMvcBuilders.standaloneSetup(workflowResource)
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
    public static StockIssue createEntityStockIssue(EntityManager em) {
        DEFAULT_ISSUE_DOCUMENT.setDraft(true);
        DEFAULT_ISSUE_DOCUMENT.setIssueDate(LocalDateTime.now());
        DEFAULT_ISSUE_DOCUMENT.setDocumentType(TransactionType.Stock_Issue);
        StockIssue stockIssue = new StockIssue()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_ISSUE_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockIssue;
    }

    public static StockIndent crateStockIndentEntity(EntityManager em) {
        StockIndent stockIndent = new StockIndent();
        stockIndent.setDocumentNumber("AAAAAA-" + Math.random());
        stockIndent.setDocument(createIndentDocument());
        stockIndent.setLatest(true);
        return stockIndent;
    }

    /**
     * Returns Object of user
     *
     * @param id
     * @param displayName
     * @param employeeNo
     * @param login
     * @return
     */
    public static UserDTO createUser(Long id, String displayName, String employeeNo, String login) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmployeeNo(employeeNo);
        user.setLogin(login);
        return user;
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
     * Returns object of IssuedocuementLine
     *
     * @return
     */
    public static List<IssueDocumentLine> createDocumentLine() {
        List<IssueDocumentLine> line = new ArrayList<>();
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        quantity.setUom(createUom());
        ItemDTO item = new ItemDTO();
        item.setId(1l);
        item.setCode("code");
        item.setName("DEFAULT_NAME");
        IssueDocumentLine issueDocumentLine = new IssueDocumentLine();
        issueDocumentLine.setId(1l);
        issueDocumentLine.setIssuedQuantity(quantity);
        issueDocumentLine.setBatchNumber("BTH12345");
        issueDocumentLine.setConsignment(false);
        issueDocumentLine.setCost(getBigDecimal(10f));
        issueDocumentLine.setItem(item);
        issueDocumentLine.setMedication(createMedication());
        line.add(issueDocumentLine);
        List sourceDocList = new ArrayList();
        issueDocumentLine.setSourceDocument(sourceDocList);
        return line;
    }

    public static Medication createMedication() {
        Medication medication = new Medication();
        medication.setId(1l);
        return medication;
    }

    /**
     * Creates IssueDocument
     *
     * @return
     */
    public static IssueDocument createIssueDocument() {

        IssueDocument isd = new IssueDocument();
        isd.setIssuedBy(createUser(1l, "User01", "emp01", "User1"));
        isd.setIssueStore(createStore());
        isd.setLines(createDocumentLine());
        isd.setIssueDate(LocalDateTime.now());
        isd.setCreatedDate(LocalDateTime.now());
        isd.setDocumentType(TransactionType.Stock_Issue);
        isd.setIssueUnit(createUnit());
        isd.setIndentStore(createStore());
        isd.setIndentUnit(createUnit());
        isd.setStatus(Status.DRAFT);

        return isd;
    }

    /**
     * @return
     */
    public static IndentDocument createIndentDocument() {

        IndentDocument ind = new IndentDocument();
        ind.setApprovedBy(createUser(1l, "User01", "emp01", "User1"));
        ind.setIssueStore(createStore());
        ind.setIndentStore(createStore());
        ind.setIndentUnit(createUnit());
        ind.setIssueUnit(createUnit());
        ind.setLines(createIndentDocumentLine());
        ind.setIndentDate(LocalDateTime.now());
        ind.setCreatedDate(LocalDateTime.now());
        ind.setDocumentType(TransactionType.Stock_Indent);
        ind.setStatus(Status.PARTIALLY_ISSUED);
        ind.setIndenterName(createUser(2l, "User01", "emp01", "User1"));
        return ind;
    }

    private static OrganizationDTO createUnit() {
        OrganizationDTO organization = new OrganizationDTO();
        organization.setId(1l);
        organization.setCode("DEFAULT_CODE");
        organization.setName("DEFAULT_NAME");
        return organization;
    }

    /**
     * @return
     */
    public static List<IndentDocumentLine> createIndentDocumentLine() {
        List<IndentDocumentLine> line = new ArrayList<>();
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        quantity.setUom(createUom());
        ItemDTO item = new ItemDTO();
        item.setId(12l);
        item.setCode("code");
        IndentDocumentLine indentDocumentLine = new IndentDocumentLine();
        indentDocumentLine.setId(1l);
        indentDocumentLine.setQuantity(quantity);
        indentDocumentLine.setBatchNumber("batch");
        indentDocumentLine.setConsignment(false);
        indentDocumentLine.setCost(getBigDecimal(10f));
        indentDocumentLine.setItem(item);
        indentDocumentLine.setMedication(createMedication());
        indentDocumentLine.setExpiryDate(LocalDate.now());
        line.add(indentDocumentLine);
        return line;
    }

    public static UOMDTO createUom() {
        UOMDTO uom = new UOMDTO();
        uom.setId(1l);
        uom.setCode("DEFAULT_CODE");
        uom.setName("DEFAULT_NAME");
        return uom;
    }

    StockIssue prepareStockIssueObjectForWorkFlow(StockIssue stockIssue) {
        stockIssue.getDocument().getLines().get(0).setIssuedQuantity(new Quantity(5f, createUom()));
        stockIssue.getDocument().getLines().get(0).setStockId(stockService.findOne(51l).getId());
        List sourceDocList = new ArrayList();
        SourceDocument src = new SourceDocument();
        stockIndentService.save(stockIndent);
        src.setDocumentNumber(stockIndent.getDocumentNumber());
        src.setQuantity(stockIndent.getDocument().getLines().get(0).getQuantity());
        src.setLineId(stockIndent.getDocument().getLines().get(0).getId());
        src.setId(stockIndent.getId());
        src.setPendingQuantity(new Quantity(0f, null));
        src.setType(TransactionType.Stock_Indent);
        src.setCreatedBy(stockIndent.getDocument().getIndenterName());
        sourceDocList.add(src);
        stockIssue.getDocument().getLines().get(0).setSourceDocument(sourceDocList);
        return stockIssue;
    }

    StockIssue prepareStockIssueObjectForWorkFlowDirectTransfer(StockIssue stockIssue) {
        stockIssue.getDocument().setStatus(Status.DRAFT);
        stockIssue.getDocument().setIssuedBy(createUser(1l, "DEFAULT_ISSUER", "DEFAULT_EMP_NO", "DEFAULT_LOGIN"));
        stockIssue.getDocument().getLines().get(0).setStockId(stockService.findOne(51l).getId());
        stockIssue.getDocument().getLines().get(0).setIssuedQuantity(new Quantity(5f, createUom()));
        return stockIssue;
    }

    @Before
    public void initTest() {
        stockIssueSearchRepository.deleteAll();
        stockIssue = createEntityStockIssue(em);
        stockIndent = crateStockIndentEntity(em);
        groupService.doIndex();
        addUserToSecurityContext(1L, "admin", "admin");
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

    public static ItemUnitAverageCost createItemUnitAverageCostOne(EntityManager em) {
        ItemUnitAverageCost itemUnitAverageCost = new ItemUnitAverageCost()
            .itemId(21L)
            .unitId(1L)
            .averageCost(BigDecimal.ONE);
        return itemUnitAverageCost;
    }

    public static ItemUnitAverageCost createItemUnitAverageCostTwo(EntityManager em) {
        ItemUnitAverageCost itemUnitAverageCost = new ItemUnitAverageCost()
            .itemId(22L)
            .unitId(1L)
            .averageCost(BigDecimal.ONE);
        return itemUnitAverageCost;
    }

    public static ItemUnitAverageCost createItemUnitAverageCostThree(EntityManager em) {
        ItemUnitAverageCost itemUnitAverageCost = new ItemUnitAverageCost()
            .itemId(23L)
            .unitId(1L)
            .averageCost(BigDecimal.ONE);
        return itemUnitAverageCost;
    }

    @Test
    public void a_setupIndex() {
        Map<String, Object> objectMap = new HashMap<>();
        if (elasticsearchTemplate.indexExists("configurations"))
            elasticsearchTemplate.deleteIndex("configurations");
        elasticsearchTemplate.createIndex("configurations");
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
        configuration.setKey("athma_stockissue_enable_workflow");
        configuration.setValue("Yes");
        configuration.setLevel(1);

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(1l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockissue_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(1l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_stockissue_workflow_definition");
        configuration2.setValue("stock_issue_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(1l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_stockdirecttransfer_enable_workflow");
        configuration3.setValue("Yes");
        configuration3.setLevel(2);

        Configuration configuration4 = new Configuration();
        configuration4.setApplicableCode("1001");
        configuration4.setApplicableTo(1l);
        configuration4.setApplicableType(ConfigurationLevel.Unit);
        configuration4.setKey("athma_stockdirecttransfer_workflow_definition");
        configuration4.setValue("stock_direct_transfer_document_process");
        configuration4.setLevel(2);

        Configuration configuration6 = new Configuration();
        configuration6.setApplicableCode("1001");
        configuration6.setApplicableTo(1l);
        configuration6.setApplicableType(ConfigurationLevel.Unit);
        configuration6.setKey("athma_stockindent_enable_workflow");
        configuration6.setValue("Yes");
        configuration6.setLevel(2);

        Configuration configuration7 = new Configuration();
        configuration7.setApplicableCode("1001");
        configuration7.setApplicableTo(1l);
        configuration7.setApplicableType(ConfigurationLevel.Unit);
        configuration7.setKey("athma_stockindent_workflow_definition");
        configuration7.setValue("stock_indent_document_process");
        configuration7.setLevel(2);

        Configuration configuration8 = new Configuration();
        configuration8.setApplicableCode("1001");
        configuration8.setApplicableTo(1l);
        configuration8.setApplicableType(ConfigurationLevel.Unit);
        configuration8.setKey("athma_date_format");
        configuration8.setValue("dd/MM/yy");
        configuration8.setLevel(2);


        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration1).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration2).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration3).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("4").withObject(configuration4).build();
        IndexQuery indexQuery6 = new IndexQueryBuilder().withId("6").withObject(configuration6).build();
        IndexQuery indexQuery7 = new IndexQueryBuilder().withId("7").withObject(configuration7).build();
        IndexQuery indexQuery8 = new IndexQueryBuilder().withId("8").withObject(configuration8).build();

        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);
        elasticsearchTemplate.index(indexQuery3, coordinates);
        elasticsearchTemplate.index(indexQuery4, coordinates);
        elasticsearchTemplate.index(indexQuery6, coordinates);
        elasticsearchTemplate.index(indexQuery7, coordinates);
        elasticsearchTemplate.index(indexQuery8, coordinates);

        elasticsearchTemplate.refresh(Configuration.class);
    }

    public IndentDocument createDefaultIndentDocument() {
        List<IndentDocumentLine> lines = new ArrayList<>();
        lines.add(createDefaultIndentDocumentLine());

        IndentDocument document = new IndentDocument();
        document.setIndentDate(LocalDateTime.now());
        document.setStatus(Status.DRAFT);
        document.setLines(lines);
        org.nh.pharmacy.domain.Organization indentUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIndentDTO = new OrganizationDTO();
        BeanUtils.copyProperties(indentUnit, organizationIndentDTO);
        org.nh.pharmacy.domain.Organization issueUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIssueDTO = new OrganizationDTO();
        BeanUtils.copyProperties(issueUnit, organizationIssueDTO);
        document.setIndentUnit(organizationIndentDTO);
        document.setIssueUnit(organizationIssueDTO);

        org.nh.pharmacy.domain.HealthcareServiceCenter issueHealthcareServiceCenter = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO issueHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(issueHealthcareServiceCenter, issueHealthcareServiceCenterDTO);

        org.nh.pharmacy.domain.HealthcareServiceCenter indentHealthcareServiceCenter = healthcareServiceCenterRepository.findById(2l).get();
        HealthcareServiceCenterDTO indentHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(indentHealthcareServiceCenter, indentHealthcareServiceCenterDTO);

        document.setIndentStore(indentHealthcareServiceCenterDTO);
        document.setIssueStore(issueHealthcareServiceCenterDTO);
        document.setDocumentType(TransactionType.Stock_Indent);
        document.setCreatedDate(LocalDateTime.now());
        document.setIndenterName(DEFAULT_USER);
        document.setStatus(Status.DRAFT);
        return document;
    }

    public IndentDocument createIndentDocumentWith2Lines() {
        List<IndentDocumentLine> lines = new ArrayList<>();
        lines.add(createDefaultIndentDocumentLine());
        lines.add(createAnotherIndentDocumentLine());

        IndentDocument document = new IndentDocument();
        document.setIndentDate(LocalDateTime.now());
        document.setStatus(Status.DRAFT);
        document.setLines(lines);
        org.nh.pharmacy.domain.Organization indentUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIndentDTO = new OrganizationDTO();
        BeanUtils.copyProperties(indentUnit, organizationIndentDTO);
        org.nh.pharmacy.domain.Organization issueUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIssueDTO = new OrganizationDTO();
        BeanUtils.copyProperties(issueUnit, organizationIssueDTO);
        document.setIndentUnit(organizationIndentDTO);
        document.setIssueUnit(organizationIssueDTO);

        org.nh.pharmacy.domain.HealthcareServiceCenter issueHealthcareServiceCenter = healthcareServiceCenterRepository.findById(1l).get();
        HealthcareServiceCenterDTO issueHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(issueHealthcareServiceCenter, issueHealthcareServiceCenterDTO);

        org.nh.pharmacy.domain.HealthcareServiceCenter indentHealthcareServiceCenter = healthcareServiceCenterRepository.findById(2l).get();
        HealthcareServiceCenterDTO indentHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(indentHealthcareServiceCenter, indentHealthcareServiceCenterDTO);

        document.setIndentStore(indentHealthcareServiceCenterDTO);
        document.setIssueStore(issueHealthcareServiceCenterDTO);
        document.setDocumentType(TransactionType.Stock_Indent);
        document.setCreatedDate(LocalDateTime.now());
        document.setIndenterName(DEFAULT_USER);
        document.setStatus(Status.DRAFT);
        return document;
    }


    public IndentDocumentLine createDefaultIndentDocumentLine() {
        IndentDocumentLine indentDocumentLine = new IndentDocumentLine();
        org.nh.pharmacy.domain.Item item = itemService.findOne(1l);
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);
        indentDocumentLine.setItem(itemDto);
        indentDocumentLine.setQuantity(createDefaultQuantity());
        indentDocumentLine.setBatchNumber("BTH12345");
        indentDocumentLine.setStockId(51l);
        indentDocumentLine.setOwner("HUL");
        indentDocumentLine.setCost(getBigDecimal(80f));
        indentDocumentLine.setSupplier("HUL");

        return indentDocumentLine;
    }

    private Quantity createDefaultQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(20f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }

    private Quantity createPartiallyQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }

    private Quantity createZeroQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(0f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }

    public IndentDocumentLine createAnotherIndentDocumentLine() {
        IndentDocumentLine indentDocumentLine = new IndentDocumentLine();
        org.nh.pharmacy.domain.Item item = itemService.findOne(2l);
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);
        indentDocumentLine.setItem(itemDto);
        indentDocumentLine.setQuantity(createDefaultQuantity());
        indentDocumentLine.setBatchNumber("BTH12345");
        indentDocumentLine.setStockId(51l);
        indentDocumentLine.setOwner("HUL");
        indentDocumentLine.setCost(getBigDecimal(100f));

        return indentDocumentLine;
    }


    @Test
    public void createStockIssue_SFA_Approved() throws Exception {
        a_setupIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(indentListA).hasSize(databaseSizeBeforeCreateIndent + 2);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(issueListA).hasSize(databaseSizeBeforeCreateIssue + 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIssue = stockIssueService.findOne(stockIssueA.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(issueListB).hasSize(databaseSizeBeforeCreateIssue + 2);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(indentListB).hasSize(databaseSizeBeforeCreateIndent + 5);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("ISSUED");


    }

    @Test
    public void createStockIssueReject_SFA_Approved() throws Exception {
        a_setupIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(indentListA).hasSize(databaseSizeBeforeCreateIndent + 2);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(issueListA).hasSize(databaseSizeBeforeCreateIssue + 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIssue = stockIssueService.findOne(stockIssueA.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(issueListB).hasSize(databaseSizeBeforeCreateIssue + 2);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("REJECTED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(indentListB).hasSize(databaseSizeBeforeCreateIndent + 4);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("APPROVED");
        assertThat(stockIndentB.getDocument().getConversionCompleted()).isFalse();


    }


    @Test
    public void createStockIssue_SFA_Approved_Partially() throws Exception {
        a_setupIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(indentListA).hasSize(databaseSizeBeforeCreateIndent + 2);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            issueDocumentLine.setIssuedQuantity(createPartiallyQuantity());
        }

        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(issueListA).hasSize(databaseSizeBeforeCreateIssue + 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIssue = stockIssueService.findOne(stockIssueA.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(issueListB).hasSize(databaseSizeBeforeCreateIssue + 2);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(indentListB).hasSize(databaseSizeBeforeCreateIndent + 5);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_ISSUED");
    }

    @Test
    public void createStockIssue_SFA_Approved_WithAllItem() throws Exception {
        a_setupIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createIndentDocumentWith2Lines());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(indentListA).hasSize(databaseSizeBeforeCreateIndent + 2);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(issueListA).hasSize(databaseSizeBeforeCreateIssue + 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getConversionCompleted()).isTrue();

        stockIssue = stockIssueService.findOne(stockIssueA.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(issueListB).hasSize(databaseSizeBeforeCreateIssue + 2);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentC = indentListC.get(indentListC.size() - 1);
        assertThat(indentListC).hasSize(databaseSizeBeforeCreateIndent + 5);
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("ISSUED");


    }


    @Test
    public void createStockIssue_SFA_Approved_WithPartiallyItem() throws Exception {
        a_setupIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createIndentDocumentWith2Lines());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(indentListA).hasSize(databaseSizeBeforeCreateIndent + 2);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            if (issueDocumentLine.getItem().getId().equals(1l)) {
                issueDocumentLine.setIssuedQuantity(createPartiallyQuantity());
            }
        }

        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(issueListA).hasSize(databaseSizeBeforeCreateIssue + 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIssue = stockIssueService.findOne(stockIssueA.getId());

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getConversionCompleted()).isFalse();

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(issueListB).hasSize(databaseSizeBeforeCreateIssue + 2);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentC = indentListC.get(indentListC.size() - 1);
        assertThat(indentListC).hasSize(databaseSizeBeforeCreateIndent + 5);
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_ISSUED");


    }

    @Test
    public void createMultipleStockIssue_SFA_Approved_WithAllScenario() throws Exception {
        a_setupIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createIndentDocumentWith2Lines());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(indentListA).hasSize(databaseSizeBeforeCreateIndent + 2);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            if (issueDocumentLine.getItem().getId().equals(1l)) {
                issueDocumentLine.setIssuedQuantity(createPartiallyQuantity());
            } else {
                issueDocumentLine.setIssuedQuantity(createZeroQuantity());
            }
        }

        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(issueListA).hasSize(databaseSizeBeforeCreateIssue + 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        List<StockIndent> indentListCC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentCC = indentListCC.get(indentListCC.size() - 1);
        assertThat(stockIndentCC.getDocument().getConversionCompleted()).isFalse();

        stockIssue = stockIssueService.findOne(stockIssueA.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(issueListB).hasSize(databaseSizeBeforeCreateIssue + 2);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(indentListB).hasSize(databaseSizeBeforeCreateIndent + 5);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_ISSUED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            if (issueDocumentLine.getItem().getId().equals(1l)) {
                issueDocumentLine.setIssuedQuantity(createPartiallyQuantity());
            } else {
                issueDocumentLine.setIssuedQuantity(createZeroQuantity());
            }
        }


        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListC = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueC = issueListC.get(issueListC.size() - 1);
        assertThat(issueListC).hasSize(databaseSizeBeforeCreateIssue + 3);
        assertThat(stockIssueC.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        indentListCC = stockIndentRepository.findAll(Sort.by("id", "version"));
        stockIndentCC = indentListCC.get(indentListCC.size() - 1);
        assertThat(stockIndentCC.getDocument().getConversionCompleted()).isFalse();

        stockIssue = stockIssueService.findOne(stockIssueC.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListD = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueD = issueListD.get(issueListD.size() - 1);
        assertThat(issueListD).hasSize(databaseSizeBeforeCreateIssue + 4);
        assertThat(stockIssueD.getDocument().getStatus().toString()).isEqualTo("REJECTED");

        List<StockIndent> indentListC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentC = indentListC.get(indentListC.size() - 1);
        assertThat(indentListC).hasSize(databaseSizeBeforeCreateIndent + 7);
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_ISSUED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            if (issueDocumentLine.getItem().getId().equals(2l)) {
                issueDocumentLine.setIssuedQuantity(createPartiallyQuantity());
            } else {
                issueDocumentLine.setIssuedQuantity(createZeroQuantity());
            }
        }

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListE = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueE = issueListE.get(issueListE.size() - 1);
        assertThat(issueListE).hasSize(databaseSizeBeforeCreateIssue + 5);
        assertThat(stockIssueE.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        indentListCC = stockIndentRepository.findAll(Sort.by("id", "version"));
        stockIndentCC = indentListCC.get(indentListCC.size() - 1);
        assertThat(stockIndentCC.getDocument().getConversionCompleted()).isFalse();

        stockIssue = stockIssueService.findOne(stockIssueE.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListF = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueF = issueListF.get(issueListF.size() - 1);
        assertThat(issueListF).hasSize(databaseSizeBeforeCreateIssue + 6);
        assertThat(stockIssueF.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListE = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentE = indentListE.get(indentListE.size() - 1);
        assertThat(indentListE).hasSize(databaseSizeBeforeCreateIndent + 10);
        assertThat(stockIndentE.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_ISSUED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());

        restStockIssueMockMvc.perform(post("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated());

        List<StockIssue> issueListG = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueG = issueListG.get(issueListG.size() - 1);
        assertThat(issueListG).hasSize(databaseSizeBeforeCreateIssue + 7);
        assertThat(stockIssueG.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        indentListCC = stockIndentRepository.findAll(Sort.by("id", "version"));
        stockIndentCC = indentListCC.get(indentListCC.size() - 1);
        assertThat(stockIndentCC.getDocument().getConversionCompleted()).isTrue();

        stockIssue = stockIssueService.findOne(stockIssueG.getId());

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        List<StockIssue> issueListH = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueH = issueListH.get(issueListH.size() - 1);
        assertThat(issueListH).hasSize(databaseSizeBeforeCreateIssue + 8);
        assertThat(stockIssueH.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListF = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentF = indentListF.get(indentListF.size() - 1);
        assertThat(indentListF).hasSize(databaseSizeBeforeCreateIndent + 13);
        assertThat(stockIndentF.getDocument().getStatus().toString()).isEqualTo("ISSUED");

    }


    @Test
    @Transactional
    public void createStockIssueWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockIssueRepository.findAll().size();

        // Create the StockIssue with an existing ID
        StockIssue existingStockIssue = new StockIssue();
        existingStockIssue.setId(1L);
        existingStockIssue.setVersion(0);
        // An entity with an existing ID cannot be created, so this API call must fail
        restStockIssueMockMvc.perform(post("/api/stock-issues")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockIssue)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockIssue> stockIssueList = stockIssueRepository.findAll();
        assertThat(stockIssueList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockIssueRepository.findAll().size();
        // set the field null
        stockIssue.setDocument(null);

        // Create the StockIssue, which fails.

        restStockIssueMockMvc.perform(post("/api/stock-issues")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isBadRequest());

        List<StockIssue> stockIssueList = stockIssueRepository.findAll();
        assertThat(stockIssueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockIssues() throws Exception {
        // Initialize the database
        stockIssueService.save(stockIssue);

        // Get all the stockIssueList
        restStockIssueMockMvc.perform(get("/api/stock-issues?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockIssue.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.issuedBy.displayName").value(hasItem(DEFAULT_ISSUE_DOCUMENT.getIssuedBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockIssue() throws Exception {
        // Initialize the database
        stockIssueService.save(stockIssue);

        // Get the stockIssue
        restStockIssueMockMvc.perform(get("/api/stock-issues/{id}", stockIssue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockIssue.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.document.issuedBy.displayName").value(DEFAULT_ISSUE_DOCUMENT.getIssuedBy().getDisplayName()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    // @Test
    @Transactional
    public void getNonExistingStockIssue() throws Exception {
        // Get the stockIssue
        restStockIssueMockMvc.perform(get("/api/stock-issues/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void deleteStockIssue() throws Exception {
        // Initialize the database
        stockIssueService.save(stockIssue);
        int databaseSizeBeforeDelete = stockIssueRepository.findAll().size();

        // Get the stockIssue
        restStockIssueMockMvc.perform(delete("/api/stock-issues/{id}", stockIssue.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockIssueExistsInEs = stockIssueSearchRepository.existsById(stockIssue.getId());
        assertThat(stockIssueExistsInEs).isFalse();

        // Validate the database is empty
        List<StockIssue> stockIssueList = stockIssueRepository.findAll();
        assertThat(stockIssueList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockIssue() throws Exception {
        // Initialize the database
        stockIssueService.save(stockIssue);
        stockIssueService.doIndex(0,200, LocalDate.now(), LocalDate.now());
        // Search the stockIssue
        restStockIssueMockMvc.perform(get("/api/_search/stock-issues?query=id:" + stockIssue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockIssue.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.issuedBy.displayName").value(hasItem(DEFAULT_ISSUE_DOCUMENT.getIssuedBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    //   @Test
    @Transactional
    public void getStockIssueStatusCount() throws Exception {
        // Initialize the database
        stockIssueService.save(stockIssue, "");

        // Get the stockIssue
        restStockIssueMockMvc.perform(get("/api/status-count/stock-issues?query=id:" + stockIssue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }


    @Test(expected = Exception.class)
    @Transactional
    public void sendForApprovalWithGreaterStock() throws Exception {
        Quantity quantity = new Quantity();
        quantity.setValue(200f);
        stockIssue.getDocument().getLines().get(0).setIssuedQuantity(quantity);
        stockIssue.getDocument().getLines().get(0).setStockId(stock.getId());
        stockIssue = createEntityStockIssue(em);
        stockIssueService.save(stockIssue);
        stockIssueService.sendForApproval(stockIssue, "SENDFORAPPROVAL");
        StockIssue currentStockIssue = stockIssueRepository.findOne(stockIssue.getId());
        List<ReserveStock> reserveStocks = reserveStockRepository.findByTransactionNumber(currentStockIssue.getDocumentNumber());
        assertThat(reserveStocks).hasSize(0);
    }

    @Test
    public void verifyWorkflow() throws Exception {
        a_setupIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");

        stockIssue = prepareStockIssueObjectForWorkFlow(stockIssue);

        MvcResult result = restStockIssueMockMvc.perform(post("/api/stock-issues?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated()).andReturn();

        stockIssue = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockIssue.class);

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        stockIssue = stockIssueService.findOne(stockIssue.getId());

        result = restStockIssueMockMvc.perform(get("/api/_workflow/stock-issues?documentNumber=" + stockIssue.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockIssueMockMvc.perform(put("/api/_workflow/stock-issues?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        stockIssueRepository.deleteAll();
    }

    @Test
    public void verifyWorkflowDirectTransfer() throws Exception {

        a_setupIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");

        stockIssue = prepareStockIssueObjectForWorkFlowDirectTransfer(stockIssue);
        stockIssue.getDocument().setDocumentType(TransactionType.Stock_Direct_Transfer);

        MvcResult result = restStockIssueMockMvc.perform(post("/api/stock-issues?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isCreated()).andReturn();

        stockIssue = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockIssue.class);

        restStockIssueMockMvc.perform(put("/api/stock-issues?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        stockIssue = stockIssueService.findOne(stockIssue.getId());

        result = restStockIssueMockMvc.perform(get("/api/_workflow/stock-issues?documentNumber=" + stockIssue.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockIssueMockMvc.perform(put("/api/_workflow/stock-issues?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIssue)))
            .andExpect(status().isOk());

        stockIssueRepository.deleteAll();
    }

}
