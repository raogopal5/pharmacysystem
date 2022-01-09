package org.nh.pharmacy.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.dto.OrganizationIdentifier;
import org.nh.pharmacy.repository.OrganizationRepository;
import org.nh.pharmacy.repository.search.OrganizationSearchRepository;
import org.nh.pharmacy.service.OrganizationService;
import org.nh.pharmacy.util.RandomNumber;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the OrganizationResource REST controller.
 *
 * @see OrganizationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class OrganizationResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ACTIVE = false;

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";

    private static final ValueSetCode DEFAULT_ORGANIZATION_TYPE = new ValueSetCode(1l, "OrganizationTypeCode", "Organization Type Code", true);

    private static final LocalDate DEFAULT_STARTED_ON = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_STARTED_ON = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_CLINICAL = false;
    private static final Boolean UPDATED_CLINICAL = true;

    private static final String DEFAULT_LICENSE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_LICENSE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_WEBSITE = "AAAAAAAAAA";
    private static final String UPDATED_WEBSITE = "BBBBBBBBBB";

    private static final String DEFAULT_IDENTIFIER_TYPE = "AAAAAAAAAA";

    private static final Date DEFAULT_VALID_FROM = new Date();

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationSearchRepository organizationSearchRepository;

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

    private MockMvc restOrganizationMockMvc;

    private Organization organization;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        OrganizationResource organizationResource = new OrganizationResource(organizationService);
        this.restOrganizationMockMvc = MockMvcBuilders.standaloneSetup(organizationResource)
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
    public static Organization createEntity(EntityManager em) {
        Organization organization = new Organization()
            .name(DEFAULT_NAME)
            .active(DEFAULT_ACTIVE)
            .code(DEFAULT_CODE)
            .startedOn(DEFAULT_STARTED_ON)
            .clinical(DEFAULT_CLINICAL)
            .licenseNumber(DEFAULT_LICENSE_NUMBER)
            .website(DEFAULT_WEBSITE);
        organization.setId(RandomNumber.getRandomNumber());
        organization.setType(DEFAULT_ORGANIZATION_TYPE);
        organization.getAddresses().add(getAddress());
        organization.getTelecoms().add(getTelecom());
        organization.getContacts().add(getContact());
        organization.setDisplayName(DEFAULT_DISPLAY_NAME);
        OrganizationIdentifier organizationIdentifier = new OrganizationIdentifier();
        organizationIdentifier.setType(DEFAULT_IDENTIFIER_TYPE);
        organizationIdentifier.setValidFrom(DEFAULT_VALID_FROM);
        List<OrganizationIdentifier> organizationIdentifierList = new ArrayList<>();
        organizationIdentifierList.add(organizationIdentifier);
        organization.setIdentifier(organizationIdentifierList);
        return organization;
    }

    private static Map<String, Object> getAddress() {

        Map<String, Object> address = new HashMap();
        address.put("Address", "New Address");
        return address;
    }

    private static Map<String, Object> getTelecom() {

        Map<String, Object> telecom = new HashMap();
        telecom.put("Mobile", "88888800000");
        return telecom;
    }

    private static Map<String, Object> getContact() {

        Map<String, Object> contact = new HashMap();
        contact.put("Email", "email@email.com");
        return contact;
    }

    public static Organization createEntityIfNotExist(EntityManager em) {
        List<Organization> organizations = em.createQuery("from " + Organization.class.getName()).getResultList();
        Organization organization = null;
        if (organizations != null && !organizations.isEmpty()) {
            organization = organizations.get(0);
        } else {
            organization = createEntity(em);
            em.persist(organization);
            em.flush();
            organization = (Organization) em.createQuery("from " + Organization.class.getName()).getResultList().get(0);
        }
        return organization;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(Organization.class))
            elasticsearchTemplate.createIndex(Organization.class);
        organizationSearchRepository.deleteAll();
        organization = createEntity(em);
    }

    @Test
    @Transactional
    public void createOrganization() throws Exception {
        int databaseSizeBeforeCreate = organizationRepository.findAll().size();

        // Create the Organization
        organizationService.save(organization);

        // Validate the Organization in the database
        List<Organization> organizationList = organizationRepository.findAll();
        assertThat(organizationList).hasSize(databaseSizeBeforeCreate + 1);
        Organization testOrganization = organizationList.get(organizationList.size() - 1);
        assertThat(testOrganization.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testOrganization.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testOrganization.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testOrganization.getType().getCode()).isEqualTo(DEFAULT_ORGANIZATION_TYPE.getCode());
        assertThat(testOrganization.getTelecoms().size()).isEqualTo(testOrganization.getTelecoms().size());
        assertThat(testOrganization.getAddresses().size()).isEqualTo(testOrganization.getAddresses().size());
        assertThat(testOrganization.getStartedOn()).isEqualTo(DEFAULT_STARTED_ON);
        assertThat(testOrganization.isClinical()).isEqualTo(DEFAULT_CLINICAL);
        assertThat(testOrganization.getLicenseNumber()).isEqualTo(DEFAULT_LICENSE_NUMBER);
        assertThat(testOrganization.getWebsite()).isEqualTo(DEFAULT_WEBSITE);
        assertThat(testOrganization.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        List<OrganizationIdentifier> identifierList = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(testOrganization.getIdentifier()), TypeFactory.defaultInstance().constructCollectionType(List.class, OrganizationIdentifier.class));
        assertThat(identifierList.get(0).getType()).isEqualTo(DEFAULT_IDENTIFIER_TYPE);
        assertThat(identifierList.get(0).getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);


        // Validate the Organization in Elasticsearch
        Organization organizationEs = organizationSearchRepository.findById(testOrganization.getId()).get();
        assertThat(organizationEs.getId()).isEqualToComparingFieldByField(testOrganization.getId());
    }

    @Test
    @Transactional
    public void getAllOrganizations() throws Exception {
        // Initialize the database
        organizationRepository.saveAndFlush(organization);

        // Get all the organizationList
        restOrganizationMockMvc.perform(get("/api/organizations?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organization.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].telecoms", hasSize(organization.getTelecoms().size())))
            .andExpect(jsonPath("$.[*].addresses", hasSize(organization.getAddresses().size())))
            .andExpect(jsonPath("$.[*].type.code").value(hasItem(DEFAULT_ORGANIZATION_TYPE.getCode())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].startedOn").value(hasItem(DEFAULT_STARTED_ON.toString())))
            .andExpect(jsonPath("$.[*].clinical").value(hasItem(DEFAULT_CLINICAL.booleanValue())))
            .andExpect(jsonPath("$.[*].licenseNumber").value(hasItem(DEFAULT_LICENSE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME.toString())))
            .andExpect(jsonPath("$.[*].website").value(hasItem(DEFAULT_WEBSITE.toString())))
        ;

    }

    @Test
    @Transactional
    public void getOrganization() throws Exception {
        // Initialize the database
        organizationRepository.saveAndFlush(organization);

        // Get the organization
        restOrganizationMockMvc.perform(get("/api/organizations/{id}", organization.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(organization.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.telecoms", hasSize(organization.getTelecoms().size())))
            .andExpect(jsonPath("$.addresses", hasSize(organization.getAddresses().size())))
            .andExpect(jsonPath("$.type.code").value(DEFAULT_ORGANIZATION_TYPE.getCode()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.startedOn").value(DEFAULT_STARTED_ON.toString()))
            .andExpect(jsonPath("$.clinical").value(DEFAULT_CLINICAL.booleanValue()))
            .andExpect(jsonPath("$.licenseNumber").value(DEFAULT_LICENSE_NUMBER.toString()))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME.toString()))
            .andExpect(jsonPath("$.website").value(DEFAULT_WEBSITE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingOrganization() throws Exception {
        // Get the organization
        restOrganizationMockMvc.perform(get("/api/organizations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchOrganization() throws Exception {
        // Initialize the database
        organizationService.save(organization);

        // Search the organization
        restOrganizationMockMvc.perform(get("/api/_search/organizations?query=id:" + organization.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organization.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].telecoms", hasSize(organization.getTelecoms().size())))
            .andExpect(jsonPath("$.[*].addresses", hasSize(organization.getAddresses().size())))
            .andExpect(jsonPath("$.[*].type.code").value(hasItem(DEFAULT_ORGANIZATION_TYPE.getCode())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].startedOn").value(hasItem(DEFAULT_STARTED_ON.toString())))
            .andExpect(jsonPath("$.[*].clinical").value(hasItem(DEFAULT_CLINICAL.booleanValue())))
            .andExpect(jsonPath("$.[*].licenseNumber").value(hasItem(DEFAULT_LICENSE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME.toString())))
            .andExpect(jsonPath("$.[*].website").value(hasItem(DEFAULT_WEBSITE.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Organization.class);
    }
}
