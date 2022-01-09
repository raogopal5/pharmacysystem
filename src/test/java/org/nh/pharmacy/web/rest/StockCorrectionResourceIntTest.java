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
import org.nh.pharmacy.domain.StockCorrection;
import org.nh.pharmacy.domain.dto.Configuration;
import org.nh.pharmacy.domain.dto.CorrectionDocument;
import org.nh.pharmacy.domain.dto.CorrectionDocumentLine;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.StockCorrectionSearchRepository;
import org.nh.pharmacy.service.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StockCorrectionResource REST controller.
 *
 * @see StockCorrectionResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class StockCorrectionResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_DOCUMENT = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT = "BBBBBBBBBB";

    private static final CorrectionDocument DEFAULT_CORRECTION_DOCUMENT = new CorrectionDocument();
    private static final CorrectionDocument UPDATED_CORRECTION_DOCUMENT = new CorrectionDocument();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    @Autowired
    private StockCorrectionRepository stockCorrectionRepository;

    @Autowired
    private StockCorrectionService stockCorrectionService;

    @Autowired
    private StockCorrectionSearchRepository stockCorrectionSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private EntityManager em;

    @Autowired
    private StockService stockService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private SpringSecurityIdentityProvider identityProvider;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UOMRepository uomRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private LocatorService locatorService;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc restStockCorrectionMockMvc;

    private StockCorrection stockCorrection;

    private MockMvc restWorkflowMockMvc;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StockCorrectionResource stockCorrectionResource = new StockCorrectionResource(stockCorrectionService,stockCorrectionRepository,stockCorrectionSearchRepository,applicationProperties);
        this.restStockCorrectionMockMvc = MockMvcBuilders.standaloneSetup(stockCorrectionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
        WorkflowResource workflowResource = new WorkflowResource(workflowService, identityProvider, jbpmTaskSearchRepository, jbpmProperties);
        this.restWorkflowMockMvc = MockMvcBuilders.standaloneSetup(workflowResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    private static UserDTO createUser(Long id, String name, String empNo) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setLogin(name);
        user.setEmployeeNo(empNo);
        user.setDisplayName(name);
        return user;
    }

    private static OrganizationDTO createUnit() {
        OrganizationDTO organization = new OrganizationDTO();
        organization.setId(1l);
        organization.setCode("1001");
        organization.setName("Athma Cardiac");
        return organization;
    }

    private List<CorrectionDocumentLine> createDefaultAdjustmentLines() {
        org.nh.pharmacy.domain.Item item = itemService.findOne(1l);
        ItemDTO itemDto = new ItemDTO();
        BeanUtils.copyProperties(item, itemDto);
        org.nh.pharmacy.domain.Locator locator = locatorService.findOne(1l);
        LocatorDTO locatorDto = new LocatorDTO();
        BeanUtils.copyProperties(locator, locatorDto);
        List<CorrectionDocumentLine> lines = new ArrayList<>();
        CorrectionDocumentLine correctionDocumentLine = new CorrectionDocumentLine();
        correctionDocumentLine.setId(1l);
        correctionDocumentLine.setCorrectionQuantity(createDefaultQuantity());
        correctionDocumentLine.setToBatchNumber("BTH123456");
        correctionDocumentLine.setFromBatchNumber("BTH12345");
        correctionDocumentLine.setFromExpiryDate(LocalDate.now());
        correctionDocumentLine.setToExpiryDate(LocalDate.now().plusDays(2));
        correctionDocumentLine.setItem(itemDto);
        correctionDocumentLine.setLocator(locatorDto);
        correctionDocumentLine.setFromMrp(getBigDecimal(80f));
        correctionDocumentLine.setToMrp(getBigDecimal(90f));
        correctionDocumentLine.setOwner("HUL");
        correctionDocumentLine.setStockQuantity(createStockQuantity());
        correctionDocumentLine.setStockId(51l);
        correctionDocumentLine.setBatchQuantity(51f);
        lines.add(correctionDocumentLine);
        return lines;
    }

    private Quantity createStockQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(stockRepository.findById(51l).get().getQuantity());
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }

    private Quantity createDefaultQuantity() {
        Quantity quantity = new Quantity();
        quantity.setValue(20f);
        quantity.setUom(uomRepository.findById(1l).get().getUOMDTO());
        return quantity;
    }


    private CorrectionDocument createDefaultCorrectionDocument() {

        CorrectionDocument correctionDocument = new CorrectionDocument();
        correctionDocument.setCreatedBy(createUser(1l, "Nitesh", "empl"));
        correctionDocument.setType(TransactionType.Stock_Correction);
        correctionDocument.setModifiedDate(LocalDateTime.now());
        org.nh.pharmacy.domain.HealthcareServiceCenter correctionHealthcareServiceCenter = healthcareServiceCenterRepository.findById(2l).get();
        HealthcareServiceCenterDTO correctionHealthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        BeanUtils.copyProperties(correctionHealthcareServiceCenter, correctionHealthcareServiceCenterDTO);
        correctionHealthcareServiceCenterDTO.setPartOf(createUnit());
        correctionDocument.setStore(correctionHealthcareServiceCenterDTO);
        correctionDocument.setLines(createDefaultAdjustmentLines());
        correctionDocument.setStatus(Status.DRAFT);
        correctionDocument.setApprovedDate(LocalDateTime.now());
        correctionDocument.setCreatedDate(LocalDateTime.now());
        correctionDocument.setId(1l);
        return correctionDocument;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StockCorrection createEntity(EntityManager em) {
        StockCorrection stockCorrection = new StockCorrection()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_CORRECTION_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return stockCorrection;
    }

    @Before
    public void initTest() {
        stockCorrectionSearchRepository.deleteAll();
        stockCorrection = createEntity(em);
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
    public void createStockCorrection() throws Exception {
        int databaseSizeBeforeCreate = stockCorrectionRepository.findAll().size();
        stockCorrection.setDocument(createDefaultCorrectionDocument());

        // Create the StockCorrection
        restStockCorrectionMockMvc.perform(post("/api/stock-corrections")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockCorrection)))
            .andExpect(status().isCreated());

        // Validate the StockCorrection in the database
        List<StockCorrection> stockCorrectionList = stockCorrectionRepository.findAll();
        assertThat(stockCorrectionList).hasSize(databaseSizeBeforeCreate + 1);
        StockCorrection testStockCorrection = stockCorrectionList.get(stockCorrectionList.size() - 1);
        assertThat(testStockCorrection.getDocumentNumber()).isNotEmpty();

        // Validate the StockCorrection in Elasticsearch
        StockCorrection stockCorrectionEs = stockCorrectionSearchRepository.findById(testStockCorrection.getId()).get();
        assertThat(stockCorrectionEs.getId()).isEqualToComparingFieldByField(testStockCorrection.getId());
    }

    @Test
    @Transactional
    public void createStockCorrectionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockCorrectionRepository.findAll().size();

        // Create the StockCorrection with an existing ID
        StockCorrection existingStockCorrection = new StockCorrection();
        existingStockCorrection.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockCorrectionMockMvc.perform(post("/api/stock-corrections")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingStockCorrection)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StockCorrection> stockCorrectionList = stockCorrectionRepository.findAll();
        assertThat(stockCorrectionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllStockCorrections() throws Exception {
        // Initialize the database
        stockCorrection.setDocument(createDefaultCorrectionDocument());
        stockCorrectionService.save(stockCorrection);

        // Get all the stockCorrectionList
        restStockCorrectionMockMvc.perform(get("/api/stock-corrections?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockCorrection.getId().intValue())))
            .andExpect(jsonPath("$.[*].document.lines.[0].batchQuantity").value(hasItem(51.0)))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty());
    }

    @Test
    @Transactional
    public void getStockCorrection() throws Exception {
        // Initialize the database
        stockCorrection.setDocument(createDefaultCorrectionDocument());
        stockCorrectionService.save(stockCorrection);

        // Get the stockCorrection
        restStockCorrectionMockMvc.perform(get("/api/stock-corrections/{id}", stockCorrection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stockCorrection.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingStockCorrection() throws Exception {
        // Get the stockCorrection
        restStockCorrectionMockMvc.perform(get("/api/stock-corrections/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateStockCorrection() throws Exception {
        stockCorrectionRepository.deleteAll();
        // Initialize the database
        stockCorrection.setDocument(createDefaultCorrectionDocument());
        stockCorrectionService.save(stockCorrection);

        int databaseSizeBeforeUpdate = stockCorrectionRepository.findAll().size();

        // Update the stockCorrection
        StockCorrection updatedStockCorrection = stockCorrectionRepository.findOne(stockCorrection.getId());
        updatedStockCorrection
            .documentNumber(UPDATED_DOCUMENT_NUMBER);
        //  .document(UPDATED_CORRECTION_DOCUMENT);
        //   .version(UPDATED_VERSION)
        //   .latest(UPDATED_LATEST);

        restStockCorrectionMockMvc.perform(put("/api/stock-corrections")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStockCorrection)))
            .andExpect(status().isOk());

        // Validate the StockCorrection in the database
        List<StockCorrection> stockCorrectionList = stockCorrectionRepository.findAll();
        assertThat(stockCorrectionList).hasSize(databaseSizeBeforeUpdate + 1);
        StockCorrection testStockCorrection = stockCorrectionList.get(stockCorrectionList.size() - 1);
        assertThat(testStockCorrection.getDocumentNumber()).isEqualTo(UPDATED_DOCUMENT_NUMBER);

        // Validate the StockCorrection in Elasticsearch
        StockCorrection stockCorrectionEs = stockCorrectionSearchRepository.findById(testStockCorrection.getId()).get();
        assertThat(stockCorrectionEs.getId()).isEqualToComparingFieldByField(testStockCorrection.getId());
    }

    @Test
    @Transactional
    public void updateNonExistingStockCorrection() throws Exception {
        int databaseSizeBeforeUpdate = stockCorrectionRepository.findAll().size();
        stockCorrection.setDocument(createDefaultCorrectionDocument());

        // Create the StockCorrection

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStockCorrectionMockMvc.perform(put("/api/stock-corrections")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockCorrection)))
            .andExpect(status().isCreated());

        // Validate the StockCorrection in the database
        List<StockCorrection> stockCorrectionList = stockCorrectionRepository.findAll();
        assertThat(stockCorrectionList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStockCorrection() throws Exception {
        // Initialize the database
        stockCorrection.setDocument(createDefaultCorrectionDocument());
        stockCorrectionService.save(stockCorrection);
        stockCorrectionSearchRepository.save(stockCorrection);
        int databaseSizeBeforeDelete = stockCorrectionRepository.findAll().size();

        // Get the stockCorrection
        restStockCorrectionMockMvc.perform(delete("/api/stock-corrections/{id}", stockCorrection.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean stockCorrectionExistsInEs = stockCorrectionSearchRepository.existsById(stockCorrection.getId());
        assertThat(stockCorrectionExistsInEs).isFalse();

        // Validate the database is empty
        List<StockCorrection> stockCorrectionList = stockCorrectionRepository.findAll();
        assertThat(stockCorrectionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStockCorrection() throws Exception {
        // Initialize the database
        stockCorrection.setDocument(createDefaultCorrectionDocument());
        stockCorrectionService.save(stockCorrection);
        stockCorrectionSearchRepository.save(stockCorrection);
        // Search the stockCorrection
        restStockCorrectionMockMvc.perform(get("/api/_search/stock-corrections?query=id:" + stockCorrection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stockCorrection.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty());
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockCorrection.class);
    }

    @Test
    public void aCreateIndex() {
        //Config for workflow test
        if (elasticsearchTemplate.indexExists("configuration"))
            elasticsearchTemplate.deleteIndex("configuration");
        elasticsearchTemplate.createIndex("configuration");

        Configuration configuration1 = new Configuration();
        configuration1.setApplicableCode("1001");
        configuration1.setApplicableTo(2l);
        configuration1.setApplicableType(ConfigurationLevel.Local);
        configuration1.setKey("athma_stockcorrection_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(2l);
        configuration2.setApplicableType(ConfigurationLevel.Local);
        configuration2.setKey("athma_stockcorrection_workflow_definition");
        configuration2.setValue("stock_correction_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(2l);
        configuration3.setApplicableType(ConfigurationLevel.Local);
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

    @Test
    public void verifyWorkflow() throws Exception {

        aCreateIndex();
        reserveStockRepository.deleteAll();
        stockCorrection.setDocument(createDefaultCorrectionDocument());

        addUserToSecurityContext(4L, "90011Z", "creator");

        MvcResult result = restStockCorrectionMockMvc.perform(post("/api/stock-corrections?action=DRAFT")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockCorrection)))
            .andExpect(status().isCreated()).andReturn();

        stockCorrection = JacksonUtil.fromString(result.getResponse().getContentAsString(), StockCorrection.class);

        restStockCorrectionMockMvc.perform(put("/api/stock-corrections?action=SENDFORAPPROVAL&validationRequired=true")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockCorrection)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=1&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        //Retrieve task content
        result = restWorkflowMockMvc.perform(get("/api/jbpm/process/process-variable?taskId=" + taskList.get(0).getTaskId() + "&variableName=content")).andExpect(status().isOk()).andReturn();
        Map taskContent = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Assert.assertEquals(String.valueOf(stockCorrection.getId()), String.valueOf(taskContent.get("document_id")));
        Assert.assertEquals(stockCorrection.getDocument().getType().name(), String.valueOf(taskContent.get("document_type")));


        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        stockCorrection = stockCorrectionService.findOne(stockCorrection.getId());

        result = restStockCorrectionMockMvc.perform(get("/api/_workflow/stock-corrections?documentNumber=" + stockCorrection.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map<String, Object> taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restStockCorrectionMockMvc.perform(put("/api/_workflow/stock-corrections?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(stockCorrection)))
            .andExpect(status().isOk());

        stockCorrectionRepository.deleteAll();
    }

    //  @Test
    /*public void zDestroy() {
        stockCorrectionRepository.deleteAll();
        stockRepository.deleteAll();
        reserveStockRepository.deleteAll();
        itemUnitAverageCostRepository.deleteAll();
    }*/
}
