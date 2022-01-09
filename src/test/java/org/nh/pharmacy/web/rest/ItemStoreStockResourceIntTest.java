package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.dto.ItemStoreStock;
import org.nh.pharmacy.repository.search.ItemStoreStockSearchRepository;
import org.nh.pharmacy.service.ItemStoreStockService;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class ItemStoreStockResourceIntTest {

    @Autowired
    private ItemStoreStockService itemStoreStockService;

    @Autowired
    private ItemStoreStockSearchRepository itemStoreStockSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    private MockMvc restItemStoreStockMockMvc;

    private ItemStoreStock itemStoreStock;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ItemStoreStockResource itemStoreLocatorMapResource = new ItemStoreStockResource(itemStoreStockService);
        this.restItemStoreStockMockMvc = MockMvcBuilders.standaloneSetup(itemStoreLocatorMapResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }


    @Before
    public void initTest() {
        itemStoreStockSearchRepository.deleteAll();
        itemStoreStock = createEntity();
    }

    private ItemStoreStock createEntity() {
        ItemStoreStock itemStoreStock = new ItemStoreStock();
        itemStoreStock.setItemId(1l);
        itemStoreStock.setStoreId(2l);
        itemStoreStock.setStock(3f);
        itemStoreStock.assignId();
        return itemStoreStock;

    }

    @Test
    @Transactional
    public void searchItemStoreStock() throws Exception {
        // Initialize the database
        itemStoreStockService.save(itemStoreStock);

        // Search the ItemStoreStock
        restItemStoreStockMockMvc.perform(get("/api/_search/item-store-stock?query=id:" + itemStoreStock.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemStoreStock.getId())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemStoreStock.class);
    }
}
