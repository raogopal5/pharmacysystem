package org.nh.pharmacy.config;

/**
 * Created by Indrajeet on 6/29/17.
 */

import liquibase.integration.spring.SpringLiquibase;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;


@Configuration
public class SchedulerConfiguration {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext,
                                 // injecting SpringLiquibase to ensure liquibase is already initialized and created the quartz tables:
                                 SpringLiquibase springLiquibase) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean createSchedulerFactoryBean(DataSource dataSource, JobFactory jobFactory,
                                                           List<Trigger> triggers,
                                                           List<JobDetail> jobDetails,
                                                           PlatformTransactionManager transactionManager) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setSchedulerName("PHARMACY_SCHEDULER");
        // this allows to update triggers in DB when updating settings in config file:
        factory.setOverwriteExistingJobs(false);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setJobFactory(jobFactory);
        factory.setConfigLocation(new ClassPathResource("scheduler.properties"));
        factory.setAutoStartup(applicationProperties.getQuartzScheduler().isEnabled());
        factory.setJobDetails(jobDetails.toArray(new JobDetail[jobDetails.size()]));
        factory.setTriggers(triggers.toArray(new Trigger[triggers.size()]));
        return factory;
    }
}
