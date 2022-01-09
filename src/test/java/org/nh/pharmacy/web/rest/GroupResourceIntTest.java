package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Group;
import org.nh.pharmacy.domain.GroupMember;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.repository.GroupRepository;
import org.nh.pharmacy.repository.search.GroupSearchRepository;
import org.nh.pharmacy.service.GroupService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the GroupResource REST controller.
 *
 * @see GroupResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class GroupResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ACTIVE = false;

    private static final Boolean DEFAULT_ACTUAL = false;

    private static final Context DEFAULT_CONTEXT = Context.Indent_Approval_Committee;

    private static final List<GroupMember> DEFAULT_MEMBERS = Arrays.asList(new GroupMember(new HashMap<String, String>() {{
        put("code", "admin");
        put("name", "admin");
    }}, false));

    private static final ValueSetCode DEFAULT_TYPE = new ValueSetCode(11L, "1.25.2.1.156.1", "Other", true);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupSearchRepository groupSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restGroupMockMvc;

    private Group group;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        GroupResource groupResource = new GroupResource(groupService);
        this.restGroupMockMvc = MockMvcBuilders.standaloneSetup(groupResource)
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
    public static Group createEntity(EntityManager em) {
        Group group = new Group()
            .name(DEFAULT_NAME)
            .code(DEFAULT_CODE)
            .active(DEFAULT_ACTIVE)
            .actual(DEFAULT_ACTUAL)
            .context(DEFAULT_CONTEXT)
            .members(DEFAULT_MEMBERS)
            .type(DEFAULT_TYPE);
        group.setId(RandomNumber.getRandomNumber());
        return group;
    }

    public static Group createEntityIfNotExist(EntityManager em) {
        List<Group> groups = em.createQuery("from " + Group.class.getName()).getResultList();
        Group group = null;
        if (groups != null && !groups.isEmpty()) {
            group = groups.get(0);
        } else {
            group = createEntity(em);
            em.persist(group);
            em.flush();
            group = (Group) em.createQuery("from " + Group.class.getName()).getResultList().get(0);
        }
        return group;
    }

    @Before
    public void initTest() {
        // groupSearchRepository.deleteAll();
        group = createEntity(em);
    }

    @Test
    @Transactional
    public void getAllGroups() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get all the groupList
        restGroupMockMvc.perform(get("/api/groups?size=1&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(group.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].actual").value(hasItem(DEFAULT_ACTUAL.booleanValue())))
            .andExpect(jsonPath("$.[*].members.[*].member").value(hasItem(DEFAULT_MEMBERS.get(0).getMember())))
            .andExpect(jsonPath("$.[*].context").value(hasItem(DEFAULT_CONTEXT.toString())))
            .andExpect(jsonPath("$.[*].type.id").value(hasItem(DEFAULT_TYPE.getId().intValue())));
    }

    @Test
    @Transactional
    public void getGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the group
        restGroupMockMvc.perform(get("/api/groups/{id}", group.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(group.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.actual").value(DEFAULT_ACTUAL.booleanValue()))
            .andExpect(jsonPath("$.context").value(DEFAULT_CONTEXT.toString()))
            .andExpect(jsonPath("$.members.[*].member").value(DEFAULT_MEMBERS.get(0).getMember()))
            .andExpect(jsonPath("$.type.id").value(DEFAULT_TYPE.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingGroup() throws Exception {
        // Get the group
        restGroupMockMvc.perform(get("/api/groups/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchGroup() throws Exception {
        // Initialize the database
        groupService.save(group);

        // Search the group
        restGroupMockMvc.perform(get("/api/_search/groups?query=id:" + group.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(group.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].actual").value(hasItem(DEFAULT_ACTUAL.booleanValue())))
            .andExpect(jsonPath("$.[*].context").value(hasItem(DEFAULT_CONTEXT.toString())))
            .andExpect(jsonPath("$.[*].members.[*].member").value(hasItem(DEFAULT_MEMBERS.get(0).getMember())))
            .andExpect(jsonPath("$.[*].type.id").value(hasItem(DEFAULT_TYPE.getId().intValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Group.class);
    }
}
