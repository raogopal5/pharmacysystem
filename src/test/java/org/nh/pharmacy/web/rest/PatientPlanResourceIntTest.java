package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.billing.config.BillingProperties;
import org.nh.billing.domain.PatientPlan;
import org.nh.billing.repository.PatientPlanRepository;
import org.nh.billing.repository.search.PatientPlanSearchRepository;
import org.nh.billing.service.PatientPlanService;
import org.nh.billing.web.rest.PatientPlanResource;
import org.nh.common.dto.ConsultantDTO;
import org.nh.common.dto.EncounterDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PatientPlanResource REST controller.
 *
 * @see PatientPlanResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class PatientPlanResourceIntTest {

    private static final PatientDTO DEFAULT_PATIENT = new PatientDTO();
    private static final PatientDTO UPDATED_PATIENT = new PatientDTO();

    private static final LocalDate DEFAULT_PLAN_FROM_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PLAN_FROM_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_PLAN_TO_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PLAN_TO_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_POLICY_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_POLICY_NUMBER = "BBBBBBBBBB";

    private static final EncounterDTO DEFAULT_ENCOUNTER = new EncounterDTO();
    private static final EncounterDTO UPDATED_ENCOUNTER = new EncounterDTO();

    private static final Boolean DEFAULT_INCLUDE_TAX_AMOUNT = false;
    private static final Boolean UPDATED_INCLUDE_TAX_AMOUNT = true;

    private static final String DEFAULT_REMARKS = "AAAAAAAAAA";
    private static final String UPDATED_REMARKS = "BBBBBBBBBB";

    private static final String DEFAULT_AUTHORIZATION_CODE = "AAAAAAAAAA";
    private static final String UPDATED_AUTHORIZATION_CODE = "BBBBBBBBBB";

    @Autowired
    private PatientPlanRepository patientPlanRepository;

    @Autowired
    private PatientPlanService patientPlanService;

    @Autowired
    private PatientPlanSearchRepository patientPlanSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;
    @Autowired
    private BillingProperties billingProperties;

    private MockMvc restPatientPlanMockMvc;

    private PatientPlan patientPlan;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PatientPlanResource patientPlanResource = new PatientPlanResource(patientPlanService,patientPlanSearchRepository,patientPlanRepository,billingProperties);
        this.restPatientPlanMockMvc = MockMvcBuilders.standaloneSetup(patientPlanResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PatientPlan createEntity(EntityManager em) {
        ConsultantDTO consultantDTO = new ConsultantDTO();
        consultantDTO.setName("AAA");
        DEFAULT_ENCOUNTER.setConsultant(consultantDTO);
        DEFAULT_PATIENT.setId(1L);
        DEFAULT_PATIENT.setFullName("AAA");
        DEFAULT_PATIENT.setDisplayName("AAA");
        PatientPlan patientPlan = new PatientPlan()
            .patient(DEFAULT_PATIENT)
            .planFromDate(DEFAULT_PLAN_FROM_DATE)
            .planToDate(DEFAULT_PLAN_TO_DATE)
            .policyNumber(DEFAULT_POLICY_NUMBER)
            .encounter(DEFAULT_ENCOUNTER)
            .includeTaxAmount(DEFAULT_INCLUDE_TAX_AMOUNT)
            .remarks(DEFAULT_REMARKS)
            .authorizationCode(DEFAULT_AUTHORIZATION_CODE);
        patientPlan.setId(1L);
        return patientPlan;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(PatientPlan.class))
            elasticsearchTemplate.createIndex(PatientPlan.class);
        patientPlanSearchRepository.deleteAll();
        patientPlan = createEntity(em);
    }

    @Test
    @Transactional
    public void createPatientPlan() throws Exception {
        int databaseSizeBeforeCreate = patientPlanRepository.findAll().size();

        // Create the PatientPlan

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isCreated());

        // Validate the PatientPlan in the database
        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeCreate + 1);
        PatientPlan testPatientPlan = patientPlanList.get(patientPlanList.size() - 1);
        assertThat(testPatientPlan.getPatient().getFullName().toString()).isEqualTo(DEFAULT_PATIENT.getFullName().toString());
        assertThat(testPatientPlan.getPlanFromDate()).isEqualTo(DEFAULT_PLAN_FROM_DATE);
        assertThat(testPatientPlan.getPlanToDate()).isEqualTo(DEFAULT_PLAN_TO_DATE);
        assertThat(testPatientPlan.getPolicyNumber()).isEqualTo(DEFAULT_POLICY_NUMBER);
        assertThat(testPatientPlan.getEncounter()).isEqualTo(DEFAULT_ENCOUNTER);
        assertThat(testPatientPlan.isIncludeTaxAmount()).isEqualTo(DEFAULT_INCLUDE_TAX_AMOUNT);
        assertThat(testPatientPlan.getRemarks()).isEqualTo(DEFAULT_REMARKS);
        assertThat(testPatientPlan.getAuthorizationCode()).isEqualTo(DEFAULT_AUTHORIZATION_CODE);

        // Validate the PatientPlan in ElasticSearch
        PatientPlan patientPlanEs = patientPlanSearchRepository.findById(testPatientPlan.getId()).get();
        assertThat(patientPlanEs.getPatient().getFullName().toString()).isEqualTo(testPatientPlan.getPatient().getFullName().toString());
    }

    @Test
    @Transactional
    public void createPatientPlanWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = patientPlanRepository.findAll().size();

        // Create the PatientPlan with an existing ID
        PatientPlan existingPatientPlan = new PatientPlan();
        existingPatientPlan.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingPatientPlan)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkPatientIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setPatient(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPlanFromDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setPlanFromDate(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPlanToDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setPlanToDate(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPolicyNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setPolicyNumber(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEncounterIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setEncounter(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIncludeTaxAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setIncludeTaxAmount(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRemarksIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setRemarks(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAuthorizationCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = patientPlanRepository.findAll().size();
        // set the field null
        patientPlan.setAuthorizationCode(null);

        // Create the PatientPlan, which fails.

        restPatientPlanMockMvc.perform(post("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(patientPlan)))
            .andExpect(status().isBadRequest());

        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPatientPlans() throws Exception {
        // Initialize the database
        patientPlanRepository.saveAndFlush(patientPlan);

        // Get all the patientPlanList
        restPatientPlanMockMvc.perform(get("/api/patient-plans?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(patientPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].patient.displayName").value(hasItem(DEFAULT_PATIENT.getDisplayName().toString())))
            .andExpect(jsonPath("$.[*].planFromDate").value(hasItem(DEFAULT_PLAN_FROM_DATE.toString())))
            .andExpect(jsonPath("$.[*].planToDate").value(hasItem(DEFAULT_PLAN_TO_DATE.toString())))
            .andExpect(jsonPath("$.[*].policyNumber").value(hasItem(DEFAULT_POLICY_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].encounter.consultant.name").value(hasItem(DEFAULT_ENCOUNTER.getConsultant().getName())))
            .andExpect(jsonPath("$.[*].includeTaxAmount").value(hasItem(DEFAULT_INCLUDE_TAX_AMOUNT.booleanValue())))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS.toString())))
            .andExpect(jsonPath("$.[*].authorizationCode").value(hasItem(DEFAULT_AUTHORIZATION_CODE.toString())));
    }

    @Test
    @Transactional
    public void getPatientPlan() throws Exception {
        // Initialize the database
        patientPlanRepository.saveAndFlush(patientPlan);

        // Get the patientPlan
        restPatientPlanMockMvc.perform(get("/api/patient-plans/{id}", patientPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(patientPlan.getId().intValue()))
            .andExpect(jsonPath("$.patient.displayName").value(DEFAULT_PATIENT.getDisplayName().toString()))
            .andExpect(jsonPath("$.planFromDate").value(DEFAULT_PLAN_FROM_DATE.toString()))
            .andExpect(jsonPath("$.planToDate").value(DEFAULT_PLAN_TO_DATE.toString()))
            .andExpect(jsonPath("$.policyNumber").value(DEFAULT_POLICY_NUMBER.toString()))
            .andExpect(jsonPath("$.encounter.consultant.name").value(DEFAULT_ENCOUNTER.getConsultant().getName()))
            .andExpect(jsonPath("$.includeTaxAmount").value(DEFAULT_INCLUDE_TAX_AMOUNT.booleanValue()))
            .andExpect(jsonPath("$.remarks").value(DEFAULT_REMARKS.toString()))
            .andExpect(jsonPath("$.authorizationCode").value(DEFAULT_AUTHORIZATION_CODE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPatientPlan() throws Exception {
        // Get the patientPlan
        restPatientPlanMockMvc.perform(get("/api/patient-plans/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePatientPlan() throws Exception {
        // Initialize the database
        patientPlanService.save(patientPlan);

        int databaseSizeBeforeUpdate = patientPlanRepository.findAll().size();
        UPDATED_PATIENT.setFullName("BBB");
        ConsultantDTO consultantDTO = new ConsultantDTO();
        consultantDTO.setName("BBB");
        UPDATED_ENCOUNTER.setConsultant(consultantDTO);

        // Update the patientPlan
        PatientPlan updatedPatientPlan = patientPlanRepository.findById(patientPlan.getId()).get();
        updatedPatientPlan
            .patient(UPDATED_PATIENT)
            .planFromDate(UPDATED_PLAN_FROM_DATE)
            .planToDate(UPDATED_PLAN_TO_DATE)
            .policyNumber(UPDATED_POLICY_NUMBER)
            .encounter(UPDATED_ENCOUNTER)
            .includeTaxAmount(UPDATED_INCLUDE_TAX_AMOUNT)
            .remarks(UPDATED_REMARKS)
            .authorizationCode(UPDATED_AUTHORIZATION_CODE);

        restPatientPlanMockMvc.perform(put("/api/patient-plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPatientPlan)))
            .andExpect(status().isOk());

        // Validate the PatientPlan in the database
        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeUpdate);
        PatientPlan testPatientPlan = patientPlanList.get(patientPlanList.size() - 1);
        assertThat(testPatientPlan.getPatient().getFullName().toString()).isEqualTo(UPDATED_PATIENT.getFullName().toString());
        assertThat(testPatientPlan.getPlanFromDate()).isEqualTo(UPDATED_PLAN_FROM_DATE);
        assertThat(testPatientPlan.getPlanToDate()).isEqualTo(UPDATED_PLAN_TO_DATE);
        assertThat(testPatientPlan.getPolicyNumber()).isEqualTo(UPDATED_POLICY_NUMBER);
        assertThat(testPatientPlan.getEncounter().getConsultant().toString()).isEqualTo(UPDATED_ENCOUNTER.getConsultant().toString());
        assertThat(testPatientPlan.isIncludeTaxAmount()).isEqualTo(UPDATED_INCLUDE_TAX_AMOUNT);
        assertThat(testPatientPlan.getRemarks()).isEqualTo(UPDATED_REMARKS);
        assertThat(testPatientPlan.getAuthorizationCode()).isEqualTo(UPDATED_AUTHORIZATION_CODE);

        // Validate the PatientPlan in ElasticSearch
        PatientPlan patientPlanEs = patientPlanSearchRepository.findById(testPatientPlan.getId()).get();
        assertThat(patientPlanEs.getPatient().getFullName().toString()).isEqualTo(testPatientPlan.getPatient().getFullName().toString());
    }

    @Test
    @Transactional
    public void deletePatientPlan() throws Exception {
        // Initialize the database
        patientPlanService.save(patientPlan);

        int databaseSizeBeforeDelete = patientPlanRepository.findAll().size();

        // Get the patientPlan
        restPatientPlanMockMvc.perform(delete("/api/patient-plans/{id}", patientPlan.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean patientPlanExistsInEs = patientPlanSearchRepository.existsById(patientPlan.getId());
        assertThat(patientPlanExistsInEs).isFalse();

        // Validate the database is empty
        List<PatientPlan> patientPlanList = patientPlanRepository.findAll();
        assertThat(patientPlanList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPatientPlan() throws Exception {
        // Initialize the database
        patientPlanService.save(patientPlan);

        // Search the patientPlan
        restPatientPlanMockMvc.perform(get("/api/_search/patient-plans?query=id:" + patientPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(patientPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].patient.displayName").value(hasItem(DEFAULT_PATIENT.getDisplayName().toString())))
            .andExpect(jsonPath("$.[*].planFromDate").value(hasItem(DEFAULT_PLAN_FROM_DATE.toString())))
            .andExpect(jsonPath("$.[*].planToDate").value(hasItem(DEFAULT_PLAN_TO_DATE.toString())))
            .andExpect(jsonPath("$.[*].policyNumber").value(hasItem(DEFAULT_POLICY_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].encounter.consultant.name").value(hasItem(DEFAULT_ENCOUNTER.getConsultant().getName())))
            .andExpect(jsonPath("$.[*].includeTaxAmount").value(hasItem(DEFAULT_INCLUDE_TAX_AMOUNT.booleanValue())))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS.toString())))
            .andExpect(jsonPath("$.[*].authorizationCode").value(hasItem(DEFAULT_AUTHORIZATION_CODE.toString())));
    }
}
