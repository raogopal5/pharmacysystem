package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.domain.ItemPricingMethod;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.enumeration.PricingMethod;
import org.nh.pharmacy.repository.ItemPricingMethodRepository;
import org.nh.pharmacy.repository.search.ItemPricingMethodSearchRepository;
import org.nh.pharmacy.service.ItemPricingMethodService;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ItemPricingMethodResource REST controller.
 *
 * @see ItemPricingMethodResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemPricingMethodResourceIntTest {

    private static final LocalDate DEFAULT_EFFECTIVE_FROM = LocalDate.ofEpochDay(0L);

    private static final Boolean DEFAULT_ACTIVE = false;

    private static final PricingMethod DEFAULT_PRICING_METHOD = PricingMethod.MRP;

    private static final Float DEFAULT_SELLING_PRICE = 1F;

    @Autowired
    private ItemPricingMethodRepository itemPricingMethodRepository;

    @Autowired
    private ItemPricingMethodService itemPricingMethodService;

    @Autowired
    private ItemPricingMethodSearchRepository itemPricingMethodSearchRepository;

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

    private MockMvc restItemPricingMethodMockMvc;

    private ItemPricingMethod itemPricingMethod;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemPricingMethodResource itemPricingMethodResource = new ItemPricingMethodResource(itemPricingMethodService);
        this.restItemPricingMethodMockMvc = MockMvcBuilders.standaloneSetup(itemPricingMethodResource)
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
    public static ItemPricingMethod createEntity(EntityManager em) {
        ItemPricingMethod itemPricingMethod = new ItemPricingMethod()
            .effectiveFrom(DEFAULT_EFFECTIVE_FROM)
            .active(DEFAULT_ACTIVE)
            .pricingMethod(DEFAULT_PRICING_METHOD)
            .sellingPrice(DEFAULT_SELLING_PRICE);
        // Add required entity
        Organization organization = OrganizationResourceIntTest.createEntity(em);
        em.persist(organization);
        em.flush();
        itemPricingMethod.setOrganization(organization);
        // Add required entity
        Item item = ItemResourceIntTest.createEntity(em);
        em.persist(item);
        em.flush();
        itemPricingMethod.setItem(item);
        itemPricingMethod.setId(RandomNumber.getRandomNumber());
        return itemPricingMethod;
    }

    @Before
    public void initTest() {
        if (!elasticsearchTemplate.indexExists("itempricingmethod"))
            elasticsearchTemplate.createIndex("itempricingmethod");
        itemPricingMethodSearchRepository.deleteAll();
        itemPricingMethod = createEntity(em);
    }

    @Test
    @Transactional
    public void getAllItemPricingMethods() throws Exception {
        // Initialize the database
        itemPricingMethodRepository.saveAndFlush(itemPricingMethod);

        // Get all the itemPricingMethodList
        restItemPricingMethodMockMvc.perform(get("/api/item-pricing-methods?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemPricingMethod.getId().intValue())))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(DEFAULT_EFFECTIVE_FROM.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].pricingMethod").value(hasItem(DEFAULT_PRICING_METHOD.toString())))
            .andExpect(jsonPath("$.[*].sellingPrice").value(hasItem(DEFAULT_SELLING_PRICE.doubleValue())));
    }

    @Test
    @Transactional
    public void getItemPricingMethod() throws Exception {
        // Initialize the database
        itemPricingMethodRepository.saveAndFlush(itemPricingMethod);

        // Get the itemPricingMethod
        restItemPricingMethodMockMvc.perform(get("/api/item-pricing-methods/{id}", itemPricingMethod.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(itemPricingMethod.getId().intValue()))
            .andExpect(jsonPath("$.effectiveFrom").value(DEFAULT_EFFECTIVE_FROM.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.pricingMethod").value(DEFAULT_PRICING_METHOD.toString()))
            .andExpect(jsonPath("$.sellingPrice").value(DEFAULT_SELLING_PRICE.doubleValue()));
    }

    @Test
    @Transactional
    public void getNonExistingItemPricingMethod() throws Exception {
        // Get the itemPricingMethod
        restItemPricingMethodMockMvc.perform(get("/api/item-pricing-methods/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchItemPricingMethod() throws Exception {
        // Initialize the database
        itemPricingMethodService.save(itemPricingMethod);

        // Search the itemPricingMethod
        restItemPricingMethodMockMvc.perform(get("/api/_search/item-pricing-methods?query=id:" + itemPricingMethod.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemPricingMethod.getId().intValue())))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(DEFAULT_EFFECTIVE_FROM.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].pricingMethod").value(hasItem(DEFAULT_PRICING_METHOD.toString())))
            .andExpect(jsonPath("$.[*].sellingPrice").value(hasItem(DEFAULT_SELLING_PRICE.doubleValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemPricingMethod.class);
    }
}
