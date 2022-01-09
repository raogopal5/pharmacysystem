package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.ItemBarcode;
import org.nh.pharmacy.repository.ItemBarcodeRepository;
import org.nh.pharmacy.repository.search.ItemBarcodeSearchRepository;
import org.nh.pharmacy.service.ItemBarcodeService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ItemBarcodeResource REST controller.
 *
 * @see ItemBarcodeResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemBarcodeResourceIntTest {

    private static final Long DEFAULT_ITEM_ID = 10001L;
    private static final Long UPDATED_ITEM_ID = 10002L;

    private static final String DEFAULT_ITEM_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ITEM_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ITEM_CODE = "AAAAAAAAAA";
    private static final String UPDATED_ITEM_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_BARCODE = "AAAAAAAAAA";
    private static final String UPDATED_BARCODE = "BBBBBBBBBB";

    @Autowired
    private ItemBarcodeRepository itemBarcodeRepository;

    @Autowired
    private ItemBarcodeSearchRepository itemBarcodeSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restItemBarcodeMockMvc;

    private ItemBarcode itemBarcode;

    @Autowired
    private ItemBarcodeService itemBarcodeService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemBarcodeResource itemBarcodeResource = new ItemBarcodeResource(itemBarcodeService,itemBarcodeRepository,itemBarcodeSearchRepository,applicationProperties);
//        ReflectionTestUtils.setField(itemBarcodeResource, "itemBarcodeSearchRepository", itemBarcodeSearchRepository);
//        ReflectionTestUtils.setField(itemBarcodeResource, "itemBarcodeRepository", itemBarcodeRepository);
        this.restItemBarcodeMockMvc = MockMvcBuilders.standaloneSetup(itemBarcodeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemBarcode createEntity(EntityManager em) {
        ItemBarcode itemBarcode = new ItemBarcode()
            .itemName(DEFAULT_ITEM_NAME)
            .itemId(DEFAULT_ITEM_ID)
            .itemCode(DEFAULT_ITEM_CODE)
            .barcode(DEFAULT_BARCODE);
        return itemBarcode;
    }

    @Before
    public void initTest() {
        itemBarcodeSearchRepository.deleteAll();
        itemBarcode = createEntity(em);
    }

    @Test
    @Transactional
    public void createItemBarcode() throws Exception {
        int databaseSizeBeforeCreate = itemBarcodeRepository.findAll().size();

        // Create the ItemBarcode

        restItemBarcodeMockMvc.perform(post("/api/item-barcodes")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemBarcode)))
            .andExpect(status().isCreated());

        // Validate the ItemBarcode in the database
        List<ItemBarcode> itemBarcodes = itemBarcodeRepository.findAll();
        assertThat(itemBarcodes).hasSize(databaseSizeBeforeCreate + 1);
        ItemBarcode testItemBarcode = itemBarcodes.get(itemBarcodes.size() - 1);
        assertThat(testItemBarcode.getItemId()).isEqualTo(DEFAULT_ITEM_ID);
        assertThat(testItemBarcode.getItemName()).isEqualTo(DEFAULT_ITEM_NAME);
        assertThat(testItemBarcode.getItemCode()).isEqualTo(DEFAULT_ITEM_CODE);
        assertThat(testItemBarcode.getBarcode()).isEqualTo(DEFAULT_BARCODE);

        // Validate the ItemBarcode in ElasticSearch
        ItemBarcode itemBarcodeEs = itemBarcodeSearchRepository.findById(testItemBarcode.getId()).get();
        assertThat(itemBarcodeEs).isEqualToComparingFieldByField(testItemBarcode);
    }

    @Test
    @Transactional
    public void getAllItemBarcodes() throws Exception {
        // Initialize the database
        itemBarcodeRepository.saveAndFlush(itemBarcode);

        // Get all the itemBarcodes
        restItemBarcodeMockMvc.perform(get("/api/item-barcodes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemBarcode.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(DEFAULT_ITEM_ID.intValue())))
            .andExpect(jsonPath("$.[*].itemName").value(hasItem(DEFAULT_ITEM_NAME.toString())))
            .andExpect(jsonPath("$.[*].itemCode").value(hasItem(DEFAULT_ITEM_CODE.toString())))
            .andExpect(jsonPath("$.[*].barcode").value(hasItem(DEFAULT_BARCODE.toString())));
    }

    @Test
    @Transactional
    public void getItemBarcode() throws Exception {
        // Initialize the database
        itemBarcodeRepository.saveAndFlush(itemBarcode);

        // Get the itemBarcode
        restItemBarcodeMockMvc.perform(get("/api/item-barcodes/{id}", itemBarcode.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(itemBarcode.getId().intValue()))
            .andExpect(jsonPath("$.itemId").value(DEFAULT_ITEM_ID.longValue()))
            .andExpect(jsonPath("$.itemName").value(DEFAULT_ITEM_NAME.toString()))
            .andExpect(jsonPath("$.itemCode").value(DEFAULT_ITEM_CODE.toString()))
            .andExpect(jsonPath("$.barcode").value(DEFAULT_BARCODE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingItemBarcode() throws Exception {
        // Get the itemBarcode
        restItemBarcodeMockMvc.perform(get("/api/item-barcodes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItemBarcode() throws Exception {
        // Initialize the database
        itemBarcodeRepository.saveAndFlush(itemBarcode);
        itemBarcodeSearchRepository.save(itemBarcode);
        int databaseSizeBeforeUpdate = itemBarcodeRepository.findAll().size();

        // Update the itemBarcode
        ItemBarcode updatedItemBarcode = itemBarcodeRepository.findById(itemBarcode.getId()).get();
        updatedItemBarcode
            .itemName(UPDATED_ITEM_NAME)
            .itemId(UPDATED_ITEM_ID)
            .itemCode(UPDATED_ITEM_CODE)
            .barcode(UPDATED_BARCODE);

        restItemBarcodeMockMvc.perform(put("/api/item-barcodes")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedItemBarcode)))
            .andExpect(status().isOk());

        // Validate the ItemBarcode in the database
        List<ItemBarcode> itemBarcodes = itemBarcodeRepository.findAll();
        assertThat(itemBarcodes).hasSize(databaseSizeBeforeUpdate);
        ItemBarcode testItemBarcode = itemBarcodes.get(itemBarcodes.size() - 1);
        assertThat(testItemBarcode.getItemId()).isEqualTo(UPDATED_ITEM_ID);
        assertThat(testItemBarcode.getItemName()).isEqualTo(UPDATED_ITEM_NAME);
        assertThat(testItemBarcode.getItemCode()).isEqualTo(UPDATED_ITEM_CODE);
        assertThat(testItemBarcode.getBarcode()).isEqualTo(UPDATED_BARCODE);

        // Validate the ItemBarcode in ElasticSearch
        ItemBarcode itemBarcodeEs = itemBarcodeSearchRepository.findById(testItemBarcode.getId()).get();
        assertThat(itemBarcodeEs).isEqualToComparingFieldByField(testItemBarcode);
    }

    @Test
    @Transactional
    public void deleteItemBarcode() throws Exception {
        // Initialize the database
        itemBarcodeRepository.saveAndFlush(itemBarcode);
        itemBarcodeSearchRepository.save(itemBarcode);
        int databaseSizeBeforeDelete = itemBarcodeRepository.findAll().size();

        // Get the itemBarcode
        restItemBarcodeMockMvc.perform(delete("/api/item-barcodes/{id}", itemBarcode.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean itemBarcodeExistsInEs = itemBarcodeSearchRepository.existsById(itemBarcode.getId());
        assertThat(itemBarcodeExistsInEs).isFalse();

        // Validate the database is empty
        List<ItemBarcode> itemBarcodes = itemBarcodeRepository.findAll();
        assertThat(itemBarcodes).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchItemBarcode() throws Exception {
        // Initialize the database
        itemBarcodeRepository.saveAndFlush(itemBarcode);
        itemBarcodeSearchRepository.save(itemBarcode);

        // Search the itemBarcode
        restItemBarcodeMockMvc.perform(get("/api/_search/item-barcodes?query=id:" + itemBarcode.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemBarcode.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(DEFAULT_ITEM_ID.intValue())))
            .andExpect(jsonPath("$.[*].itemName").value(hasItem(DEFAULT_ITEM_NAME.toString())))
            .andExpect(jsonPath("$.[*].itemCode").value(hasItem(DEFAULT_ITEM_CODE.toString())))
            .andExpect(jsonPath("$.[*].barcode").value(hasItem(DEFAULT_BARCODE.toString())));
    }
}
