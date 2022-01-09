package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.SavedAuditCriteria;
import org.nh.pharmacy.domain.dto.AuditCriteria;
import org.nh.pharmacy.repository.SavedAuditCriteriaRepository;
import org.nh.pharmacy.repository.search.SavedAuditCriteriaSearchRepository;
import org.nh.pharmacy.service.SavedAuditCriteriaService;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the SavedAuditCriteriaResource REST controller.
 *
 * @see SavedAuditCriteriaResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class SavedAuditCriteriaResourceIntTest {

    private static final List<AuditCriteria> DEFAULT_AUDIT_CRITERIAS = new ArrayList<>();
    private static final List<AuditCriteria> UPDATED_AUDIT_CRITERIAS = new ArrayList<>();

    private static final Organization DEFAULT_UNIT = new Organization();
    private static final Organization UPDATED_UNIT = new Organization();

    @Autowired
    private SavedAuditCriteriaRepository savedAuditCriteriaRepository;

    @Autowired
    private SavedAuditCriteriaService savedAuditCriteriaService;

    @Autowired
    private SavedAuditCriteriaSearchRepository savedAuditCriteriaSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSavedAuditCriteriaMockMvc;

    private SavedAuditCriteria savedAuditCriteria;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Before
    public void setup() {
        DEFAULT_AUDIT_CRITERIAS.add(createAuditCritera(1l, "DEFAULT_USER", "DEFAULT_USER", "EMP001"));
        UPDATED_AUDIT_CRITERIAS.add(createAuditCritera(1l, "DEFAULT_USER", "DEFAULT_USER", "EMP001"));
        DEFAULT_UNIT.setId(1l);
        UPDATED_UNIT.setId(2l);
        MockitoAnnotations.initMocks(this);
        SavedAuditCriteriaResource savedAuditCriteriaResource = new SavedAuditCriteriaResource(savedAuditCriteriaService,savedAuditCriteriaRepository,savedAuditCriteriaSearchRepository,applicationProperties);
        this.restSavedAuditCriteriaMockMvc = MockMvcBuilders.standaloneSetup(savedAuditCriteriaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    private AuditCriteria createAuditCritera(Long i, String login, String displayName, String empNo) {
        AuditCriteria auditCriteria = new AuditCriteria();
        auditCriteria.setAuditFilter(new ArrayList<>());
        auditCriteria.setAuditingUser(createUser(i, login, displayName, empNo));
        return auditCriteria;
    }

    private UserDTO createUser(Long i, String login, String displayName, String empNo) {
        UserDTO user = new UserDTO();
        user.setId(i);
        user.setLogin(login);
        user.setDisplayName(displayName);
        user.setEmployeeNo(empNo);
        return user;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SavedAuditCriteria createEntity(EntityManager em) {
        SavedAuditCriteria savedAuditCriteria = new SavedAuditCriteria()
            .auditCriterias(DEFAULT_AUDIT_CRITERIAS)
            .unit(DEFAULT_UNIT);
        return savedAuditCriteria;
    }

    @Before
    public void initTest() {
        savedAuditCriteriaSearchRepository.deleteAll();
        savedAuditCriteria = createEntity(em);
    }

    @Test
    @Transactional
    public void createSavedAuditCriteria() throws Exception {
        int databaseSizeBeforeCreate = savedAuditCriteriaRepository.findAll().size();

        // Create the SavedAuditCriteria

        restSavedAuditCriteriaMockMvc.perform(post("/api/saved-audit-criteria")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(savedAuditCriteria)))
            .andExpect(status().isCreated());

        // Validate the SavedAuditCriteria in the database
        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeCreate + 1);
        SavedAuditCriteria testSavedAuditCriteria = savedAuditCriteriaList.get(savedAuditCriteriaList.size() - 1);
        assertThat(testSavedAuditCriteria.getAuditCriterias().get(0).getAuditingUser().getDisplayName()).isEqualTo(DEFAULT_AUDIT_CRITERIAS.get(0).getAuditingUser().getDisplayName());
        assertThat(testSavedAuditCriteria.getUnit().getId()).isEqualTo(DEFAULT_UNIT.getId());

        // Validate the SavedAuditCriteria in Elasticsearch
        SavedAuditCriteria savedAuditCriteriaEs = savedAuditCriteriaSearchRepository.findById(testSavedAuditCriteria.getId()).get();
        assertThat(savedAuditCriteriaEs.getId()).isEqualToComparingFieldByField(testSavedAuditCriteria.getId());
    }

    @Test
    @Transactional
    public void createSavedAuditCriteriaWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = savedAuditCriteriaRepository.findAll().size();

        // Create the SavedAuditCriteria with an existing ID
        SavedAuditCriteria existingSavedAuditCriteria = new SavedAuditCriteria();
        existingSavedAuditCriteria.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSavedAuditCriteriaMockMvc.perform(post("/api/saved-audit-criteria")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(existingSavedAuditCriteria)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkAuditCriteriasIsRequired() throws Exception {
        int databaseSizeBeforeTest = savedAuditCriteriaRepository.findAll().size();
        // set the field null
        savedAuditCriteria.setAuditCriterias(null);

        // Create the SavedAuditCriteria, which fails.

        restSavedAuditCriteriaMockMvc.perform(post("/api/saved-audit-criteria")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(savedAuditCriteria)))
            .andExpect(status().isBadRequest());

        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUnitIsRequired() throws Exception {
        int databaseSizeBeforeTest = savedAuditCriteriaRepository.findAll().size();
        // set the field null
        savedAuditCriteria.setUnit(null);

        // Create the SavedAuditCriteria, which fails.

        restSavedAuditCriteriaMockMvc.perform(post("/api/saved-audit-criteria")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(savedAuditCriteria)))
            .andExpect(status().isBadRequest());

        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSavedAuditCriteria() throws Exception {
        // Initialize the database
        savedAuditCriteriaRepository.saveAndFlush(savedAuditCriteria);

        // Get all the savedAuditCriteriaList
        restSavedAuditCriteriaMockMvc.perform(get("/api/saved-audit-criteria?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedAuditCriteria.getId().intValue())))
            .andExpect(jsonPath("$.[*].unit.id").value(hasItem(DEFAULT_UNIT.getId().intValue())));
    }

    @Test
    @Transactional
    public void getSavedAuditCriteria() throws Exception {
        // Initialize the database
        savedAuditCriteriaRepository.saveAndFlush(savedAuditCriteria);

        // Get the savedAuditCriteria
        restSavedAuditCriteriaMockMvc.perform(get("/api/saved-audit-criteria/{id}", savedAuditCriteria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(savedAuditCriteria.getId().intValue()))
            .andExpect(jsonPath("$.unit.id").value(DEFAULT_UNIT.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingSavedAuditCriteria() throws Exception {
        // Get the savedAuditCriteria
        restSavedAuditCriteriaMockMvc.perform(get("/api/saved-audit-criteria/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSavedAuditCriteria() throws Exception {
        // Initialize the database
        savedAuditCriteriaService.save(savedAuditCriteria);

        int databaseSizeBeforeUpdate = savedAuditCriteriaRepository.findAll().size();

        // Update the savedAuditCriteria
        SavedAuditCriteria updatedSavedAuditCriteria = savedAuditCriteriaRepository.findById(savedAuditCriteria.getId()).get();
        updatedSavedAuditCriteria
            .auditCriterias(UPDATED_AUDIT_CRITERIAS)
            .unit(UPDATED_UNIT);

        restSavedAuditCriteriaMockMvc.perform(put("/api/saved-audit-criteria")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedSavedAuditCriteria)))
            .andExpect(status().isOk());

        // Validate the SavedAuditCriteria in the database
        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeUpdate);
        SavedAuditCriteria testSavedAuditCriteria = savedAuditCriteriaList.get(savedAuditCriteriaList.size() - 1);
        assertThat(testSavedAuditCriteria.getUnit().getId()).isEqualTo(UPDATED_UNIT.getId());

        // Validate the SavedAuditCriteria in Elasticsearch
        SavedAuditCriteria savedAuditCriteriaEs = savedAuditCriteriaSearchRepository.findById(testSavedAuditCriteria.getId()).get();
        assertThat(savedAuditCriteriaEs.getId()).isEqualToComparingFieldByField(testSavedAuditCriteria.getId());
    }

    @Test
    @Transactional
    public void updateNonExistingSavedAuditCriteria() throws Exception {
        int databaseSizeBeforeUpdate = savedAuditCriteriaRepository.findAll().size();

        // Create the SavedAuditCriteria

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSavedAuditCriteriaMockMvc.perform(put("/api/saved-audit-criteria")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(savedAuditCriteria)))
            .andExpect(status().isCreated());

        // Validate the SavedAuditCriteria in the database
        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSavedAuditCriteria() throws Exception {
        // Initialize the database
        savedAuditCriteriaService.save(savedAuditCriteria);

        int databaseSizeBeforeDelete = savedAuditCriteriaRepository.findAll().size();

        // Get the savedAuditCriteria
        restSavedAuditCriteriaMockMvc.perform(delete("/api/saved-audit-criteria/{id}", savedAuditCriteria.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean savedAuditCriteriaExistsInEs = savedAuditCriteriaSearchRepository.existsById(savedAuditCriteria.getId());
        assertThat(savedAuditCriteriaExistsInEs).isFalse();

        // Validate the database is empty
        List<SavedAuditCriteria> savedAuditCriteriaList = savedAuditCriteriaRepository.findAll();
        assertThat(savedAuditCriteriaList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchSavedAuditCriteria() throws Exception {
        // Initialize the database
        savedAuditCriteriaService.save(savedAuditCriteria);

        // Search the savedAuditCriteria
        restSavedAuditCriteriaMockMvc.perform(get("/api/_search/saved-audit-criteria?query=id:" + savedAuditCriteria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedAuditCriteria.getId().intValue())))
            .andExpect(jsonPath("$.[*].unit.id").value(hasItem(DEFAULT_UNIT.getId().intValue())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SavedAuditCriteria.class);
    }
}
