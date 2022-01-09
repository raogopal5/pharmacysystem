package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.enumeration.FSNType;
import org.nh.pharmacy.domain.enumeration.VEDCategory;
import org.nh.pharmacy.repository.ItemRepository;
import org.nh.pharmacy.repository.search.ItemSearchRepository;
import org.nh.pharmacy.service.ItemService;
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
 * Test class for the ItemResource REST controller.
 *
 * @see ItemResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    private static final Boolean DEFAULT_BATCH_TRACKED = false;

    private static final Boolean DEFAULT_EXPIRY_DATE_REQUIRED = false;

    private static final Boolean DEFAULT_ACTIVE = false;

    private static final String DEFAULT_REMARKS = "AAAAAAAAAA";

    private static final FSNType DEFAULT_FSN_TYPE = FSNType.Fast;

    private static final VEDCategory DEFAULT_VED_CATEGORY = VEDCategory.Vital;

    private static final ValueSetCode DEFAULT_GROUP = new ValueSetCode(13L, "ITEM_GROUP", "Item Group", true);

    private static final ValueSetCode DEFAULT_TYPE = new ValueSetCode(14L, "ITEM_TYPE", "Item Type", true);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemSearchRepository itemSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restItemMockMvc;

    private Item item;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Item createEntity(EntityManager em) {
        Item item = new Item()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .batchTracked(DEFAULT_BATCH_TRACKED)
            .expiryDateRequired(DEFAULT_EXPIRY_DATE_REQUIRED)
            .active(DEFAULT_ACTIVE)
            .remarks(DEFAULT_REMARKS)
            .fsnType(DEFAULT_FSN_TYPE)
            .vedCategory(DEFAULT_VED_CATEGORY);
        item.setType(DEFAULT_TYPE);
        item.setGroup(DEFAULT_GROUP);
        // Add required entity
        ItemCategory category = ItemCategoryResourceIntTest.createEntityIfNotExist(em);
        item.setCategory(category);
        // Add required entity
        Group materialGroup = GroupResourceIntTest.createEntityIfNotExist(em);
        item.setMaterialGroup(materialGroup);
        // Add required entity
        UOM trackUOM = UOMResourceIntTest.createEntityIfNotExist(em);
        item.setTrackUOM(trackUOM);
        item.setId(RandomNumber.getRandomNumber());
        return item;
    }

    public static Item createEntityIfNotExist(EntityManager em) {
        List<Item> items = em.createQuery("from " + Item.class.getName()).getResultList();
        Item item = null;
        if (items != null && !items.isEmpty()) {
            item = items.get(0);
        } else {
            item = createEntity(em);
            em.persist(item);
            em.flush();
        }
        return item;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemResource itemResource = new ItemResource(itemService);
        this.restItemMockMvc = MockMvcBuilders.standaloneSetup(itemResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        itemSearchRepository.deleteAll();
        item = createEntity(em);
    }

    @Test
    @Transactional
    public void getAllItems() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList
        restItemMockMvc.perform(get("/api/items?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].batchTracked").value(hasItem(DEFAULT_BATCH_TRACKED.booleanValue())))
            .andExpect(jsonPath("$.[*].expiryDateRequired").value(hasItem(DEFAULT_EXPIRY_DATE_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS.toString())))
            .andExpect(jsonPath("$.[*].fsnType").value(hasItem(DEFAULT_FSN_TYPE.toString())))
            .andExpect(jsonPath("$.[*].vedCategory").value(hasItem(DEFAULT_VED_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].type.id").value(hasItem(DEFAULT_TYPE.getId().intValue())))
            .andExpect(jsonPath("$.[*].group.id").value(hasItem(DEFAULT_GROUP.getId().intValue())));
    }

    @Test
    @Transactional
    public void getItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get the item
        restItemMockMvc.perform(get("/api/items/{id}", item.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(item.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.batchTracked").value(DEFAULT_BATCH_TRACKED.booleanValue()))
            .andExpect(jsonPath("$.expiryDateRequired").value(DEFAULT_EXPIRY_DATE_REQUIRED.booleanValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.remarks").value(DEFAULT_REMARKS.toString()))
            .andExpect(jsonPath("$.fsnType").value(DEFAULT_FSN_TYPE.toString()))
            .andExpect(jsonPath("$.vedCategory").value(DEFAULT_VED_CATEGORY.toString()))
            .andExpect(jsonPath("$.type.id").value(DEFAULT_TYPE.getId().intValue()))
            .andExpect(jsonPath("$.group.id").value(DEFAULT_GROUP.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingItem() throws Exception {
        // Get the item
        restItemMockMvc.perform(get("/api/items/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchItem() throws Exception {
        // Initialize the database
        itemService.save(item);

        // Search the item
        restItemMockMvc.perform(get("/api/_search/items?query=id:" + item.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].batchTracked").value(hasItem(DEFAULT_BATCH_TRACKED.booleanValue())))
            .andExpect(jsonPath("$.[*].expiryDateRequired").value(hasItem(DEFAULT_EXPIRY_DATE_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS.toString())))
            .andExpect(jsonPath("$.[*].fsnType").value(hasItem(DEFAULT_FSN_TYPE.toString())))
            .andExpect(jsonPath("$.[*].vedCategory").value(hasItem(DEFAULT_VED_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].type.id").value(hasItem(DEFAULT_TYPE.getId().intValue())))
            .andExpect(jsonPath("$.[*].group.id").value(hasItem(DEFAULT_GROUP.getId().intValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Item.class);
    }
}
