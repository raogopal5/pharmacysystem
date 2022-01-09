package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.common.dto.EncounterDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.enumeration.AdmissionAndNursingStatus;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.dto.IPDispenseReturnDocumentLine;
import org.nh.pharmacy.domain.dto.MedicationRequestDocument;
import org.nh.pharmacy.domain.enumeration.MedicationRequestStatus;
import org.nh.pharmacy.repository.MedicationRequestRepository;
import org.nh.pharmacy.repository.search.MedicationRequestSearchRepository;
import org.nh.pharmacy.repository.search.MedicationSearchRepository;
import org.nh.pharmacy.service.MedicationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CPOEMedicationRequestResource REST controller.
 *
 * @see MedicationRequestResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class MedicationRequestResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final MedicationRequestDocument DEFAULT_DOCUMENT = new MedicationRequestDocument();
    private static final MedicationRequestDocument UPDATED_DOCUMENT = new MedicationRequestDocument();

    public static final String DEFAULT_EMPLOYEE_NO = "AAAAAAAAAAA";
    public static final String DEFAULT_LOGIN_NAME = "AAAAAAAAAAA";
    public static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAAA";

    public static final UserDTO DEFAULT_USER = getUser();

    private static final Integer DEFAULT_VERSION = 1;
    private static final Integer UPDATED_VERSION = 2;

    private static final Boolean DEFAULT_LATEST = false;
    private static final Boolean UPDATED_LATEST = true;

    @Autowired
    private MedicationRequestRepository medicationRequestRepository;

    @Autowired
    private MedicationRequestService medicationRequestService;

    @Autowired
    private MedicationRequestSearchRepository medicationRequestSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private
    MedicationSearchRepository medicationSearchRepository;

    @Autowired
    private
    ApplicationProperties applicationProperties;

    @Autowired
    private EntityManager em;

    private MockMvc restMedicationRequestMockMvc;

    private MedicationRequest medicationRequest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MedicationRequestResource medicationRequestResource = new MedicationRequestResource(medicationRequestService, medicationRequestRepository,
            medicationSearchRepository,
            applicationProperties);
        this.restMedicationRequestMockMvc = MockMvcBuilders.standaloneSetup(medicationRequestResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MedicationRequest createEntity(EntityManager em) {
        DEFAULT_DOCUMENT.setConsultant(DEFAULT_USER);
        MedicationRequest medicationRequest = new MedicationRequest()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);

        UserDTO createdBy = new UserDTO();
        createdBy.setLogin("ABC");
        createdBy.setDisplayName("ABC");

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setMrn("MRN-001");
        patientDTO.setAge(20);
        patientDTO.setFullName("ABC");
        patientDTO.setGender("M");
        patientDTO.setDisplayName("ABC");

        EncounterDTO encounterDTO= new EncounterDTO();
        encounterDTO.setVisitNumber("IP-001");

        medicationRequest.getDocument().setCreatedBy(createdBy);
        medicationRequest.getDocument().setConsultant(createdBy);
        medicationRequest.getDocument().setCreatedDate(LocalDateTime.now());
        medicationRequest.getDocument().setPatient(patientDTO);
        medicationRequest.getDocument().setEncounter(encounterDTO);
        return medicationRequest;
    }

    public static UserDTO getUser() {
        UserDTO user = new UserDTO();
        user.setDisplayName(DEFAULT_DISPLAY_NAME);
        user.setLogin(DEFAULT_LOGIN_NAME);
        user.setEmployeeNo(DEFAULT_EMPLOYEE_NO);
        return user;
    }

    @Before
    public void initTest() {
        medicationRequestSearchRepository.deleteAll();
        medicationRequest = createEntity(em);
    }

    @Test
    @Transactional
    public void createMedicationRequest() throws Exception {
        int databaseSizeBeforeCreate = medicationRequestRepository.findAll().size();

        // Create the MedicationRequest

        restMedicationRequestMockMvc.perform(post("/api/medication-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medicationRequest)))
            .andExpect(status().isCreated());

        // Validate the MedicationRequest in the database
        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeCreate + 1);
        MedicationRequest testMedicationRequest = medicationRequests.get(medicationRequests.size() - 1);
        assertThat(testMedicationRequest.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT_NUMBER);
        assertThat(testMedicationRequest.getDocument()).isEqualTo(DEFAULT_DOCUMENT);
        assertThat(testMedicationRequest.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testMedicationRequest.isLatest()).isEqualTo(DEFAULT_LATEST);

        // Validate the MedicationRequest in ElasticSearch
        MedicationRequest medicationRequestEs = medicationRequestSearchRepository.findById(testMedicationRequest.getId()).get();
        assertThat(medicationRequestEs).isEqualToComparingFieldByField(testMedicationRequest);
    }

    @Test
    @Transactional
    public void checkDocumentNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRequestRepository.findAll().size();
        // set the field null
        medicationRequest.setDocumentNumber(null);

        // Create the MedicationRequest, which fails.

        restMedicationRequestMockMvc.perform(post("/api/medication-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medicationRequest)))
            .andExpect(status().isBadRequest());

        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRequestRepository.findAll().size();
        // set the field null
        medicationRequest.setDocument(null);

        // Create the MedicationRequest, which fails.

        restMedicationRequestMockMvc.perform(post("/api/medication-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medicationRequest)))
            .andExpect(status().isBadRequest());

        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRequestRepository.findAll().size();
        // set the field null
        medicationRequest.setVersion(null);

        // Create the MedicationRequest, which fails.

        restMedicationRequestMockMvc.perform(post("/api/medication-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medicationRequest)))
            .andExpect(status().isBadRequest());

        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLatestIsRequired() throws Exception {
        int databaseSizeBeforeTest = medicationRequestRepository.findAll().size();
        // set the field null
        medicationRequest.setLatest(null);

        // Create the MedicationRequest, which fails.

        restMedicationRequestMockMvc.perform(post("/api/medication-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(medicationRequest)))
            .andExpect(status().isBadRequest());

        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMedicationRequests() throws Exception {
        // Initialize the database
        medicationRequestRepository.saveAndFlush(medicationRequest);

        // Get all the medicationRequests
        restMedicationRequestMockMvc.perform(get("/api/medication-requests?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicationRequest.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(DEFAULT_DOCUMENT_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].document.consultant.displayName").value(hasItem(DEFAULT_DOCUMENT.getConsultant().getDisplayName())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getMedicationRequest() throws Exception {
        // Initialize the database
        medicationRequestRepository.saveAndFlush(medicationRequest);

        // Get the medicationRequest
        restMedicationRequestMockMvc.perform(get("/api/medication-requests/{id}", medicationRequest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medicationRequest.getId().intValue()))
            .andExpect(jsonPath("$.document.consultant.displayName").value(DEFAULT_DOCUMENT.getConsultant().getDisplayName()))
            .andExpect(jsonPath("$.documentNumber").value(DEFAULT_DOCUMENT_NUMBER.toString()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingMedicationRequest() throws Exception {
        // Get the medicationRequest
        restMedicationRequestMockMvc.perform(get("/api/medication-requests/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMedicationRequest() throws Exception {
        // Initialize the database
        medicationRequestService.save(medicationRequest);

        int databaseSizeBeforeUpdate = medicationRequestRepository.findAll().size();

        // Update the medicationRequest
        MedicationRequest updatedMedicationRequest = medicationRequestRepository.findById(medicationRequest.getId()).get();
        updatedMedicationRequest
            .documentNumber(UPDATED_DOCUMENT_NUMBER)
            .document(UPDATED_DOCUMENT)
            .version(UPDATED_VERSION)
            .latest(UPDATED_LATEST);

        restMedicationRequestMockMvc.perform(put("/api/medication-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedMedicationRequest)))
            .andExpect(status().isOk());

        // Validate the MedicationRequest in the database
        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeUpdate);
        MedicationRequest testMedicationRequest = medicationRequests.get(medicationRequests.size() - 1);
        assertThat(testMedicationRequest.getDocumentNumber()).isEqualTo(UPDATED_DOCUMENT_NUMBER);
        assertThat(testMedicationRequest.getDocument()).isEqualTo(UPDATED_DOCUMENT);
        assertThat(testMedicationRequest.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testMedicationRequest.isLatest()).isEqualTo(UPDATED_LATEST);

        // Validate the MedicationRequest in ElasticSearch
        MedicationRequest medicationRequestEs = medicationRequestSearchRepository.findById(testMedicationRequest.getId()).get();
        assertThat(medicationRequestEs).isEqualToComparingFieldByField(testMedicationRequest);
    }

    @Test
    @Transactional
    public void deleteMedicationRequest() throws Exception {
        // Initialize the database
        medicationRequestService.save(medicationRequest);

        int databaseSizeBeforeDelete = medicationRequestRepository.findAll().size();

        // Get the medicationRequest
        restMedicationRequestMockMvc.perform(delete("/api/medication-requests/{id}", medicationRequest.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean medicationRequestExistsInEs = medicationRequestSearchRepository.existsById(medicationRequest.getId());
        assertThat(medicationRequestExistsInEs).isFalse();

        // Validate the database is empty
        List<MedicationRequest> medicationRequests = medicationRequestRepository.findAll();
        assertThat(medicationRequests).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchMedicationRequest() throws Exception {
        // Initialize the databaseIpDispenseReturnPrintServiceImpl
        medicationRequestService.save(medicationRequest);

        // Search the medicationRequest
        restMedicationRequestMockMvc.perform(get("/api/_search/medication-requests?query=id:" + medicationRequest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicationRequest.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(DEFAULT_DOCUMENT_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].document.consultant.displayName").value(hasItem(DEFAULT_USER.getDisplayName().toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    public void testExportMedicationRequest() throws  Exception{
        medicationRequest.getDocument().setPatientStatus(AdmissionAndNursingStatus.UNDER_DAY_CARE);
        medicationRequest.getDocument().setPlanName("ABC");
        medicationRequest.getDocument().setMedicationRequestStatus(MedicationRequestStatus.DISPENSED);
        MedicationRequest savedMedicationRequest = medicationRequestService.save(medicationRequest);
        restMedicationRequestMockMvc.perform(get("/api/_export/medication-requests?query=*"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }
}
