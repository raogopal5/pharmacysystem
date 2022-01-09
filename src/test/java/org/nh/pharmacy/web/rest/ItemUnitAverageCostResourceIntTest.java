package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.ItemUnitAverageCost;
import org.nh.pharmacy.repository.ItemUnitAverageCostRepository;
import org.nh.pharmacy.service.ItemUnitAverageCostService;
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
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.common.util.BigDecimalUtil.getBigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ItemUnitAverageCostResource REST controller.
 *
 * @see ItemUnitAverageCostResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemUnitAverageCostResourceIntTest {

    private static final Long DEFAULT_UNIT_ID = 11L;
    private static final Long UPDATED_UNIT_ID = 22L;

    private static final Long DEFAULT_ITEM_ID = 11L;
    private static final Long UPDATED_ITEM_ID = 22L;

    private static final BigDecimal DEFAULT_AVERAGE_COST = BigDecimal.ONE;
    private static final BigDecimal UPDATED_AVERAGE_COST = getBigDecimal(2F);

    @Autowired
    private ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    @Autowired
    private ItemUnitAverageCostService itemUnitAverageCostService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restItemUnitAverageCostMockMvc;

    private ItemUnitAverageCost itemUnitAverageCost;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemUnitAverageCostResource itemUnitAverageCostResource = new ItemUnitAverageCostResource(itemUnitAverageCostService);
        this.restItemUnitAverageCostMockMvc = MockMvcBuilders.standaloneSetup(itemUnitAverageCostResource)
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
    public static ItemUnitAverageCost createEntity(EntityManager em) {
        ItemUnitAverageCost itemUnitAverageCost = new ItemUnitAverageCost()
            .unitId(DEFAULT_UNIT_ID)
            .itemId(DEFAULT_ITEM_ID)
            .averageCost(DEFAULT_AVERAGE_COST);
        return itemUnitAverageCost;
    }

    @Before
    public void initTest() {
        itemUnitAverageCost = createEntity(em);
    }

    @Test
    @Transactional
    public void createItemUnitAverageCost() throws Exception {
        int databaseSizeBeforeCreate = itemUnitAverageCostRepository.findAll().size();

        // Create the ItemUnitAverageCost

        restItemUnitAverageCostMockMvc.perform(post("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemUnitAverageCost)))
            .andExpect(status().isCreated());

        // Validate the ItemUnitAverageCost in the database
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeCreate + 1);
        ItemUnitAverageCost testItemUnitAverageCost = itemUnitAverageCostList.get(itemUnitAverageCostList.size() - 1);
        assertThat(testItemUnitAverageCost.getUnitId()).isEqualTo(DEFAULT_UNIT_ID);
        assertThat(testItemUnitAverageCost.getItemId()).isEqualTo(DEFAULT_ITEM_ID);
        assertThat(testItemUnitAverageCost.getAverageCost()).isEqualTo(DEFAULT_AVERAGE_COST);
    }

    @Test
    @Transactional
    public void createItemUnitAverageCostWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = itemUnitAverageCostRepository.findAll().size();

        // Create the ItemUnitAverageCost with an existing ID
        ItemUnitAverageCost existingItemUnitAverageCost = new ItemUnitAverageCost();
        existingItemUnitAverageCost.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restItemUnitAverageCostMockMvc.perform(post("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingItemUnitAverageCost)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkUnitIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemUnitAverageCostRepository.findAll().size();
        // set the field null
        itemUnitAverageCost.setUnitId(null);

        // Create the ItemUnitAverageCost, which fails.

        restItemUnitAverageCostMockMvc.perform(post("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemUnitAverageCost)))
            .andExpect(status().isBadRequest());

        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkItemIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemUnitAverageCostRepository.findAll().size();
        // set the field null
        itemUnitAverageCost.setItemId(null);

        // Create the ItemUnitAverageCost, which fails.

        restItemUnitAverageCostMockMvc.perform(post("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemUnitAverageCost)))
            .andExpect(status().isBadRequest());

        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAverageCostIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemUnitAverageCostRepository.findAll().size();
        // set the field null
        itemUnitAverageCost.setAverageCost(null);

        // Create the ItemUnitAverageCost, which fails.

        restItemUnitAverageCostMockMvc.perform(post("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemUnitAverageCost)))
            .andExpect(status().isBadRequest());

        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllItemUnitAverageCosts() throws Exception {
        // Initialize the database
        itemUnitAverageCostRepository.saveAndFlush(itemUnitAverageCost);

        // Get all the itemUnitAverageCostList
        restItemUnitAverageCostMockMvc.perform(get("/api/item-unit-average-costs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemUnitAverageCost.getId().intValue())))
            .andExpect(jsonPath("$.[*].unitId").value(hasItem(DEFAULT_UNIT_ID.intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(DEFAULT_ITEM_ID.intValue())))
            .andExpect(jsonPath("$.[*].averageCost").value(hasItem(DEFAULT_AVERAGE_COST.doubleValue())));
    }

    @Test
    @Transactional
    public void getItemUnitAverageCost() throws Exception {
        // Initialize the database
        itemUnitAverageCostRepository.saveAndFlush(itemUnitAverageCost);

        // Get the itemUnitAverageCost
        restItemUnitAverageCostMockMvc.perform(get("/api/item-unit-average-costs/{id}", itemUnitAverageCost.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(itemUnitAverageCost.getId().intValue()))
            .andExpect(jsonPath("$.unitId").value(DEFAULT_UNIT_ID.intValue()))
            .andExpect(jsonPath("$.itemId").value(DEFAULT_ITEM_ID.intValue()))
            .andExpect(jsonPath("$.averageCost").value(DEFAULT_AVERAGE_COST.doubleValue()));
    }

    @Test
    @Transactional
    public void getNonExistingItemUnitAverageCost() throws Exception {
        // Get the itemUnitAverageCost
        restItemUnitAverageCostMockMvc.perform(get("/api/item-unit-average-costs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItemUnitAverageCost() throws Exception {
        // Initialize the database
        itemUnitAverageCostService.save(itemUnitAverageCost);

        int databaseSizeBeforeUpdate = itemUnitAverageCostRepository.findAll().size();

        // Update the itemUnitAverageCost
        ItemUnitAverageCost updatedItemUnitAverageCost = itemUnitAverageCostRepository.findById(itemUnitAverageCost.getId()).get();
        updatedItemUnitAverageCost
            .unitId(UPDATED_UNIT_ID)
            .itemId(UPDATED_ITEM_ID)
            .averageCost(UPDATED_AVERAGE_COST);

        restItemUnitAverageCostMockMvc.perform(put("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedItemUnitAverageCost)))
            .andExpect(status().isOk());

        // Validate the ItemUnitAverageCost in the database
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeUpdate);
        ItemUnitAverageCost testItemUnitAverageCost = itemUnitAverageCostList.get(itemUnitAverageCostList.size() - 1);
        assertThat(testItemUnitAverageCost.getUnitId()).isEqualTo(UPDATED_UNIT_ID);
        assertThat(testItemUnitAverageCost.getItemId()).isEqualTo(UPDATED_ITEM_ID);
        assertThat(testItemUnitAverageCost.getAverageCost()).isEqualTo(UPDATED_AVERAGE_COST);
    }

    @Test
    @Transactional
    public void updateNonExistingItemUnitAverageCost() throws Exception {
        int databaseSizeBeforeUpdate = itemUnitAverageCostRepository.findAll().size();

        // Create the ItemUnitAverageCost

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restItemUnitAverageCostMockMvc.perform(put("/api/item-unit-average-costs")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemUnitAverageCost)))
            .andExpect(status().isCreated());

        // Validate the ItemUnitAverageCost in the database
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteItemUnitAverageCost() throws Exception {
        // Initialize the database
        itemUnitAverageCostService.save(itemUnitAverageCost);

        int databaseSizeBeforeDelete = itemUnitAverageCostRepository.findAll().size();

        // Get the itemUnitAverageCost
        restItemUnitAverageCostMockMvc.perform(delete("/api/item-unit-average-costs/{id}", itemUnitAverageCost.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ItemUnitAverageCost> itemUnitAverageCostList = itemUnitAverageCostRepository.findAll();
        assertThat(itemUnitAverageCostList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemUnitAverageCost.class);
    }
}
