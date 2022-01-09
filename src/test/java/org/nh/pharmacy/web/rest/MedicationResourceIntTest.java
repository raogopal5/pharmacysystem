package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.enumeration.DrugSchedule;
import org.nh.pharmacy.repository.MedicationRepository;
import org.nh.pharmacy.repository.search.MedicationSearchRepository;
import org.nh.pharmacy.service.MedicationService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the MedicationResource REST controller.
 *
 * @see MedicationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class MedicationResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_BRAND = false;
    private static final Boolean UPDATED_BRAND = true;

    private static final String DEFAULT_DRUG_STRENGTH = "AAAAAAAAAA";
    private static final String UPDATED_DRUG_STRENGTH = "BBBBBBBBBB";

    private static final String DEFAULT_MANUFACTURER = "AAAAAAAAAA";
    private static final String UPDATED_MANUFACTURER = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Boolean DEFAULT_AUTHORIZATION_REQUIRED = false;
    private static final Boolean UPDATED_AUTHORIZATION_REQUIRED = true;

    private static final Boolean DEFAULT_NARCOTIC = false;
    private static final Boolean UPDATED_NARCOTIC = true;

    private static final ValueSetCode DEFAULT_DRUG_FORM = new ValueSetCode(1l, "DrugFormCode", "Drug Form Code", true);
    private static final ValueSetCode UPDATED_DRUG_FORM = new ValueSetCode(1l, "DrugFormCode", "Drug Form Code", true);

    private static final DrugSchedule DEFAULT_DRUG_SCHEDULE = DrugSchedule.H;
    private static final DrugSchedule UPDATED_DRUG_SCHEDULE = DrugSchedule.X;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private MedicationSearchRepository medicationSearchRepository;

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

    private MockMvc restMedicationMockMvc;

    private Medication medication;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MedicationResource medicationResource = new MedicationResource(medicationService);
        this.restMedicationMockMvc = MockMvcBuilders.standaloneSetup(medicationResource)
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
    public static Medication createEntity(EntityManager em) {
        Medication medication = new Medication()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .brand(DEFAULT_BRAND)
            .drugStrength(DEFAULT_DRUG_STRENGTH)
            .manufacturer(DEFAULT_MANUFACTURER)
            .active(DEFAULT_ACTIVE)
            .authorizationRequired(DEFAULT_AUTHORIZATION_REQUIRED)
            .narcotic(DEFAULT_NARCOTIC)
            .drugForm(DEFAULT_DRUG_FORM)
            .drugSchedule(DEFAULT_DRUG_SCHEDULE);
        return medication;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(Medication.class))
            elasticsearchTemplate.createIndex(Medication.class);
        medicationSearchRepository.deleteAll();
        medication = createEntity(em);
    }

    @Test
    @Transactional
    public void createMedication() throws Exception {
        int databaseSizeBeforeCreate = medicationRepository.findAll().size();

        // Create the Medication

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isCreated());

        // Validate the Medication in the database
        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeCreate + 1);
        Medication testMedication = medicationList.get(medicationList.size() - 1);
        assertThat(testMedication.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testMedication.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMedication.isBrand()).isEqualTo(DEFAULT_BRAND);
        assertThat(testMedication.getDrugStrength()).isEqualTo(DEFAULT_DRUG_STRENGTH);
        assertThat(testMedication.getManufacturer()).isEqualTo(DEFAULT_MANUFACTURER);
        assertThat(testMedication.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testMedication.isAuthorizationRequired()).isEqualTo(DEFAULT_AUTHORIZATION_REQUIRED);
        assertThat(testMedication.isNarcotic()).isEqualTo(DEFAULT_NARCOTIC);
        assertThat(testMedication.getDrugForm().getCode()).isEqualTo(DEFAULT_DRUG_FORM.getCode());
        assertThat(testMedication.getDrugSchedule()).isEqualTo(DEFAULT_DRUG_SCHEDULE);
        // Validate the Medication in Elasticsearch
        Medication medicationEs = medicationSearchRepository.findById(testMedication.getId()).get();
        assertThat(medicationEs).isEqualToComparingFieldByField(testMedication);
    }

    @Test
    @Transactional
    public void createMedicationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = medicationRepository.findAll().size();

        // Create the Medication with an existing ID
        Medication existingMedication = new Medication();
        existingMedication.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingMedication)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setCode(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setName(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBrandIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setBrand(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setActive(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAuthorizationRequiredIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setAuthorizationRequired(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNarcoticIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setNarcotic(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }

    //Commented, since the field drugSchedule is nullable
    /*@Test
    @Transactional
    public void checkDrugScheduleIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRepository.findAll().size();
        // set the field null
        medication.setDrugSchedule(null);

        // Create the Medication, which fails.

        restMedicationMockMvc.perform(post("/api/medications")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medication)))
            .andExpect(status().isBadRequest());

        List<Medication> medicationList = medicationRepository.findAll();
        assertThat(medicationList).hasSize(databaseSizeBeforeTest);
    }*/

    @Test
    @Transactional
    public void getAllMedications() throws Exception {
        // Initialize the database
        medicationRepository.saveAndFlush(medication);

        // Get all the medicationList
        restMedicationMockMvc.perform(get("/api/medications?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medication.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].brand").value(hasItem(DEFAULT_BRAND.booleanValue())))
            .andExpect(jsonPath("$.[*].drugStrength").value(hasItem(DEFAULT_DRUG_STRENGTH.toString())))
            .andExpect(jsonPath("$.[*].manufacturer").value(hasItem(DEFAULT_MANUFACTURER.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].authorizationRequired").value(hasItem(DEFAULT_AUTHORIZATION_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].narcotic").value(hasItem(DEFAULT_NARCOTIC.booleanValue())))
            .andExpect(jsonPath("$.[*].drugForm.code").value(hasItem(DEFAULT_DRUG_FORM.getCode())))
            .andExpect(jsonPath("$.[*].drugSchedule").value(hasItem(DEFAULT_DRUG_SCHEDULE.toString())));
    }

    @Test
    @Transactional
    public void getMedication() throws Exception {
        // Initialize the database
        medicationRepository.saveAndFlush(medication);

        // Get the medication
        restMedicationMockMvc.perform(get("/api/medications/{id}", medication.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medication.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.brand").value(DEFAULT_BRAND.booleanValue()))
            .andExpect(jsonPath("$.drugStrength").value(DEFAULT_DRUG_STRENGTH.toString()))
            .andExpect(jsonPath("$.manufacturer").value(DEFAULT_MANUFACTURER.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.authorizationRequired").value(DEFAULT_AUTHORIZATION_REQUIRED.booleanValue()))
            .andExpect(jsonPath("$.narcotic").value(DEFAULT_NARCOTIC.booleanValue()))
            .andExpect(jsonPath("$.drugForm.code").value(DEFAULT_DRUG_FORM.getCode()))
            .andExpect(jsonPath("$.drugSchedule").value(DEFAULT_DRUG_SCHEDULE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMedication() throws Exception {
        // Get the medication
        restMedicationMockMvc.perform(get("/api/medications/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchMedication() throws Exception {
        // Initialize the database
        medicationService.save(medication);

        // Search the medication
        restMedicationMockMvc.perform(get("/api/_search/medications?query=id:" + medication.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medication.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].brand").value(hasItem(DEFAULT_BRAND.booleanValue())))
            .andExpect(jsonPath("$.[*].drugStrength").value(hasItem(DEFAULT_DRUG_STRENGTH.toString())))
            .andExpect(jsonPath("$.[*].manufacturer").value(hasItem(DEFAULT_MANUFACTURER.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].authorizationRequired").value(hasItem(DEFAULT_AUTHORIZATION_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].narcotic").value(hasItem(DEFAULT_NARCOTIC.booleanValue())))
            .andExpect(jsonPath("$.[*].drugForm.code").value(hasItem(DEFAULT_DRUG_FORM.getCode())))
            .andExpect(jsonPath("$.[*].drugSchedule").value(hasItem(DEFAULT_DRUG_SCHEDULE.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Medication.class);
    }
}
