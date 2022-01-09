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
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.StockReceiptSearchRepository;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.pharmacy.web.rest.mapper.IssueToReceiptMapper;
import org.nh.pharmacy.web.rest.mapper.ReversalToReceiptMapper;
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
import static org.nh.pharmacy.web.rest.StockIssueResourceIntTest.crateStockIndentEntity;
import static org.nh.pharmacy.web.rest.StockIssueResourceIntTest.createDocumentLine;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockReceiptResource REST controller.
 *
 * @see StockReceiptResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockReceiptResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_DOCUMENT = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT = "BBBBBBBBBB";

    private static final ReceiptDocument DEFAULT_RECEIPT_DOCUMENT = new ReceiptDocument();
    private static final ReceiptDocument UPDATED_RECEIPT_DOCUMENT = new ReceiptDocument();

    private static final IssueDocument DEFAULT_ISSUE_DOCUMENT = new IssueDocument();
    private static final IndentDocument DEFAULT_INDENT_DOCUMENT = new IndentDocument();
    private static final ReversalDocument DEFAULT_REVERSAL_DOCUMENT = new ReversalDocument();

    private static final ReceiptDocumentLine DEFAULT_RECEIPT_DOCUMENT_LINE = new ReceiptDocumentLine();

    private static final List<ReceiptDocumentLine> DEFAULT_LINE = Arrays.asList(new ReceiptDocumentLine());
    private static final List<IssueDocumentLine> DEFAULT_ISSUE_LINE = Arrays.asList(new IssueDocumentLine());
    private static final List<IndentDocumentLine> DEFAULT_INDENT_LINE = Arrays.asList(new IndentDocumentLine());
    private static final List<ReversalDocumentLine> DEFAULT_REVERSAL_LINE = Arrays.asList(new ReversalDocumentLine());

    private static final ValueSetCode DEFAULT_REASON = new ValueSetCode(1L, "REASON", "Damaged", true);

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAA";
    private static final String UPDATED_EMPLOYEE_NO = "BBBBBBBBBB";

    private static final UserDTO DEFAULT_USER = createUser(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DISPLAY_NAME, DEFAULT_EMPLOYEE_NO);
    private static final UserDTO UPDATED_USER = createUser(UPDATED_ID, UPDATED_NAME, UPDATED_DISPLAY_NAME, UPDATED_EMPLOYEE_NO);

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    private static final Long DEFAULT_ITEM_ID = 1L;
    private static final String DEFAULT_BATCH_NO = "BatchNo";
    private static final LocalDate DEFAULT_EXPIRY_DATE = LocalDate.ofEpochDay(0L);
    private static final String DEFAULT_OWNER = "Owner";
    private static final BigDecimal DEFAULT_COST = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_MRP = BigDecimal.ONE;
    private static final Float DEFAULT_QUANTITY = 100F;
    private static final BigDecimal DEFAULT_STOCK_VALUE = BigDecimal.ONE;
    private static final Long DEFAULT_STORE_ID = 1L;
    private static final Long DEFAULT_LOCATOR_ID = 1L;
    private static final String DEFAULT_SUPPLIER = "Supplier";
    private static final Long DEFAULT_UOM_ID = 1L;
    private static final String DEFAULT_SKU = "AAAAAAAAAA";
    private static final Long DEFAULT_UNIT_ID = 1L;
    private static final Boolean DEFAULT_CONSIGNMENT = false;

    @Autowired
    private StockReceiptRepository stockReceiptRepository;

    @Autowired
    private StockReceiptService stockReceiptService;

    @Autowired
    private StockIssueService stockIssueService;

    @Autowired
    private StockIndentService stockIndentService;

    @Autowired
    private StockIndentRepository stockIndentRepository;

    @Autowired
    StockIssueRepository stockIssueRepository;

    @Autowired
    private StockReceiptSearchRepository stockReceiptSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    @Autowired
    private StockReversalService stockReversalService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private IssueToReceiptMapper issueToReceiptMapper;

    @Autowired
    private ReversalToReceiptMapper reversalToReceiptMapper;

    @Autowired
    private StockReversalRepository stockReversalRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private GroupService groupService;

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
    private ItemStoreLocatorMapService itemStoreLocatorMapService;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc restStockReceiptMockMvc;

    private MockMvc restWorkflowMockMvc;

    private StockReceipt stockReceipt;

    private StockIndent stockIndent;

    private StockIssue stockIssue;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockReceiptResource stockReceiptResource = new StockReceiptResource(stockReceiptService,stockReceiptRepository,stockReceiptSearchRepository,applicationProperties);
        this.restStockReceiptMockMvc = MockMvcBuilders.standaloneSetup(stockReceiptResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
        WorkflowResource workflowResource = new WorkflowResource(workflowService, identityProvider, jbpmTaskSearchRepository, jbpmProperties);
        this.restWorkflowMockMvc = MockMvcBuilders.standaloneSetup(workflowResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    public static UserDTO createUser(Long id, String displayName, String employeeNo, String login) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmployeeNo(employeeNo);
        user.setLogin(login);
        return user;
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

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public StockReceipt createEntity(EntityManager em) {
        //   DEFAULT_RECEIPT_DOCUMENT.setReceivedBy(DEFAULT_USER);
        StockReceipt stockReceipt = new StockReceipt()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST)
            .document(DEFAULT_RECEIPT_DOCUMENT);
        return stockReceipt;
    }

    public static StockIssue createEntityStockIssue(EntityManager em) {
        DEFAULT_ISSUE_DOCUMENT.setIssuedBy(DEFAULT_USER);
        DEFAULT_ISSUE_DOCUMENT.setStatus(Status.APPROVED);
        DEFAULT_ISSUE_DOCUMENT.setLines(DEFAULT_ISSUE_LINE);
        StockIssue stockIssue = new StockIssue()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_ISSUE_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockIssue;
    }

    public static StockIndent createEntityStockIndent(EntityManager em) {
        DEFAULT_INDENT_DOCUMENT.setIndenterName(DEFAULT_USER);
        DEFAULT_INDENT_DOCUMENT.setStatus(Status.PARTIALLY_ISSUED);
        DEFAULT_INDENT_DOCUMENT.setLines(DEFAULT_INDENT_LINE);
        StockIndent stockIndent = new StockIndent()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_INDENT_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockIndent;
    }

    public static StockReversal createEntityStockReversal(EntityManager em) {
        DEFAULT_REVERSAL_DOCUMENT.setReversalMadeBy(DEFAULT_USER);
        DEFAULT_REVERSAL_DOCUMENT.setStatus(Status.APPROVED);
        DEFAULT_REVERSAL_LINE.get(0).setReason(DEFAULT_REASON);
        DEFAULT_REVERSAL_LINE.get(0).setRejectedQuantity(new Quantity(10f, null));
        DEFAULT_REVERSAL_DOCUMENT.setLines(DEFAULT_REVERSAL_LINE);
        DEFAULT_REVERSAL_DOCUMENT.setSourceType(TransactionType.Stock_Receipt);
        StockReversal stockReversal = new StockReversal()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_REVERSAL_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockReversal;
    }

    private ItemDTO createItem() {
        ItemDTO item = new ItemDTO();
        item.setId(1l);
        return item;

    }

    private LocatorDTO createLocator() {
        LocatorDTO locator = new LocatorDTO();
        locator.setId(1l);
        return locator;
    }

    private Quantity createQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(5f);
        UOMDTO uom = new UOMDTO();
        uom.setId(1L);
        uom.setCode("cccc");
        uom.setName("nnnnnn");
        uom.setActive(true);
        quantity.setUom(uom);
        return quantity;
    }

    private Quantity rejectQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(1f);
        UOMDTO uom = new UOMDTO();
        uom.setId(1L);
        uom.setCode("cccc");
        uom.setName("nnnnnn");
        uom.setActive(true);
        quantity.setUom(uom);
        return quantity;
    }

    public static HealthcareServiceCenterDTO createStore() {
        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(2l);
        hsc.setCode("NH");
        hsc.setName("Narayana HealthCare");
        hsc.setPartOf(createOrganization());
        return hsc;
    }

    private HealthcareServiceCenterDTO createHealthcareServiceCenter() {
        HealthcareServiceCenterDTO healthcareServiceCenter = new HealthcareServiceCenterDTO();
        healthcareServiceCenter.setId(1L);
        return healthcareServiceCenter;
    }

    private static OrganizationDTO createOrganization() {
        OrganizationDTO organization = new OrganizationDTO();
        organization.setId(1L);
        return organization;
    }

    @Before
    public void initTest() {
        reserveStockRepository.deleteAll();
        stockReceiptSearchRepository.deleteAll();
        stockReceipt = createEntity(em);
        stockIndent = crateStockIndentEntity(em);
        itemStoreLocatorMapService.doIndex();
        groupService.doIndex();
        addUserToSecurityContext(1L, "admin", "admin");
    }

    @Test
    public void aCreateIndex() {
        Map<String, Object> objectMap = new HashMap<>();
        if (elasticsearchTemplate.indexExists("configurations"))
            elasticsearchTemplate.deleteIndex("configurations");
        elasticsearchTemplate.createIndex("configurations");
        objectMap.put("athma_stockreceipt_enable_workflow", "Yes");
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
        configuration.setKey("athma_stockreceipt_enable_workflow");
        configuration.setValue("Yes");
        configuration.setLevel(1);

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(1l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockreceipt_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(1l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_stockreceipt_workflow_definition");
        configuration2.setValue("stock_receipt_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(1l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_stockissue_enable_workflow");
        configuration3.setValue("Yes");
        configuration3.setLevel(2);

        Configuration configuration4 = new Configuration();
        configuration4.setApplicableCode("1001");
        configuration4.setApplicableTo(1l);
        configuration4.setApplicableType(ConfigurationLevel.Unit);
        configuration4.setKey("athma_stockissue_workflow_definition");
        configuration4.setValue("stock_issue_document_process");
        configuration4.setLevel(2);


        Configuration configuration5 = new Configuration();
        configuration5.setApplicableCode("1001");
        configuration5.setApplicableTo(1l);
        configuration5.setApplicableType(ConfigurationLevel.Unit);
        configuration5.setKey("athma_stockindent_enable_workflow");
        configuration5.setValue("Yes");
        configuration5.setLevel(2);

        Configuration configuration6 = new Configuration();
        configuration6.setApplicableCode("1001");
        configuration6.setApplicableTo(1l);
        configuration6.setApplicableType(ConfigurationLevel.Unit);
        configuration6.setKey("athma_stockindent_workflow_definition");
        configuration6.setValue("stock_indent_document_process");
        configuration6.setLevel(2);

        Configuration configuration7 = new Configuration();
        configuration7.setApplicableCode("1001");
        configuration7.setApplicableTo(1l);
        configuration7.setApplicableType(ConfigurationLevel.Unit);
        configuration7.setKey("athma_stockreceipt_enable_workflow");
        configuration7.setValue("Yes");
        configuration7.setLevel(2);

        Configuration configuration8 = new Configuration();
        configuration8.setApplicableCode("1001");
        configuration8.setApplicableTo(1l);
        configuration8.setApplicableType(ConfigurationLevel.Unit);
        configuration8.setKey("athma_stockreceipt_workflow_definition");
        configuration8.setValue("stock_receipt_document_process");
        configuration8.setLevel(2);

        Configuration configuration9 = new Configuration();
        configuration9.setApplicableCode("1001");
        configuration9.setApplicableTo(1l);
        configuration9.setApplicableType(ConfigurationLevel.Unit);
        configuration9.setKey("athma_stockdirecttransfer_enable_workflow");
        configuration9.setValue("Yes");
        configuration9.setLevel(2);

        Configuration configuration10 = new Configuration();
        configuration10.setApplicableCode("1001");
        configuration10.setApplicableTo(1l);
        configuration10.setApplicableType(ConfigurationLevel.Unit);
        configuration10.setKey("athma_stockdirecttransfer_workflow_definition");
        configuration10.setValue("stock_direct_transfer_document_process");
        configuration10.setLevel(2);

        Configuration configuration11 = new Configuration();
        configuration11.setApplicableCode("1001");
        configuration11.setApplicableTo(1l);
        configuration11.setApplicableType(ConfigurationLevel.Unit);
        configuration11.setKey("athma_date_format");
        configuration11.setValue("dd/MM/yy");
        configuration11.setLevel(2);


        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration1).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration2).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("4").withObject(configuration3).build();
        IndexQuery indexQuery5 = new IndexQueryBuilder().withId("5").withObject(configuration4).build();
        IndexQuery indexQuery6 = new IndexQueryBuilder().withId("6").withObject(configuration5).build();
        IndexQuery indexQuery7 = new IndexQueryBuilder().withId("7").withObject(configuration6).build();
        IndexQuery indexQuery8 = new IndexQueryBuilder().withId("8").withObject(configuration7).build();
        IndexQuery indexQuery9 = new IndexQueryBuilder().withId("9").withObject(configuration8).build();
        IndexQuery indexQuery10 = new IndexQueryBuilder().withId("10").withObject(configuration9).build();
        IndexQuery indexQuery11 = new IndexQueryBuilder().withId("11").withObject(configuration10).build();
        IndexQuery indexQuery12 = new IndexQueryBuilder().withId("12").withObject(configuration11).build();

        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);
        elasticsearchTemplate.index(indexQuery3, coordinates);
        elasticsearchTemplate.index(indexQuery4, coordinates);
        elasticsearchTemplate.index(indexQuery5, coordinates);
        elasticsearchTemplate.index(indexQuery6, coordinates);
        elasticsearchTemplate.index(indexQuery7, coordinates);
        elasticsearchTemplate.index(indexQuery8, coordinates);
        elasticsearchTemplate.index(indexQuery9, coordinates);
        elasticsearchTemplate.index(indexQuery10, coordinates);
        elasticsearchTemplate.index(indexQuery11, coordinates);
        elasticsearchTemplate.index(indexQuery12, coordinates);

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
        indentDocumentLine.setStockId(53l);
        indentDocumentLine.setSku("HUL~2~BTH12345~2019-05-30~100.0~80.0~true");
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

    private Quantity createRejectedQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(5f);
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
        indentDocumentLine.setStockId(53l);
        indentDocumentLine.setSku("HUL~2~BTH12345~2019-05-30~100.0~80.0~true");
        indentDocumentLine.setOwner("HUL");
        indentDocumentLine.setCost(getBigDecimal(80f));
        indentDocumentLine.setSupplier("HUL");

        return indentDocumentLine;
    }

    @Test
    public void createStockReceipt_Draft_SFA() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("DRAFT");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");


    }


    @Test
    public void createStockReceiptFor_SFA_Approved() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("ISSUED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIndent> indentListC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentC = indentListC.get(indentListC.size() - 1);
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

    }

    @Test
    public void createStockReceiptAgainstMultipleIssue() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            issueDocumentLine.setIssuedQuantity(createPartiallyQuantity());
        }
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_ISSUED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        StockIndent stockIndentC = stockIndentRepository.findOne(stockIndent.getId());
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_PROCESSED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListC = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueC = issueListC.get(issueListC.size() - 1);
        assertThat(stockIssueC.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueC.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListD = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptD = stockReceiptListD.get(stockReceiptListD.size() - 1);
        assertThat(stockReceiptD.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        List<StockIssue> issueListCC = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueCC = issueListCC.get(issueListCC.size() - 1);
        assertThat(stockIssueCC.getDocument().getConversionCompleted()).isTrue();

        stockReceipt = stockReceiptService.findOne(stockReceiptD.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListE = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptE = stockReceiptListE.get(stockReceiptListE.size() - 1);
        assertThat(stockReceiptE.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListD = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueD = issueListD.get(issueListD.size() - 1);
        assertThat(stockIssueD.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIndent> indentListD = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentD = indentListD.get(indentListD.size() - 1);
        assertThat(stockIndentD.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

    }


    @Test
    public void createStockReceiptMultiple_SFA_Approved() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("ISSUED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            receiptDocumentLine.setAcceptedQuantity(createPartiallyQuantity());
        }

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        StockReceipt stockReceiptB = stockReceiptRepository.findOne(stockReceipt.getId());
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        StockIssue stockIssueB = stockIssueRepository.findOne(stockIssue.getId());
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_PROCESSED");

        StockIndent stockIndentC = stockIndentRepository.findOne(stockIndent.getId());
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_RECEIVED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueB.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListC = stockReceiptRepository.findAll();
        StockReceipt stockReceiptC = stockReceiptListC.get(stockReceiptListC.size() - 1);
        assertThat(stockReceiptC.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptC.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        StockReceipt stockReceiptD = stockReceiptRepository.findOne(stockReceipt.getId());
        assertThat(stockReceiptD.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        StockIssue stockIssueC = stockIssueRepository.findOne(stockIssue.getId());
        assertThat(stockIssueC.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        StockIndent indentListD = stockIndentRepository.findOne(stockIndent.getId());
        assertThat(indentListD.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

    }


    @Test
    public void createStockReceiptWithRejectedQuantity() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        /*stockIssue = stockIssueService.save(stockIssue,"SENDFORAPPROVAL");
        stockIssue = stockIssueService.save(stockIssue,"REJECTED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(java.time.LocalDateTime.now());*/

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("ISSUED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            receiptDocumentLine.setAcceptedQuantity(createPartiallyQuantity());
            receiptDocumentLine.setRejectedQuantity(createPartiallyQuantity());
        }

        int databaseSizeBeforeCreateReversal = stockReversalRepository.findAll().size();

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        StockIssue stockIssueB = stockIssueRepository.findOne(stockIssue.getId());
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIndent> indentListC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentC = indentListC.get(indentListC.size() - 1);
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("REVERSAL_PENDING");
        assertThat(stockIndentC.getDocument().getConversionCompleted()).isFalse();

        List<StockReversal> stockReversalListA = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalA = stockReversalListA.get(stockReversalListA.size() - 1);
        assertThat(stockReversalA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertReversalToReceipt(null, stockReversalA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListC = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptC = stockReceiptListC.get(stockReceiptListC.size() - 1);
        assertThat(stockReceiptC.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptC.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListD = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptD = stockReceiptListD.get(stockReceiptListD.size() - 1);
        assertThat(stockReceiptD.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockReversal> stockReversalListB = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalB = stockReversalListB.get(stockReversalListB.size() - 1);
        assertThat(stockReversalB.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIndent> indentListD = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentD = indentListD.get(indentListD.size() - 1);
        assertThat(stockIndentD.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_PROCESSED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListC = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueC = issueListC.get(issueListC.size() - 1);
        assertThat(stockIssueC.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueC.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListE = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptE = stockReceiptListE.get(stockReceiptListE.size() - 1);
        assertThat(stockReceiptE.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptE.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListF = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptF = stockReceiptListF.get(stockReceiptListF.size() - 1);
        assertThat(stockReceiptF.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListD = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueD = issueListD.get(issueListD.size() - 1);
        assertThat(stockIssueD.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIndent> indentListE = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentE = indentListE.get(indentListE.size() - 1);
        assertThat(stockIndentE.getDocument().getStatus().toString()).isEqualTo("PROCESSED");


    }


    @Test
    public void createStockReceiptWithMulRejectedQuantity() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

       /* stockIssue = stockIssueService.save(stockIssue,"SENDFORAPPROVAL");
        stockIssue = stockIssueService.save(stockIssue,"REJECTED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(java.time.LocalDateTime.now());*/

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIndent> indentListB = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentB = indentListB.get(indentListB.size() - 1);
        assertThat(stockIndentB.getDocument().getStatus().toString()).isEqualTo("ISSUED");

        StockReceipt stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            receiptDocumentLine.setAcceptedQuantity(createZeroQuantity());
            receiptDocumentLine.setRejectedQuantity(createRejectedQuantity());
        }

        int databaseSizeBeforeCreateReversal = stockReversalRepository.findAll().size();

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        StockIssue stockIssueB = stockIssueRepository.findOne(stockIssue.getId());
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_PROCESSED");

        List<StockIndent> indentListC = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentC = indentListC.get(indentListC.size() - 1);
        assertThat(stockIndentC.getDocument().getStatus().toString()).isEqualTo("REVERSAL_PENDING");

        List<StockReversal> stockReversalListA = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalA = stockReversalListA.get(stockReversalListA.size() - 1);
        assertThat(stockReversalA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        StockReceipt stockReceipt2 = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt2.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt2.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt2.getDocument().setApprovedBy(DEFAULT_USER);
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt2.getDocument().getLines()) {
            receiptDocumentLine.setAcceptedQuantity(createPartiallyQuantity());
            receiptDocumentLine.setRejectedQuantity(createRejectedQuantity());
        }

        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt2)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListC = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptC = stockReceiptListC.get(stockReceiptListC.size() - 1);

        stockReceipt2 = stockReceiptService.findOne(stockReceiptC.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt2)))
            .andExpect(status().isOk());

        List<StockIndent> indentListD = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentD = indentListD.get(indentListD.size() - 1);
        assertThat(stockIndentD.getDocument().getStatus().toString()).isEqualTo("REVERSAL_PENDING");


        stockReceipt = stockReceiptService.convertReversalToReceipt(null, stockReversalA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListD = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptD = stockReceiptListD.get(stockReceiptListD.size() - 1);

        stockReceipt = stockReceiptService.findOne(stockReceiptD.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());


        List<StockIndent> indentListE = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentE = indentListE.get(indentListE.size() - 1);
        assertThat(stockIndentE.getDocument().getStatus().toString()).isEqualTo("REVERSAL_PENDING");

        List<StockReversal> stockReversalListB = stockReversalRepository.findAll();
        StockReversal stockReversalB = stockReversalListB.get(stockReversalListB.size() - 1);

/*
        stockReceipt = stockReceiptService.convertReversalToReceipt(null,stockReversalB.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(java.time.LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt))).andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListE = stockReceiptRepository.findAll();
        StockReceipt stockReceiptE = stockReceiptListE.get(stockReceiptListE.size() - 1);

        stockReceipt = stockReceiptService.findOne(stockReceiptE.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());



        List<StockIndent> indentListF = stockIndentRepository.findAll();
        StockIndent stockIndentF = indentListF.get(indentListF.size() - 1);
        assertThat(stockIndentF.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_PROCESSED");*/

    }


    @Test
    public void createStockReceiptAgainstDTWhenReversal() throws Exception {
        aCreateIndex();
        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createIndentDocumentWith2Lines());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        stockIssue.getDocument().setDocumentType(TransactionType.Stock_Direct_Transfer);
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            issueDocumentLine.setSourceDocument(null);
        }
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "REJECTED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        stockIssue.getDocument().setDocumentType(TransactionType.Stock_Direct_Transfer);
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            issueDocumentLine.setSourceDocument(null);
        }

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        StockIssue stockIssueA = stockIssueRepository.findOne(stockIssue.getId());
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            if (receiptDocumentLine.getItem().getId().equals(1l)) {
                receiptDocumentLine.setAcceptedQuantity(createPartiallyQuantity());
                receiptDocumentLine.setRejectedQuantity(createPartiallyQuantity());
            } else {
                receiptDocumentLine.setAcceptedQuantity(createZeroQuantity());
            }
        }

        int databaseSizeBeforeCreateReversal = stockReversalRepository.findAll().size();

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("REVERSAL_PENDING");


        List<StockReversal> stockReversalListA = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalA = stockReversalListA.get(stockReversalListA.size() - 1);
        assertThat(stockReversalA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertReversalToReceipt(null, stockReversalA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListC = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptC = stockReceiptListC.get(stockReceiptListC.size() - 1);
        assertThat(stockReceiptC.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptC.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListD = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptD = stockReceiptListD.get(stockReceiptListD.size() - 1);
        assertThat(stockReceiptD.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockReversal> stockReversalListB = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalB = stockReversalListB.get(stockReversalListB.size() - 1);
        assertThat(stockReversalB.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIssue> issueListD = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueD = issueListD.get(issueListD.size() - 1);
        assertThat(stockIssueD.getDocument().getStatus().toString()).isEqualTo("PARTIALLY_PROCESSED");


        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);


        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListX = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptX = stockReceiptListX.get(stockReceiptListX.size() - 1);
        assertThat(stockReceiptX.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptX.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListY = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptY = stockReceiptListY.get(stockReceiptListY.size() - 1);
        assertThat(stockReceiptY.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListX = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueX = issueListX.get(issueListX.size() - 1);
        assertThat(stockIssueX.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

    }

    @Test
    public void createStockReceiptAgainstDT() throws Exception {
        aCreateIndex();

        int databaseSizeBeforeCreateIndent = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        em.detach(stockIndent);
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);
        assertThat(stockIndentA.getDocument().getStatus().toString()).isEqualTo("APPROVED");


        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        stockIssue.getDocument().setDocumentType(TransactionType.Stock_Direct_Transfer);
        stockIssue.getDocument().setLines(createDocumentLine());
        stockIssue.getDocument().getLines().get(0).setSourceDocument(new ArrayList<>());
        stockIssue.getDocument().getLines().get(0).setStockId(53l);
        stockIssue.getDocument().getLines().get(0).setSku("HUL~2~BTH12345~2019-05-30~100.0~80.0~true");
//        stockIssue.getDocument().getLines().get(0).getSourceDocument().add(sourceDocumentForIndent);
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            issueDocumentLine.setSourceDocument(null);
        }
        int databaseSizeBeforeCreateIssue = stockIssueRepository.findAll().size();

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "REJECTED");

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        stockIssue.getDocument().setDocumentType(TransactionType.Stock_Direct_Transfer);
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            issueDocumentLine.setSourceDocument(null);
        }

        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        em.detach(stockIssue);
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);
        assertThat(stockIssueA.getDocument().getStatus().toString()).isEqualTo("APPROVED");
        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            receiptDocumentLine.setAcceptedQuantity(createPartiallyQuantity());
            receiptDocumentLine.setRejectedQuantity(createPartiallyQuantity());
        }

        int databaseSizeBeforeCreateReversal = stockReversalRepository.findAll().size();

        int databaseSizeBeforeCreateReceipt = stockReceiptRepository.findAll().size();
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListA = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptA = stockReceiptListA.get(stockReceiptListA.size() - 1);
        assertThat(stockReceiptA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptA.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListB = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptB = stockReceiptListB.get(stockReceiptListB.size() - 1);
        assertThat(stockReceiptB.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockIssue> issueListB = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueB = issueListB.get(issueListB.size() - 1);
        assertThat(stockIssueB.getDocument().getStatus().toString()).isEqualTo("REVERSAL_PENDING");


        List<StockReversal> stockReversalListA = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalA = stockReversalListA.get(stockReversalListA.size() - 1);
        assertThat(stockReversalA.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        stockReceipt = stockReceiptService.convertReversalToReceipt(null, stockReversalA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);
        restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated());

        List<StockReceipt> stockReceiptListC = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptC = stockReceiptListC.get(stockReceiptListC.size() - 1);
        assertThat(stockReceiptC.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockReceipt = stockReceiptService.findOne(stockReceiptC.getId());
        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        List<StockReceipt> stockReceiptListD = stockReceiptRepository.findAll(Sort.by("id", "version"));
        StockReceipt stockReceiptD = stockReceiptListD.get(stockReceiptListD.size() - 1);
        assertThat(stockReceiptD.getDocument().getStatus().toString()).isEqualTo("APPROVED");

        List<StockReversal> stockReversalListB = stockReversalRepository.findAll(Sort.by("id", "version"));
        StockReversal stockReversalB = stockReversalListB.get(stockReversalListB.size() - 1);
        assertThat(stockReversalB.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

        List<StockIssue> issueListD = stockIssueRepository.findAll(Sort.by("id", "version"));
        StockIssue stockIssueD = issueListD.get(issueListD.size() - 1);
        assertThat(stockIssueD.getDocument().getStatus().toString()).isEqualTo("PROCESSED");

    }


    private ReceiptDocument createReceiptDocument() {
        List<Stock> stockList = stockRepository.findAll();
        Stock stock = stockList.get(stockList.size() - 1);
        SourceDocument sourceDocumentForIndent = new SourceDocument();
        StockIndent stockIndentEn = createEntityStockIndent(em);
        stockIndentEn.setDocument(createDefaultIndentDocument());
        stockIndentService.save(stockIndentEn);
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        StockIndent stockIndent = stockIndentList.get(stockIndentList.size() - 1);
        sourceDocumentForIndent.setId(stockIndent.getId());
        sourceDocumentForIndent.setDocumentNumber(stockIndent.getDocumentNumber());
        sourceDocumentForIndent.setType(TransactionType.Stock_Indent);
        SourceDocument sourceDocumentForIssue = new SourceDocument();
        StockIssue stockIssueEn = createEntityStockIssue(em);
        stockIssueEn.getDocument().setLines(createDocumentLine());
        stockIssueEn.getDocument().getLines().get(0).setSourceDocument(new ArrayList<>());
        stockIssueEn.getDocument().getLines().get(0).getSourceDocument().add(sourceDocumentForIndent);
        org.nh.pharmacy.domain.Organization issueUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIssueDTO = new OrganizationDTO();
        BeanUtils.copyProperties(issueUnit, organizationIssueDTO);
        stockIssueEn.getDocument().setIssueUnit(organizationIssueDTO);
        stockIssueService.save(stockIssueEn);
        List<StockIssue> stockIssueList = stockIssueRepository.findAll();
        StockIssue stockIssue = stockIssueList.get(stockIssueList.size() - 1);
        sourceDocumentForIssue.setId(stockIssue.getId());
        sourceDocumentForIssue.setDocumentNumber(stockIssue.getDocumentNumber());
        sourceDocumentForIssue.setType(TransactionType.Stock_Issue);
        sourceDocumentForIssue.setQuantity(new Quantity(7f, null));
        sourceDocumentForIssue.setPendingQuantity(new Quantity(7f, null));
        List<SourceDocument> sourceDocumentList = new ArrayList<>();
        sourceDocumentList.add(sourceDocumentForIssue);
        sourceDocumentList.add(sourceDocumentForIndent);
        DEFAULT_RECEIPT_DOCUMENT_LINE.setId(1L);
        DEFAULT_RECEIPT_DOCUMENT_LINE.setSourceDocument(sourceDocumentList);
        DEFAULT_RECEIPT_DOCUMENT_LINE.setItem(createItem());
        DEFAULT_RECEIPT_DOCUMENT_LINE.setLocator(createLocator());
        DEFAULT_RECEIPT_DOCUMENT_LINE.setStockId(stock.getId());
        DEFAULT_RECEIPT_DOCUMENT_LINE.setBatchNumber("BATCH");
        DEFAULT_RECEIPT_DOCUMENT_LINE.setOwner("OWNER");
        DEFAULT_RECEIPT_DOCUMENT_LINE.setAcceptedQuantity(createQuantity());
        DEFAULT_RECEIPT_DOCUMENT_LINE.setRejectedQuantity(rejectQuantity());
        DEFAULT_RECEIPT_DOCUMENT_LINE.setCost(getBigDecimal(2.5f));
        DEFAULT_RECEIPT_DOCUMENT_LINE.setConsignment(true);
        DEFAULT_RECEIPT_DOCUMENT_LINE.setBarCode("BAR_Code");
        DEFAULT_RECEIPT_DOCUMENT_LINE.setId(1L);
        DEFAULT_RECEIPT_DOCUMENT_LINE.setSupplier("supplier");

        List list = new ArrayList();
        list.add(DEFAULT_RECEIPT_DOCUMENT_LINE);
        DEFAULT_RECEIPT_DOCUMENT.setReceivedBy(DEFAULT_USER);
        DEFAULT_RECEIPT_DOCUMENT.setLines(list);
        DEFAULT_RECEIPT_DOCUMENT.setIndentStore(createHealthcareServiceCenter());
        DEFAULT_RECEIPT_DOCUMENT.setIssueStore(createHealthcareServiceCenter());
        DEFAULT_RECEIPT_DOCUMENT.setIndentUnit(createOrganization());
        DEFAULT_RECEIPT_DOCUMENT.setIssueUnit(createOrganization());
        DEFAULT_RECEIPT_DOCUMENT.setReceiptDate(LocalDateTime.now());
        DEFAULT_RECEIPT_DOCUMENT.setApprovedDate(LocalDateTime.now());
        DEFAULT_RECEIPT_DOCUMENT.setDocumentType(TransactionType.Stock_Receipt);
        DEFAULT_RECEIPT_DOCUMENT.setCreatedDate(LocalDateTime.now());
        DEFAULT_RECEIPT_DOCUMENT.setStatus(Status.DRAFT);
        return DEFAULT_RECEIPT_DOCUMENT;
    }


    private ReversalDocument createRevarsalDocument() {
        SourceDocument sourceDocumentForIndent = new SourceDocument();
        StockIndent stockIndentEn = createEntityStockIndent(em);
        stockIndentEn.setDocument(createDefaultIndentDocument());
        stockIndentService.save(stockIndentEn);
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        StockIndent stockIndent = stockIndentList.get(stockIndentList.size() - 1);
        sourceDocumentForIndent.setId(stockIndent.getId());
        sourceDocumentForIndent.setDocumentNumber(stockIndent.getDocumentNumber());
        sourceDocumentForIndent.setType(TransactionType.Stock_Indent);
        SourceDocument sourceDocumentForIssue = new SourceDocument();
        StockIssue stockIssueEn = createEntityStockIssue(em);
        stockIssueEn.getDocument().setLines(createDocumentLine());
        stockIssueEn.getDocument().getLines().get(0).setSourceDocument(new ArrayList<>());
        stockIssueEn.getDocument().getLines().get(0).getSourceDocument().add(sourceDocumentForIndent);
        org.nh.pharmacy.domain.Organization issueUnit = organizationRepository.findById(1l).get();
        OrganizationDTO organizationIssueDTO = new OrganizationDTO();
        BeanUtils.copyProperties(issueUnit, organizationIssueDTO);
        stockIssueEn.getDocument().setIssueUnit(organizationIssueDTO);
        stockIssueService.save(stockIssueEn);
        List<StockIssue> stockIssueList = stockIssueRepository.findAll();
        StockIssue stockIssue = stockIssueList.get(stockIssueList.size() - 1);
        sourceDocumentForIssue.setId(stockIssue.getId());
        sourceDocumentForIssue.setDocumentNumber(stockIssue.getDocumentNumber());
        sourceDocumentForIssue.setType(TransactionType.Stock_Issue);
        SourceDocument sourceDocumentForReversal = new SourceDocument();
        List<SourceDocument> sourceDocumentList = new ArrayList<>();
        sourceDocumentList.add(sourceDocumentForIssue);
        sourceDocumentList.add(sourceDocumentForIndent);
        sourceDocumentList.add(sourceDocumentForReversal);

        ReversalDocumentLine reversalDocumentLine = new ReversalDocumentLine();
        reversalDocumentLine.setId(1L);
        reversalDocumentLine.setSourceDocument(sourceDocumentList);
        reversalDocumentLine.setItem(createItem());
        reversalDocumentLine.setLocator(createLocator());
        reversalDocumentLine.setStockId(1L);
        reversalDocumentLine.setBatchNumber("BATCH");
        reversalDocumentLine.setOwner("OWNER");
        reversalDocumentLine.setRejectedQuantity(createQuantity());
        //reversalDocumentLine.setRejectedQuantity(rejectQuantityForReversal());
        reversalDocumentLine.setCost(getBigDecimal(2.5f));
        reversalDocumentLine.setConsignment(true);
        reversalDocumentLine.setBarCode("BAR_Code");
        reversalDocumentLine.setId(1L);
        reversalDocumentLine.setSupplier("supplier");

        List list = new ArrayList();
        list.add(reversalDocumentLine);
        //DEFAULT_REVERSAL_DOCUMENT.setReceivedBy(DEFAULT_USER);
        DEFAULT_REVERSAL_DOCUMENT.setLines(list);
        DEFAULT_REVERSAL_DOCUMENT.setIndentStore(createHealthcareServiceCenter());
        DEFAULT_REVERSAL_DOCUMENT.setIssueStore(createHealthcareServiceCenter());
        DEFAULT_REVERSAL_DOCUMENT.setIndentUnit(createOrganization());
        DEFAULT_REVERSAL_DOCUMENT.setIssueUnit(createOrganization());
        //DEFAULT_REVERSAL_DOCUMENT.setReceiptDate(LocalDateTime.now());
        DEFAULT_REVERSAL_DOCUMENT.setApprovedDate(LocalDateTime.now());
        DEFAULT_REVERSAL_DOCUMENT.setDocumentType(TransactionType.Stock_Reversal);
        return DEFAULT_REVERSAL_DOCUMENT;

    }

    private List<SourceDocument> createSourceDocument() {
        List<SourceDocument> list = new ArrayList<>();
        SourceDocument sourceDocument = new SourceDocument();
        sourceDocument.setId(1l);
        sourceDocument.setDocumentNumber("BBBBBBB");
        sourceDocument.setType(TransactionType.Stock_Indent);
        sourceDocument.setQuantity(createQuantity());
        sourceDocument.setPendingQuantity(createQuantity());
        sourceDocument.setLineId(1l);
        list.add(sourceDocument);
        return list;
    }

    public Stock createStockEntity(EntityManager em) {
        Stock stock = new Stock()
            .itemId(DEFAULT_ITEM_ID)
            .batchNo(DEFAULT_BATCH_NO)
            .expiryDate(DEFAULT_EXPIRY_DATE)
            .owner(DEFAULT_OWNER)
            .cost(DEFAULT_COST)
            .mrp(DEFAULT_MRP)
            .quantity(DEFAULT_QUANTITY)
            .stockValue(DEFAULT_STOCK_VALUE)
            .storeId(DEFAULT_STORE_ID)
            .locatorId(DEFAULT_LOCATOR_ID)
            .supplier(DEFAULT_SUPPLIER)
            .uomId(DEFAULT_UOM_ID)
            .sku(DEFAULT_SKU)
            .unitId(DEFAULT_UNIT_ID)
            .consignment(DEFAULT_CONSIGNMENT);
        return stock;
    }

    @Test
    @Transactional
    public void createStockReceiptWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockReceiptRepository.findAll().size();

        // Create the StockReceipt with an existing ID
        StockReceipt existingStockReceipt = new StockReceipt();
        existingStockReceipt.setId(1L);
        existingStockReceipt.setVersion(0);
        existingStockReceipt.setDocument(DEFAULT_RECEIPT_DOCUMENT);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockReceiptMockMvc.perform(post("/api/stock-receipts")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockReceipt)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockReceipt> stockReceiptList = stockReceiptRepository.findAll();
        assertThat(stockReceiptList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllStockReceipts() throws Exception {
        stockReceiptRepository.deleteAll();
        stockIndentRepository.deleteAll();
        // Initialize the database
        stockReceipt.setDocument(createReceiptDocument());
        stockReceiptService.save(stockReceipt);

        // Get all the stockReceiptList
        restStockReceiptMockMvc.perform(get("/api/stock-receipts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockReceipt.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.receivedBy.displayName").value(hasItem(DEFAULT_RECEIPT_DOCUMENT.getReceivedBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockReceipt() throws Exception {
        stockReceiptRepository.deleteAll();
        stockIndentRepository.deleteAll();
        // Initialize the database
        stockReceipt.setDocument(createReceiptDocument());
        stockReceiptService.save(stockReceipt);

        // Get the stockReceipt
        restStockReceiptMockMvc.perform(get("/api/stock-receipts/{id}", stockReceipt.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockReceipt.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.document.receivedBy.displayName").value(DEFAULT_RECEIPT_DOCUMENT.getReceivedBy().getDisplayName()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStockReceipt() throws Exception {
        // Get the stockReceipt
        restStockReceiptMockMvc.perform(get("/api/stock-receipts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteStockReceipt() throws Exception {
        stockReceiptRepository.deleteAll();
        stockIndentRepository.deleteAll();
        // Initialize the database
        stockReceipt.setDocument(createReceiptDocument());
        stockReceiptService.save(stockReceipt);

        int databaseSizeBeforeDelete = stockReceiptRepository.findAll().size();

        // Get the stockReceipt
        restStockReceiptMockMvc.perform(delete("/api/stock-receipts/{id}", stockReceipt.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockReceiptExistsInEs = stockReceiptSearchRepository.existsById(stockReceipt.getId());
        assertThat(stockReceiptExistsInEs).isFalse();

        // Validate the database is empty
        List<StockReceipt> stockReceiptList = stockReceiptRepository.findAll();
        assertThat(stockReceiptList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockReceipt() throws Exception {
        stockReceiptRepository.deleteAll();
        stockIndentRepository.deleteAll();
        // Initialize the database
        stockReceipt.setDocument(createReceiptDocument());
        stockReceiptService.save(stockReceipt);
        stockReceiptService.doIndex(0,200, LocalDate.now(), LocalDate.now());

        // Search the stockReceipt
        restStockReceiptMockMvc.perform(get("/api/_search/stock-receipts?query=id:" + stockReceipt.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockReceipt.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.receivedBy.displayName").value(hasItem(DEFAULT_RECEIPT_DOCUMENT.getReceivedBy().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void testReceiptToIssueConversion() throws Exception {
        StockIssue stockIssuenEn = createEntityStockIssue(em);
        stockIssueService.save(stockIssuenEn);
        StockIssue stockIssue = stockIssueRepository.findAll().get(0);
        stockIssue.getDocument().setDocumentType(TransactionType.Stock_Issue);
        stockIssue.getDocument().getLines().get(0).setIssuedQuantity(createQuantity());
        stockIssue.getDocument().getLines().get(0).setSourceDocument(createSourceDocument());
        StockReceipt stockReceiptConverted = issueToReceiptMapper.convertFromstockIssue(stockIssue);
        assertThat(stockReceiptConverted.getDocumentNumber()).isNull();
        assertThat(stockReceiptConverted.getDocument().getLines().get(0).getSourceDocument().get(0).getType()).isEqualTo(TransactionType.Stock_Indent);
        stockReceiptConverted = stockReceiptService.save(stockReceiptConverted);
        stockReceiptService.doIndex(0,200, LocalDate.now(), LocalDate.now());
        restStockReceiptMockMvc.perform(get("/api/_convert/stock-receipts/from-stock-issue?docId=" + stockIssue.getId())
            .contentType(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

    }

    @Test
    @Transactional
    public void testReversalToReceiptConversion() throws Exception {
        stockReceiptRepository.deleteAll();
        stockIndentRepository.deleteAll();
        StockReversal stockReversal = createEntityStockReversal(em);
        stockReversal.setDocument(createRevarsalDocument());
        StockReceipt stockReceiptConverted = reversalToReceiptMapper.convertFromstockReversal(stockReversal);
        assertThat(stockReceiptConverted.getDocumentNumber()).isNull();
        stockReceiptService.save(stockReceiptConverted);
        stockReceiptService.doIndex(0,200, LocalDate.now(), LocalDate.now());
        stockReversal = stockReversalService.save(stockReversal);
        restStockReceiptMockMvc.perform(get("/api/_convert/stock-receipts/from-stock-reversal?docId=" + stockReversal.getId())
            .contentType(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    public StockReceipt prepareStockReceiptForWorkflow() throws Exception {

        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APPROVED");
        stockIndentService.index(stockIndent);
        List<StockIndent> indentListA = stockIndentRepository.findAll();
        StockIndent stockIndentA = indentListA.get(indentListA.size() - 1);

        stockIssue = stockIssueService.convertIndentToIssue(null, stockIndentA.getDocumentNumber());
        stockIssue.getDocument().setIssuedBy(DEFAULT_USER);
        stockIssue.getDocument().setIssueDate(LocalDateTime.now());
        stockIssue = stockIssueService.save(stockIssue, "SENDFORAPPROVAL");
        stockIssue = stockIssueService.save(stockIssue, "APPROVED");
        stockIssueService.index(stockIssue);
        List<StockIssue> issueListA = stockIssueRepository.findAll();
        StockIssue stockIssueA = issueListA.get(issueListA.size() - 1);

        stockReceipt = stockReceiptService.convertIssueToReceipt(null, stockIssueA.getDocumentNumber());
        stockReceipt.getDocument().setReceiptDate(LocalDateTime.now());
        stockReceipt.getDocument().setReceivedBy(DEFAULT_USER);
        stockReceipt.getDocument().setApprovedBy(DEFAULT_USER);

        return stockReceipt;
    }

    // @Test
    public void verifyWorkflow() throws Exception {

        aCreateIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");

        stockReceipt = prepareStockReceiptForWorkflow();

        MvcResult result = restStockReceiptMockMvc.perform(post("/api/stock-receipts?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isCreated()).andReturn();

        stockReceipt = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockReceipt.class);

        restStockReceiptMockMvc.perform(put("/api/stock-receipts?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        stockReceipt = stockReceiptService.findOne(stockReceipt.getId());

        result = restStockReceiptMockMvc.perform(get("/api/_workflow/stock-receipts?documentNumber=" + stockReceipt.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        //Retrieve task content
        result = restWorkflowMockMvc.perform(get("/api/jbpm/process/process-variable?taskId=" + taskId + "&variableName=content")).andExpect(status().isOk()).andReturn();
        Map taskContent = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Assert.assertEquals(String.valueOf(stockReceipt.getId()), String.valueOf(taskContent.get("document_id")));
        Assert.assertEquals(stockReceipt.getDocument().getDocumentType().name(), String.valueOf(taskContent.get("document_type")));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockReceiptMockMvc.perform(put("/api/_workflow/stock-receipts?transition=Rejected&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockReceipt)))
            .andExpect(status().isOk());

        stockReceiptRepository.deleteAll();
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockReceipt.class);
    }

}
