package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.billing.config.BillingProperties;
import org.nh.billing.domain.Plan;
import org.nh.billing.domain.PlanOrganizationMapping;
import org.nh.billing.repository.PlanOrganizationMappingRepository;
import org.nh.billing.repository.search.PlanOrganizationMappingSearchRepository;
import org.nh.billing.service.PlanOrganizationMappingService;
import org.nh.billing.web.rest.PlanOrganizationMappingResource;
import org.nh.common.dto.OrganizationDTO;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PlanOrganizationMappingResource REST controller.
 *
 * @see PlanOrganizationMappingResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class PlanOrganizationMappingResourceIntTest {

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    @Inject
    private PlanOrganizationMappingRepository planOrganizationMappingRepository;

    @Inject
    private PlanOrganizationMappingService planOrganizationMappingService;

    @Inject
    private PlanOrganizationMappingSearchRepository planOrganizationMappingSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private MockMvc restPlanOrganizationMappingMockMvc;

    private PlanOrganizationMapping planOrganizationMapping;
    @Inject
    private BillingProperties applicationProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PlanOrganizationMappingResource planOrganizationMappingResource = new PlanOrganizationMappingResource(planOrganizationMappingService, planOrganizationMappingRepository,
            planOrganizationMappingSearchRepository,applicationProperties);
        ReflectionTestUtils.setField(planOrganizationMappingResource, "planOrganizationMappingService", planOrganizationMappingService);
        this.restPlanOrganizationMappingMockMvc = MockMvcBuilders.standaloneSetup(planOrganizationMappingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PlanOrganizationMapping createEntity(EntityManager em) {
        PlanOrganizationMapping planOrganizationMapping = new PlanOrganizationMapping()
            .active(DEFAULT_ACTIVE);
        // Add required entity
        Plan plan = PlanResourceIntTest.createEntity(em);
        em.persist(plan);
        em.flush();
        planOrganizationMapping.setPlan(plan);
        // Add required entity
        OrganizationDTO unit = new OrganizationDTO();
        unit.setId(1L);
        unit.setCode("AAA");
        unit.setName("BBB");
        unit.setActive(true);
        planOrganizationMapping.setUnit(unit);
        planOrganizationMapping.setId(1L);
        return planOrganizationMapping;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(PlanOrganizationMapping.class))
            elasticsearchTemplate.createIndex(PlanOrganizationMapping.class);
        planOrganizationMappingSearchRepository.deleteAll();
        planOrganizationMapping = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlanOrganizationMapping() throws Exception {
        int databaseSizeBeforeCreate = planOrganizationMappingRepository.findAll().size();

        // Create the PlanOrganizationMapping

        restPlanOrganizationMappingMockMvc.perform(post("/api/plan-organization-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planOrganizationMapping)))
            .andExpect(status().isCreated());

        // Validate the PlanOrganizationMapping in the database
        List<PlanOrganizationMapping> planOrganizationMappings = planOrganizationMappingRepository.findAll();
        assertThat(planOrganizationMappings).hasSize(databaseSizeBeforeCreate + 1);
        PlanOrganizationMapping testPlanOrganizationMapping = planOrganizationMappings.get(planOrganizationMappings.size() - 1);
        assertThat(testPlanOrganizationMapping.isActive()).isEqualTo(DEFAULT_ACTIVE);

        // Validate the PlanOrganizationMapping in ElasticSearch
        PlanOrganizationMapping planOrganizationMappingEs = planOrganizationMappingSearchRepository.findById(testPlanOrganizationMapping.getId()).get();
        assertThat(planOrganizationMappingEs).isEqualToComparingFieldByField(testPlanOrganizationMapping);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = planOrganizationMappingRepository.findAll().size();
        // set the field null
        planOrganizationMapping.setActive(null);

        // Create the PlanOrganizationMapping, which fails.

        restPlanOrganizationMappingMockMvc.perform(post("/api/plan-organization-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planOrganizationMapping)))
            .andExpect(status().isBadRequest());

        List<PlanOrganizationMapping> planOrganizationMappings = planOrganizationMappingRepository.findAll();
        assertThat(planOrganizationMappings).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlanOrganizationMappings() throws Exception {
        // Initialize the database
        planOrganizationMappingRepository.saveAndFlush(planOrganizationMapping);

        // Get all the planOrganizationMappings
        restPlanOrganizationMappingMockMvc.perform(get("/api/plan-organization-mappings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planOrganizationMapping.getId().intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getPlanOrganizationMapping() throws Exception {
        // Initialize the database
        planOrganizationMappingRepository.saveAndFlush(planOrganizationMapping);

        // Get the planOrganizationMapping
        restPlanOrganizationMappingMockMvc.perform(get("/api/plan-organization-mappings/{id}", planOrganizationMapping.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(planOrganizationMapping.getId().intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPlanOrganizationMapping() throws Exception {
        // Get the planOrganizationMapping
        restPlanOrganizationMappingMockMvc.perform(get("/api/plan-organization-mappings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlanOrganizationMapping() throws Exception {
        // Initialize the database
        planOrganizationMappingService.save(planOrganizationMapping);

        int databaseSizeBeforeUpdate = planOrganizationMappingRepository.findAll().size();

        // Update the planOrganizationMapping
        PlanOrganizationMapping updatedPlanOrganizationMapping = planOrganizationMappingRepository.findById(planOrganizationMapping.getId()).get();
        updatedPlanOrganizationMapping
            .active(UPDATED_ACTIVE);

        restPlanOrganizationMappingMockMvc.perform(put("/api/plan-organization-mappings")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlanOrganizationMapping)))
            .andExpect(status().isOk());

        // Validate the PlanOrganizationMapping in the database
        List<PlanOrganizationMapping> planOrganizationMappings = planOrganizationMappingRepository.findAll();
        assertThat(planOrganizationMappings).hasSize(databaseSizeBeforeUpdate);
        PlanOrganizationMapping testPlanOrganizationMapping = planOrganizationMappings.get(planOrganizationMappings.size() - 1);
        assertThat(testPlanOrganizationMapping.isActive()).isEqualTo(UPDATED_ACTIVE);

        // Validate the PlanOrganizationMapping in ElasticSearch
        PlanOrganizationMapping planOrganizationMappingEs = planOrganizationMappingSearchRepository.findById(testPlanOrganizationMapping.getId()).get();
        assertThat(planOrganizationMappingEs).isEqualToComparingFieldByField(testPlanOrganizationMapping);
    }

    @Test
    @Transactional
    public void deletePlanOrganizationMapping() throws Exception {
        // Initialize the database
        planOrganizationMappingService.save(planOrganizationMapping);

        int databaseSizeBeforeDelete = planOrganizationMappingRepository.findAll().size();

        // Get the planOrganizationMapping
        restPlanOrganizationMappingMockMvc.perform(delete("/api/plan-organization-mappings/{id}", planOrganizationMapping.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean planOrganizationMappingExistsInEs = planOrganizationMappingSearchRepository.existsById(planOrganizationMapping.getId());
        assertThat(planOrganizationMappingExistsInEs).isFalse();

        // Validate the database is empty
        List<PlanOrganizationMapping> planOrganizationMappings = planOrganizationMappingRepository.findAll();
        assertThat(planOrganizationMappings).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPlanOrganizationMapping() throws Exception {
        // Initialize the database
        planOrganizationMappingService.save(planOrganizationMapping);

        // Search the planOrganizationMapping
        restPlanOrganizationMappingMockMvc.perform(get("/api/_search/plan-organization-mappings?query=id:" + planOrganizationMapping.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planOrganizationMapping.getId().intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }
}
