package org.nh.pharmacy.web.rest;

import com.netflix.discovery.converters.Auto;
import org.junit.Assert;
import org.nh.common.dto.*;
import org.nh.pharmacy.PharmacyApp;

import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;

import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.dto.IPDispenseReturnDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseReturnRequestDocument;
import org.nh.pharmacy.domain.enumeration.AdmissionStatus;
import org.nh.pharmacy.domain.enumeration.IPReturnRequestStatus;
import org.nh.pharmacy.repository.IPDispenseReturnRequestRepository;
import org.nh.pharmacy.service.IPDispenseReturnRequestService;
import org.nh.pharmacy.service.IPDispenseReturnRequestPdfService;
import org.nh.pharmacy.repository.search.IPDispenseReturnRequestSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.service.LocatorService;
import org.nh.repository.hibernate.type.JacksonUtil;
import org.nh.security.AuthenticatedUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the IPDispenseReturnRequestResource REST controller.
 *
 * @see IPDispenseReturnRequestResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class IPDispenseReturnRequestResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final org.nh.pharmacy.domain.dto.IPDispenseReturnRequestDocument DEFAULT_DOCUMENT = new IPDispenseReturnRequestDocument();
    private static final IPDispenseReturnRequestDocument UPDATED_DOCUMENT = new IPDispenseReturnRequestDocument();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = false;
    private static final Boolean UPDATED_LATEST = true;

    @Autowired
    private IPDispenseReturnRequestRepository iPDispenseReturnRequestRepository;

    @Autowired
    private IPDispenseReturnRequestService iPDispenseReturnRequestService;

    @Autowired
    private IPDispenseReturnRequestPdfService iPDispenseReturnRequestPdfService;

    @Autowired
    private IPDispenseReturnRequestSearchRepository iPDispenseReturnRequestSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restIPDispenseReturnRequestMockMvc;

    private IPDispenseReturnRequest iPDispenseReturnRequest;

    @Autowired
    private LocatorService locatorService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        IPDispenseReturnRequestResource iPDispenseReturnRequestResource = new IPDispenseReturnRequestResource(iPDispenseReturnRequestService,iPDispenseReturnRequestPdfService, iPDispenseReturnRequestRepository, iPDispenseReturnRequestSearchRepository, applicationProperties);
        ReflectionTestUtils.setField(iPDispenseReturnRequestResource, "iPDispenseReturnRequestService", iPDispenseReturnRequestService);
        this.restIPDispenseReturnRequestMockMvc = MockMvcBuilders.standaloneSetup(iPDispenseReturnRequestResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public IPDispenseReturnRequest createEntity(EntityManager em) {
        OrganizationDTO unit = new OrganizationDTO();
        unit.setId(1l);
        unit.setCode("5011");
        unit.setActive(true);
        DEFAULT_DOCUMENT.setUnit(unit);

        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setVisitNumber("001");

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setMrn("001");
        patientDTO.setFullName("Narayana");
        patientDTO.setAge(30);
        patientDTO.setGender("Male");

        UOMDTO uom = new UOMDTO();
        uom.setId(1l);
        uom.setCode("ABC");
        uom.setName("ANBC");

        org.nh.pharmacy.domain.Locator locator = locatorService.findOne(1l);
        LocatorDTO locatorDto = new LocatorDTO();
        BeanUtils.copyProperties(locator, locatorDto);

        HealthcareServiceCenterDTO requestingHsc = new HealthcareServiceCenterDTO();
        requestingHsc.setId(1l);
        requestingHsc.setCode("REQ-001");
        requestingHsc.setName("ReqtHSC");

        UserDTO user = new UserDTO();
        user.setDisplayName("abc");
        user.setId(1l);

        HealthcareServiceCenterDTO patientLocation = new HealthcareServiceCenterDTO();
        patientLocation.setName("HSR");
        patientLocation.setCode("HSR-001");

        String bedNumber ="G-001";
        IPDispenseReturnDocumentLine dispenseReturnDocumentLine = new IPDispenseReturnDocumentLine();
        dispenseReturnDocumentLine.setRequestedReturnQuantity(10f);
        dispenseReturnDocumentLine.setDispensedQuantity(10f);
        dispenseReturnDocumentLine.setName("Aspirin");
        dispenseReturnDocumentLine.setMrp(BigDecimal.TEN);
        dispenseReturnDocumentLine.setCode("orderitem-1");
        dispenseReturnDocumentLine.setName("Crocin");
        dispenseReturnDocumentLine.setBatchNumber("Batch-no-1");
        dispenseReturnDocumentLine.setCost(getBigDecimal(200f));
        dispenseReturnDocumentLine.setNetAmount(getBigDecimal(90.0f));
        dispenseReturnDocumentLine.setDiscountAmount(getBigDecimal(10.0f));
        dispenseReturnDocumentLine.setGrossAmount(getBigDecimal(100.0f));
        dispenseReturnDocumentLine.setStockId(11l);
        dispenseReturnDocumentLine.setItemId(1l);
        dispenseReturnDocumentLine.setLineNumber(1l);
        dispenseReturnDocumentLine.setReturnAmount(getBigDecimal(10f));
        dispenseReturnDocumentLine.setUom(uom);
        dispenseReturnDocumentLine.setLocator(locatorDto);
        dispenseReturnDocumentLine.setOwner("Owner");
        dispenseReturnDocumentLine.setSupplier("Supplier");

        SourceDTO source = new SourceDTO();
        source.setId(1l);
        source.setLineItemId(2l);
        source.setReferenceNumber("ABC");
        dispenseReturnDocumentLine.setDispenseRef(source);

        List<IPDispenseReturnDocumentLine> documentLines = new ArrayList<>();
        documentLines.add(dispenseReturnDocumentLine);
        DEFAULT_DOCUMENT.setDispenseReturnDocumentLines(documentLines);
        DEFAULT_DOCUMENT.setCreatedBy(user);
        DEFAULT_DOCUMENT.setReturnStatus(IPReturnRequestStatus.PENDING);
        DEFAULT_DOCUMENT.setPatient(patientDTO);
        DEFAULT_DOCUMENT.setEncounter(encounterDTO);
        DEFAULT_DOCUMENT.setSourceHSC(requestingHsc);
        DEFAULT_DOCUMENT.setReturnTOHSC(requestingHsc);
        DEFAULT_DOCUMENT.setRequestedBy(user);
        DEFAULT_DOCUMENT.setCreatedBy(user);
        DEFAULT_DOCUMENT.setRequestedDate(LocalDateTime.now());
        DEFAULT_DOCUMENT.setCreatedDate(LocalDateTime.now());
        DEFAULT_DOCUMENT.setPatientLocation(patientLocation);
        DEFAULT_DOCUMENT.setBedNumber(bedNumber);
        DEFAULT_DOCUMENT.setPatientStatus(AdmissionStatus.ADMITTED_TO_IP);
        DEFAULT_DOCUMENT.setReturnTOUnit(unit);
        IPDispenseReturnRequest iPDispenseReturnRequest = new IPDispenseReturnRequest()
                .documentNumber(DEFAULT_DOCUMENT_NUMBER)
                .document(DEFAULT_DOCUMENT)
                .version(DEFAULT_VERSION)
                .latest(DEFAULT_LATEST);
        return iPDispenseReturnRequest;
    }

    @Before
    public void initTest() {
        iPDispenseReturnRequestSearchRepository.deleteAll();
        addUserToSecurityContext(1l, "admin", "admin");
        iPDispenseReturnRequest = createEntity(em);
    }

    @Test
    @Transactional
    public void createIPDispenseReturnRequest() throws Exception {
        int databaseSizeBeforeCreate = iPDispenseReturnRequestRepository.findAll().size();

        // Create the IPDispenseReturnRequest

        restIPDispenseReturnRequestMockMvc.perform(post("/api/ip-dispense-return-requests")
                .contentType(TestUtil.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(iPDispenseReturnRequest)))
                .andExpect(status().isCreated());

        // Validate the IPDispenseReturnRequest in the database
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        assertThat(iPDispenseReturnRequests).hasSize(databaseSizeBeforeCreate + 1);
        IPDispenseReturnRequest testIPDispenseReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size() - 1);
        assertThat(testIPDispenseReturnRequest.getDocumentNumber()).isNotEmpty();
        assertThat(testIPDispenseReturnRequest.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testIPDispenseReturnRequest.isLatest()).isEqualTo(true);

        // Validate the IPDispenseReturnRequest in ElasticSearch
        IPDispenseReturnRequest iPDispenseReturnRequestEs = iPDispenseReturnRequestSearchRepository.findById(testIPDispenseReturnRequest.getId()).get();
        assertThat(iPDispenseReturnRequestEs.getDocumentNumber()).isNotEmpty();
        assertThat(iPDispenseReturnRequestEs.getVersion()).isEqualTo(testIPDispenseReturnRequest.getVersion());
        assertThat(iPDispenseReturnRequestEs.isLatest()).isEqualTo(testIPDispenseReturnRequest.isLatest());
    }

    @Test
    @Transactional
    public void getIPDispenseReturnsQty() throws Exception {

        MvcResult mvcResult = restIPDispenseReturnRequestMockMvc.perform(post("/api/ip-dispense-return-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(iPDispenseReturnRequest)))
            .andExpect(status().isCreated()).andReturn();

        IPDispenseReturnRequest ipDispenseReturnRequest = JacksonUtil.fromString(mvcResult.getResponse().getContentAsString(),IPDispenseReturnRequest.class);

        restIPDispenseReturnRequestMockMvc.perform(get("/api/ip-pending-dispense-return-requests-qty?mrn"+ipDispenseReturnRequest.getDocument().getPatient().getMrn()
        +"&visitNumber="+ipDispenseReturnRequest.getDocument().getEncounter().getVisitNumber())).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.itemName").value(iPDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0).getName()))
            .andExpect(jsonPath("$.ipReturnRequestStatus").value(iPDispenseReturnRequest.getDocument().getReturnStatus().name()))
            .andExpect(jsonPath("$.visitNumber").value(iPDispenseReturnRequest.getDocument().getEncounter().getVisitNumber()))
            .andExpect(jsonPath("$.returnhsc.code").value(iPDispenseReturnRequest.getDocument().getReturnTOHSC().getCode()));
    }

    @Test
    @Transactional
    public void getIPDispenseReturns() throws Exception {

        MvcResult mvcResult = restIPDispenseReturnRequestMockMvc.perform(post("/api/ip-dispense-return-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(iPDispenseReturnRequest)))
            .andExpect(status().isCreated()).andReturn();

        IPDispenseReturnRequest ipDispenseReturnRequest = JacksonUtil.fromString(mvcResult.getResponse().getContentAsString(),IPDispenseReturnRequest.class);

        restIPDispenseReturnRequestMockMvc.perform(get("/api/ip-pending-dispense-return-orders?mrn"+ipDispenseReturnRequest.getDocument().getPatient().getMrn()
            +"&visitNumber="+ipDispenseReturnRequest.getDocument().getEncounter().getVisitNumber())).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.itemName").value(iPDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0).getName()))
            .andExpect(jsonPath("$.ipReturnRequestStatus").value(iPDispenseReturnRequest.getDocument().getReturnStatus().name()))
            .andExpect(jsonPath("$.visitNumber").value(iPDispenseReturnRequest.getDocument().getEncounter().getVisitNumber()))
            .andExpect(jsonPath("$.documentNumber").value(iPDispenseReturnRequest.getDocumentNumber()))
            .andExpect(jsonPath("$.returnhsc.code").value(iPDispenseReturnRequest.getDocument().getReturnTOHSC().getCode()));
    }

    @Test
    @Transactional
    public void getAllIPDispenseReturnRequests() throws Exception {
        // Initialize the database
        iPDispenseReturnRequestService.save(iPDispenseReturnRequest);

        // Get all the iPDispenseReturnRequests
        restIPDispenseReturnRequestMockMvc.perform(get("/api/ip-dispense-return-requests?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(iPDispenseReturnRequest.getId().intValue())))
                .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(startsWith("DRR"))))
                .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
                .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getIPDispenseReturnRequest() throws Exception {
        // Initialize the database
        iPDispenseReturnRequestService.save(iPDispenseReturnRequest);

        // Get the iPDispenseReturnRequest
        restIPDispenseReturnRequestMockMvc.perform(get("/api/ip-dispense-return-requests/{id}", iPDispenseReturnRequest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(iPDispenseReturnRequest.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").value(startsWith("DRR")))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(UPDATED_LATEST));
    }

    @Test
    @Transactional
    public void getNonExistingIPDispenseReturnRequest() throws Exception {
        // Get the iPDispenseReturnRequest
        restIPDispenseReturnRequestMockMvc.perform(get("/api/i-p-dispense-return-requests/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateIPDispenseReturnRequest() throws Exception {
        // Initialize the database
        iPDispenseReturnRequestService.save(iPDispenseReturnRequest);

        int databaseSizeBeforeUpdate = iPDispenseReturnRequestRepository.findAll().size();

        // Update the iPDispenseReturnRequest
        IPDispenseReturnRequest updatedIPDispenseReturnRequest = iPDispenseReturnRequestRepository.findOne(iPDispenseReturnRequest.getId());
        updatedIPDispenseReturnRequest
                .documentNumber(UPDATED_DOCUMENT_NUMBER)
                .document(UPDATED_DOCUMENT)
                .latest(UPDATED_LATEST);

        restIPDispenseReturnRequestMockMvc.perform(put("/api/ip-dispense-return-requests")
                .contentType(TestUtil.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedIPDispenseReturnRequest)))
                .andExpect(status().isOk());

        // Validate the IPDispenseReturnRequest in the database
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        assertThat(iPDispenseReturnRequests).hasSize(databaseSizeBeforeUpdate+1);
        IPDispenseReturnRequest testIPDispenseReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size() - 1);
        assertThat(testIPDispenseReturnRequest.getDocumentNumber()).isEqualTo(UPDATED_DOCUMENT_NUMBER);
        assertThat(testIPDispenseReturnRequest.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testIPDispenseReturnRequest.isLatest()).isEqualTo(UPDATED_LATEST);

        // Validate the IPDispenseReturnRequest in ElasticSearch
        IPDispenseReturnRequest iPDispenseReturnRequestEs = iPDispenseReturnRequestSearchRepository.findById(testIPDispenseReturnRequest.getId()).get();
        assertThat(iPDispenseReturnRequestEs.getVersion()).isEqualTo(testIPDispenseReturnRequest.getVersion());
    }

    @Test
    @Transactional
    public void deleteIPDispenseReturnRequest() throws Exception {
        // Initialize the database
        iPDispenseReturnRequestService.save(iPDispenseReturnRequest);

        int databaseSizeBeforeDelete = iPDispenseReturnRequestRepository.findAll().size();

        // Get the iPDispenseReturnRequest
        restIPDispenseReturnRequestMockMvc.perform(delete("/api/ip-dispense-return-requests/{id}", iPDispenseReturnRequest.getId())
                .accept(TestUtil.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean iPDispenseReturnRequestExistsInEs = iPDispenseReturnRequestSearchRepository.existsById(iPDispenseReturnRequest.getId());
        assertThat(iPDispenseReturnRequestExistsInEs).isFalse();

        // Validate the database is empty
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        assertThat(iPDispenseReturnRequests).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchIPDispenseReturnRequest() throws Exception {
        // Initialize the database
        iPDispenseReturnRequestService.save(iPDispenseReturnRequest);

        // Search the iPDispenseReturnRequest
        restIPDispenseReturnRequestMockMvc.perform(get("/api/_search/ip-dispense-return-requests?query=id:" + iPDispenseReturnRequest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(iPDispenseReturnRequest.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(startsWith("DRR"))))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(UPDATED_LATEST.booleanValue())));
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

    @Test
    @Transactional
    public void testExportIPDispenseReturnRequests() throws  Exception{
        iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        restIPDispenseReturnRequestMockMvc.perform(get("/api/_export/ip-dispense-return-requests?query=*"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }

    @Test
    public void testExportIPDispenseReturn() throws  Exception{
        IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        IPDispenseReturnDocumentLine documentLine =ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0);
        documentLine.setAcceptedReturnQuantity(10f);
        documentLine.setEarlierReturnQuantity(0f);
        documentLine.setPreviousAcceptedReturnQty(0f);
        iPDispenseReturnRequestService.processIPDispenseReturnRequest(ipDispenseReturnRequest,"ACCEPT", new HashMap<>());
        restIPDispenseReturnRequestMockMvc.perform(get("/api/_export/ip-dispense-return?query=*"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }

    /**
     * Method to test accept pharmacy return partially
     * @throws Exception
     */
    @Test
    public void testProcessIPReturnRequest() throws  Exception{
        IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        IPDispenseReturnDocumentLine documentLine =ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0);
        documentLine.setAcceptedReturnQuantity(2f);
        documentLine.setEarlierReturnQuantity(0f);
        documentLine.setPreviousAcceptedReturnQty(0f);
        restIPDispenseReturnRequestMockMvc.perform(post("/api/process-ip-dispense-request/ip-dispense-return-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(ipDispenseReturnRequest)).param("action","ACCEPT"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        IPDispenseReturnRequest updatedIPReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size()-1);
        Assert.assertEquals(IPReturnRequestStatus.PARTIALLY_RETURNED,updatedIPReturnRequest.getDocument().getReturnStatus());
    }

    /**
     * Method to test accept pharmacy return fully
     * @throws Exception
     */
    @Test
    public void testAcceptIPReturnRequest() throws  Exception{
        IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        IPDispenseReturnDocumentLine documentLine =ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0);
        documentLine.setAcceptedReturnQuantity(10f);
        documentLine.setEarlierReturnQuantity(0f);
        documentLine.setPreviousAcceptedReturnQty(0f);
        restIPDispenseReturnRequestMockMvc.perform(post("/api/process-ip-dispense-request/ip-dispense-return-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(ipDispenseReturnRequest)).param("action","ACCEPT"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        IPDispenseReturnRequest updatedIPReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size()-1);
        Assert.assertEquals(IPReturnRequestStatus.RETURNED,updatedIPReturnRequest.getDocument().getReturnStatus());
    }

    /**
     * Method to test reject pharmacy return fully
     * @throws Exception
     */
    @Test
    public void testRejectIPReturnRequest() throws  Exception{
        IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        IPDispenseReturnDocumentLine documentLine =ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0);
        documentLine.setAcceptedReturnQuantity(10f);
        documentLine.setEarlierReturnQuantity(0f);
        documentLine.setPreviousAcceptedReturnQty(0f);
        restIPDispenseReturnRequestMockMvc.perform(post("/api/process-ip-dispense-request/ip-dispense-return-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(ipDispenseReturnRequest)).param("action","REJECT"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        IPDispenseReturnRequest updatedIPReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size()-1);
        Assert.assertEquals(IPReturnRequestStatus.REJECTED,updatedIPReturnRequest.getDocument().getReturnStatus());
    }

    /**
     * Method to test reject pharmacy return partially i.e., after partially accepting, reject the item
     * @throws Exception
     */
    @Test
    public void testPartialRejectIPReturnRequest() throws  Exception{
        IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        IPDispenseReturnDocumentLine documentLine =ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0);
        documentLine.setAcceptedReturnQuantity(4f);
        documentLine.setEarlierReturnQuantity(0f);
        documentLine.setPreviousAcceptedReturnQty(0f);
        iPDispenseReturnRequestService.processIPDispenseReturnRequest(ipDispenseReturnRequest,"ACCEPT", new HashMap<>());
        restIPDispenseReturnRequestMockMvc.perform(post("/api/process-ip-dispense-request/ip-dispense-return-requests")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(ipDispenseReturnRequest)).param("action","REJECT"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        IPDispenseReturnRequest updatedIPReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size()-1);
        Assert.assertEquals(IPReturnRequestStatus.PARTIALLY_REJECTED,updatedIPReturnRequest.getDocument().getReturnStatus());
    }

    /**
     * Method to test accept pharmacy return fully
     * @throws Exception
     */
    @Test
    public void testAcceptIPDirectReturn() throws  Exception{
        //IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        IPDispenseReturnDocumentLine documentLine =iPDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().get(0);
        documentLine.setAcceptedReturnQuantity(10f);
        documentLine.setEarlierReturnQuantity(0f);
        documentLine.setPreviousAcceptedReturnQty(0f);
        restIPDispenseReturnRequestMockMvc.perform(post("/api/ip-dispense-direct-return")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(iPDispenseReturnRequest)).param("action","ACCEPT"))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        List<IPDispenseReturnRequest> iPDispenseReturnRequests = iPDispenseReturnRequestRepository.findAll();
        IPDispenseReturnRequest updatedIPReturnRequest = iPDispenseReturnRequests.get(iPDispenseReturnRequests.size()-1);
        Assert.assertEquals(IPReturnRequestStatus.RETURNED,updatedIPReturnRequest.getDocument().getReturnStatus());
    }
}
