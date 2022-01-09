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
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.repository.search.JBPMTaskSearchRepository;
import org.nh.jbpm.security.SpringSecurityIdentityProvider;
import org.nh.jbpm.service.WorkflowService;
import org.nh.jbpm.web.rest.WorkflowResource;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.StockIndent;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.OrganizationSearchRepository;
import org.nh.pharmacy.repository.search.StockIndentSearchRepository;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.repository.hibernate.type.JacksonUtil;
import org.nh.security.AuthenticatedUser;
import org.nh.seqgen.exception.SequenceGenerateException;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockIndentResource REST controller.
 *
 * @see StockIndentResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockIndentResourceIntTest {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final IndentDocument DEFAULT_DOCUMENT = new IndentDocument();
    private static final IndentDocument UPDATED_DOCUMENT = new IndentDocument();

    private static final List<IndentDocumentLine> DEFAULT_LINE = Arrays.asList(new IndentDocumentLine());


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

    @Autowired
    private StockIndentRepository stockIndentRepository;

    @Autowired
    private StockIndentService stockIndentService;

    @Autowired
    private SpringSecurityIdentityProvider identityProvider;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private StockIndentSearchRepository stockIndentSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private ItemStoreStockViewRepository itemStoreStockViewRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ItemStoreStockViewService itemStoreStockViewService;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private StockIssueService stockIssueService;

    @Autowired
    private StockIssueRepository stockIssueRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UOMRepository uomRepository;

    @Autowired
    private OrganizationSearchRepository organizationSearchRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    private MockMvc restStockIndentMockMvc;

    private MockMvc restWorkflowMockMvc;

    private StockIndent stockIndent;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockIndentResource stockIndentResource = new StockIndentResource(stockIndentService, applicationProperties,stockIndentRepository,stockIndentSearchRepository);
        this.restStockIndentMockMvc = MockMvcBuilders.standaloneSetup(stockIndentResource)
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

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StockIndent createEntity(EntityManager em) {
        DEFAULT_DOCUMENT.setIndenterName(DEFAULT_USER);
        DEFAULT_DOCUMENT.setLines(DEFAULT_LINE);
        DEFAULT_DOCUMENT.setStatus(null);
        DEFAULT_DOCUMENT.setIndentDate(LocalDateTime.now());
        DEFAULT_DOCUMENT.setIndentUnit(createUnit(3l));
        DEFAULT_DOCUMENT.setDraft(true);
        HealthcareServiceCenterDTO healthcareServiceCenter = new HealthcareServiceCenterDTO();
        healthcareServiceCenter.setName("General Store");
        DEFAULT_DOCUMENT.setIssueStore(healthcareServiceCenter);
        DEFAULT_DOCUMENT.setDocumentType(TransactionType.Stock_Indent);
        StockIndent stockIndent = new StockIndent()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(createIndentDocument(em))
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockIndent;
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

    public static IndentDocumentLine createIndentDocumentLine(EntityManager em) {
        IndentDocumentLine line = new IndentDocumentLine();
        line.setQuantity(createQuantity());
        line.setItem(createNewItem());
        //line.setLocator(createLocator());
        return line;

    }

    public static IndentDocument createIndentDocument(EntityManager em) {
        List<IndentDocumentLine> lines = new ArrayList<>();
        lines.add(createIndentDocumentLine(em));

        IndentDocument document = new IndentDocument();
        document.setIndentDate(LocalDateTime.now());
        document.setStatus(Status.DRAFT);
        document.setLines(lines);
        document.setIndentUnit(createUnit(3l));
        document.setIssueUnit(createUnit(3l));
        document.setIndentStore(createHsc(1l));
        document.setIssueStore(createHsc(2l));
        document.setDocumentType(TransactionType.Stock_Indent);
        document.setCreatedDate(LocalDateTime.now());
        document.setIndenterName(DEFAULT_USER);
        document.setStatus(Status.DRAFT);
        return document;
    }

    public IndentDocument createDefaultIndentDocument() {
        List<IndentDocumentLine> lines = new ArrayList<>();
        lines.add(createDefaultIndentDocumentLine());

        IndentDocument document = new IndentDocument();
        document.setIndentDate(LocalDateTime.now());
        document.setStatus(Status.DRAFT);
        document.setLines(lines);
        org.nh.pharmacy.domain.Organization indentUnit = organizationRepository.findById(3l).get();
        OrganizationDTO organizationIndentDTO = new OrganizationDTO();
        BeanUtils.copyProperties(indentUnit, organizationIndentDTO);
        org.nh.pharmacy.domain.Organization issueUnit = organizationRepository.findById(3l).get();
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
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        indentDocumentLine.setQuantity(quantity);

        return indentDocumentLine;
    }


    private static Quantity createQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(10f);
        UOMDTO uom = new UOMDTO();
        uom.setId(1l);
        quantity.setUom(uom);
        return quantity;
    }

    private static ItemDTO createNewItem() {
        ItemDTO item = new ItemDTO();
        item.setId(1l);
        item.setName("DEFAULT_ITEM_NAME");
        return item;
    }

    private static OrganizationDTO createUnit(Long id) {
        OrganizationDTO organization = new OrganizationDTO();
        organization.setId(id);
        organization.setCode("DEFAULT_UNIT");
        organization.setName("DEFAULT_UNIT_NAME");
        return organization;
    }

    private static HealthcareServiceCenterDTO createHsc(Long id) {
        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(id);
        hsc.setName("DEFAULT_HSC_NAME");
        hsc.setCode("DEFAULT_HSC_CODE");
        return hsc;
    }

    @Before
    public void initTest() {
        stockIndentSearchRepository.deleteAll();
        stockIndent = createEntity(em);
        groupService.doIndex();
        addUserToSecurityContext(1L, "admin", "admin");
        aCreateIndex();
    }

    @Test
    public void createStockIndent_Draft_Delete() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        restStockIndentMockMvc.perform(post("/api/stock-indents?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        List<StockIndent> stockIndentListForDraft = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForDraft = stockIndentListForDraft.get(stockIndentListForDraft.size() - 1);
        assertThat(stockIndentListForDraft).hasSize(databaseSizeBeforeCreate + 1);
        assertThat(stockIndentForDraft.getDocument().getStatus().toString()).isEqualTo("DRAFT");

        stockIndent = stockIndentService.findOne(stockIndentForDraft.getId());


        int databaseSizeBeforeDelete = stockIndentRepository.findAll().size();

        // Get the stockIndent
        restStockIndentMockMvc.perform(delete("/api/stock-indents/{id}", stockIndent.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean stockIndentExistsInEs = stockIndentSearchRepository.existsById(stockIndent.getId());
        assertThat(stockIndentExistsInEs).isFalse();

        // Validate the database is empty
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeDelete - 1);

    }

    @Test
    public void createStockIndent_Draft_SFA_Rejected() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        restStockIndentMockMvc.perform(post("/api/stock-indents?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        List<StockIndent> stockIndentListForDraft = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForDraft = stockIndentListForDraft.get(stockIndentListForDraft.size() - 1);
        assertThat(stockIndentListForDraft).hasSize(databaseSizeBeforeCreate + 1);
        assertThat(stockIndentForDraft.getDocument().getStatus().toString()).isEqualTo("DRAFT");

        stockIndent = stockIndentService.findOne(stockIndentForDraft.getId());
        restStockIndentMockMvc.perform(put("/api/stock-indents?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        List<StockIndent> stockIndentListForSFA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForSFA = stockIndentListForSFA.get(stockIndentListForSFA.size() - 1);
        assertThat(stockIndentListForSFA).hasSize(databaseSizeBeforeCreate + 2);
        assertThat(stockIndentForSFA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIndent = stockIndentService.findOne(stockIndent.getId());
        restStockIndentMockMvc.perform(put("/api/stock-indents?act=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        List<StockIndent> stockIndentListForRejected = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForRejected = stockIndentListForRejected.get(stockIndentListForRejected.size() - 1);
        assertThat(stockIndentListForRejected).hasSize(databaseSizeBeforeCreate + 3);
        assertThat(stockIndentForRejected.getDocument().getStatus().toString()).isEqualTo("REJECTED");

    }

    @Test
    public void createStockIndent_SFA_Rejected() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        restStockIndentMockMvc.perform(put("/api/stock-indents?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        List<StockIndent> stockIndentListForSFA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForSFA = stockIndentListForSFA.get(stockIndentListForSFA.size() - 1);
        assertThat(stockIndentListForSFA).hasSize(databaseSizeBeforeCreate + 1);
        assertThat(stockIndentForSFA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIndent = stockIndentService.findOne(stockIndentForSFA.getId());
        restStockIndentMockMvc.perform(put("/api/stock-indents?act=REJECTED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        List<StockIndent> stockIndentListForRejected = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForRejected = stockIndentListForRejected.get(stockIndentListForRejected.size() - 1);
        assertThat(stockIndentListForRejected).hasSize(databaseSizeBeforeCreate + 2);
        assertThat(stockIndentForRejected.getDocument().getStatus().toString()).isEqualTo("REJECTED");

    }

    @Test
    public void createStockIndent_SFA_Approved() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        restStockIndentMockMvc.perform(put("/api/stock-indents?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        List<StockIndent> stockIndentListForSFA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForSFA = stockIndentListForSFA.get(stockIndentListForSFA.size() - 1);
        assertThat(stockIndentListForSFA).hasSize(databaseSizeBeforeCreate + 1);
        assertThat(stockIndentForSFA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIndent = stockIndentService.findOne(stockIndentForSFA.getId());
        restStockIndentMockMvc.perform(put("/api/stock-indents?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        List<StockIndent> stockIndentListForApproved = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForApproved = stockIndentListForApproved.get(stockIndentListForApproved.size() - 1);
        assertThat(stockIndentListForApproved).hasSize(databaseSizeBeforeCreate + 2);
        assertThat(stockIndentForApproved.getDocument().getStatus().toString()).isEqualTo("APPROVED");

    }


    @Test
    public void createStockIndent_Draft_SFA_Approved() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        restStockIndentMockMvc.perform(post("/api/stock-indents?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        List<StockIndent> stockIndentListForDraft = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForDraft = stockIndentListForDraft.get(stockIndentListForDraft.size() - 1);
        assertThat(stockIndentListForDraft).hasSize(databaseSizeBeforeCreate + 1);
        assertThat(stockIndentForDraft.getDocument().getStatus().toString()).isEqualTo("DRAFT");

        stockIndent = stockIndentService.findOne(stockIndentForDraft.getId());
        restStockIndentMockMvc.perform(put("/api/stock-indents?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        List<StockIndent> stockIndentListForSFA = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForSFA = stockIndentListForSFA.get(stockIndentListForSFA.size() - 1);
        assertThat(stockIndentListForSFA).hasSize(databaseSizeBeforeCreate + 2);
        assertThat(stockIndentForSFA.getDocument().getStatus().toString()).isEqualTo("WAITING_FOR_APPROVAL");

        stockIndent = stockIndentService.findOne(stockIndent.getId());
        restStockIndentMockMvc.perform(put("/api/stock-indents?act=APPROVED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        List<StockIndent> stockIndentListForApproved = stockIndentRepository.findAll(Sort.by("id", "version"));
        StockIndent stockIndentForApproved = stockIndentListForApproved.get(stockIndentListForApproved.size() - 1);
        assertThat(stockIndentListForApproved).hasSize(databaseSizeBeforeCreate + 3);
        assertThat(stockIndentForApproved.getDocument().getStatus().toString()).isEqualTo("APPROVED");

    }

    @Test
    @Transactional
    public void createStockIndentWithNull() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();

        // Create the StockIndent

        restStockIndentMockMvc.perform(post("/api/stock-indents?act=null")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        // Validate the StockIndent in the database
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeCreate + 1);
        StockIndent testStockIndent = stockIndentList.get(stockIndentList.size() - 1);
        assertThat(testStockIndent.getDocumentNumber()).isNotEmpty();
        assertThat(testStockIndent.getDocument().getIndenterName().getLogin()).isEqualTo(DEFAULT_NAME);
        assertThat(testStockIndent.getDocument().getIndenterName().getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testStockIndent.getDocument().getIndenterName().getEmployeeNo()).isEqualTo(DEFAULT_EMPLOYEE_NO);

        assertThat(testStockIndent.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testStockIndent.isLatest()).isEqualTo(DEFAULT_LATEST);
        assertThat(testStockIndent.getDocument().getStatus().toString()).isEqualTo("DRAFT");

        // Validate the StockIndent in ElasticSearch
        StockIndent stockIndentEs = stockIndentSearchRepository.findById(testStockIndent.getId()).get();
        assertThat(stockIndentEs.getDocument().getIndenterName().getLogin()).isEqualTo(testStockIndent.getDocument().getIndenterName().getLogin());
        assertThat(stockIndentEs.getDocument().getIndenterName().getDisplayName()).isEqualTo(testStockIndent.getDocument().getIndenterName().getDisplayName());
        assertThat(stockIndentEs.getDocument().getIndenterName().getEmployeeNo()).isEqualTo(testStockIndent.getDocument().getIndenterName().getEmployeeNo());
        assertThat(stockIndentEs.getDocumentNumber()).isEqualTo(testStockIndent.getDocumentNumber());
    }

    @Test
    @Transactional
    public void createStockIndentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockIndentRepository.findAll().size();

        // Create the StockIndent with an existing ID
        StockIndent existingStockIndent = new StockIndent();
        existingStockIndent.setId(1L);
        existingStockIndent.setVersion(0);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockIndentMockMvc.perform(post("/api/stock-indents")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockIndent)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkLatestIsRequired() throws Exception {
        int databaseSizeBeforeTest = stockIndentRepository.findAll().size();
        // set the field null
        stockIndent.setLatest(null);

        // Create the StockIndent, which fails.
        restStockIndentMockMvc.perform(post("/api/stock-indents")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isBadRequest());

        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllStockIndents() throws Exception {
        // Initialize the database
        stockIndent.setDocument(createDefaultIndentDocument());
        stockIndentService.save(stockIndent);

        // Get all the stockIndentList
        restStockIndentMockMvc.perform(get("/api/stock-indents?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockIndent.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.indenterName.login").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].document.indenterName.displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].document.indenterName.employeeNo").value(hasItem(DEFAULT_EMPLOYEE_NO)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockIndent() throws Exception {
        // Initialize the database
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());
        stockIndentService.save(stockIndent);

        // Get the stockIndent
        restStockIndentMockMvc.perform(get("/api/stock-indents/{id}", stockIndent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockIndent.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.document.indenterName.login").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.document.indenterName.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.document.indenterName.employeeNo").value(DEFAULT_EMPLOYEE_NO))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStockIndent() throws Exception {
        // Get the stockIndent
        restStockIndentMockMvc.perform(get("/api/stock-indents/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    //@Test
    @Transactional
    public void updateStockIndent() throws Exception {
        // Initialize the database
        stockIndent = createEntity(em);
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());
        stockIndent = stockIndentService.save(stockIndent);

        int databaseSizeBeforeUpdate = stockIndentRepository.findAll().size();

        UPDATED_DOCUMENT.setIndenterName(UPDATED_USER);

        // Update the stockIndent
        StockIndent updatedStockIndent = stockIndentRepository.findOne(stockIndent.getId());
        updatedStockIndent
            .documentNumber(UPDATED_DOCUMENT_NUMBER)
            //.document(UPDATED_DOCUMENT)
            .latest(DEFAULT_LATEST);
        updatedStockIndent.getDocument().setIndenterName(UPDATED_USER);
        restStockIndentMockMvc.perform(put("/api/stock-indents")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockIndent)))
            .andExpect(status().isOk());

        // Validate the StockIndent in the database
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeUpdate + 1);
        StockIndent testStockIndent = stockIndentList.get(stockIndentList.size() - 1);
        assertThat(testStockIndent.getDocumentNumber()).isNotEmpty();
        assertThat(testStockIndent.getDocument().getIndenterName().getLogin()).isEqualTo(UPDATED_NAME);
        assertThat(testStockIndent.getDocument().getIndenterName().getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
        assertThat(testStockIndent.getDocument().getIndenterName().getEmployeeNo()).isEqualTo(UPDATED_EMPLOYEE_NO);
        assertThat(testStockIndent.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testStockIndent.isLatest()).isEqualTo(DEFAULT_LATEST);

        // Validate the StockIndent in ElasticSearch
        StockIndent stockIndentEs = stockIndentSearchRepository.findById(testStockIndent.getId()).get();
        assertThat(stockIndentEs.getDocument().getIndenterName().getLogin()).isEqualTo(testStockIndent.getDocument().getIndenterName().getLogin());
        assertThat(stockIndentEs.getDocument().getIndenterName().getDisplayName()).isEqualTo(testStockIndent.getDocument().getIndenterName().getDisplayName());
        assertThat(stockIndentEs.getDocument().getIndenterName().getEmployeeNo()).isEqualTo(testStockIndent.getDocument().getIndenterName().getEmployeeNo());

        assertThat(stockIndentEs.getDocumentNumber()).isEqualTo(testStockIndent.getDocumentNumber());
        assertThat(stockIndentEs.getVersion()).isEqualTo(testStockIndent.getVersion());


    }

    @Test
    @Transactional
    public void updateNonExistingStockIndent() throws Exception {
        int databaseSizeBeforeUpdate = stockIndentRepository.findAll().size();
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());

        // Create the StockIndent

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockIndentMockMvc.perform(put("/api/stock-indents")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated());

        // Validate the StockIndent in the database
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockIndent() throws Exception {
        // Initialize the database
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());
        stockIndentService.save(stockIndent);

        int databaseSizeBeforeDelete = stockIndentRepository.findAll().size();

        // Get the stockIndent
        restStockIndentMockMvc.perform(delete("/api/stock-indents/{id}", stockIndent.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean stockIndentExistsInEs = stockIndentSearchRepository.existsById(stockIndent.getId());
        assertThat(stockIndentExistsInEs).isFalse();

        // Validate the database is empty
        List<StockIndent> stockIndentList = stockIndentRepository.findAll();
        assertThat(stockIndentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockIndent() throws Exception {
        // Initialize the database
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());
        stockIndentService.save(stockIndent);
        stockIndentService.doIndex(0,200, LocalDate.now(), LocalDate.now());
        // Search the stockIndent
        restStockIndentMockMvc.perform(get("/api/_search/stock-indents?query=id:" + stockIndent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockIndent.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].document.indenterName.login").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].document.indenterName.displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].document.indenterName.employeeNo").value(hasItem(DEFAULT_EMPLOYEE_NO)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getStockIndentStatusCount() throws Exception {
        // Initialize the database
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());
        StockIndent stockIndentTest = stockIndentService.save(stockIndent, "");

        // Get the stockIndent
        restStockIndentMockMvc.perform(get("/api/status-count/stock-indents?query=id:" + stockIndent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockIndent.class);
    }


    @Test(expected = BusinessRuleViolationException.class)
    @Transactional
    public void checkBusinessRuleSendForApproval() throws Exception {
        stockIndent.getDocument().setStatus(Status.WAITING_FOR_APPROVAL);
        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
    }

    @Test(expected = BusinessRuleViolationException.class)
    @Transactional
    public void checkBusinessRuleDelete() throws Exception {
        stockIndent.getDocument().setStatus(Status.DRAFT);
        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndentService.delete(stockIndent.getId());
    }

    @Test
    public void verifyWorkflow() throws Exception {

        aCreateIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");
        MvcResult result = restStockIndentMockMvc.perform(post("/api/stock-indents?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isCreated()).andReturn();

        stockIndent = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockIndent.class);

        restStockIndentMockMvc.perform(put("/api/stock-indents?act=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=3&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        restWorkflowMockMvc.perform(get("/api/jbpm/task/personal?unitId=3&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();

        stockIndent = stockIndentService.findOne(stockIndent.getId());

        result = restStockIndentMockMvc.perform(get("/api/_workflow/stock-indents?documentNumber=" + stockIndent.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        //Retrieve task content
        result = restWorkflowMockMvc.perform(get("/api/jbpm/process/process-variable?taskId=" + taskId + "&variableName=content")).andExpect(status().isOk()).andReturn();
        Map taskContent = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Assert.assertEquals(String.valueOf(stockIndent.getId()), String.valueOf(taskContent.get("document_id")));
        Assert.assertEquals(stockIndent.getDocument().getDocumentType().name(), String.valueOf(taskContent.get("document_type")));

        restStockIndentMockMvc.perform(put("/api/_workflow/stock-indents?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockIndent)))
            .andExpect(status().isOk());

        //Check workflow service methods

        restWorkflowMockMvc.perform(get("/api/jbpm/process/process-definitions")).andExpect(status().isOk());

        restWorkflowMockMvc.perform(get("/api/jbpm/process/process-instances?processDefinitionId=stock_indent_document_process")).andExpect(status().isOk());

        restWorkflowMockMvc.perform(get("/api/jbpm/process/process-tasks?processInstanceId=1")).andExpect(status().isOk());

        restWorkflowMockMvc.perform(get("/api/jbpm/process/process-image?processDefinitionId=stock_indent_document_process")).andExpect(status().isOk());

        //stockIndentService.delete(stockIndent.getId());
        stockIndentRepository.deleteAll();
    }

    @Test
    public void aCreateIndex() {
        if (elasticsearchTemplate.indexExists("configurations"))
            elasticsearchTemplate.deleteIndex("configurations");
        elasticsearchTemplate.createIndex("configurations");
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("athma_stockindent_autoclose_local_period_days", "14");
        objectMap.put("athma_stockindent_restriction_local_openreceipt_duration_hours", "1");

        org.nh.pharmacy.dto.Configurations conf = new org.nh.pharmacy.dto.Configurations();
        conf.setId(1l);
        conf.setApplicableTo(3l);
        conf.setApplicableType(ConfigurationLevel.Unit.name());
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
        configuration.setKey("athma_stockindent_enable_workflow");
        configuration.setValue("Yes");
        configuration.setLevel(1);

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(3l);
        configuration1.setApplicableType(ConfigurationLevel.Unit);
        configuration1.setKey("athma_stockindent_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(3l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_stockindent_workflow_definition");
        configuration2.setValue("stock_indent_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(3l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_date_format");
        configuration3.setValue("dd/MM/yy");
        configuration3.setLevel(2);

        Configuration configuration4 = new Configuration();
        configuration4.setApplicableCode("1001");
        configuration4.setApplicableTo(3l);
        configuration4.setApplicableType(ConfigurationLevel.Unit);
        configuration4.setKey("athma_stockindent_autoclose_local_period_days");
        configuration4.setValue("14");
        configuration4.setLevel(2);

        Configuration configuration5 = new Configuration();
        configuration5.setApplicableCode("1001");
        configuration5.setApplicableTo(3l);
        configuration5.setApplicableType(ConfigurationLevel.Unit);
        configuration5.setKey("athma_stockindent_restriction_local_openreceipt_duration_hours");
        configuration5.setValue("1");
        configuration5.setLevel(2);

        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration1).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration2).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("4").withObject(configuration3).build();
        IndexQuery indexQuery5 = new IndexQueryBuilder().withId("5").withObject(configuration4).build();
        IndexQuery indexQuery6 = new IndexQueryBuilder().withId("6").withObject(configuration5).build();

        IndexCoordinates coordinates = IndexCoordinates.of("configuration");
        elasticsearchTemplate.index(indexQuery1, coordinates);
        elasticsearchTemplate.index(indexQuery2, coordinates);
        elasticsearchTemplate.index(indexQuery3, coordinates);
        elasticsearchTemplate.index(indexQuery4, coordinates);
        elasticsearchTemplate.index(indexQuery5, coordinates);
        elasticsearchTemplate.index(indexQuery6, coordinates);

        elasticsearchTemplate.refresh(Configuration.class);
    }

    @Test
    @Transactional
    public void verifyValidityDate() throws Exception {
        StockIndent stockIndent = createEntity(em);
        LocalDateTime validityDays = LocalDateTime.now().plusDays(14);
        int day = validityDays.getDayOfMonth();
        int month = validityDays.getMonthValue();
        int year = validityDays.getYear();


        LocalDateTime indentDate = stockIndent.getDocument().getIndentDate();
        stockIndentService.assignValidityDate(stockIndent);
        assertThat(stockIndent.getDocument().getIndentValidDate()).isEqualTo(LocalDateTime.of(year, month, day, 23, 59, 59, 00));
    }

    @Test
    public void schedulerForIndentAutoClose() throws BusinessRuleViolationException, SequenceGenerateException, FieldValidationException {
        aCreateIndex();
        StockIndent stockIndent = createEntity(em);
        stockIndent.setDocumentNumber(null);
        stockIndent.setDocument(createDefaultIndentDocument());
        stockIndent = stockIndentService.save(stockIndent, "SENDFORAPPROVAL");
        stockIndent = stockIndentService.save(stockIndent, "APROVED");
        stockIndentService.doCloseBySystem();
    }

    @Test
    @Transactional
    public void downloadStockIndentTemplate() throws Exception {
        restStockIndentMockMvc.perform(get("/api/_download/stock-indents/template"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

    }

    @Test
    public void testValidationForIndentRestriction() throws Exception {
        aCreateIndex();
        stockIndentRepository.deleteAll();
        stockIssueRepository.deleteAll();
        StockIndent stockIndent = createEntity(em);
        stockIndent.getDocument().setStatus(Status.ISSUED);
        stockIndent.getDocument().setModifiedDate(LocalDateTime.now().minusHours(2));
        StockIndent indent = stockIndentService.save(stockIndent);
        stockIndentService.index(indent);

        StockIssue issue = stockIssueService.convertIndentToIssue(indent.getId(), indent.getDocumentNumber());
        issue.setDocumentNumber("DRAFT");
        issue.getDocument().setStatus(Status.APPROVED);
        issue.getDocument().setModifiedDate(LocalDateTime.now().minusHours(2));
        issue = stockIssueService.save(issue);
        stockIssueService.index(issue);

        indent.setId(null);
        indent.getDocument().setStatus(Status.DRAFT);
        restStockIndentMockMvc.perform(post("/api/stock-indents?act=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(indent)))
            .andExpect(status().isCreated());
    }


}

