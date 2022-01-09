package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.billing.config.BillingProperties;
import org.nh.billing.domain.PlanRule;
import org.nh.billing.domain.PlanRules;
import org.nh.billing.domain.dto.PlanRuleDetail;
import org.nh.billing.domain.enumeration.Days;
import org.nh.billing.domain.enumeration.PlanRuleType;
import org.nh.billing.domain.enumeration.VisitType;
import org.nh.billing.repository.PlanRuleRepository;
import org.nh.billing.repository.search.PlanRuleSearchRepository;
import org.nh.billing.service.PlanRuleService;
import org.nh.billing.web.rest.PlanRuleResource;
import org.nh.common.dto.GroupDTO;
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
import java.time.*;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.pharmacy.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//import org.nh.billing.domain.dto.PlanRule;

/**
 * Test class for the PlanRuleResource REST controller.
 *
 * @see PlanRuleResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class PlanRuleResourceIntTest {

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE_CODE = "AAA";
    private static final String UPDATED_TYPE_CODE = "BBB";

    private static final Integer DEFAULT_UUID = 1;
    private static final Integer UPDATED_UUID = 2;

    private static final Integer DEFAULT_VERSION = 1;
    private static final Integer UPDATED_VERSION = 2;

    private static final Integer DEFAULT_LEVEL = 1;
    private static final Integer UPDATED_LEVEL = 2;

    private static PlanRules planRules = new PlanRules(Arrays.asList(createPlanRuleDetail()));
    private static final PlanRules DEFAULT_PLAN_RULES = planRules;
    private static final PlanRules UPDATED_PLAN_RULES = planRules;

    private static final LocalDateTime DEFAULT_EFFECTIVE_FROM = LocalDateTime.now();
    private static final LocalDateTime UPDATED_EFFECTIVE_FROM = LocalDateTime.now();

    private static final LocalDateTime DEFAULT_EFFECTIVE_TO = LocalDateTime.now();
    private static final LocalDateTime UPDATED_EFFECTIVE_TO = LocalDateTime.now();

    @Autowired
    private PlanRuleRepository planRuleRepository;

    @Autowired
    private PlanRuleService planRuleService;

    @Autowired
    private PlanRuleSearchRepository planRuleSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private MockMvc restPlanRuleMockMvc;

    private org.nh.billing.domain.PlanRule planRule;
    @Autowired
    private BillingProperties billingProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PlanRuleResource planRuleResource = new PlanRuleResource(planRuleService,planRuleRepository,planRuleSearchRepository,billingProperties);
        this.restPlanRuleMockMvc = MockMvcBuilders.standaloneSetup(planRuleResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static org.nh.billing.domain.PlanRule createEntity(EntityManager em) {
        org.nh.billing.domain.PlanRule planRule = new org.nh.billing.domain.PlanRule()
            .type(DEFAULT_TYPE)
            .typeCode(DEFAULT_TYPE_CODE)
            .uuid(DEFAULT_UUID)
            .version(DEFAULT_VERSION)
            .level(DEFAULT_LEVEL)
            .PlanRules(DEFAULT_PLAN_RULES)
            .effectiveFrom(DEFAULT_EFFECTIVE_FROM)
            .effectiveTo(DEFAULT_EFFECTIVE_TO);
        planRule.setId(1L);
        return planRule;
    }

    public static PlanRuleDetail createPlanRuleDetail() {
        GroupDTO group = new GroupDTO();
        group.setId(1l);
        group.setName("abc");
        group.setCode("123abc");
        PlanRuleDetail planRule = new PlanRuleDetail();
        planRule.setActive(true);
        planRule.setAliasCode("abc");
        planRule.setAliasName("bcd");
        planRule.setMinAmount(22F);
        planRule.setMaxAmount(28F);
        planRule.setAuthorizationExclusion(true);
        planRule.setDays(Days.Friday);
        planRule.setExclusion(true);
        planRule.setMinAge(34);
        planRule.setGender("Male");
        planRule.setIsGeneric(true);
        planRule.setGroup(group);
        planRule.setPatientCopayment(34F);
        planRule.setPlanRuleType(PlanRuleType.Plan);
        planRule.setMinQuantity(67);
        planRule.setMaxQuantity(80);
        planRule.setVisitType(Arrays.asList("Daycare", "Emergency"));
        planRule.setMaxAge(12);
        planRule.setMinAge(78);
        planRule.setSponsorPayment(55F);
        return planRule;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(PlanRule.class))
            elasticsearchTemplate.createIndex(PlanRule.class);
        planRuleSearchRepository.deleteAll();
        planRule = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlanRule() throws Exception {
        int databaseSizeBeforeCreate = planRuleRepository.findAll().size();

        // Create the PlanRule

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isCreated());

        // Validate the PlanRule in the database
        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeCreate + 1);
        org.nh.billing.domain.PlanRule testPlanRule = planRuleList.get(planRuleList.size() - 1);
        assertThat(testPlanRule.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testPlanRule.getTypeCode()).isEqualTo(DEFAULT_TYPE_CODE);
        assertThat(testPlanRule.getUuid()).isEqualTo(DEFAULT_UUID);
        assertThat(testPlanRule.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testPlanRule.getLevel()).isEqualTo(DEFAULT_LEVEL);
        assertThat(testPlanRule.getPlanRules()).isEqualTo(DEFAULT_PLAN_RULES);
        assertThat(testPlanRule.getEffectiveFrom()).isEqualTo(DEFAULT_EFFECTIVE_FROM);
        assertThat(testPlanRule.getEffectiveTo()).isEqualTo(DEFAULT_EFFECTIVE_TO);

        // Validate the PlanRule in ElasticSearch
        org.nh.billing.domain.PlanRule planRuleEs = planRuleSearchRepository.findById(testPlanRule.getId()).get();
        assertThat(planRuleEs).isEqualToComparingFieldByField(testPlanRule);
    }

    @Test
    @Transactional
    public void createPlanRuleWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = planRuleRepository.findAll().size();

        // Create the PlanRule with an existing ID
        org.nh.billing.domain.PlanRule existingPlanRule = new org.nh.billing.domain.PlanRule();
        existingPlanRule.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingPlanRule)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRuleRepository.findAll().size();
        // set the field null
        planRule.setType(null);

        // Create the PlanRule, which fails.

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isBadRequest());

        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRuleRepository.findAll().size();
        // set the field null
        planRule.setTypeCode(null);

        // Create the PlanRule, which fails.

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isBadRequest());

        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUuidIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRuleRepository.findAll().size();
        // set the field null
        planRule.setUuid(null);

        // Create the PlanRule, which fails.

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isBadRequest());

        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRuleRepository.findAll().size();
        // set the field null
        planRule.setVersion(null);

        // Create the PlanRule, which fails.

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isBadRequest());

        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLevelIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRuleRepository.findAll().size();
        // set the field null
        planRule.setLevel(null);

        // Create the PlanRule, which fails.

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isBadRequest());

        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPlanRulesIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRuleRepository.findAll().size();
        // set the field null
        planRule.setPlanRules(null);

        // Create the PlanRule, which fails.

        restPlanRuleMockMvc.perform(post("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planRule)))
            .andExpect(status().isBadRequest());

        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlanRules() throws Exception {
        // Initialize the database
        planRuleRepository.saveAndFlush(planRule);

        // Get all the planRuleList
        restPlanRuleMockMvc.perform(get("/api/plan-rules?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].typeCode").value(hasItem(DEFAULT_TYPE_CODE)))
            .andExpect(jsonPath("$.[*].uuid").value(hasItem(DEFAULT_UUID)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].planRules.planRuleDetailsList.[0].minAmount").value(22.0))
            .andExpect(jsonPath("$.[*].effectiveFrom".toString()).value(hasItem(sameInstant(DEFAULT_EFFECTIVE_FROM))))
            .andExpect(jsonPath("$.[*].effectiveTo").value(hasItem(sameInstant(DEFAULT_EFFECTIVE_TO))));
    }

    @Test
    @Transactional
    public void getPlanRule() throws Exception {
        // Initialize the database
        planRuleRepository.saveAndFlush(planRule);

        // Get the planRule
        restPlanRuleMockMvc.perform(get("/api/plan-rules/{id}", planRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(planRule.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.typeCode").value(DEFAULT_TYPE_CODE))
            .andExpect(jsonPath("$.uuid").value(DEFAULT_UUID))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.level").value(DEFAULT_LEVEL))
            .andExpect(jsonPath("$..planRules.planRuleDetailsList.[0].minAmount").value(22.0))
            .andExpect(jsonPath("$.effectiveFrom").value(sameInstant(DEFAULT_EFFECTIVE_FROM)))
            .andExpect(jsonPath("$.effectiveTo").value(sameInstant(DEFAULT_EFFECTIVE_TO)));
    }

    @Test
    @Transactional
    public void getNonExistingPlanRule() throws Exception {
        // Get the planRule
        restPlanRuleMockMvc.perform(get("/api/plan-rules/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlanRule() throws Exception {
        // Initialize the database
        planRuleService.save(planRule);

        int databaseSizeBeforeUpdate = planRuleRepository.findAll().size();

        // Update the planRule
        org.nh.billing.domain.PlanRule updatedPlanRule = planRuleRepository.findById(planRule.getId()).get();
        updatedPlanRule
            .type(UPDATED_TYPE)
            .typeCode(UPDATED_TYPE_CODE)
            .uuid(UPDATED_UUID)
            .version(UPDATED_VERSION)
            .level(UPDATED_LEVEL)
            .PlanRules(UPDATED_PLAN_RULES)
            .effectiveFrom(UPDATED_EFFECTIVE_FROM)
            .effectiveTo(UPDATED_EFFECTIVE_TO);

        restPlanRuleMockMvc.perform(put("/api/plan-rules")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlanRule)))
            .andExpect(status().isOk());

        // Validate the PlanRule in the database
        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeUpdate);
        org.nh.billing.domain.PlanRule testPlanRule = planRuleList.get(planRuleList.size() - 1);
        assertThat(testPlanRule.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testPlanRule.getTypeCode()).isEqualTo(UPDATED_TYPE_CODE);
        assertThat(testPlanRule.getUuid()).isEqualTo(UPDATED_UUID);
        assertThat(testPlanRule.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testPlanRule.getLevel()).isEqualTo(UPDATED_LEVEL);
        assertThat(testPlanRule.getPlanRules()).isEqualTo(UPDATED_PLAN_RULES);
        assertThat(testPlanRule.getEffectiveFrom()).isEqualTo(UPDATED_EFFECTIVE_FROM);
        assertThat(testPlanRule.getEffectiveTo()).isEqualTo(UPDATED_EFFECTIVE_TO);

        // Validate the PlanRule in ElasticSearch
        org.nh.billing.domain.PlanRule planRuleEs = planRuleSearchRepository.findById(testPlanRule.getId()).get();
        assertThat(planRuleEs).isEqualToComparingFieldByField(testPlanRule);
    }

    @Test
    @Transactional
    public void deletePlanRule() throws Exception {
        // Initialize the database
        planRuleService.save(planRule);

        int databaseSizeBeforeDelete = planRuleRepository.findAll().size();

        // Get the planRule
        restPlanRuleMockMvc.perform(delete("/api/plan-rules/{id}", planRule.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean planRuleExistsInEs = planRuleSearchRepository.existsById(planRule.getId());
        assertThat(planRuleExistsInEs).isFalse();

        // Validate the database is empty
        List<org.nh.billing.domain.PlanRule> planRuleList = planRuleRepository.findAll();
        assertThat(planRuleList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPlanRule() throws Exception {
        // Initialize the database
        planRuleService.save(planRule);

        // Search the planRule
        restPlanRuleMockMvc.perform(get("/api/_search/plan-rules?query=id:" + planRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].typeCode").value(hasItem(DEFAULT_TYPE_CODE)))
            .andExpect(jsonPath("$.[*].uuid").value(hasItem(DEFAULT_UUID)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].planRules.planRuleDetailsList.[0].minAmount").value(hasItem(22.0)))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(sameInstant(DEFAULT_EFFECTIVE_FROM))))
            .andExpect(jsonPath("$.[*].effectiveTo").value(hasItem(sameInstant(DEFAULT_EFFECTIVE_TO))));
    }
}
