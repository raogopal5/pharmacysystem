package org.nh.pharmacy.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.SchedulerEntity;
import org.nh.pharmacy.service.SchedulerService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the SchedulerResource REST controller.
 *
 * @see SchedulerResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class SchedulerResourceIntTest {

    private static final String trigger_Name = "PHARMACY_STOCKINDENT_AUTOCLOSE";
    private static final String updated_crone_exp = "0 33 0 * * ? *";
    private static final long negativeNextFireTime = -34223445;
    private static final String wrong_crone_exp = "0 65 0 * * ? *";
    private static final Long lowRepeatIntervalTime = 55555L;
    private static final String simpleTrigger_Name = "TriggerTest";
    private static final Long allowedRepeatIntervalTime = 88888L;
    private static final String injectionQuery = "'anything' OR 1=1";
    private static final String injection_crone_exp = "0 45 0 * * ? *";
    private static final long updatedNextFireTime = 9999999;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    @Autowired
    private SchedulerService schedulerService;

    private MockMvc restSchedulerMockMvc;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private SchedulerResource schedulerResource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restSchedulerMockMvc = MockMvcBuilders.standaloneSetup(schedulerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Test
    @Transactional
    public void getAllInfo() throws Exception {

        // Get all the AllSchedulerList
        restSchedulerMockMvc.perform(get("/api/schedulers"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].scheduler").value(hasItem(trigger_Name.toString())));
    }

    @Test
    @Transactional
    public void updateScheduler() throws Exception {

        // Update the scheduler
        SchedulerEntity schedulerEntity = schedulerService.findBySchedulerName(trigger_Name);
        schedulerEntity.setExpression(updated_crone_exp);
        restSchedulerMockMvc.perform(put("/api/schedulers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(schedulerEntity)))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void updateSchedulerWithNegativeNextFireTime() throws Exception {

        // Update the scheduler
        SchedulerEntity schedulerEntity = schedulerService.findBySchedulerName(trigger_Name);
        schedulerEntity.setExpression(updated_crone_exp);
        schedulerEntity.setNextFireTime(negativeNextFireTime);
        restSchedulerMockMvc.perform(put("/api/schedulers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(schedulerEntity)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateSchedulerWithWrongCroneExpression() throws Exception {

        // Update the scheduler
        SchedulerEntity schedulerEntity = schedulerService.findBySchedulerName(trigger_Name);
        schedulerEntity.setExpression(wrong_crone_exp);
        restSchedulerMockMvc.perform(put("/api/schedulers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(schedulerEntity)))
            .andExpect(status().isBadRequest());
    }

    // simple trigger is not there so can't be tested now

   /* @Test
    @Transactional
    public void updateSchedulerWithLowRepeatIntervalTime() throws Exception {

        // Update the scheduler
        SchedulerEntity schedulerEntity = schedulerService.findBySchedulerName(simpleTrigger_Name);
        schedulerEntity.setRepeatInterval(lowRepeatIntervalTime);
        restSchedulerMockMvc.perform(put("/api/schedulers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(schedulerEntity)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateSchedulerWithAllowedRepeatIntervalTime() throws Exception {

        // Update the scheduler
        SchedulerEntity schedulerEntity = schedulerService.findBySchedulerName(simpleTrigger_Name);
        schedulerEntity.setRepeatInterval(allowedRepeatIntervalTime);
        restSchedulerMockMvc.perform(put("/api/schedulers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(schedulerEntity)))
            .andExpect(status().isOk());
    }*/

    @Test
    @Transactional
    public void sqlInjectionCheckForUpdateSchedulerCroneExpression() throws Exception {

        // Update the scheduler
        SchedulerEntity schedulerEntity = schedulerService.findBySchedulerName(injectionQuery);
        assertThat(schedulerEntity).isEqualTo(null);
        schedulerEntity = new SchedulerEntity();
        schedulerEntity.setScheduler(injectionQuery);
        schedulerEntity.setExpression(updated_crone_exp);
        schedulerEntity.setNextFireTime(negativeNextFireTime);
        restSchedulerMockMvc.perform(put("/api/schedulers")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(schedulerEntity)))
            .andExpect(status().isNotFound());
    }
}




