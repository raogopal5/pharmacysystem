package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.ItemCategory;
import org.nh.pharmacy.repository.ItemCategoryRepository;
import org.nh.pharmacy.repository.search.ItemCategorySearchRepository;
import org.nh.pharmacy.service.ItemCategoryService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ItemCategoryResource REST controller.
 *
 * @see ItemCategoryResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemCategoryResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ACTIVE = false;

    private static final Boolean DEFAULT_GROUP = false;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private ItemCategoryService itemCategoryService;

    @Autowired
    private ItemCategorySearchRepository itemCategorySearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restItemCategoryMockMvc;

    private ItemCategory itemCategory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemCategoryResource itemCategoryResource = new ItemCategoryResource(itemCategoryService);
        this.restItemCategoryMockMvc = MockMvcBuilders.standaloneSetup(itemCategoryResource)
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
    public static ItemCategory createEntity(EntityManager em) {
        ItemCategory itemCategory = new ItemCategory()
            .code(DEFAULT_CODE)
            .description(DEFAULT_DESCRIPTION)
            .active(DEFAULT_ACTIVE)
            .group(DEFAULT_GROUP);
        itemCategory.setId(RandomNumber.getRandomNumber());
        return itemCategory;
    }

    public static ItemCategory createEntityIfNotExist(EntityManager em) {
        List<ItemCategory> itemCategoryList = em.createQuery("from " + ItemCategory.class.getName()).getResultList();
        ItemCategory itemCategory = null;
        if (itemCategoryList != null && !itemCategoryList.isEmpty()) {
            itemCategory = itemCategoryList.get(0);
        } else {
            itemCategory = createEntity(em);
            em.persist(itemCategory);
            em.flush();
            itemCategory = (ItemCategory) em.createQuery("from " + ItemCategory.class.getName()).getResultList().get(0);
        }
        return itemCategory;
    }

    @Before
    public void initTest() {
        itemCategorySearchRepository.deleteAll();
        itemCategory = createEntity(em);
    }

    @Test
    @Transactional
    public void getAllItemCategories() throws Exception {
        // Initialize the database
        itemCategoryRepository.saveAndFlush(itemCategory);

        // Get all the itemCategoryList
        restItemCategoryMockMvc.perform(get("/api/item-categories?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemCategory.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].group").value(hasItem(DEFAULT_GROUP.booleanValue())));
    }

    @Test
    @Transactional
    public void getItemCategory() throws Exception {
        // Initialize the database
        itemCategoryRepository.saveAndFlush(itemCategory);

        // Get the itemCategory
        restItemCategoryMockMvc.perform(get("/api/item-categories/{id}", itemCategory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(itemCategory.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.group").value(DEFAULT_GROUP.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingItemCategory() throws Exception {
        // Get the itemCategory
        restItemCategoryMockMvc.perform(get("/api/item-categories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchItemCategory() throws Exception {
        // Initialize the database
        itemCategoryService.save(itemCategory);

        // Search the itemCategory
        restItemCategoryMockMvc.perform(get("/api/_search/item-categories?query=id:" + itemCategory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemCategory.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].group").value(hasItem(DEFAULT_GROUP.booleanValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemCategory.class);
    }
}
