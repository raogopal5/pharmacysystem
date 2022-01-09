package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.HealthcareServiceCenter;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.domain.ItemStoreLocatorMap;
import org.nh.pharmacy.domain.Locator;
import org.nh.pharmacy.repository.ItemStoreLocatorMapRepository;
import org.nh.pharmacy.repository.search.ItemStoreLocatorMapSearchRepository;
import org.nh.pharmacy.service.ItemStoreLocatorMapService;
import org.nh.pharmacy.util.RandomNumber;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ItemStoreLocatorMapResource REST controller.
 *
 * @see ItemStoreLocatorMapResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemStoreLocatorMapResourceIntTest {

    private static final Boolean DEFAULT_ACTIVE = false;

    @Autowired
    private ItemStoreLocatorMapRepository itemStoreLocatorMapRepository;

    @Autowired
    private ItemStoreLocatorMapService itemStoreLocatorMapService;

    @Autowired
    private ItemStoreLocatorMapSearchRepository itemStoreLocatorMapSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restItemStoreLocatorMapMockMvc;

    private ItemStoreLocatorMap itemStoreLocatorMap;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemStoreLocatorMapResource itemStoreLocatorMapResource = new ItemStoreLocatorMapResource(itemStoreLocatorMapService);
        this.restItemStoreLocatorMapMockMvc = MockMvcBuilders.standaloneSetup(itemStoreLocatorMapResource)
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
    public static ItemStoreLocatorMap createEntity(EntityManager em) {
        ItemStoreLocatorMap itemStoreLocatorMap = new ItemStoreLocatorMap()
            .active(DEFAULT_ACTIVE);
        // Add required entity
        Item item = ItemResourceIntTest.createEntity(em);
        em.persist(item);
        em.flush();
        itemStoreLocatorMap.setItem(item);
        // Add required entity
        HealthcareServiceCenter healthCareServiceCenter = HealthcareServiceCenterResourceIntTest.createEntity(em);
        em.persist(healthCareServiceCenter);
        em.flush();
        itemStoreLocatorMap.setHealthCareServiceCenter(healthCareServiceCenter);
        // Add required entity
        Locator locator = LocatorResourceIntTest.createEntity(em);
        em.persist(locator);
        em.flush();
        itemStoreLocatorMap.setLocator(locator);
        itemStoreLocatorMap.setId(RandomNumber.getRandomNumber());
        return itemStoreLocatorMap;
    }

    @Before
    public void initTest() {
        itemStoreLocatorMapSearchRepository.deleteAll();
        itemStoreLocatorMap = createEntity(em);
    }

    @Test
    @Transactional
    public void getAllItemStoreLocatorMaps() throws Exception {
        // Initialize the database
        itemStoreLocatorMapRepository.saveAndFlush(itemStoreLocatorMap);

        // Get all the itemStoreLocatorMapList
        restItemStoreLocatorMapMockMvc.perform(get("/api/item-store-locator-maps?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemStoreLocatorMap.getId().intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getItemStoreLocatorMap() throws Exception {
        // Initialize the database
        itemStoreLocatorMapRepository.saveAndFlush(itemStoreLocatorMap);

        // Get the itemStoreLocatorMap
        restItemStoreLocatorMapMockMvc.perform(get("/api/item-store-locator-maps/{id}", itemStoreLocatorMap.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(itemStoreLocatorMap.getId().intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingItemStoreLocatorMap() throws Exception {
        // Get the itemStoreLocatorMap
        restItemStoreLocatorMapMockMvc.perform(get("/api/item-store-locator-maps/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchItemStoreLocatorMap() throws Exception {
        // Initialize the database
        itemStoreLocatorMapService.save(itemStoreLocatorMap);

        // Search the itemStoreLocatorMap
        restItemStoreLocatorMapMockMvc.perform(get("/api/_search/item-store-locator-maps?query=id:" + itemStoreLocatorMap.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemStoreLocatorMap.getId().intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemStoreLocatorMap.class);
    }
}
