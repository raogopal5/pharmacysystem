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
import org.nh.billing.domain.Invoice;
import org.nh.billing.domain.PlanRule;
import org.nh.billing.domain.PlanRules;
import org.nh.billing.domain.TaxMapping;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.*;
import org.nh.billing.repository.PamIntegrationRepository;
import org.nh.billing.repository.search.InvoiceSearchRepository;
import org.nh.billing.repository.search.ReceiptSearchRepository;
import org.nh.billing.service.InvoiceReceiptService;
import org.nh.billing.service.InvoiceService;
import org.nh.common.dto.*;
import org.nh.common.enumeration.DocumentType;
import org.nh.common.util.BigDecimalUtil;
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
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.dto.Configuration;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.nh.pharmacy.domain.enumeration.*;
import org.nh.pharmacy.dto.ValueSet;
import org.nh.pharmacy.dto.ValueSetCode;
import org.nh.pharmacy.repository.DispenseRepository;
import org.nh.pharmacy.repository.StockRepository;
import org.nh.pharmacy.repository.search.DispenseSearchRepository;
import org.nh.pharmacy.repository.search.HealthcareServiceCenterSearchRepository;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.service.consumers.Consumer;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
import org.nh.pharmacy.web.rest.mapper.DispenseToReceiptMapper;
import org.nh.pharmacy.web.rest.mapper.DispenseToReturnMapper;
import org.nh.pharmacy.web.rest.mapper.InvoiceMapper;
import org.nh.pharmacy.web.rest.util.TransformWorkBookToRuleUtils;
import org.nh.security.AuthenticatedUser;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the DispenseResource REST controller.
 *
 * @see DispenseResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class DispenseResourceIntTest {

    private static final String DEFAULT_DOCUMENT_NUMBER = "DD-2001-000000001";
    private static final String UPDATED_DOCUMENT_NUMBER = "DD-2001-000000002";

    private static final DispenseDocument DEFAULT_DOCUMENT = new DispenseDocument();
    private static final DispenseDocument UPDATED_DOCUMENT = new DispenseDocument();

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    private static final Boolean DEFAULT_LATEST = true;
    private static final Boolean UPDATED_LATEST = false;

    private org.nh.pharmacy.domain.User userObj = createUserObj();


    @Autowired
    private DispenseRepository dispenseRepository;

    @Autowired
    private DispenseService dispenseService;

    @Autowired
    private DispenseSearchRepository dispenseSearchRepository;

    @Autowired
    private InvoiceSearchRepository invoiceSearchRepository;

    @Autowired
    private InvoiceReceiptService invoiceReceiptService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    SpringSecurityIdentityProvider identityProvider;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    InvoiceMapper invoiceMapper;

    @Autowired
    DispenseToReturnMapper dispenseToReturnMapper;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    DispenseToReceiptMapper dispenseToReceiptMapper;

    @Autowired
    GroupService groupService;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    BillingService billingService;

    @Autowired
    StockService stockService;

    @Autowired
    ReceiptSearchRepository receiptSearchRepository;

    @Autowired
    HealthcareServiceCenterSearchRepository hscSearchRepository;

    @Autowired
    Consumer consumer;

    @Autowired
    UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PamIntegrationRepository pamIntegrationRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private IpPrintService ipPrintService;




    private MockMvc restDispenseMockMvc;

    private MockMvc restWorkflowMockMvc;

    private Dispense dispense;

    private PlanExecutionService planExecutionService;

    private Stock stock;

    private DispensePrintBarcodeService dispensePrintBarcodeService;

    @Autowired
    JBPMTaskSearchRepository jbpmTaskSearchRepository;

    @Autowired
    JbpmProperties jbpmProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DispenseResource dispenseResource = new DispenseResource(dispenseService, billingService, planExecutionService,dispenseRepository,dispenseSearchRepository,applicationProperties,ipPrintService, dispensePrintBarcodeService);
        this.restDispenseMockMvc = MockMvcBuilders.standaloneSetup(dispenseResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
        WorkflowResource workflowResource = new WorkflowResource(workflowService, identityProvider, jbpmTaskSearchRepository, jbpmProperties);
        this.restWorkflowMockMvc = MockMvcBuilders.standaloneSetup(workflowResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
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
        configuration1.setKey("athma_pharmacy_dispense_enable_workflow");
        configuration1.setValue("Yes");
        configuration1.setLevel(2);

        Configuration configuration2 = new Configuration();
        configuration2.setApplicableCode("1001");
        configuration2.setApplicableTo(3l);
        configuration2.setApplicableType(ConfigurationLevel.Unit);
        configuration2.setKey("athma_pharmacy_dispense_workflow_definition");
        configuration2.setValue("dispense_document_process");
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
        configuration4.setKey("athma_pharmacy_dispense_discount_type");
        configuration4.setValue("FLAT_DISCOUNT");
        configuration4.setLevel(2);

        Configuration configuration5 = new Configuration();
        configuration5.setApplicableCode("1001");
        configuration5.setApplicableTo(3l);
        configuration5.setApplicableType(ConfigurationLevel.Unit);
        configuration5.setKey("athma_pharmacy_dispense_default_discount_percentage");
        configuration5.setValue("0.0");
        configuration5.setLevel(2);

        Configuration configuration6 = new Configuration();
        configuration6.setApplicableCode("1001");
        configuration6.setApplicableTo(3l);
        configuration6.setApplicableType(ConfigurationLevel.Unit);
        configuration6.setKey("athma_pharmacy_dispense_tax_calculation_type");
        configuration6.setValue("");
        configuration6.setLevel(2);

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

        HSCServiceDTO hscServiceDTO1 = new HSCServiceDTO();
        hscServiceDTO1.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO1.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO1.getServiceMaster().setId(2l);
        hscServiceDTO1.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO2 = new HSCServiceDTO();
        hscServiceDTO2.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO2.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO2.getServiceMaster().setId(3l);
        hscServiceDTO2.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO3 = new HSCServiceDTO();
        hscServiceDTO3.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO3.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO3.getServiceMaster().setId(4l);
        hscServiceDTO3.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO4 = new HSCServiceDTO();
        hscServiceDTO4.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO4.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO4.getServiceMaster().setId(5l);
        hscServiceDTO4.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO5 = new HSCServiceDTO();
        hscServiceDTO5.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO5.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO5.getServiceMaster().setId(6l);
        hscServiceDTO5.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO6 = new HSCServiceDTO();
        hscServiceDTO6.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO6.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO6.getServiceMaster().setId(7l);
        hscServiceDTO6.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO7 = new HSCServiceDTO();
        hscServiceDTO7.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO7.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO7.getServiceMaster().setId(8l);
        hscServiceDTO7.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO8 = new HSCServiceDTO();
        hscServiceDTO8.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO8.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO8.getServiceMaster().setId(9l);
        hscServiceDTO8.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO9 = new HSCServiceDTO();
        hscServiceDTO9.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO9.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO9.getServiceMaster().setId(100l);
        hscServiceDTO9.getHealthcareServiceCenter().setId(1l);

        HSCServiceDTO hscServiceDTO10 = new HSCServiceDTO();
        hscServiceDTO10.setServiceMaster(new ServiceMasterDTO());
        hscServiceDTO10.setHealthcareServiceCenter(new HealthcareServiceCenterDTO());
        hscServiceDTO10.getServiceMaster().setId(101l);
        hscServiceDTO10.getHealthcareServiceCenter().setId(1l);

        InPatientDTO inPatientDTO = new InPatientDTO();
        EncounterDTO encounterDTO= new EncounterDTO();
        inPatientDTO.setEncounter(encounterDTO);



        IndexQuery indexQuery1 = new IndexQueryBuilder().withId("1").withObject(configuration1).build();
        IndexQuery indexQuery2 = new IndexQueryBuilder().withId("2").withObject(configuration2).build();
        IndexQuery indexQuery3 = new IndexQueryBuilder().withId("3").withObject(configuration3).build();
        IndexQuery indexQuery4 = new IndexQueryBuilder().withId("4").withObject(configuration4).build();
        IndexQuery indexQuery5 = new IndexQueryBuilder().withId("5").withObject(configuration5).build();
        IndexQuery indexQuery6 = new IndexQueryBuilder().withId("6").withObject(configuration6).build();
        IndexQuery indexQuery7 = new IndexQueryBuilder().withId("1").withObject(valueSetCode).build();
        IndexQuery indexQuery8 = new IndexQueryBuilder().withId("1").withObject(hscServiceDTO).build();
        IndexQuery indexQuery9 = new IndexQueryBuilder().withId("2").withObject(hscServiceDTO1).build();
        IndexQuery indexQuery10 = new IndexQueryBuilder().withId("3").withObject(hscServiceDTO2).build();
        IndexQuery indexQuery11 = new IndexQueryBuilder().withId("4").withObject(hscServiceDTO3).build();
        IndexQuery indexQuery12 = new IndexQueryBuilder().withId("5").withObject(hscServiceDTO4).build();
        IndexQuery indexQuery13 = new IndexQueryBuilder().withId("6").withObject(hscServiceDTO5).build();
        IndexQuery indexQuery14 = new IndexQueryBuilder().withId("7").withObject(hscServiceDTO6).build();
        IndexQuery indexQuery15 = new IndexQueryBuilder().withId("8").withObject(hscServiceDTO7).build();
        IndexQuery indexQuery16 = new IndexQueryBuilder().withId("9").withObject(hscServiceDTO8).build();
        IndexQuery indexQuery17 = new IndexQueryBuilder().withId("10").withObject(hscServiceDTO9).build();
        IndexQuery indexQuery18 = new IndexQueryBuilder().withId("11").withObject(hscServiceDTO10).build();
        IndexQuery indexQuery19 = new IndexQueryBuilder().withId("1").withObject(inPatientDTO).build();

        elasticsearchTemplate.index(indexQuery1, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery2, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery3, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery4, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery5, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery6, IndexCoordinates.of("configuration"));
        elasticsearchTemplate.index(indexQuery7, IndexCoordinates.of("valuesetcode"));
        elasticsearchTemplate.index(indexQuery8, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery9, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery10, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery11, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery12, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery13, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery14, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery15, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery16, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery17, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery18, IndexCoordinates.of("hscservice"));
        elasticsearchTemplate.index(indexQuery19, IndexCoordinates.of("inpatient"));


        elasticsearchTemplate.refresh(Configuration.class);
        elasticsearchTemplate.refresh(ValueSetCode.class);
        elasticsearchTemplate.refresh(HSCServiceDTO.class);
        elasticsearchTemplate.refresh(InPatientDTO.class);
    }

    private org.nh.pharmacy.domain.User createUserObj() {
        org.nh.pharmacy.domain.User user = new org.nh.pharmacy.domain.User();
        user.setId(1001l);
        user.setLogin("Admin");
        user.setEmployeeNo("335036");
        user.setDisplayName("User 1");
        user.setActive(true);
        user.setStatus(UserStatus.Active);
        user.setUserType(UserType.User);
        return user;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dispense createEntity() {

        Dispense dispense = new Dispense()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .document(DEFAULT_DOCUMENT)
            .version(DEFAULT_VERSION)
            .latest(DEFAULT_LATEST);
        return dispense;
    }

    @Before
    public void initTest() throws IOException {
        dispenseSearchRepository.deleteAll();
        dispense = createEntity();
        userService.save(userObj);
        dispense.document(populateDispenseDocument());
        groupService.doIndex();
        indexItemTaxMapping();
        addUserToSecurityContext(1L, "admin", "admin");
    }

    private void indexItemTaxMapping() throws IOException {
        TaxMapping taxMapping = new TaxMapping();
        taxMapping.setItemTaxMapping(new ArrayList<>());

        taxMapping.getItemTaxMapping().add(getItemTaxMapping(1l, "SGST", "SGST-5%", 1l, "ITEM-001"));
        taxMapping.getItemTaxMapping().add(getItemTaxMapping(2l, "CGST", "CGST-5%", 1l, "ITEM-001"));

        taxMapping.getItemTaxMapping().add(getItemTaxMapping(3l, "SGST", "SGST-5%", 8l, "ITEM-008"));
        taxMapping.getItemTaxMapping().add(getItemTaxMapping(4l, "CGST", "CGST-5%", 8l, "ITEM-008"));

        taxMapping.getItemTaxMapping().add(getItemTaxMapping(5l, "SGST", "SGST-5%", 9l, "ITEM-009"));
        taxMapping.getItemTaxMapping().add(getItemTaxMapping(6l, "CGST", "CGST-5%", 9l, "ITEM-009"));

        IndexQuery taxMappingQuery = new IndexQueryBuilder()
            .withObject(taxMapping).build();

        elasticsearchTemplate.deleteIndex(TaxMapping.class);
        elasticsearchTemplate.index(taxMappingQuery, IndexCoordinates.of("taxmapping"));

        elasticsearchTemplate.refresh(IndexCoordinates.of("taxmapping"));
    }

    private ItemTaxMapping getItemTaxMapping(long id, String taxCode, String taxDefCode, Long itemId, String itemCode) throws IOException {
        ItemTaxMapping itemTaxMapping = new ItemTaxMapping();
        itemTaxMapping.setInclusive(false);
        itemTaxMapping.setId(id);
        TaxableItem taxableItem = new TaxableItem();
        taxableItem.setId(itemId);
        taxableItem.setCode(itemCode);
        itemTaxMapping.setItem(taxableItem);

        itemTaxMapping.setUnit((org.nh.common.dto.OrganizationDTO) transforEntityToDTO(em.find(Organization.class, 3l), org.nh.common.dto.OrganizationDTO.class));
        itemTaxMapping.setTaxDefinition(new TaxDefinition());
        itemTaxMapping.getTaxDefinition().setActive(true);
        itemTaxMapping.getTaxDefinition().setTaxComponent(new ValueSetCodeDTO());
        itemTaxMapping.getTaxDefinition().getTaxComponent().setCode(taxCode);
        itemTaxMapping.getTaxDefinition().setCode(taxDefCode);
        itemTaxMapping.getTaxDefinition().setTaxtype(new ValueSetCodeDTO());
        itemTaxMapping.getTaxDefinition().getTaxtype().setCode("Inclusive");
        itemTaxMapping.getTaxDefinition().setTaxCalculation(new TaxCalculation());
        itemTaxMapping.getTaxDefinition().getTaxCalculation().setPercentage(5);
        itemTaxMapping.getTaxDefinition().getTaxCalculation().setType(Type.Percentage);
        return itemTaxMapping;
    }

    private DispenseDocument populateDispenseDocument() throws IOException {
        DispenseDocument dispenseDocument = new DispenseDocument();
        dispenseDocument.setHsc((HealthcareServiceCenterDTO) transforEntityToDTO(
            em.find(org.nh.pharmacy.domain.HealthcareServiceCenter.class, Long.valueOf(1)), HealthcareServiceCenterDTO.class));
        dispenseDocument.setDispenseUnit(dispenseDocument.getHsc().getPartOf());
        dispenseDocument.setUnitDiscountPercentage(10f);
        dispenseDocument.setDiscountPercentage(true);
        PatientDTO patient = new PatientDTO();
        patient.setId(1l);
        patient.setGender("Male");
        patient.setAge(20);
        dispenseDocument.setPatient(patient);

        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setId(1l);
        organizationDTO.setCode("valuesetcode.json");
        organizationDTO.setName("valuesetcode.json");

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(1l);
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
        dispenseDocument.setEncounter(encounterDTO);

        dispenseDocument.setDispenseTaxes(new ArrayList<>());
        dispenseDocument.setDispensePlans(populateDispensePlans());

        dispenseDocument.setDispenseDocumentLines(populateDispenseDocumentLines());

        return dispenseDocument;
    }

    private List<DispenseDocumentLine> populateDispenseDocumentLines() {
        List<DispenseDocumentLine> lines = new ArrayList<>();
        DispenseDocumentLine line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(1l);
        line.setCode("ITEM-001");
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(2l);
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(3l);
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(4l);
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(5l);
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(6l);
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(7l);
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(8l);
        line.setCode("ITEM-008");
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(9l);
        line.setCode("ITEM-009");
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));
        line.setItemDiscount(true);
        line.setPercentDiscount(true);
        line.setEnteredUserDiscount(10f);

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(100l);
        line.setCode("ITEM-100");
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(100f));
        line.setItemDiscount(true);
        line.setEnteredUserDiscount(102f);

        line = new DispenseDocumentLine();
        lines.add(line);
        line.setItemId(101l);
        line.setCode("ITEM-101");
        line.setQuantity(150f);
        line.setMrp(getBigDecimal(49.84f));
        line.setItemDiscount(true);
        line.setPercentDiscount(true);
        line.setEnteredUserDiscount(10f);

        return lines;
    }

    private List<DispensePlan> populateDispensePlans() throws IOException {
        List<DispensePlan> dispensePlans = new ArrayList<>();

        DispensePlan dPlan = new DispensePlan();
        dispensePlans.add(dPlan);
        dPlan.setPlanRef(new PlanRef());
        dPlan.getPlanRef().setId(1l);
        dPlan.getPlanRef().setCode("ATHMA-PLAN");
        dPlan.getPlanRef().setName("ATHMA Plan");
        dPlan.setSponsorRef(new org.nh.common.dto.OrganizationDTO());
        PlanRule planRule = new PlanRule();
        planRule.setLevel(0);
        planRule.setVersion(0);
        planRule.setTypeId(1l);
        planRule.setTypeCode("ATHMA-PLAN");
        planRule.setType("plan");
        dPlan.setPlanRule(TransformWorkBookToRuleUtils.uploadPlanRuleExcel(this.getClass().getResourceAsStream("/files/athma_plan_rules.xlsx"), planRule));

        dPlan = new DispensePlan();
        dispensePlans.add(dPlan);
        dPlan.setPlanRef(new PlanRef());
        dPlan.getPlanRef().setId(2l);
        dPlan.getPlanRef().setCode("ATHMA-ALTERNATE-PLAN");
        dPlan.getPlanRef().setName("ATHMA Alternate Plan");
        dPlan.setSponsorRef(new org.nh.common.dto.OrganizationDTO());
        planRule = new PlanRule();
        planRule.setLevel(0);
        planRule.setVersion(0);
        planRule.setTypeId(2l);
        planRule.setTypeCode("ATHMA-ALTERNATE-PLAN");
        planRule.setType("plan");
        dPlan.setPlanRule(TransformWorkBookToRuleUtils.uploadPlanRuleExcel(this.getClass().getResourceAsStream("/files/athma_alternate_plan_rules.xlsx"), planRule));

        return dispensePlans;
    }

    private Object transforEntityToDTO(Object source, Class<?> target) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(source), target);
    }

    @Test
    @Transactional
    public void createDispense() throws Exception {
        int databaseSizeBeforeCreate = dispenseRepository.findAll().size();

        // Create the Dispense

        restDispenseMockMvc.perform(post("/api/dispenses")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isCreated());

        // Validate the Dispense in the database
        List<Dispense> dispenseList = dispenseRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseList).hasSize(databaseSizeBeforeCreate + 1);
        Dispense testDispense = dispenseList.get(dispenseList.size() - 1);

        assertThat(testDispense.getDocumentNumber()).isNotEmpty();
        assertThat(testDispense.getVersion()).isEqualTo(dispense.getVersion());
        assertThat(testDispense.isLatest()).isEqualTo(dispense.isLatest());

        // Validate the Dispense in Elasticsearch
        Dispense dispenseEs = dispenseSearchRepository.findOne(testDispense.getId());
        assertThat(dispenseEs.getDocumentNumber()).isNotEmpty();
        assertThat(dispenseEs.getVersion()).isEqualTo(dispenseEs.getVersion());
        assertThat(dispenseEs.isLatest()).isEqualTo(dispenseEs.isLatest());
    }

    @Test
    @Transactional
    public void createDispenseWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = dispenseRepository.findAll().size();

        // Create the Dispense with an existing ID
        Dispense existingDispense = new Dispense();
        existingDispense.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDispenseMockMvc.perform(post("/api/dispenses")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingDispense)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Dispense> dispenseList = dispenseRepository.findAll();
        assertThat(dispenseList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDocumentIsRequired() throws Exception {
        int databaseSizeBeforeTest = dispenseRepository.findAll().size();
        // set the field null
        dispense.setDocument(null);

        // Create the Dispense, which fails.

        restDispenseMockMvc.perform(post("/api/dispenses")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isBadRequest());

        List<Dispense> dispenseList = dispenseRepository.findAll();
        assertThat(dispenseList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDispenses() throws Exception {
        // Initialize the database
        dispenseService.save(dispense);

        // Get all the dispenseList
        restDispenseMockMvc.perform(get("/api/dispenses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dispense.getId().intValue())))
            .andExpect(jsonPath("$.[*].documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].latest").value(hasItem(DEFAULT_LATEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getDispense() throws Exception {
        // Initialize the database
        dispenseService.save(dispense);

        // Get the dispense
        restDispenseMockMvc.perform(get("/api/dispenses/{id}", dispense.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(dispense.getId().intValue()))
            .andExpect(jsonPath("$.documentNumber").isNotEmpty())
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.latest").value(DEFAULT_LATEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingDispense() throws Exception {
        // Get the dispense
        restDispenseMockMvc.perform(get("/api/dispenses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDispense() throws Exception {
        // Initialize the database
        dispenseService.save(dispense);

        int databaseSizeBeforeUpdate = dispenseRepository.findAll().size();
        // Update the dispense
        Dispense updatedDispense = dispenseRepository.findOne(dispense.getId());
        UserDTO user = new UserDTO();
        user.setId(2l);
        user.setEmployeeNo("E-2");
        user.setLogin("Admin");
        user.setDisplayName("User-2");
        updatedDispense.getDocument().setApprovedBy(user);

        restDispenseMockMvc.perform(put("/api/dispenses")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedDispense)))
            .andExpect(status().isOk());

        // Validate the Dispense in the database
        List<Dispense> dispenseList = dispenseRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseList).hasSize(databaseSizeBeforeUpdate + 1);
        Dispense testDispense = dispenseList.get(dispenseList.size() - 1);
        assertThat(testDispense.getDocumentNumber()).isNotEmpty();
        assertThat(testDispense.getDocument().getApprovedBy().getId()).isEqualTo(updatedDispense.getDocument().getApprovedBy().getId());

        // Validate the Dispense in Elasticsearch
        Dispense dispenseEs = dispenseSearchRepository.findOne(testDispense.getId());
        assertThat(dispenseEs.getDocumentNumber()).isNotEmpty();
        assertThat(dispenseEs.getDocument().getApprovedBy().getId()).isEqualTo(updatedDispense.getDocument().getApprovedBy().getId());
    }

    @Test
    @Transactional
    public void updateNonExistingDispense() throws Exception {
        int databaseSizeBeforeUpdate = dispenseRepository.findAll().size();

        // Create the Dispense

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDispenseMockMvc.perform(put("/api/dispenses")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isCreated());

        // Validate the Dispense in the database
        List<Dispense> dispenseList = dispenseRepository.findAll();
        assertThat(dispenseList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteDispense() throws Exception {
        // Initialize the database
        dispense.getDocument().setDispenseStatus(DispenseStatus.DRAFT);
        dispenseService.save(dispense);

        int databaseSizeBeforeDelete = dispenseRepository.findAll().size();

        // Get the dispense
        restDispenseMockMvc.perform(delete("/api/dispenses/{id}", dispense.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean dispenseExistsInEs = dispenseSearchRepository.existsById(dispense.getId());
        assertThat(dispenseExistsInEs).isFalse();

        // Validate the database is empty
        List<Dispense> dispenseList = dispenseRepository.findAll();
        assertThat(dispenseList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void getInvoiceByDispenseId() throws Exception {
        createIndex();
        //Dispense document
        dispense.document(this.populateDispenseDocument());
        dispense.getDocument().setDispenseStatus(DispenseStatus.DISPENSED);
        Invoice savedInvoice = invoiceMapper.convertDispenseToInvoice(dispense);
        SourceDTO invoiceSource = new SourceDTO();
        invoiceSource.setId(dispense.getId());
        invoiceSource.setDocumentType(DocumentType.DISPENSE);
        invoiceSource.setReferenceNumber(dispense.getDocumentNumber());
        savedInvoice.getInvoiceDocument().setSource(invoiceSource);
        savedInvoice = invoiceService.createInvoiceDispense(savedInvoice, "CONFIRMED");
        SourceDTO sourceRef = new SourceDTO();
        sourceRef.setId(savedInvoice.getId());
        sourceRef.setReferenceNumber(savedInvoice.getInvoiceNumber());
        dispense.getDocument().setSource(sourceRef);
        dispenseService.save(dispense);
        restDispenseMockMvc.perform(get("/api/_invoice/dispenses?dispenseId=" + dispense.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(savedInvoice.getId().intValue()))
            .andExpect(jsonPath("$.invoiceNumber").value(savedInvoice.getInvoiceNumber()))
            .andExpect(jsonPath("$.version").value(savedInvoice.getVersion()))
            .andExpect(jsonPath("$.latest").value(savedInvoice.isLatest()));
    }

    // @Test
    @Transactional
    public void exportDispenses() throws Exception {
        // Initialize the database
        dispenseService.save(dispense);

        //Response is OutputStream. Not able to validate response,So validating status and content type.
        restDispenseMockMvc.perform(get("/api/_export/dispenses?query=id:" + dispense.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    @Transactional
    public void searchPatient() throws Exception {
        // Initialize the database

        PatientDTO patient = new PatientDTO();
        patient.setGender("Male");
        patient.setAge(20);
        patient.setMrn("p-1001-10001");
        dispense.getDocument().setPatient(patient);
        dispenseService.save(dispense);

        //Response is OutputStream. Not able to validate response,So validating status and content type.
        restDispenseMockMvc.perform(get("/api/search-patient/dispense?query=p-1001-10001"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].mrn").value(dispense.getDocument().getPatient().getMrn()));
    }

    @Test
    @Transactional
    public void getDispenseStatusCount() throws Exception {
        // Initialize the database
        dispense.getDocument().setDispenseStatus(DispenseStatus.DRAFT);
        dispenseService.save(dispense);

        restDispenseMockMvc.perform(get("/api/status-count/dispenses?query=*"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.DRAFT").value(1));

    }

    private BigDecimal roundOff(BigDecimal value) {
        return BigDecimalUtil.roundOff(value, 2);
    }

    //@Test
    /*public void taxCalculationForDispense() throws Exception {
        createIndex();
        MvcResult mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=ALL")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk())
            .andReturn();
        Dispense result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);
        result.getDocument().getDispenseDocumentLines().forEach(line -> {
            if (line.getItemId() == 1) {
                assertThat(line.getSponsorGrossAmount()).isEqualTo(roundOff(line.getGrossAmount() * 0.60f));
                assertThat(line.getPatientGrossAmount()).isEqualTo(roundOff(line.getGrossAmount() * 0.40f));
                assertThat(line.getSponsorTaxAmount()).isZero();
                assertThat(line.getPatientTaxAmount()).isEqualTo(roundOff(line.getGrossAmount() * 0.10f));
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.06f));
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
                assertThat(roundOff(line.getPlanDiscAmount() - line.getSponsorDiscAmount())).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.04f));
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPlanDiscAmount() + line.getSponsorDiscAmount());
                if (line.getDispenseItemPlans() != null)
                    line.getDispenseItemPlans().forEach(dispenseItemPlan -> {
                        assertThat(dispenseItemPlan.getPlanRef().getCode()).isEqualTo("ATHMA-ALTERNATE-PLAN");
                    });
            } else if (line.getItemId() == 2) {
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
                assertThat(line.getPlanDiscAmount() - line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.05f));
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() - line.getPatientTotalDiscAmount());
                if (line.getDispenseItemPlans() != null)
                    line.getDispenseItemPlans().forEach(dispenseItemPlan -> {
                        assertThat(dispenseItemPlan.getPlanRef().getCode()).isEqualTo("ATHMA-PLAN");
                    });
            } else if (line.getItemId() == 3) {
                assertThat(line.getSponsorGrossAmount()).isZero();
                assertThat(line.getSponsorNetAmount()).isZero();
                assertThat(line.getPlanDiscAmount()).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.05f));
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() - line.getPatientTotalDiscAmount());
            } else if (line.getItemId() == 4) {
                assertThat(line.getPatientGrossAmount()).isZero();
                assertThat(line.getPatientNetAmount()).isZero();
                assertThat(line.getPatientTotalDiscAmount()).isZero();
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
            } else if (line.getItemId() == 5) {
                assertThat(line.getSponsorDiscAmount()).isZero();
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
                assertThat(line.getPlanDiscAmount() - line.getSponsorDiscAmount()).isEqualTo(line.getPatientGrossAmount());
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() - line.getPatientTotalDiscAmount());
            } else if (line.getItemId() == 6) {
                assertThat(line.getPlanDiscAmount() - line.getSponsorDiscAmount()).isZero();
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount());
                assertThat(line.getSponsorDiscAmount()).isEqualTo(line.getSponsorGrossAmount());
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
            } else if (line.getItemId() == 7) {
                assertThat(line.getPlanDiscAmount()).isEqualTo(line.getPatientGrossAmount() + line.getSponsorGrossAmount());
                assertThat(line.getPatientNetAmount()).isZero();
                assertThat(line.getSponsorNetAmount()).isZero();
            } else if (line.getItemId() == 8) {
                assertThat(roundOff(line.getPlanDiscAmount() - line.getSponsorDiscAmount())).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.05f));
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getSponsorTaxAmount()).isZero();
                Float sponsorNetAmount = roundOff(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
                assertThat(line.getSponsorNetAmount()).isBetween(sponsorNetAmount - 0.01f, sponsorNetAmount + 0.01f);
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPatientTotalDiscAmount());
            } else if (line.getItemId() == 9) {
                assertThat(line.getPatientGrossAmount()).isEqualTo(roundOff(line.getGrossAmount() * 0.15f));
                assertThat(roundOff(line.getPlanDiscAmount() - line.getSponsorDiscAmount())).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.05f));
                assertThat(line.getUserDiscount()).isEqualTo(roundOff((line.getPatientGrossAmount() - line.getPlanDiscAmount() + line.getSponsorDiscAmount()) * 0.10f));
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getSponsorTaxAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getPatientTaxAmount()).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.10f));
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() + line.getSponsorTaxAmount() - line.getSponsorDiscAmount());
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPatientTotalDiscAmount());
            } else if (line.getItemId() == 100) {
                assertThat(roundOff(line.getPlanDiscAmount() - line.getSponsorDiscAmount())).isEqualTo(roundOff(line.getPatientGrossAmount() * 0.05f));
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getSponsorTaxAmount()).isZero();
                assertThat(line.getSponsorNetAmount()).isEqualTo(line.getSponsorGrossAmount() - line.getSponsorDiscAmount());
                assertThat(line.getPatientNetAmount()).isEqualTo(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPatientTotalDiscAmount());
                assertThat(line.getPatientNetAmount()).isZero();
            } else if (line.getItemId() == 101) {
                Float patientPlanDiscount = roundOff(line.getPatientGrossAmount() * 0.05f);
                assertThat(roundOff(line.getPlanDiscAmount() - line.getSponsorDiscAmount())).isBetween(roundOff(patientPlanDiscount - 0.01f), roundOff(patientPlanDiscount + 0.01f));
                assertThat(line.getSponsorDiscAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() * 0.10f));
                assertThat(line.getSponsorTaxAmount()).isZero();
                assertThat(line.getUserDiscount()).isEqualTo(roundOff((line.getPatientGrossAmount() - line.getPlanDiscAmount() + line.getSponsorDiscAmount()) * 0.10f));
                assertThat(line.getSponsorNetAmount()).isEqualTo(roundOff(line.getSponsorGrossAmount() - line.getSponsorDiscAmount()));
                assertThat(line.getPatientNetAmount()).isEqualTo(roundOff(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPatientTotalDiscAmount()));
            }
        });
        if (result.getDocument().getDispensePlans() != null) {
            result.getDocument().getDispensePlans().forEach(dispensePlan -> {
                if (dispensePlan.getPlanRef().getCode().equals("ATHMA-PLAN"))
                    assertThat(dispensePlan.getTotalTax()).isGreaterThan(0);
                if (dispensePlan.getPlanRef().getCode().equals("ATHMA-ALTERNATE-PLAN"))
                    assertThat(dispensePlan.getTotalTax()).isZero();
            });

        }
    }*/

    // @Test
    /*public void checkForDiscountAmountCalculation() throws Exception {
//        dispense.getDocument().setUnitDiscountPercentage(10f);
        dispense.getDocument().setPatientDiscount(getBigDecimal(250f));
        dispense.getDocument().setDiscountPercentage(false);
        dispense.getDocument().getDispenseDocumentLines().clear();
        dispense.getDocument().getDispensePlans().clear();
        DispenseDocumentLine line = new DispenseDocumentLine();
        line.setItemId(100l);
        line.setCode("ITEM-100");
        line.setQuantity(15f);
        line.setMrp(49.84f);
        dispense.getDocument().getDispenseDocumentLines().add(line);

        line = new DispenseDocumentLine();
        line.setItemId(101l);
        line.setCode("ITEM-101");
        line.setQuantity(1f);
        line.setMrp(49.88f);
        dispense.getDocument().getDispenseDocumentLines().add(line);

        MvcResult mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=ALL")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk())
            .andReturn();
        Dispense result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);
        assertThat(result.getDocument().getPatientDiscount()).isEqualTo(dispense.getDocument().getPatientDiscount());

        line = new DispenseDocumentLine();
        line.setItemId(999l);
        line.setCode("ITEM-999");
        line.setQuantity(10f);
        line.setMrp(100f);
        result.getDocument().getDispenseDocumentLines().add(line);

        mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=NEW-LINE&lineIndex=2")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(result)))
            .andExpect(status().isOk())
            .andReturn();
        result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);
        assertThat(result.getDocument().getPatientDiscount()).isEqualTo(roundOff(dispense.getDocument().getPatientDiscount()
            + (line.getQuantity() * line.getMrp() * result.getDocument().getUnitDiscountPercentage() * 0.01f)));

        result.getDocument().setPatientDiscount(dispense.getDocument().getPatientDiscount());
        mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=APPLY-DISCOUNT")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(result)))
            .andExpect(status().isOk())
            .andReturn();
        result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);
        assertThat(result.getDocument().getPatientDiscount()).isEqualTo(dispense.getDocument().getPatientDiscount());
        result.getDocument().getDispenseDocumentLines().forEach(line1 -> line1.setQuantity(line1.getQuantity() + 1));
        mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=APPLY-DISCOUNT")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(result)))
            .andExpect(status().isOk())
            .andReturn();
        result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);
        assertThat(result.getDocument().getPatientDiscount()).isEqualTo(dispense.getDocument().getPatientDiscount());

    }*/

    @Test
    public void checkForApplyPlan() throws Exception {
        createIndex();
        dispense.getDocument().getDispenseDocumentLines().clear();
        DispenseDocumentLine line = new DispenseDocumentLine();
        line.setItemId(100l);
        line.setCode("ITEM-100");
        line.setQuantity(15f);
        line.setMrp(getBigDecimal(49.84f));
        dispense.getDocument().getDispenseDocumentLines().add(line);

        line = new DispenseDocumentLine();
        line.setItemId(101l);
        line.setCode("ITEM-101");
        line.setQuantity(1f);
        line.setMrp(getBigDecimal(49.88f));
        dispense.getDocument().getDispenseDocumentLines().add(line);

        MvcResult mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=ALL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk())
            .andReturn();
        Dispense result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);

        assertThat(result.getDocument().getUnitDiscountAmount()).isZero();
        result.getDocument().getDispensePlans().clear();

        mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=APPLY-PLAN")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(result)))
            .andExpect(status().isOk())
            .andReturn();
        result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);

//        assertThat(result.getDocument().getUnitDiscountAmount()).isGreaterThan(0f);
        result.getDocument().getDispensePlans().addAll(dispense.getDocument().getDispensePlans());

        mvcResult = restDispenseMockMvc.perform(post("/api/_calculate/dispense?action=APPLY-PLAN")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(result)))
            .andExpect(status().isOk())
            .andReturn();
        result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Dispense.class);
        assertThat(result.getDocument().getUnitDiscountAmount()).isZero();

    }

    //     @Test TODO fix after complete dispense test cases
    public void createSponsorInvoice() throws Exception {

        //Dispense document
        dispense.document(this.populateDispenseDocument());
        stock = stockRepository.findAll().get(0);
        DispenseDocumentLine dispenseDocumentLine = dispense.getDocument().getDispenseDocumentLines().get(0);
        dispenseDocumentLine.setStockId(stock.getId());
        dispenseDocumentLine.setItemId(stock.getItemId());
        dispenseDocumentLine.setBatchNumber(stock.getBatchNo());
        Map dispenseMap = billingService.saveDispenseWithAction(dispense, null);
        dispense = (Dispense) dispenseMap.get("dispense");
        Map<String, Object> billingDocument = billingService.approveBillingProcess(dispense.getId());
    }

    @Test
    public void verifyWorkflow() throws Exception {
        pamIntegrationRepository.deleteAll();
        createIndex();

        dispense.setDocument(this.populateDispenseDocumentData());
        stock = stockRepository.findById(51l).get();
        DispenseDocumentLine dispenseDocumentLine = dispense.getDocument().getDispenseDocumentLines().get(0);
        dispenseDocumentLine.setStockId(stock.getId());
        dispenseDocumentLine.setItemId(stock.getItemId());
        dispenseDocumentLine.setBatchNumber(stock.getBatchNo());
        dispense.getDocument().setDiscountAmount(getBigDecimal(10f));
        dispense.getDocument().setUnitDiscountAmount(getBigDecimal(0f));
        dispense.getDocument().setPlanDiscountAmount(getBigDecimal(0f));
        dispense.getDocument().setUserDiscountAmount(getBigDecimal(5f));
        addUserToSecurityContext(4L, "90011Z", "creator");

        Map dispenseMap = billingService.saveDispenseWithAction(dispense, "DRAFT");
        dispense = (Dispense) dispenseMap.get("dispense");

        restDispenseMockMvc.perform(put("/api/dispenses?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        MvcResult result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=3&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        List<TaskInfo> taskList = objectMapper.readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        dispense = dispenseService.findOne(dispense.getId());

        result = restDispenseMockMvc.perform(get("/api/_workflow/dispenses?documentNumber=" + dispense.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        Map taskDetails = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });

        Integer taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restDispenseMockMvc.perform(put("/api/_workflow/dispenses?transition=Rejected&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk());

        addUserToSecurityContext(4L, "90011Z", "creator");

        dispense = dispenseService.findOne(dispense.getId());

        restDispenseMockMvc.perform(put("/api/dispenses?action=SENDFORAPPROVAL")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk());

        addUserToSecurityContext(3L, "90011X", "Approver");

        result = restWorkflowMockMvc.perform(get("/api/jbpm/task/group?unitId=3&page=0&size=20&sort=taskId,desc")).andExpect(status().isOk()).andReturn();
        taskList = objectMapper.readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, TaskInfo.class));

        restWorkflowMockMvc.perform(get("/api/jbpm/task/claim?taskId=" + taskList.get(0).getTaskId())).andExpect(status().isOk());

        dispense = dispenseService.findOne(dispense.getId());

        result = restDispenseMockMvc.perform(get("/api/_workflow/dispenses?documentNumber=" + dispense.getDocumentNumber() + "&userId=90011X"))
            .andExpect(status().isOk()).andReturn();
        taskDetails = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });

        taskId = (Integer) taskDetails.get("taskId");

        restWorkflowMockMvc.perform(get("/api/jbpm/task/start?taskId=" + taskId)).andExpect(status().isOk());

        restDispenseMockMvc.perform(put("/api/_workflow/dispenses?transition=Approved&taskId=" + taskId + "")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk());

        addUserToSecurityContext(4L, "90011Z", "creator");

        dispense = dispenseService.findOne(dispense.getId());

        restDispenseMockMvc.perform(put("/api/dispenses?action=CONFIRMED")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isOk());


    }

    private DispenseDocument populateDispenseDocumentData() {
        DispenseDocument dispenseDocument = new DispenseDocument();

        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(1l);
        hsc.setCode("hsc-1");
        hsc.setName("Healthcare service center HSR Layout");
        OrganizationDTO org = new OrganizationDTO();
        org.setId(11l);
        org.setCode("Org_1");
        org.setName("Organization Name");
        hsc.setPartOf(org);
        dispenseDocument.setHsc(hsc);

        UserDTO user = new UserDTO();
        user.setId(1001l);
        user.setDisplayName("User 1");
        user.setLogin("Admin");
        user.setEmployeeNo("335036");
        dispenseDocument.setDispenseUser(user);

        List<DispensePlan> dispensePlans = new ArrayList<>();
        //Document level Plan
        DispensePlan dispensePlan = new DispensePlan();
        PlanRuleDetail planRuleDetail = new PlanRuleDetail();
        planRuleDetail.setId(101l);
        planRuleDetail.setActive(true);
        GroupDTO group = new GroupDTO();
        group.setId(1l);
        group.setName("DRUG");
        group.setCode("123DRUG");
        PlanRuleComponent components = new PlanRuleComponent();
        components.setId(1l);
        components.setName("Crocin");
        components.setCode("crocin-1");
        components.setGroups(group);
        planRuleDetail.setComponent(components);
        planRuleDetail.setPatientCopayment(60.0f);
        planRuleDetail.setSponsorPayment(40.0f);
        planRuleDetail.setPlanRuleType(PlanRuleType.Item);

        //Base discount
        AppliedOnBasePatientSponsor baseDiscount = new AppliedOnBasePatientSponsor();
        baseDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        baseDiscount.setValue(2.0f);
        baseDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnBase(baseDiscount);

        //Sponsor discount
        AppliedOnBasePatientSponsor sponsorDiscount = new AppliedOnBasePatientSponsor();
        sponsorDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        sponsorDiscount.setValue(1.0f);
        sponsorDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnSponsorAmount(sponsorDiscount);

        //Patient discount
        AppliedOnBasePatientSponsor patientDiscount = new AppliedOnBasePatientSponsor();
        patientDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        patientDiscount.setValue(5.0f);
        patientDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnPatientAmount(patientDiscount);

        List<PlanRuleDetail> planRuleDetails = new ArrayList<>();
        planRuleDetails.add(planRuleDetail);
        PlanRules planRules = new PlanRules(planRuleDetails);

        PlanRule planRule = new PlanRule();
        planRule.setPlanRules(planRules);
        dispensePlan.setPlanRule(planRule);

        PlanRef planRef = new PlanRef();
        planRef.setCode("");
        dispensePlan.setPlanRef(planRef);
        dispensePlans.add(dispensePlan);
        dispenseDocument.setDispensePlans(dispensePlans);

        DispenseTax dispenseTax = new DispenseTax();
        dispenseTax.setTaxCode("GST");
        dispenseTax.setTaxAmount(getBigDecimal(140f));

        List<DispenseTax> dispenseTaxes = new ArrayList<>();
        dispenseTaxes.add(dispenseTax);
        dispenseDocument.setDispenseTaxes(dispenseTaxes);

        PatientDTO patient = new PatientDTO();
        patient.setId(1l);
        patient.setAge(45);
        patient.setGender("male");
        patient.setMrn("MRN-2001-00001");
        patient.setFullName("Patient User");
        dispenseDocument.setPatient(patient);

        //dispense unit
        OrganizationDTO unit = new OrganizationDTO();
        unit.setId(3l);
        unit.setCode("unit-1001");
        dispenseDocument.setDispenseUnit(unit);

        ConsultantDTO consultant = new ConsultantDTO();
        consultant.setId(3001l);
        consultant.setName("Dr Consultant");
        dispenseDocument.setConsultant(consultant);

        Map<String, Object> orderSource = new HashMap<>();
        orderSource.put("orderNo", "ORDER-2001-000001");
        dispenseDocument.setOrderSource(orderSource);

        SourceDTO source = new SourceDTO();
        source.setReferenceNumber("INV-2001-000001");
        dispenseDocument.setSource(source);
        //Payment details
        PaymentDetail paymentDetail = new PaymentDetail();

        paymentDetail.setBaseCurrency("INR");
        paymentDetail.setTransactionCurrency("USD");
        paymentDetail.setExchangeRate(getBigDecimal(65f));
        paymentDetail.setTotalAmount(getBigDecimal(100f));
        paymentDetail.setUnsettledAmount(getBigDecimal(100f));
        paymentDetail.setPaymentMode(PaymentMode.CARD);
        paymentDetail.setAdjustmentStatus(AdjustmentStatus.PARTIALLY_ADJUSTED);
        paymentDetail.setReceiptType(ReceiptType.VALID);
        paymentDetail.setPaymentMode(PaymentMode.CARD);
        paymentDetail.setHsc(hsc);

        UserDTO receivedBy = new UserDTO();
        receivedBy.setLogin("Admin");
        receivedBy.setId(1001l);
        receivedBy.setDisplayName("User 1");
        paymentDetail.setReceivedBy(receivedBy);

        UserDTO approvedBy = new UserDTO();
        approvedBy.setLogin("Approve User 1");
        approvedBy.setId(101l);
        paymentDetail.setApprovedBy(approvedBy);

        Map<String, Object> cardDetails = new HashMap<>();
        cardDetails.put("CardNo", "1234678912345678");
        cardDetails.put("expiryDate", "05/22");
        cardDetails.put("NomeOnCard", "Patient 1");

        Map<String, Object> machineDetails = new HashMap<>();
        machineDetails.put("machineId", "CITI0001MM");

        paymentDetail.setBankDetails(cardDetails);
        paymentDetail.setMachineDetails(machineDetails);
        paymentDetail.setReceivedBy(receivedBy);
        paymentDetail.setReceivedDate(LocalDateTime.now());
        paymentDetail.setApprovedDate(LocalDateTime.now());

        List<PaymentDetail> paymentDetails = new ArrayList<>();
        paymentDetails.add(paymentDetail);
        dispenseDocument.setPaymentDetails(paymentDetails);

        DispenseDocumentLine dispenseDocLine = new DispenseDocumentLine();
        //Order item
        OrderItem orderItem = new OrderItem();
        orderItem.setCode("orderitem-1");
        orderItem.setName("Crocin");
        orderItem.setDosageInstruction("twice in a day");
        orderItem.setQuantity(2f);
        orderItem.setUom("Strip");

        Map<String, Object> item = new HashMap();
        item.put("id", 11l);
        item.put("code", "item-1");
        orderItem.setItem(item);

        dispenseDocLine.setOrderItem(orderItem);
        //Dispense items
        List<DispenseItemPlan> dispenseItemPlans = new ArrayList<>();
        dispenseDocLine.setCode("orderitem-1");
        dispenseDocLine.setName("Crocin");
        dispenseDocLine.setQuantity(2f);
        dispenseDocLine.setBarCode("122234255523523523");
        dispenseDocLine.setBatchNumber("Batch-no-1");
        dispenseDocLine.setDispenseItemPlans(dispenseItemPlans);
        dispenseDocLine.setStockId(11l);
        dispenseDocLine.setItemId(1l);
        dispenseDocLine.setLineNumber(1l);
        dispenseDocLine.setMrp(getBigDecimal(100f));

        DispenseTax dispenseLineTax = new DispenseTax();
        dispenseLineTax.setTaxCode("GST");
        TaxDefinition taxDefinition = new TaxDefinition();
        taxDefinition.setId(1l);
        taxDefinition.setName("CGST");

        TaxCalculation taxCalculation = new TaxCalculation();
        taxCalculation.setType(Type.Percentage);
        taxCalculation.setPercentage(5.0f);
        taxDefinition.setTaxCalculation(taxCalculation);
        dispenseLineTax.setTaxDefinition(taxDefinition);

        List<DispenseTax> dispenseLineTaxes = new ArrayList<>();
        dispenseLineTaxes.add(dispenseLineTax);
        dispenseDocLine.setDispenseTaxes(dispenseLineTaxes);

        dispenseDocLine.setItemDiscount(true);
        dispenseDocLine.setPercentDiscount(true);
        dispenseDocLine.setUserDiscount(getBigDecimal(2f));
        dispenseDocLine.setTotalTaxInPercent(5.0f);
        dispenseDocLine.setUnitDiscount(getBigDecimal(2f));

        //Dispense document line
        List<DispenseDocumentLine> dispenseDocumentLines = new ArrayList<>();
        dispenseDocumentLines.add(dispenseDocLine);
        dispenseDocument.setDispenseDate(LocalDateTime.now());
        dispenseDocument.setDispenseDocumentLines(dispenseDocumentLines);
        dispenseDocument.setDispenseStatus(DispenseStatus.DRAFT);

        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setId(1l);
        organizationDTO.setCode("valuesetcode.json");
        organizationDTO.setName("valuesetcode.json");

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(1l);
        patientDTO.setMrn("ABCD123");
        patientDTO.setGender("Male");
        patientDTO.setAge(20);

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
        dispenseDocument.setEncounter(encounterDTO);

        return dispenseDocument;
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Dispense.class);
    }

    @Test
    public void generateSequenceNumber() throws Exception {
        restDispenseMockMvc.perform(get("/api/generate/transactionNumber"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Transactional
    public void createIPDispense() throws Exception {
        createIndex();
        int databaseSizeBeforeCreate = dispenseRepository.findAll().size();

        MedicationRequestDocument medicationRequestDocument = new MedicationRequestDocument();
        UserDTO createdBy = new UserDTO();
        createdBy.setDisplayName("Abc");
        createdBy.setLogin("abc");
        createdBy.setId(1L);
        createdBy.setMobileNo("999999999");
        medicationRequestDocument.setCreatedBy(createdBy);
        Map<String,Object> orderSource = new HashMap<>();
        orderSource.put("document",medicationRequestDocument);

        dispense.getDocument().setOrderSource(orderSource);
        // Create the Dispense

        dispense.getDocument().setDispenseDocumentLines(dispense.getDocument().getDispenseDocumentLines().subList(0,1));
        dispense.getDocument().getDispenseDocumentLines().get(0).setBatchNumber("BTH12345");
        dispense.getDocument().getDispenseDocumentLines().get(0).setStockId(51l);
        restDispenseMockMvc.perform(post("/api/ip-dispenses?action=dispensed")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isCreated());

        // Validate the Dispense in the database
        List<Dispense> dispenseList = dispenseRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseList).hasSize(databaseSizeBeforeCreate + 1);
        Dispense testDispense = dispenseList.get(dispenseList.size() - 1);

        assertThat(testDispense.getDocumentNumber()).isNotEmpty();
        assertThat(testDispense.getVersion()).isEqualTo(dispense.getVersion());
        assertThat(testDispense.isLatest()).isEqualTo(dispense.isLatest());
        assertThat(testDispense.getDocumentNumber()).startsWith("DISI");

        // Validate the Dispense in Elasticsearch
        Dispense dispenseEs = dispenseSearchRepository.findOne(testDispense.getId());
        assertThat(dispenseEs.getDocumentNumber()).isNotEmpty();
        assertThat(dispenseEs.getVersion()).isEqualTo(dispenseEs.getVersion());
        assertThat(dispenseEs.isLatest()).isEqualTo(dispenseEs.isLatest());
    }

    @Test
    @Transactional
    public void createIPDirectDispense() throws Exception {
        int databaseSizeBeforeCreate = dispenseRepository.findAll().size();

        // Create the Dispense

        dispense.getDocument().setDispenseType(DispenseType.DIRECT_ISSUE);
        restDispenseMockMvc.perform(post("/api/ip-direct-dispenses")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(dispense)))
            .andExpect(status().isCreated());

        // Validate the Dispense in the database
        List<Dispense> dispenseList = dispenseRepository.findAll(Sort.by("id", "version"));
        assertThat(dispenseList).hasSize(databaseSizeBeforeCreate + 1);
        Dispense testDispense = dispenseList.get(dispenseList.size() - 1);

        assertThat(testDispense.getDocumentNumber()).isNotEmpty();
        assertThat(testDispense.getVersion()).isEqualTo(dispense.getVersion());
        assertThat(testDispense.isLatest()).isEqualTo(dispense.isLatest());
        assertThat(testDispense.getDocumentNumber()).startsWith("DCI");

        // Validate the Dispense in Elasticsearch
        Dispense dispenseEs = dispenseSearchRepository.findOne(testDispense.getId());
        assertThat(dispenseEs.getDocumentNumber()).isNotEmpty();
        assertThat(dispenseEs.getVersion()).isEqualTo(dispenseEs.getVersion());
        assertThat(dispenseEs.isLatest()).isEqualTo(dispenseEs.isLatest());
    }


    @Test
    @Transactional
    public void exportIPDispenses() throws Exception {

        createIndex();
        UserDTO dispeningUser = new UserDTO();
        dispeningUser.setDisplayName("DispensingUser");

        HealthcareServiceCenterDTO dispensingHsc = new HealthcareServiceCenterDTO();
        dispensingHsc.setName("DispensingHsc");

        HealthcareServiceCenterDTO orderingHsc = new HealthcareServiceCenterDTO();
        orderingHsc.setName("orderingHsc");

        Map<String,Object> orderSource = new HashMap<>();
        orderSource.put("documentNumber","MED-001");
        orderSource.put("orderedDate",LocalDateTime.now());
        MedicationRequestDocument medicationRequestDocument = new MedicationRequestDocument();
        medicationRequestDocument.setCreatedDate(LocalDateTime.now());
        orderSource.put("document",medicationRequestDocument);

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setDisplayName("patient-001");
        patientDTO.setFullName("patient-001");

        List<DispensePlan> dispensePlans = new ArrayList<>();

        DispensePlan dispensePlan = new DispensePlan();
        OrganizationDTO sponsorRef =new OrganizationDTO();
        sponsorRef.setName("sponso1");
        dispensePlan.setSponsorRef(sponsorRef);


        DispensePlan dispensePlan2 = new DispensePlan();
        OrganizationDTO sponsorRef2 =new OrganizationDTO();
        sponsorRef2.setName("sponsor2");
        dispensePlan2.setSponsorRef(sponsorRef2);

        dispensePlans.add(dispensePlan);
        dispensePlans.add(dispensePlan2);

        ConsultantDTO consultantDTO = new ConsultantDTO();
        consultantDTO.setName("consultant");
        consultantDTO.setDisplayName("Consultant");

        dispense.getDocument().setConsultant(consultantDTO);
        dispense.getDocument().setOrderingHSC(orderingHsc);
        dispense.getDocument().setHsc(dispensingHsc);
        dispense.getDocument().setPatient(patientDTO);
        dispense.getDocument().setOrderSource(orderSource);
        dispense.getDocument().setDispenseUser(dispeningUser);
        dispense.getDocument().setDispenseDate(LocalDateTime.now());
        dispense.getDocument().setDispensePlans(dispensePlans);
        dispense.getDocument().setConsultant(consultantDTO);
        dispenseService.save(dispense);


        //Response is OutputStream. Not able to validate response,So validating status and content type.
        restDispenseMockMvc.perform(get("/api/_export/ip-dispenses?query=*"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
