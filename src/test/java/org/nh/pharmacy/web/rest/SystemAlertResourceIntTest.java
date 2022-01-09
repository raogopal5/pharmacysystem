package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.SystemAlertRepository;
import org.nh.pharmacy.service.SystemAlertService;
import org.nh.pharmacy.web.rest.errors.ExceptionTranslator;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.pharmacy.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the SystemAlertResource REST controller.
 *
 * @see SystemAlertResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class SystemAlertResourceIntTest {

    private static final String DEFAULT_FROM_CLASS = "AAAAAAAAAA";
    private static final String UPDATED_FROM_CLASS = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_ON_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_ON_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private SystemAlertRepository systemAlertRepository;

    @Autowired
    private SystemAlertService systemAlertService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSystemAlertMockMvc;

    private SystemAlert systemAlert;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        SystemAlertResource systemAlertResource = new SystemAlertResource(systemAlertService);
        this.restSystemAlertMockMvc = MockMvcBuilders.standaloneSetup(systemAlertResource)
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
    public static SystemAlert createEntity(EntityManager em) {
        SystemAlert systemAlert = new SystemAlert()
            .fromClass(DEFAULT_FROM_CLASS)
            .onDate(DEFAULT_ON_DATE)
            .message(DEFAULT_MESSAGE)
            .description(DEFAULT_DESCRIPTION);
        return systemAlert;
    }

    @Before
    public void initTest() {
        systemAlert = createEntity(em);
    }

    @Test
    @Transactional
    public void createSystemAlert() throws Exception {
        int databaseSizeBeforeCreate = systemAlertRepository.findAll().size();

        // Create the SystemAlert

        restSystemAlertMockMvc.perform(post("/api/system-alerts")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(systemAlert)))
            .andExpect(status().isCreated());

        // Validate the SystemAlert in the database
        List<SystemAlert> systemAlertList = systemAlertRepository.findAll();
        assertThat(systemAlertList).hasSize(databaseSizeBeforeCreate + 1);
        SystemAlert testSystemAlert = systemAlertList.get(systemAlertList.size() - 1);
        assertThat(testSystemAlert.getFromClass()).isEqualTo(DEFAULT_FROM_CLASS);
        assertThat(testSystemAlert.getOnDate()).isEqualTo(DEFAULT_ON_DATE);
        assertThat(testSystemAlert.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testSystemAlert.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);

    }


    @Test
    @Transactional
    public void getAllSystemAlerts() throws Exception {
        // Initialize the database
        systemAlertRepository.saveAndFlush(systemAlert);

        // Get all the systemAlertList
        restSystemAlertMockMvc.perform(get("/api/system-alerts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(systemAlert.getId().intValue())))
            .andExpect(jsonPath("$.[*].fromClass").value(hasItem(DEFAULT_FROM_CLASS.toString())))
            .andExpect(jsonPath("$.[*].onDate").value(hasItem(sameInstant(DEFAULT_ON_DATE))))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    public void getSystemAlert() throws Exception {
        // Initialize the database
        systemAlertRepository.saveAndFlush(systemAlert);

        // Get the systemAlert
        restSystemAlertMockMvc.perform(get("/api/system-alerts/{id}", systemAlert.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(systemAlert.getId().intValue()))
            .andExpect(jsonPath("$.fromClass").value(DEFAULT_FROM_CLASS.toString()))
            .andExpect(jsonPath("$.onDate").value(sameInstant(DEFAULT_ON_DATE)))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSystemAlert() throws Exception {
        // Get the systemAlert
        restSystemAlertMockMvc.perform(get("/api/system-alerts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSystemAlert() throws Exception {
        // Initialize the database
        systemAlertService.save(systemAlert);

        int databaseSizeBeforeUpdate = systemAlertRepository.findAll().size();

        // Update the systemAlert
        SystemAlert updatedSystemAlert = systemAlertRepository.findById(systemAlert.getId()).get();
        updatedSystemAlert
            .fromClass(UPDATED_FROM_CLASS)
            .onDate(UPDATED_ON_DATE)
            .message(UPDATED_MESSAGE)
            .description(UPDATED_DESCRIPTION);

        restSystemAlertMockMvc.perform(put("/api/system-alerts")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedSystemAlert)))
            .andExpect(status().isOk());

        // Validate the SystemAlert in the database
        List<SystemAlert> systemAlertList = systemAlertRepository.findAll();
        assertThat(systemAlertList).hasSize(databaseSizeBeforeUpdate);
        SystemAlert testSystemAlert = systemAlertList.get(systemAlertList.size() - 1);
        assertThat(testSystemAlert.getFromClass()).isEqualTo(UPDATED_FROM_CLASS);
        assertThat(testSystemAlert.getOnDate()).isEqualTo(UPDATED_ON_DATE);
        assertThat(testSystemAlert.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testSystemAlert.getDescription()).isEqualTo(UPDATED_DESCRIPTION);

    }

    @Test
    @Transactional
    public void updateNonExistingSystemAlert() throws Exception {
        int databaseSizeBeforeUpdate = systemAlertRepository.findAll().size();

        // Create the SystemAlert

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSystemAlertMockMvc.perform(put("/api/system-alerts")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(systemAlert)))
            .andExpect(status().isCreated());

        // Validate the SystemAlert in the database
        List<SystemAlert> systemAlertList = systemAlertRepository.findAll();
        assertThat(systemAlertList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSystemAlert() throws Exception {
        // Initialize the database
        systemAlertService.save(systemAlert);

        int databaseSizeBeforeDelete = systemAlertRepository.findAll().size();

        // Get the systemAlert
        restSystemAlertMockMvc.perform(delete("/api/system-alerts/{id}", systemAlert.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SystemAlert> systemAlertList = systemAlertRepository.findAll();
        assertThat(systemAlertList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SystemAlert.class);
    }
}
