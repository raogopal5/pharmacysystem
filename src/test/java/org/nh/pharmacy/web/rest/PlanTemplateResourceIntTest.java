package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.billing.config.BillingProperties;
import org.nh.billing.domain.PlanTemplate;
import org.nh.billing.repository.PlanTemplateRepository;
import org.nh.billing.repository.search.PlanTemplateSearchRepository;
import org.nh.billing.service.PlanTemplateService;
import org.nh.billing.web.rest.PlanTemplateResource;
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
 * Test class for the PlanTemplateResource REST controller.
 *
 * @see PlanTemplateResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class PlanTemplateResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    @Autowired
    private PlanTemplateRepository planTemplateRepository;

    @Autowired
    private PlanTemplateService planTemplateService;

    @Autowired
    private PlanTemplateSearchRepository planTemplateSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private MockMvc restPlanTemplateMockMvc;

    private PlanTemplate planTemplate;
    @Autowired
    private BillingProperties billingProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PlanTemplateResource planTemplateResource = new PlanTemplateResource(planTemplateService,planTemplateRepository,planTemplateSearchRepository,billingProperties);
        this.restPlanTemplateMockMvc = MockMvcBuilders.standaloneSetup(planTemplateResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PlanTemplate createEntity(EntityManager em) {
        PlanTemplate planTemplate = new PlanTemplate()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .active(DEFAULT_ACTIVE);
        planTemplate.setId(1L);
        return planTemplate;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(PlanTemplate.class))
            elasticsearchTemplate.createIndex(PlanTemplate.class);
        planTemplateSearchRepository.deleteAll();
        planTemplate = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlanTemplate() throws Exception {
        int databaseSizeBeforeCreate = planTemplateRepository.findAll().size();

        // Create the PlanTemplate

        restPlanTemplateMockMvc.perform(post("/api/plan-templates")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planTemplate)))
            .andExpect(status().isCreated());

        // Validate the PlanTemplate in the database
        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeCreate + 1);
        PlanTemplate testPlanTemplate = planTemplateList.get(planTemplateList.size() - 1);
        assertThat(testPlanTemplate.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testPlanTemplate.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPlanTemplate.isActive()).isEqualTo(DEFAULT_ACTIVE);

        // Validate the PlanTemplate in ElasticSearch
        PlanTemplate planTemplateEs = planTemplateSearchRepository.findById(testPlanTemplate.getId()).get();
        assertThat(planTemplateEs).isEqualToComparingFieldByField(testPlanTemplate);
    }

    @Test
    @Transactional
    public void createPlanTemplateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = planTemplateRepository.findAll().size();

        // Create the PlanTemplate with an existing ID
        PlanTemplate existingPlanTemplate = new PlanTemplate();
        existingPlanTemplate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanTemplateMockMvc.perform(post("/api/plan-templates")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingPlanTemplate)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = planTemplateRepository.findAll().size();
        // set the field null
        planTemplate.setCode(null);

        // Create the PlanTemplate, which fails.

        restPlanTemplateMockMvc.perform(post("/api/plan-templates")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planTemplate)))
            .andExpect(status().isBadRequest());

        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = planTemplateRepository.findAll().size();
        // set the field null
        planTemplate.setName(null);

        // Create the PlanTemplate, which fails.

        restPlanTemplateMockMvc.perform(post("/api/plan-templates")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planTemplate)))
            .andExpect(status().isBadRequest());

        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = planTemplateRepository.findAll().size();
        // set the field null
        planTemplate.setActive(null);

        // Create the PlanTemplate, which fails.

        restPlanTemplateMockMvc.perform(post("/api/plan-templates")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planTemplate)))
            .andExpect(status().isBadRequest());

        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlanTemplates() throws Exception {
        // Initialize the database
        planTemplateRepository.saveAndFlush(planTemplate);

        // Get all the planTemplateList
        restPlanTemplateMockMvc.perform(get("/api/plan-templates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planTemplate.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getPlanTemplate() throws Exception {
        // Initialize the database
        planTemplateRepository.saveAndFlush(planTemplate);

        // Get the planTemplate
        restPlanTemplateMockMvc.perform(get("/api/plan-templates/{id}", planTemplate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(planTemplate.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPlanTemplate() throws Exception {
        // Get the planTemplate
        restPlanTemplateMockMvc.perform(get("/api/plan-templates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlanTemplate() throws Exception {
        // Initialize the database
        planTemplateService.save(planTemplate);

        int databaseSizeBeforeUpdate = planTemplateRepository.findAll().size();

        // Update the planTemplate
        PlanTemplate updatedPlanTemplate = planTemplateRepository.findById(planTemplate.getId()).get();
        updatedPlanTemplate
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .active(UPDATED_ACTIVE);

        restPlanTemplateMockMvc.perform(put("/api/plan-templates")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlanTemplate)))
            .andExpect(status().isOk());

        // Validate the PlanTemplate in the database
        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeUpdate);
        PlanTemplate testPlanTemplate = planTemplateList.get(planTemplateList.size() - 1);
        assertThat(testPlanTemplate.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testPlanTemplate.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPlanTemplate.isActive()).isEqualTo(UPDATED_ACTIVE);

        // Validate the PlanTemplate in ElasticSearch
        PlanTemplate planTemplateEs = planTemplateSearchRepository.findById(testPlanTemplate.getId()).get();
        assertThat(planTemplateEs).isEqualToComparingFieldByField(testPlanTemplate);
    }

    @Test
    @Transactional
    public void deletePlanTemplate() throws Exception {
        // Initialize the database
        planTemplateService.save(planTemplate);

        int databaseSizeBeforeDelete = planTemplateRepository.findAll().size();

        // Get the planTemplate
        restPlanTemplateMockMvc.perform(delete("/api/plan-templates/{id}", planTemplate.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean planTemplateExistsInEs = planTemplateSearchRepository.existsById(planTemplate.getId());
        assertThat(planTemplateExistsInEs).isFalse();

        // Validate the database is empty
        List<PlanTemplate> planTemplateList = planTemplateRepository.findAll();
        assertThat(planTemplateList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPlanTemplate() throws Exception {
        // Initialize the database
        planTemplateService.save(planTemplate);

        // Search the planTemplate
        restPlanTemplateMockMvc.perform(get("/api/_search/plan-templates?query=id:" + planTemplate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planTemplate.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }
}
