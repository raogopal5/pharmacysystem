package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Calendar;
import org.nh.pharmacy.domain.Holidays;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.enumeration.HolidayType;
import org.nh.pharmacy.repository.CalendarRepository;
import org.nh.pharmacy.repository.search.CalendarSearchRepository;
import org.nh.pharmacy.service.CalendarService;
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
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CalendarResource REST controller.
 *
 * @see CalendarResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class CalendarResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final Boolean DEFAULT_STATUS = false;
    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarSearchRepository calendarSearchRepository;

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

    private MockMvc restCalendarMockMvc;

    private Calendar calendar;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CalendarResource calendarResource = new CalendarResource(calendarService);
        this.restCalendarMockMvc = MockMvcBuilders.standaloneSetup(calendarResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Calendar createEntity(EntityManager em) {
        Calendar calendar = new Calendar()
                .name(DEFAULT_NAME)
                .status(DEFAULT_STATUS)
                .addHolidays(createHolidays())
                .description(DEFAULT_DESCRIPTION);
        // Add required entity
        Organization createdFor = OrganizationResourceIntTest.createEntityIfNotExist(em);
        calendar.setCreatedFor(createdFor);
        calendar.setId(RandomNumber.getRandomNumber());
        return calendar;
    }

    public static Holidays createHolidays(){
        Holidays holidays = new Holidays();
        holidays.setId(RandomNumber.getRandomNumber());
        holidays.setDate(ZonedDateTime.now());
        holidays.setOccasion("Independence day");
        holidays.setType(HolidayType.Public_Holiday);
        return holidays;
    }

    @Before
    public void initTest() {
        if(!elasticsearchTemplate.indexExists(Calendar.class))
            elasticsearchTemplate.createIndex(Calendar.class);
        calendarSearchRepository.deleteAll();
        calendar = createEntity(em);
    }

    @Test
    @Transactional
    public void createCalendar() throws Exception {
        int databaseSizeBeforeCreate = calendarRepository.findAll().size();

        // Create the Calendar
        calendarService.save(calendar);

        // Validate the Calendar in the database
        List<Calendar> calendarList = calendarRepository.findAll();
        assertThat(calendarList).hasSize(databaseSizeBeforeCreate + 1);
        Calendar testCalendar = calendarList.get(calendarList.size() - 1);
        assertThat(testCalendar.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCalendar.isStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testCalendar.getHolidays().size()).isEqualTo(calendar.getHolidays().size());
        assertThat(testCalendar.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);

        // Validate the Calendar in Elasticsearch
        Calendar calendarEs = calendarSearchRepository.findById(testCalendar.getId()).get();
        assertThat(calendarEs).isEqualToComparingFieldByField(testCalendar);
    }

    @Test
    @Transactional
    public void getAllCalendars() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        // Get all the calendarList
        restCalendarMockMvc.perform(get("/api/calendars?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(calendar.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].holidays", hasSize(calendar.getHolidays().size())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    public void getCalendar() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        // Get the calendar
        restCalendarMockMvc.perform(get("/api/calendars/{id}", calendar.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(calendar.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.booleanValue()))
            .andExpect(jsonPath("$.holidays", hasSize(calendar.getHolidays().size())))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCalendar() throws Exception {
        // Get the calendar
        restCalendarMockMvc.perform(get("/api/calendars/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void searchCalendar() throws Exception {
        // Initialize the database
        calendarService.save(calendar);

        // Search the calendar
        restCalendarMockMvc.perform(get("/api/_search/calendars?query=id:" + calendar.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(calendar.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].holidays", hasSize(calendar.getHolidays().size())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Calendar.class);
    }
}
