package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.billing.config.BillingProperties;
import org.nh.billing.domain.Plan;
import org.nh.billing.repository.PlanRepository;
import org.nh.billing.repository.search.PlanSearchRepository;
import org.nh.billing.service.PlanService;
import org.nh.billing.web.rest.PlanResource;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PlanResource REST controller.
 *
 * @see PlanResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class PlanResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_LEVEL = 1;
    private static final Integer UPDATED_LEVEL = 2;

    private static final Integer DEFAULT_UUID = 1;
    private static final Integer UPDATED_UUID = 2;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanSearchRepository planSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;
    @Autowired
    private BillingProperties billingProperties;

    private MockMvc restPlanMockMvc;

    private Plan plan;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PlanResource planResource = new PlanResource(planService,planRepository,planSearchRepository,billingProperties);
        this.restPlanMockMvc = MockMvcBuilders.standaloneSetup(planResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Plan createEntity(EntityManager em) {
        Plan plan = new Plan()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .level(DEFAULT_LEVEL)
            .uuid(DEFAULT_UUID)
            .active(DEFAULT_ACTIVE);
        OrganizationDTO sponsor = new OrganizationDTO();
        sponsor.setName("AAA");
        sponsor.setCode("BBB");
        sponsor.setActive(true);
        plan.setSponsor(sponsor);
        plan.setId(1L);
        return plan;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(Plan.class))
            elasticsearchTemplate.createIndex(Plan.class);
        planSearchRepository.deleteAll();
        plan = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlan() throws Exception {
        int databaseSizeBeforeCreate = planRepository.findAll().size();

        // Create the Plan

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isCreated());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeCreate + 1);
        Plan testPlan = planList.get(planList.size() - 1);
        assertThat(testPlan.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testPlan.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPlan.getLevel()).isEqualTo(DEFAULT_LEVEL);
        assertThat(testPlan.getUuid()).isEqualTo(DEFAULT_UUID);
        assertThat(testPlan.isActive()).isEqualTo(DEFAULT_ACTIVE);

        // Validate the Plan in ElasticSearch
        Plan planEs = planSearchRepository.findById(testPlan.getId()).get();
        assertThat(planEs).isEqualToComparingFieldByField(testPlan);
    }

    @Test
    @Transactional
    public void createPlanWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = planRepository.findAll().size();

        // Create the Plan with an existing ID
        Plan existingPlan = new Plan();
        existingPlan.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingPlan)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setCode(null);

        // Create the Plan, which fails.

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setName(null);

        // Create the Plan, which fails.

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLevelIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setLevel(null);

        // Create the Plan, which fails.

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUuidIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setUuid(null);

        // Create the Plan, which fails.

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setActive(null);

        // Create the Plan, which fails.

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlans() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].uuid").value(hasItem(DEFAULT_UUID)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getPlan() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get the plan
        restPlanMockMvc.perform(get("/api/plans/{id}", plan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(plan.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.level").value(DEFAULT_LEVEL))
            .andExpect(jsonPath("$.uuid").value(DEFAULT_UUID))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPlan() throws Exception {
        // Get the plan
        restPlanMockMvc.perform(get("/api/plans/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlan() throws Exception {
        // Initialize the database
        planService.save(plan);

        int databaseSizeBeforeUpdate = planRepository.findAll().size();

        // Update the plan
        Plan updatedPlan = planRepository.findById(plan.getId()).get();
        updatedPlan
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .level(UPDATED_LEVEL)
            .uuid(UPDATED_UUID)
            .active(UPDATED_ACTIVE);

        restPlanMockMvc.perform(put("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlan)))
            .andExpect(status().isOk());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeUpdate);
        Plan testPlan = planList.get(planList.size() - 1);
        assertThat(testPlan.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testPlan.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPlan.getLevel()).isEqualTo(UPDATED_LEVEL);
        assertThat(testPlan.getUuid()).isEqualTo(UPDATED_UUID);
        assertThat(testPlan.isActive()).isEqualTo(UPDATED_ACTIVE);

        // Validate the Plan in ElasticSearch
        Plan planEs = planSearchRepository.findById(testPlan.getId()).get();
        assertThat(planEs).isEqualToComparingFieldByField(testPlan);
    }

    @Test
    @Transactional
    public void deletePlan() throws Exception {
        // Initialize the database
        planService.save(plan);

        int databaseSizeBeforeDelete = planRepository.findAll().size();

        // Get the plan
        restPlanMockMvc.perform(delete("/api/plans/{id}", plan.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean planExistsInEs = planSearchRepository.existsById(plan.getId());
        assertThat(planExistsInEs).isFalse();

        // Validate the database is empty
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPlan() throws Exception {
        // Initialize the database
        planService.save(plan);

        // Search the plan
        restPlanMockMvc.perform(get("/api/_search/plans?query=id:" + plan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].uuid").value(hasItem(DEFAULT_UUID)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }
}
