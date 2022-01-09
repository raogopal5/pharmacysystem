package org.nh.pharmacy.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.common.dto.ValueSetCodeDTO;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.repository.HealthcareServiceCenterRepository;
import org.nh.pharmacy.repository.search.HealthcareServiceCenterSearchRepository;
import org.nh.pharmacy.service.HealthcareServiceCenterService;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the HealthcareServiceCenterResource REST controller.
 *
 * @see HealthcareServiceCenterResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class HealthcareServiceCenterResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String DEFAULT_LICENSE_NUMBER = "AAAAAAAAAA";
    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final Boolean DEFAULT_ACTIVE = false;
    private static final List<AvailableTime> DEFAULT_AVAILABLE_TIME = Arrays.asList(new AvailableTime("MON", "", "", false));
    private static final List<NotAvailableTime> DEFAULT_NOT_AVAILABLE_TIME = Arrays.asList(new NotAvailableTime(new Date(), null, "Out of station"));
    private static final Boolean DEFAULT_APPOINTMENT_REQUIRED = false;
    private static final String DEFAULT_COMMENTS = "AAAAAAAAAA";
    private static final ValueSetCode DEFAULT_SERVICE_CATEGORY = new ValueSetCode(1l, "ServiceCategoryCode", "Service Category Code", true);
    private static final List<ValueSetCodeDTO> DEFAULT_SUB_CATEGORY = Arrays.asList(new ValueSetCodeDTO(1l, "SubCategoryCode", "Sub Category Code"));

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    @Autowired
    private HealthcareServiceCenterService healthcareServiceCenterService;

    @Autowired
    private HealthcareServiceCenterSearchRepository healthcareServiceCenterSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private MockMvc restHealthcareServiceCenterMockMvc;

    private HealthcareServiceCenter healthcareServiceCenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        HealthcareServiceCenterResource healthcareServiceCenterResource = new HealthcareServiceCenterResource(healthcareServiceCenterService);
        this.restHealthcareServiceCenterMockMvc = MockMvcBuilders.standaloneSetup(healthcareServiceCenterResource)
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
    public static HealthcareServiceCenter createEntity(EntityManager em) {
        HealthcareServiceCenter healthcareServiceCenter = new HealthcareServiceCenter()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .active(DEFAULT_ACTIVE)
            .availableTime(DEFAULT_AVAILABLE_TIME)
            .notAvailableTime(DEFAULT_NOT_AVAILABLE_TIME)
            .appointmentRequired(DEFAULT_APPOINTMENT_REQUIRED)
            .serviceCategory(DEFAULT_SERVICE_CATEGORY)
            .capabilities(DEFAULT_SUB_CATEGORY)
            .comments(DEFAULT_COMMENTS);
        healthcareServiceCenter.setLicenseNumber(DEFAULT_LICENSE_NUMBER);
        healthcareServiceCenter.setId(RandomNumber.getRandomNumber());
        healthcareServiceCenter.setDisplayName(DEFAULT_DISPLAY_NAME);
        // Add required entity
        Organization partOf = OrganizationResourceIntTest.createEntityIfNotExist(em);
        healthcareServiceCenter.setPartOf(partOf);
        healthcareServiceCenter.getTelecom().add(getTelecom());
        // Add required entity
        Location location = LocationResourceIntTest.createEntity(em);
        em.persist(location);
        em.flush();
        healthcareServiceCenter.setLocation(location);
        return healthcareServiceCenter;
    }

    public static HealthcareServiceCenter createEntityIfNotExist(EntityManager em) {
        List<HealthcareServiceCenter> healthcareServiceCenters = em.createQuery("from " + HealthcareServiceCenter.class.getName()).getResultList();
        HealthcareServiceCenter healthcareServiceCenter = null;
        if (healthcareServiceCenters != null && !healthcareServiceCenters.isEmpty()) {
            healthcareServiceCenter = healthcareServiceCenters.get(0);
        } else {
            healthcareServiceCenter = createEntity(em);
            em.persist(healthcareServiceCenter);
            em.flush();
            healthcareServiceCenter = (HealthcareServiceCenter) em.createQuery("from " + HealthcareServiceCenter.class.getName()).getResultList().get(0);
        }
        return healthcareServiceCenter;
    }

    private static Map<String, Object> getTelecom() {

        Map<String, Object> telecom = new HashMap();
        telecom.put("Mobile", "88888800000");
        return telecom;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(HealthcareServiceCenter.class))
            elasticsearchTemplate.createIndex(HealthcareServiceCenter.class);
        healthcareServiceCenterSearchRepository.deleteAll();
        healthcareServiceCenter = createEntity(em);
    }

    @Test
    @Transactional
    public void createHealthcareServiceCenter() throws Exception {
        int databaseSizeBeforeCreate = healthcareServiceCenterRepository.findAll().size();

        // Create the HealthcareServiceCenter
        healthcareServiceCenterService.save(healthcareServiceCenter);
        // Validate the HealthcareServiceCenter in the database
        List<HealthcareServiceCenter> healthcareServiceCenterList = healthcareServiceCenterRepository.findAll();
        assertThat(healthcareServiceCenterList).hasSize(databaseSizeBeforeCreate + 1);
        HealthcareServiceCenter testHealthcareServiceCenter = healthcareServiceCenterList.get(healthcareServiceCenterList.size() - 1);
        assertThat(testHealthcareServiceCenter.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testHealthcareServiceCenter.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testHealthcareServiceCenter.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testHealthcareServiceCenter.getLicenseNumber()).isEqualTo(DEFAULT_LICENSE_NUMBER);

        List<AvailableTime> availableTimeList = mapper.readValue(
            mapper.writeValueAsString(testHealthcareServiceCenter.getAvailableTime()), new TypeReference<List<AvailableTime>>() {
            });
        List<NotAvailableTime> notAvailableTimeList = mapper.readValue(
            mapper.writeValueAsString(testHealthcareServiceCenter.getNotAvailableTime()), new TypeReference<List<NotAvailableTime>>() {
            });

        testHealthcareServiceCenter.setAvailableTime(availableTimeList);
        testHealthcareServiceCenter.setNotAvailableTime(notAvailableTimeList);

        assertThat(testHealthcareServiceCenter.getAvailableTime().get(0).getDayOfWeek()).isEqualTo(DEFAULT_AVAILABLE_TIME.get(0).getDayOfWeek());
        assertThat(testHealthcareServiceCenter.getNotAvailableTime().get(0).getDescription()).isEqualTo(DEFAULT_NOT_AVAILABLE_TIME.get(0).getDescription());
        assertThat(testHealthcareServiceCenter.getServiceCategory().getCode()).isEqualTo(DEFAULT_SERVICE_CATEGORY.getCode());
        assertThat(testHealthcareServiceCenter.getCapabilities().get(0).getCode()).isEqualTo(DEFAULT_SUB_CATEGORY.get(0).getCode());
        assertThat(testHealthcareServiceCenter.getTelecom().size()).isEqualTo(healthcareServiceCenter.getTelecom().size());
        assertThat(testHealthcareServiceCenter.isAppointmentRequired()).isEqualTo(DEFAULT_APPOINTMENT_REQUIRED);
        assertThat(testHealthcareServiceCenter.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
        assertThat(testHealthcareServiceCenter.getComments()).isEqualTo(DEFAULT_COMMENTS);

        // Validate the HealthcareServiceCenter in Elasticsearch
        HealthcareServiceCenter healthcareServiceCenterEs = healthcareServiceCenterSearchRepository.findById(testHealthcareServiceCenter.getId()).get();
        assertThat(healthcareServiceCenterEs).isEqualToComparingFieldByField(testHealthcareServiceCenter);
    }

    @Test
    @Transactional
    public void getAllHealthcareServiceCenters() throws Exception {
        // Initialize the database
        healthcareServiceCenterRepository.saveAndFlush(healthcareServiceCenter);

        // Get all the healthcareServiceCenterList
        restHealthcareServiceCenterMockMvc.perform(get("/api/healthcare-service-centers?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(healthcareServiceCenter.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].availableTime.[*].dayOfWeek").value(hasItem(DEFAULT_AVAILABLE_TIME.get(0).getDayOfWeek())))
            .andExpect(jsonPath("$.[*].notAvailableTime.[*].description").value(hasItem(DEFAULT_NOT_AVAILABLE_TIME.get(0).getDescription())))
            .andExpect(jsonPath("$.[*].serviceCategory.code").value(hasItem(DEFAULT_SERVICE_CATEGORY.getCode())))
            .andExpect(jsonPath("$.[*].capabilities.[*].code").value(hasItem(DEFAULT_SUB_CATEGORY.get(0).getCode())))
            .andExpect(jsonPath("$.[*].telecom", hasSize(healthcareServiceCenter.getTelecom().size())))
            .andExpect(jsonPath("$.[*].appointmentRequired").value(hasItem(DEFAULT_APPOINTMENT_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME.toString())))
            .andExpect(jsonPath("$.[*].comments").value(hasItem(DEFAULT_COMMENTS.toString())));
    }

    @Test
    @Transactional
    public void getHealthcareServiceCenter() throws Exception {
        // Initialize the database
        healthcareServiceCenterRepository.saveAndFlush(healthcareServiceCenter);

        // Get the healthcareServiceCenter
        restHealthcareServiceCenterMockMvc.perform(get("/api/healthcare-service-centers/{id}", healthcareServiceCenter.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(healthcareServiceCenter.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.availableTime.[*].dayOfWeek").value(hasItem(DEFAULT_AVAILABLE_TIME.get(0).getDayOfWeek())))
            .andExpect(jsonPath("$.notAvailableTime.[*].description").value(hasItem(DEFAULT_NOT_AVAILABLE_TIME.get(0).getDescription())))
            .andExpect(jsonPath("$.serviceCategory.code").value(DEFAULT_SERVICE_CATEGORY.getCode()))
            .andExpect(jsonPath("$.capabilities.[*].code").value(DEFAULT_SUB_CATEGORY.get(0).getCode()))
            .andExpect(jsonPath("$.telecom", hasSize(healthcareServiceCenter.getTelecom().size())))
            .andExpect(jsonPath("$.appointmentRequired").value(DEFAULT_APPOINTMENT_REQUIRED.booleanValue()))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME.toString()))
            .andExpect(jsonPath("$.comments").value(DEFAULT_COMMENTS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingHealthcareServiceCenter() throws Exception {
        // Get the healthcareServiceCenter
        restHealthcareServiceCenterMockMvc.perform(get("/api/healthcare-service-centers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchHealthcareServiceCenter() throws Exception {
        // Initialize the database
        healthcareServiceCenterService.save(healthcareServiceCenter);

        // Search the healthcareServiceCenter
        restHealthcareServiceCenterMockMvc.perform(get("/api/_search/healthcare-service-centers?query=id:" + healthcareServiceCenter.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(healthcareServiceCenter.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].availableTime.[*].dayOfWeek").value(hasItem(DEFAULT_AVAILABLE_TIME.get(0).getDayOfWeek())))
            .andExpect(jsonPath("$.[*].notAvailableTime.[*].description").value(hasItem(DEFAULT_NOT_AVAILABLE_TIME.get(0).getDescription())))
            .andExpect(jsonPath("$.[*].appointmentRequired").value(hasItem(DEFAULT_APPOINTMENT_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME.toString())))
            .andExpect(jsonPath("$.[*].comments").value(hasItem(DEFAULT_COMMENTS.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HealthcareServiceCenter.class);
    }
}
