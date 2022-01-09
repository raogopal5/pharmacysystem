package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.enumeration.ItemStoreStockViewType;
import org.nh.pharmacy.repository.*;
import org.nh.pharmacy.repository.search.ItemStoreStockViewSearchRepository;
import org.nh.pharmacy.service.ItemStoreStockViewService;
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
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.nh.pharmacy.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ItemStoreStockViewResource REST controller.
 *
 * @see ItemStoreStockViewResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemStoreStockViewResourceIntTest {

    private static final Long DEFAULT_ITEM_ID = 11L;
    private static final Long UPDATED_ITEM_ID = 2L;

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final ItemStoreStockViewType DEFAULT_TYPE = ItemStoreStockViewType.GENERIC;
    private static final ItemStoreStockViewType UPDATED_TYPE = ItemStoreStockViewType.BRAND;

    private static final Float DEFAULT_AVAILABLE_STOCK = 1F;
    private static final Float UPDATED_AVAILABLE_STOCK = 2F;

    private static final LocalDateTime DEFAULT_STOCKLAST_SYNC_DATE = LocalDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final LocalDateTime UPDATED_STOCKLAST_SYNC_DATE = LocalDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Map<String, Object> DEFAULT_STORE = new HashMap();
    private static final Map<String, Object> UPDATED_STORE = new HashMap();

    private static final Float DEFAULT_CONSUMED_QTY_CURR_MONTH = 1F;
    private static final Float UPDATED_CONSUMED_QTY_CURR_MONTH = 2F;

    private static final Float DEFAULT_CONSUMED_QTY_LAST_MONTH = 1F;
    private static final Float UPDATED_CONSUMED_QTY_LAST_MONTH = 2F;

    private static final Float DEFAULT_TRANSIT_QTY = 1F;
    private static final Float UPDATED_TRANSIT_QTY = 2F;

    @Autowired
    private ItemStoreStockViewRepository itemStoreStockViewRepository;

    @Autowired
    private ItemStoreStockViewService itemStoreStockViewService;

    @Autowired
    private ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restItemStoreStockViewMockMvc;

    private ItemStoreStockView itemStoreStockView;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockFlowRepository stockFlowRepository;

    @Autowired
    private StockSourceRepository stockSourceRepository;

    @Autowired
    private ItemUnitAverageCostRepository itemUnitAverageCostRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private HealthcareServiceCenterRepository healthcareServiceCenterRepository;

    private Stock stock;

    @Autowired
    private ReserveStockRepository reserveStockRepository;

    @Autowired
    private ApplicationProperties applicationProperties;
    @Before
    public void setup() {
        DEFAULT_STORE.put("store1", "AAAAAAAA");
        UPDATED_STORE.put("store2", "BBBBBBBB");
        MockitoAnnotations.initMocks(this);
        ItemStoreStockViewResource itemStoreStockViewResource = new ItemStoreStockViewResource(itemStoreStockViewService,itemStoreStockViewRepository,itemStoreStockViewSearchRepository,applicationProperties);
        this.restItemStoreStockViewMockMvc = MockMvcBuilders.standaloneSetup(itemStoreStockViewResource)
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
    public static ItemStoreStockView createEntity(EntityManager em) {
        ItemStoreStockView itemStoreStockView = new ItemStoreStockView()
            .itemId(DEFAULT_ITEM_ID)
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .availableStock(DEFAULT_AVAILABLE_STOCK)
            .stocklastSyncDate(DEFAULT_STOCKLAST_SYNC_DATE)
            .store(DEFAULT_STORE)
            .consumedQtyCurrMonth(DEFAULT_CONSUMED_QTY_CURR_MONTH)
            .consumedQtyLastMonth(DEFAULT_CONSUMED_QTY_LAST_MONTH)
            .transitQty(DEFAULT_TRANSIT_QTY);
        return itemStoreStockView;
    }

    @Before
    public void initTest() {
        itemStoreStockViewSearchRepository.deleteAll();
        itemStoreStockView = createEntity(em);
    }


    @Test
    @Transactional
    public void searchItemStoreStockView() throws Exception {
        // Initialize the database
        itemStoreStockViewService.save(itemStoreStockView);

        // Search the itemStoreStockView
        restItemStoreStockViewMockMvc.perform(get("/api/_search/item-store-stock-views?query=id:" + itemStoreStockView.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemStoreStockView.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemId").value(hasItem(DEFAULT_ITEM_ID.intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].availableStock").value(hasItem(DEFAULT_AVAILABLE_STOCK.doubleValue())))
            .andExpect(jsonPath("$.[*].stocklastSyncDate").value(hasItem(sameInstant(DEFAULT_STOCKLAST_SYNC_DATE))))
            .andExpect(jsonPath("$.[*].store").isNotEmpty())
            .andExpect(jsonPath("$.[*].consumedQtyCurrMonth").value(hasItem(DEFAULT_CONSUMED_QTY_CURR_MONTH.doubleValue())))
            .andExpect(jsonPath("$.[*].consumedQtyLastMonth").value(hasItem(DEFAULT_CONSUMED_QTY_LAST_MONTH.doubleValue())))
            .andExpect(jsonPath("$.[*].transitQty").value(hasItem(DEFAULT_TRANSIT_QTY.doubleValue())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemStoreStockView.class);
    }

    @Test
    @Transactional
    public void checkUpdateItemStoreStockView() {
        Item item = ItemResourceIntTest.createEntity(em);
        item.setDispensableGenericName("GenericName");
        item = itemRepository.saveAndFlush(item);

        HealthcareServiceCenter hsc = HealthcareServiceCenterResourceIntTest.createEntity(em);
        hsc = healthcareServiceCenterRepository.saveAndFlush(hsc);

        stock = StockResourceIntTest.createEntity(em);
        stock.setItemId(item.getId());
        stock.storeId(hsc.getId());
        stock = stockRepository.saveAndFlush(stock);


        StockFlow stockFlow = StockFlowResourceIntTest.createEntity(em).stockId(stock.getId());
        stockFlow.setItemId(item.getId());
        stockFlow.setStoreId(hsc.getId());
        stockFlowRepository.saveAndFlush(stockFlow);
        Set<Long> ids = new HashSet<>();
        ids.add(item.getId());
        long storeId = stock.getStoreId();
        itemStoreStockViewService.updateItemStoreStockView(ids, storeId);

        ItemStoreStockView itemStoreStockView2 = itemStoreStockViewRepository.findAll().stream()
            .filter(itemStoreStockView1 -> itemStoreStockView1.getType().equals(ItemStoreStockViewType.GENERIC)).findAny().get();
        assertThat(itemStoreStockView2.getType()).isEqualTo(ItemStoreStockViewType.GENERIC);
        assertThat(itemStoreStockView2.getName()).isEqualTo(item.getDispensableGenericName());
    }

    @Test
    @Transactional
    public void createItemStoreStockView() throws Exception {
        int databaseSizeBeforeCreate = itemStoreStockViewRepository.findAll().size();

        // Create the ItemStoreStockView

        restItemStoreStockViewMockMvc.perform(post("/api/item-store-stock-views")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemStoreStockView)))
            .andExpect(status().isCreated());

        // Validate the ItemStoreStockView in the database
        List<ItemStoreStockView> itemStoreStockViewList = itemStoreStockViewRepository.findAll();
        assertThat(itemStoreStockViewList).hasSize(databaseSizeBeforeCreate + 1);
        ItemStoreStockView testItemStoreStockView = itemStoreStockViewList.get(itemStoreStockViewList.size() - 1);
        assertThat(testItemStoreStockView.getCode()).isEqualTo(DEFAULT_CODE);

        // Validate the ItemStoreStockView in ElasticSearch
        ItemStoreStockView itemStoreStockViewEs = itemStoreStockViewSearchRepository.findById(testItemStoreStockView.getId()).get();
        assertThat(itemStoreStockViewEs).isEqualToComparingFieldByField(testItemStoreStockView);
    }

}
