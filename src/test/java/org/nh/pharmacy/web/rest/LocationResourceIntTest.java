package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Location;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.repository.LocationRepository;
import org.nh.pharmacy.repository.search.LocationSearchRepository;
import org.nh.pharmacy.service.LocationService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the LocationResource REST controller.
 *
 * @see LocationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class LocationResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final Boolean DEFAULT_ACTIVE = false;
    private static final ValueSetCode DEFAULT_LOCATION_STATUS = new ValueSetCode(1l, "LocationStatusCode", "Location status", true);
    private static final ValueSetCode DEFAULT_LOCATION_MODE = new ValueSetCode(1l, "LocationModeCode", "Location Mode", true);

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationSearchRepository locationSearchRepository;

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

    private MockMvc restLocationMockMvc;

    private Location location;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LocationResource locationResource = new LocationResource(locationService);
        this.restLocationMockMvc = MockMvcBuilders.standaloneSetup(locationResource)
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
    public static Location createEntity(EntityManager em) {
        Location location = new Location()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .active(DEFAULT_ACTIVE);
        location.setId(RandomNumber.getRandomNumber());
        location.setStatus(DEFAULT_LOCATION_STATUS);
        location.setMode(DEFAULT_LOCATION_MODE);
        location.getAddresses().add(getAddress());
        location.getTelecoms().add(getTelecom());
        return location;
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


    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(Location.class))
            elasticsearchTemplate.createIndex(Location.class);
        locationSearchRepository.deleteAll();
        location = createEntity(em);
    }

    @Test
    @Transactional
    public void createLocation() throws Exception {
        int databaseSizeBeforeCreate = locationRepository.findAll().size();

        // Create the Location
        locationService.save(location);

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeCreate + 1);
        Location testLocation = locationList.get(locationList.size() - 1);
        assertThat(testLocation.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testLocation.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testLocation.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testLocation.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testLocation.getStatus().getCode()).isEqualTo(DEFAULT_LOCATION_STATUS.getCode());
        assertThat(testLocation.getMode().getCode()).isEqualTo(DEFAULT_LOCATION_MODE.getCode());
        assertThat(testLocation.getAddresses().size()).isEqualTo(location.getAddresses().size());
        assertThat(testLocation.getTelecoms().size()).isEqualTo(location.getTelecoms().size());
        // Validate the Location in Elasticsearch
        Location locationEs = locationSearchRepository.findById(testLocation.getId()).get();
        assertThat(locationEs).isEqualToComparingFieldByField(testLocation);
    }

    @Test
    @Transactional
    public void getAllLocations() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);

        // Get all the locationList
        restLocationMockMvc.perform(get("/api/locations?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].status.code").value(hasItem(DEFAULT_LOCATION_STATUS.getCode())))
            .andExpect(jsonPath("$.[*].mode.code").value(hasItem(DEFAULT_LOCATION_MODE.getCode())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].telecoms", hasSize(location.getTelecoms().size())))
            .andExpect(jsonPath("$.[*].addresses", hasSize(location.getAddresses().size())));
    }

    @Test
    @Transactional
    public void getLocation() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);

        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(location.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.status.code").value(DEFAULT_LOCATION_STATUS.getCode()))
            .andExpect(jsonPath("$.mode.code").value(DEFAULT_LOCATION_MODE.getCode()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.telecoms", hasSize(location.getTelecoms().size())))
            .andExpect(jsonPath("$.addresses", hasSize(location.getAddresses().size())));
    }

    @Test
    @Transactional
    public void getNonExistingLocation() throws Exception {
        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchLocation() throws Exception {
        // Initialize the database
        locationService.save(location);

        // Search the location
        restLocationMockMvc.perform(get("/api/_search/locations?query=id:" + location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].status.code").value(hasItem(DEFAULT_LOCATION_STATUS.getCode())))
            .andExpect(jsonPath("$.[*].mode.code").value(hasItem(DEFAULT_LOCATION_MODE.getCode())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].telecoms", hasSize(location.getTelecoms().size())))
            .andExpect(jsonPath("$.[*].addresses", hasSize(location.getAddresses().size())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Location.class);
    }
}
