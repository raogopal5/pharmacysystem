package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Locator;
import org.nh.pharmacy.repository.LocatorRepository;
import org.nh.pharmacy.repository.search.LocatorSearchRepository;
import org.nh.pharmacy.service.LocatorService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the LocatorResource REST controller.
 *
 * @see LocatorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class LocatorResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ACTIVE = false;

    @Autowired
    private LocatorRepository locatorRepository;

    @Autowired
    private LocatorService locatorService;

    @Autowired
    private LocatorSearchRepository locatorSearchRepository;

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

    private MockMvc restLocatorMockMvc;

    private Locator locator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LocatorResource locatorResource = new LocatorResource(locatorService);
        this.restLocatorMockMvc = MockMvcBuilders.standaloneSetup(locatorResource)
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
    public static Locator createEntity(EntityManager em) {
        Locator locator = new Locator()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .active(DEFAULT_ACTIVE);
        locator.setId(RandomNumber.getRandomNumber());
        return locator;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists(Locator.class))
            elasticsearchTemplate.createIndex(Locator.class);
        locatorSearchRepository.deleteAll();
        locator = createEntity(em);
    }

    public static Locator createEntityIfNotExist(EntityManager em) {
        List<Locator> locators = em.createQuery("from " + Locator.class.getName()).getResultList();
        Locator locator = null;
        if (locators != null && !locators.isEmpty()) {
            locator = locators.get(0);
        } else {
            locator = createEntity(em);
            em.persist(locator);
            em.flush();
            locator = (Locator) em.createQuery("from " + Locator.class.getName()).getResultList().get(0);
        }
        return locator;
    }

    @Test
    @Transactional
    public void getAllLocators() throws Exception {
        // Initialize the database
        locatorRepository.saveAndFlush(locator);

        // Get all the locatorList
        restLocatorMockMvc.perform(get("/api/locators?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(locator.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getLocator() throws Exception {
        // Initialize the database
        locatorRepository.saveAndFlush(locator);

        // Get the locator
        restLocatorMockMvc.perform(get("/api/locators/{id}", locator.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(locator.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingLocator() throws Exception {
        // Get the locator
        restLocatorMockMvc.perform(get("/api/locators/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchLocator() throws Exception {
        // Initialize the database
        locatorService.save(locator);

        // Search the locator
        restLocatorMockMvc.perform(get("/api/_search/locators?query=id:" + locator.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(locator.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Locator.class);
    }
}
