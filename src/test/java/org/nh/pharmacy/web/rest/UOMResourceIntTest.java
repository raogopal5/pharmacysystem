package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.UOM;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.repository.UOMRepository;
import org.nh.pharmacy.repository.search.UOMSearchRepository;
import org.nh.pharmacy.service.UOMService;
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
 * Test class for the UOMResource REST controller.
 *
 * @see UOMResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class UOMResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ACTIVE = false;

    private static final Float DEFAULT_CONVERSION_FACTOR = 1F;

    private static final ValueSetCode DEFAULT_UOM_TYPE = new ValueSetCode(13L, "UOM_TYPE", "UOM Type", true);

    @Autowired
    private UOMRepository uOMRepository;

    @Autowired
    private UOMService uOMService;

    @Autowired
    private UOMSearchRepository uOMSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restUOMMockMvc;

    private UOM uOM;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        UOMResource uOMResource = new UOMResource(uOMService);
        this.restUOMMockMvc = MockMvcBuilders.standaloneSetup(uOMResource)
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
    public static UOM createEntity(EntityManager em) {
        UOM uOM = new UOM()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .active(DEFAULT_ACTIVE)
            .conversionFactor(DEFAULT_CONVERSION_FACTOR)
            .uomType(DEFAULT_UOM_TYPE);
        uOM.setId(RandomNumber.getRandomNumber());
        return uOM;
    }

    @Before
    public void initTest() {
        uOMSearchRepository.deleteAll();
        uOM = createEntity(em);
    }

    public static UOM createEntityIfNotExist(EntityManager em) {
        List<UOM> uomList = em.createQuery("from " + UOM.class.getName()).getResultList();
        UOM uom = null;
        if (uomList != null && !uomList.isEmpty()) {
            uom = uomList.get(0);
        } else {
            uom = createEntity(em);
            em.persist(uom);
            em.flush();
            uom = (UOM) em.createQuery("from " + UOM.class.getName()).getResultList().get(0);
        }
        return uom;
    }

    @Test
    @Transactional
    public void getAllUOMS() throws Exception {
        // Initialize the database
        uOMRepository.saveAndFlush(uOM);

        // Get all the uOMList
        restUOMMockMvc.perform(get("/api/uoms?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(uOM.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].conversionFactor").value(hasItem(DEFAULT_CONVERSION_FACTOR.doubleValue())))
            .andExpect(jsonPath("$.[*].uomType.id").value(hasItem(DEFAULT_UOM_TYPE.getId().intValue())));
    }

    @Test
    @Transactional
    public void getUOM() throws Exception {
        // Initialize the database
        uOMRepository.saveAndFlush(uOM);

        // Get the uOM
        restUOMMockMvc.perform(get("/api/uoms/{id}", uOM.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(uOM.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.conversionFactor").value(DEFAULT_CONVERSION_FACTOR.doubleValue()))
            .andExpect(jsonPath("$.uomType.id").value(DEFAULT_UOM_TYPE.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingUOM() throws Exception {
        // Get the uOM
        restUOMMockMvc.perform(get("/api/uoms/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchUOM() throws Exception {
        // Initialize the database
        uOMService.save(uOM);

        // Search the uOM
        restUOMMockMvc.perform(get("/api/_search/uoms?query=id:" + uOM.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(uOM.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].conversionFactor").value(hasItem(DEFAULT_CONVERSION_FACTOR.doubleValue())))
            .andExpect(jsonPath("$.[*].uomType.id").value(hasItem(DEFAULT_UOM_TYPE.getId().intValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UOM.class);
    }
}
