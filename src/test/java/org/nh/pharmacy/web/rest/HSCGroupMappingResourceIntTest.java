package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Group;
import org.nh.pharmacy.domain.HSCGroupMapping;
import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.nh.pharmacy.repository.HSCGroupMappingRepository;
import org.nh.pharmacy.repository.search.HSCGroupMappingSearchRepository;
import org.nh.pharmacy.service.HSCGroupMappingService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the HSCGroupMappingResource REST controller.
 *
 * @see HSCGroupMappingResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class HSCGroupMappingResourceIntTest {

    private static final LocalDate DEFAULT_EFFECTIVE_FROM = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EFFECTIVE_FROM = LocalDate.now(ZoneId.systemDefault());

    @Autowired
    private HSCGroupMappingRepository hSCGroupMappingRepository;

    @Autowired
    private HSCGroupMappingService hSCGroupMappingService;

    @Autowired
    private HSCGroupMappingSearchRepository hSCGroupMappingSearchRepository;

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

    private MockMvc restHSCGroupMappingMockMvc;

    private HSCGroupMapping hSCGroupMapping;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        HSCGroupMappingResource hSCGroupMappingResource = new HSCGroupMappingResource(hSCGroupMappingService);
        this.restHSCGroupMappingMockMvc = MockMvcBuilders.standaloneSetup(hSCGroupMappingResource)
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
    public static HSCGroupMapping createEntity(EntityManager em) {
        HSCGroupMapping hSCGroupMapping = new HSCGroupMapping()
            .effectiveFrom(DEFAULT_EFFECTIVE_FROM);
        // Add required entity
        HealthcareServiceCenter healthcareServiceCenter = HealthcareServiceCenterResourceIntTest.createEntity(em);
        em.persist(healthcareServiceCenter);
        em.flush();
        hSCGroupMapping.setHealthcareServiceCenter(healthcareServiceCenter);
        // Add required entity
        Group group = GroupResourceIntTest.createEntity(em);
        em.persist(group);
        em.flush();
        hSCGroupMapping.setGroup(group);
        hSCGroupMapping.setId(RandomNumber.getRandomNumber());
        return hSCGroupMapping;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(HSCGroupMapping.class))
            elasticsearchTemplate.createIndex(HSCGroupMapping.class);
        hSCGroupMappingSearchRepository.deleteAll();
        hSCGroupMapping = createEntity(em);
    }

    @Test
    @Transactional
    public void createHSCGroupMapping() throws Exception {
        int databaseSizeBeforeCreate = hSCGroupMappingRepository.findAll().size();

        // Create the HSCGroupMapping

        restHSCGroupMappingMockMvc.perform(put("/api/hsc-group-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(hSCGroupMapping)))
            .andExpect(status().isOk());

        // Validate the HSCGroupMapping in the database
        List<HSCGroupMapping> hSCGroupMappingList = hSCGroupMappingRepository.findAll();
        assertThat(hSCGroupMappingList).hasSize(databaseSizeBeforeCreate + 1);
        HSCGroupMapping testHSCGroupMapping = hSCGroupMappingList.get(hSCGroupMappingList.size() - 1);
        assertThat(testHSCGroupMapping.getEffectiveFrom()).isEqualTo(DEFAULT_EFFECTIVE_FROM);

        // Validate the HSCGroupMapping in Elasticsearch
        HSCGroupMapping hSCGroupMappingEs = hSCGroupMappingSearchRepository.findById(testHSCGroupMapping.getId()).get();
        assertThat(hSCGroupMappingEs).isEqualToComparingFieldByField(testHSCGroupMapping);
    }

    @Test
    @Transactional
    public void createHSCGroupMappingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = hSCGroupMappingRepository.findAll().size();

        // Create the HSCGroupMapping with an existing ID
        HSCGroupMapping existingHSCGroupMapping = new HSCGroupMapping();
        existingHSCGroupMapping.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restHSCGroupMappingMockMvc.perform(post("/api/hsc-group-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingHSCGroupMapping)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<HSCGroupMapping> hSCGroupMappingList = hSCGroupMappingRepository.findAll();
        assertThat(hSCGroupMappingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkEffectiveFromIsRequired() throws Exception {
        int databaseSizeBeforeTest = hSCGroupMappingRepository.findAll().size();
        // set the field null
        hSCGroupMapping.setEffectiveFrom(null);

        // Create the HSCGroupMapping, which fails.

        restHSCGroupMappingMockMvc.perform(post("/api/hsc-group-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(hSCGroupMapping)))
            .andExpect(status().isBadRequest());

        List<HSCGroupMapping> hSCGroupMappingList = hSCGroupMappingRepository.findAll();
        assertThat(hSCGroupMappingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllHSCGroupMappings() throws Exception {
        // Initialize the database
        hSCGroupMappingRepository.saveAndFlush(hSCGroupMapping);

        // Get all the hSCGroupMappingList
        restHSCGroupMappingMockMvc.perform(get("/api/hsc-group-mappings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hSCGroupMapping.getId().intValue())))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(DEFAULT_EFFECTIVE_FROM.toString())));
    }

    @Test
    @Transactional
    public void getHSCGroupMapping() throws Exception {
        // Initialize the database
        hSCGroupMappingRepository.saveAndFlush(hSCGroupMapping);

        // Get the hSCGroupMapping
        restHSCGroupMappingMockMvc.perform(get("/api/hsc-group-mappings/{id}", hSCGroupMapping.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(hSCGroupMapping.getId().intValue()))
            .andExpect(jsonPath("$.effectiveFrom").value(DEFAULT_EFFECTIVE_FROM.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingHSCGroupMapping() throws Exception {
        // Get the hSCGroupMapping
        restHSCGroupMappingMockMvc.perform(get("/api/hsc-group-mappings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateHSCGroupMapping() throws Exception {
        // Initialize the database
        hSCGroupMappingService.save(hSCGroupMapping);

        int databaseSizeBeforeUpdate = hSCGroupMappingRepository.findAll().size();

        // Update the hSCGroupMapping
        HSCGroupMapping updatedHSCGroupMapping = hSCGroupMappingRepository.findById(hSCGroupMapping.getId()).get();
        updatedHSCGroupMapping
            .effectiveFrom(UPDATED_EFFECTIVE_FROM);

        restHSCGroupMappingMockMvc.perform(put("/api/hsc-group-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedHSCGroupMapping)))
            .andExpect(status().isOk());

        // Validate the HSCGroupMapping in the database
        List<HSCGroupMapping> hSCGroupMappingList = hSCGroupMappingRepository.findAll();
        assertThat(hSCGroupMappingList).hasSize(databaseSizeBeforeUpdate);
        HSCGroupMapping testHSCGroupMapping = hSCGroupMappingList.get(hSCGroupMappingList.size() - 1);
        assertThat(testHSCGroupMapping.getEffectiveFrom()).isEqualTo(UPDATED_EFFECTIVE_FROM);

        // Validate the HSCGroupMapping in Elasticsearch
        HSCGroupMapping hSCGroupMappingEs = hSCGroupMappingSearchRepository.findById(testHSCGroupMapping.getId()).get();
        assertThat(hSCGroupMappingEs).isEqualToComparingFieldByField(testHSCGroupMapping);
    }

    @Test
    @Transactional
    public void updateNonExistingHSCGroupMapping() throws Exception {
        int databaseSizeBeforeUpdate = hSCGroupMappingRepository.findAll().size();

        // Create the HSCGroupMapping

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restHSCGroupMappingMockMvc.perform(put("/api/hsc-group-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(hSCGroupMapping)))
            .andExpect(status().isOk());

        // Validate the HSCGroupMapping in the database
        List<HSCGroupMapping> hSCGroupMappingList = hSCGroupMappingRepository.findAll();
        assertThat(hSCGroupMappingList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteHSCGroupMapping() throws Exception {
        // Initialize the database
        hSCGroupMappingService.save(hSCGroupMapping);

        int databaseSizeBeforeDelete = hSCGroupMappingRepository.findAll().size();

        // Get the hSCGroupMapping
        restHSCGroupMappingMockMvc.perform(delete("/api/hsc-group-mappings/{id}", hSCGroupMapping.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean hSCGroupMappingExistsInEs = hSCGroupMappingSearchRepository.existsById(hSCGroupMapping.getId());
        assertThat(hSCGroupMappingExistsInEs).isFalse();

        // Validate the database is empty
        List<HSCGroupMapping> hSCGroupMappingList = hSCGroupMappingRepository.findAll();
        assertThat(hSCGroupMappingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchHSCGroupMapping() throws Exception {
        // Initialize the database
        hSCGroupMappingService.save(hSCGroupMapping);

        // Search the hSCGroupMapping
        restHSCGroupMappingMockMvc.perform(get("/api/_search/hsc-group-mappings?query=id:" + hSCGroupMapping.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hSCGroupMapping.getId().intValue())))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(DEFAULT_EFFECTIVE_FROM.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HSCGroupMapping.class);
    }
}
