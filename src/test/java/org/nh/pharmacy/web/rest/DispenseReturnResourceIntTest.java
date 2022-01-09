package org.nh.pharmacy.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.billing.domain.Invoice;
import org.nh.billing.domain.Receipt;
import org.nh.billing.domain.Refund;
import org.nh.billing.domain.dto.TaxCalculation;
import org.nh.billing.domain.dto.TaxDefinition;
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
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.ReturnStatus;
import org.nh.pharmacy.dto.ValueSet;
import org.nh.pharmacy.dto.ValueSetCode;
import org.nh.pharmacy.repository.DispenseReturnRepository;
import org.nh.pharmacy.repository.search.DispenseReturnSearchRepository;
import org.nh.pharmacy.service.DispenseReturnService;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.IpDispenseReturnPrintService;
import org.nh.pharmacy.service.LocatorService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.pharmacy.web.rest.mapper.ReturnToInvoiceMapper;
import org.nh.pharmacy.web.rest.mapper.ReturnToRefundMapper;
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
 * Test class for the DispenseReturnResource REST controller.
 *
 * @see DispenseReturnResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class DispenseReturnResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "DD-2001-000000001";
    private static final String UPDATED_DOCUMENT_NUMBER = "DD-2001-000000002";

    private static final DispenseReturnDocument DEFAULT_DOCUMENT = new DispenseReturnDocument();
    private static final DispenseReturnDocument UPDATED_DOCUMENT = new DispenseReturnDocument();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    @Autowired
    private DispenseReturnRepository dispenseReturnRepository;

    @Autowired
    private DispenseReturnService dispenseReturnService;

    @Autowired
    private IpDispenseReturnPrintService ipDispenseReturnPrintService;

    @Autowired
    private DispenseReturnSearchRepository dispenseReturnSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    ReturnToInvoiceMapper returnToInvoiceMapper;

    @Autowired
    ReturnToRefundMapper returnToRefundMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    SpringSecurityIdentityProvider identityProvider;

    @Autowired
    GroupService groupService;

    @Autowired
    private LocatorService locatorService;

    @Autowired
    private ApplicationProperties applicationProperties;


    private MockMvc restDispenseReturnMockMvc;

    private MockMvc restWorkflowMockMvc;

    private DispenseReturn dispenseReturn;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DispenseReturnResource dispenseReturnResource = new DispenseReturnResource(dispenseReturnService,dispenseReturnRepository,dispenseReturnSearchRepository,applicationProperties,ipDispenseReturnPrintService);
        this.restDispenseReturnMockMvc = MockMvcBuilders.standaloneSetup(dispenseReturnResource)
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
    public static DispenseReturn createEntity(EntityManager em) {
        DispenseReturn dispenseReturn = new DispenseReturn()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return dispenseReturn;
    }

    @Before
    public void initTest() {
        dispenseReturnSearchRepository.deleteAll();
        dispenseReturn = createEntity(em);
        groupService.doIndex();
        addUserToSecurityContext(1l, "admin", "admin");
        dispenseReturn.document(this.populateReturnDocumentData());
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
        configuration1.setKey("athma_pharmacy_dispense_return_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(3l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_pharmacy_dispense_return_workflow_definition");
        configuration2.setValue("dispense_return_document_process");
        configuration2.setLevel(2);

        Configuration configuration3 = new Configuration();
        configuration3.setApplicableCode("1001");
        configuration3.setApplicableTo(3l);
        configuration3.setApplicableType(ConfigurationLevel.Unit);
        configuration3.setKey("athma_date_format");
        configuration3.setValue("dd/MM/yy");
        configuration3.setLevel(2);

        ValueSetCode valueSetCode = new ValueSetCode();
        valueSetCode.setCode("ENC");
        valueSetCode.setActive(Boolean.TRUE);
        valueSetCode.setValueSet(new ValueSet());
        valueSetCode.getValueSet().setCode("EncounterClass");

        HSCServiceDTO hscServiceDTO = new HSCServiceDTO();
        hscServiceDTO.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO.getServiceMaster().setId(1l);
        hscServiceDTO.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO.getHealthcareServiceCenter().setId(1l);

        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration1).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration2).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration3).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("1").withObject(valueSetCode).build();
        IndexQuery indexQuery5 = new IndexQueryBuilder().withId("1").withObject(hscServiceDTO).build();

        elasticsearchTemplate.index(indexQuery1, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery2, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery3, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery4, IndexCoordinates.of("valuesetcode"));
        elasticsearchTemplate.index(indexQuery5, IndexCoordinates.of("hscservice"));

        elasticsearchTemplate.refresh(Configuration.class);
        elasticsearchTemplate.refresh(ValueSetCode.class);
        elasticsearchTemplate.refresh(HSCServiceDTO.class);
    }

    @Test
    @Transactional
    public void createDispenseReturn() throws Exception {
        int databaseSizeBeforeCreate = dispenseReturnRepository.findAll().size();

        // Create the DispenseReturn

        restDispenseReturnMockMvc.perform(post("/api/dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isCreated());

        // Validate the DispenseReturn in the database
        // Validate the Dispense in the database
        List<DispenseReturn> dispenseReturns = dispenseReturnRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseReturns).hasSize(databaseSizeBeforeCreate + 1);
        DispenseReturn testDispenseReturn = dispenseReturns.get(dispenseReturns.size() - 1);

        assertThat(testDispenseReturn.getDocumentNumber()).isNotEmpty();
        assertThat(testDispenseReturn.getVersion()).isEqualTo(dispenseReturn.getVersion());
        assertThat(testDispenseReturn.isLatest()).isEqualTo(dispenseReturn.isLatest());

        // Validate the Dispense in Elasticsearch
        DispenseReturn dispenseReturnEs = dispenseReturnSearchRepository.findById(testDispenseReturn.getId()).get();
        assertThat(dispenseReturnEs.getDocumentNumber()).isNotEmpty();
        assertThat(dispenseReturnEs.getVersion()).isEqualTo(dispenseReturnEs.getVersion());
        assertThat(dispenseReturnEs.isLatest()).isEqualTo(dispenseReturnEs.isLatest());
    }

    @Test
    @Transactional
    public void createDispenseReturnWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = dispenseReturnRepository.findAll().size();

        // Create the DispenseReturn with an existing ID
        DispenseReturn existingDispenseReturn = new DispenseReturn();
        existingDispenseReturn.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDispenseReturnMockMvc.perform(post("/api/dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingDispenseReturn)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findAll();
        assertThat(dispenseReturnList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = dispenseReturnRepository.findAll().size();
        // set the field null
        dispenseReturn.setDocument(null);

        // Create the DispenseReturn, which fails.

        restDispenseReturnMockMvc.perform(post("/api/dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isBadRequest());

        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findAll();
        assertThat(dispenseReturnList).hasSize(databaseSizeBeforeTest);
    }


    @Test
    @Transactional
    public void checkLatestIsRequired() throws Exception {
        int databaseSizeBeforeTest = dispenseReturnRepository.findAll().size();
        // set the field null
        dispenseReturn.setLatest(null);

        // Create the DispenseReturn, which fails.

        restDispenseReturnMockMvc.perform(post("/api/dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isBadRequest());

        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findAll();
        assertThat(dispenseReturnList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDispenseReturns() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);

        // Get all the dispenseReturnList
        restDispenseReturnMockMvc.perform(get("/api/dispense-returns?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dispenseReturn.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getDispenseReturn() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);

        // Get the dispenseReturn
        restDispenseReturnMockMvc.perform(get("/api/dispense-returns/{id}", dispenseReturn.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dispenseReturn.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingDispenseReturn() throws Exception {
        // Get the dispenseReturn
        restDispenseReturnMockMvc.perform(get("/api/dispense-returns/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDispenseReturn() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);
        int databaseSizeBeforeUpdate = dispenseReturnRepository.findAll().size();

        // Update the dispenseReturn
        DispenseReturn updatedDispenseReturn = dispenseReturnRepository.findOne(dispenseReturn.getId());
        updatedDispenseReturn
            .documentNumber(UPDATED_DOCUMENT_NUMBER);

        restDispenseReturnMockMvc.perform(put("/api/dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedDispenseReturn)))
            .andExpect(status().isOk());

        // Validate the DispenseReturn in the database
        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseReturnList).hasSize(databaseSizeBeforeUpdate + 1);
        DispenseReturn testDispenseReturn = dispenseReturnList.get(dispenseReturnList.size() - 1);
        assertThat(testDispenseReturn.getDocumentNumber()).isEqualTo(UPDATED_DOCUMENT_NUMBER);

        // Validate the DispenseReturn in Elasticsearch
        DispenseReturn dispenseReturnEs = dispenseReturnSearchRepository.findById(testDispenseReturn.getId()).get();
        assertThat(dispenseReturnEs.getId()).isEqualTo(testDispenseReturn.getId());
        assertThat(dispenseReturnEs.getDocumentNumber()).isEqualTo(testDispenseReturn.getDocumentNumber());
    }

    @Test
    @Transactional
    public void updateNonExistingDispenseReturn() throws Exception {
        int databaseSizeBeforeUpdate = dispenseReturnRepository.findAll().size();

        // Create the DispenseReturn

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDispenseReturnMockMvc.perform(put("/api/dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isCreated());

        // Validate the DispenseReturn in the database
        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findAll();
        assertThat(dispenseReturnList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteDispenseReturn() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);

        int databaseSizeBeforeDelete = dispenseReturnRepository.findAll().size();

        // Get the dispenseReturn
        restDispenseReturnMockMvc.perform(delete("/api/dispense-returns/{id}", dispenseReturn.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean dispenseReturnExistsInEs = dispenseReturnSearchRepository.existsById(dispenseReturn.getId());
        assertThat(dispenseReturnExistsInEs).isFalse();

        // Validate the database is empty
        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findAll();
        assertThat(dispenseReturnList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchDispenseReturn() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);

        // Search the dispenseReturn
        restDispenseReturnMockMvc.perform(get("/api/_search/dispense-returns?query=id:" + dispenseReturn.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dispenseReturn.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DispenseReturn.class);
    }

    @Test
    public void processDispenseReturn() throws Exception {
        createIndex();
        dispenseReturn = dispenseReturnService.save(dispenseReturn);
        Map<String, Object> result = dispenseReturnService.processDispenseReturn(dispenseReturn, "CONFIRMED");

        Invoice invoice = (Invoice) result.get("invoice");
        Refund refund = (Refund) result.get("refund");
        Receipt receipt = (Receipt) result.get("receipt");
        assertThat(invoice.getInvoiceNumber()).isNotEmpty();
//        assertThat(refund.getRefundNumber()).isNotEmpty();
//        assertThat(receipt.getReceiptNumber()).isNotEmpty();
    }

    private DispenseReturnDocument populateReturnDocumentData() {
        DispenseReturnDocument returnDocument = new DispenseReturnDocument();

        PatientDTO patient = new PatientDTO();
        patient.setId(1001l);
        patient.setAge(45);
        patient.setGender("male");
        patient.setMrn("MRN-2001-00001");
        patient.setFullName("Patient User");
        patient.setDisplayName("Patient User");
        returnDocument.setPatient(patient);

        //  Map<String, Object> hsc = new HashMap();
        // hsc.put("id",1l);hsc.put("code","hsc-1");hsc.put("name","Healthcare service center HSR Layout");
        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(1L);
        hsc.setName("Healthcare service center HSR Layout");
        hsc.setCode("hsc-1");
        returnDocument.setHsc(hsc);
        returnDocument.setReturnhsc(hsc);

        org.nh.pharmacy.domain.Locator locator = locatorService.findOne(1l);
        LocatorDTO locatorDto = new LocatorDTO();
        BeanUtils.copyProperties(locator, locatorDto);

        UserDTO user = new UserDTO();
        user.setId(1001l);
        user.setDisplayName("User 1");
        user.setLogin("Admin");
        returnDocument.setReceivedBy(user);

        SourceDTO invoiceRef = new SourceDTO();
        invoiceRef.setId(1l);
        invoiceRef.setReferenceNumber("INV-2001-D00001");
        returnDocument.setInvoiceRef(invoiceRef);
        //dispense unit
        OrganizationDTO unit = new OrganizationDTO();
        unit.setId(3l);
        unit.setCode("unit-1001");
        returnDocument.setDispenseUnit(unit);

        //dispense return unit
        OrganizationDTO returnUnit = new OrganizationDTO();
        returnUnit.setId(3l);
        returnUnit.setCode("unit-1001");
        returnDocument.setReturnUnit(returnUnit);

        List<DispensePlan> dispensePlans = new ArrayList<>();
        DispensePlan dispensePlan = new DispensePlan();
        //dispensePlan.setSponserId(1l); dispensePlan.setAmount(100.0f);dispensePlan.setStatus("Active");dispensePlan.setPlanId(1l);
        dispensePlans.add(dispensePlan);
        returnDocument.setPlans(dispensePlans);

        DispenseTax dispenseTax = new DispenseTax();
        dispenseTax.setTaxCode("GST");
        dispenseTax.setTaxAmount(getBigDecimal(140f));
        dispenseTax.setTaxDefinition(new TaxDefinition());
        dispenseTax.getTaxDefinition().setTaxCalculation(new TaxCalculation());
        dispenseTax.getTaxDefinition().getTaxCalculation().setPercentage(10f);

        List<DispenseTax> dispenseTaxes = new ArrayList<>();
        dispenseTaxes.add(dispenseTax);
        returnDocument.setTaxes(dispenseTaxes);

        UserDTO approvedBy = new UserDTO();
        approvedBy.setId(1001l);
        approvedBy.setDisplayName("User 1");
        approvedBy.setLogin("Admin");
        approvedBy.setEmployeeNo("335036");
        returnDocument.setApprovedBy(approvedBy);

        DispenseReturnDocumentLine dispenseReturnDocumentLine = new DispenseReturnDocumentLine();

        List<DispenseItemPlan> dispenseItemPlans = new ArrayList<>();
        DispenseItemPlan dispenseItemPlan = new DispenseItemPlan();
        //dispenseItemPlan.setId(2l);dispenseItemPlan.setAmount(150f);dispenseItemPlan.setQuantity(10f);dispenseItemPlan.setRate(15f);

        dispenseReturnDocumentLine.setCode("orderitem-1");
        dispenseReturnDocumentLine.setName("Crocin");
        dispenseReturnDocumentLine.setQuantity(2f);
        dispenseReturnDocumentLine.setBatchNumber("Batch-no-1");
        dispenseReturnDocumentLine.setCost(getBigDecimal(200f));
        dispenseReturnDocumentLine.setItemPlans(dispenseItemPlans);
        dispenseReturnDocumentLine.setItemTaxes(dispenseTaxes);
        dispenseReturnDocumentLine.setNetAmount(getBigDecimal(90.0f));
        dispenseReturnDocumentLine.setDiscountAmount(getBigDecimal(10.0f));
        dispenseReturnDocumentLine.setPatientAmount(getBigDecimal(80.0f));
        dispenseReturnDocumentLine.setSponsorAmount(getBigDecimal(10.0f));
        dispenseReturnDocumentLine.setGrossAmount(getBigDecimal(100.0f));
        dispenseReturnDocumentLine.setStockId(11l);
        dispenseReturnDocumentLine.setItemId(1l);
        dispenseReturnDocumentLine.setLineNumber(1l);
        dispenseReturnDocumentLine.setReturnAmount(getBigDecimal(10f));
        dispenseReturnDocumentLine.setUom(createUom());
        dispenseReturnDocumentLine.setLocator(locatorDto);
        dispenseReturnDocumentLine.setOwner("Owner");
        dispenseReturnDocumentLine.setSupplier("Supplier");

        //Dispense dispenseReturnDocumentLine line
        List<DispenseReturnDocumentLine> dispenseReturnDocumentLines = new ArrayList<>();

        dispenseReturnDocumentLines.add(dispenseReturnDocumentLine);
        returnDocument.setReturnDate(LocalDateTime.now());
        returnDocument.setDispenseReturnDocumentLines(dispenseReturnDocumentLines);
        returnDocument.setReturnStatus(ReturnStatus.DRAFT);
        returnDocument.setCreatedDate(LocalDateTime.now());

        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setId(1l);
        organizationDTO.setCode("valuesetcode.json");
        organizationDTO.setName("valuesetcode.json");

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setMrn("ABCD123");
        patientDTO.setGender("Male");
        patientDTO.setAge(20);

        HealthcareServiceCenterDTO healthcareServiceCenterDTO = new HealthcareServiceCenterDTO();
        healthcareServiceCenterDTO.setId(1l);
        healthcareServiceCenterDTO.setCode("ANC");
        healthcareServiceCenterDTO.setName("LAB");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1l);
        userDTO.setDisplayName("ABC");

        ConsultantDTO consultant = new ConsultantDTO();
        consultant.setId(2l);
        consultant.setDisplayName(DEFAULT_DOCUMENT_NUMBER);

        ValueSetCodeDTO encounterClass = new ValueSetCodeDTO();
        encounterClass.setCode("ENC");

        ValueSetCodeDTO tariffClass = new ValueSetCodeDTO();
        tariffClass.setCode("GENERAL");

        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setPatient(patientDTO);
        encounterDTO.setUnit(organizationDTO);
        encounterDTO.setConsultant(consultant);
        encounterDTO.setEncounterClass(encounterClass);
        encounterDTO.setTariffClass(tariffClass);
        returnDocument.setEncounter(encounterDTO);
        returnDocument.setDispenseTaxes(new ArrayList<>());
        return returnDocument;
    }

    @Test
    @Transactional
    public void getDispenseReturnStatusCount() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);
        restDispenseReturnMockMvc.perform(get("/api/status-count/dispense-returns?query=id:" + dispenseReturn.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }

    @Test
    @Transactional
    public void exportDispenseReturn() throws Exception {
        // Initialize the database
        dispenseReturnService.save(dispenseReturn);

        //Response is OutputStream. Not able to validate response,So validating status and content type.
        restDispenseReturnMockMvc.perform(get("/api/_export/dispense-returns?query=id:" + dispenseReturn.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void verifyWorkflow() throws Exception {

        createIndex();

        addUserToSecurityContext(4L, "90011Z", "creator");

        Map dispenseReturnMap = dispenseReturnService.processDispenseReturn(dispenseReturn, "DRAFT");
        dispenseReturn = (DispenseReturn) dispenseReturnMap.get("dispenseReturn");

        restDispenseReturnMockMvc.perform(put("/api/dispense-returns?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        MvcResult result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=3&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = new ObjectMapper().readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        dispenseReturn = dispenseReturnService.findOne(dispenseReturn.getId());

        result = restDispenseReturnMockMvc.perform(get("/api/_workflow/dispense-returns?documentNumber=" + dispenseReturn.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map taskDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String,Object>>() {
        });

        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restDispenseReturnMockMvc.perform(put("/api/_workflow/dispense-returns?transition=Rejected&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isOk());

    }

    private UOMDTO createUom() {
        UOMDTO uom = new UOMDTO();
        uom.setId(1L);
        uom.setCode("cccc");
        uom.setName("nnnnnn");
        return uom;
    }

    @Test
    public void createIPDispenseReturn() throws Exception {
        int databaseSizeBeforeCreate = dispenseReturnRepository.findAll().size();

        // Create the DispenseReturn

        dispenseReturn.getDocument().setIpDispense(true);
        restDispenseReturnMockMvc.perform(post("/api/ip-dispense-returns")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispenseReturn)))
            .andExpect(status().isCreated());

        // Validate the DispenseReturn in the database
        // Validate the Dispense in the database
        List<DispenseReturn> dispenseReturns = dispenseReturnRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseReturns).hasSize(databaseSizeBeforeCreate + 1);
        DispenseReturn testDispenseReturn = dispenseReturns.get(dispenseReturns.size() - 1);

        assertThat(testDispenseReturn.getDocumentNumber()).isNotEmpty();
        assertThat(testDispenseReturn.getVersion()).isEqualTo(dispenseReturn.getVersion());
        assertThat(testDispenseReturn.isLatest()).isEqualTo(dispenseReturn.isLatest());

        // Validate the Dispense in Elasticsearch
        DispenseReturn dispenseReturnEs = dispenseReturnSearchRepository.findById(testDispenseReturn.getId()).get();
        assertThat(dispenseReturnEs.getDocumentNumber()).isNotEmpty();
        assertThat(dispenseReturnEs.getVersion()).isEqualTo(dispenseReturnEs.getVersion());
        assertThat(dispenseReturnEs.isLatest()).isEqualTo(dispenseReturnEs.isLatest());
    }
}
