package org.nh.pharmacy.job;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;


@Configuration
public class JobConfiguration {

    private static String cronExpression = "0 0 8 1/1 * ? *";

    private static String cronExpDispenseAutoClose = "0 0 * * * ? *";

    @Bean
    public JobDetail dispenseAutoCloseJobDetail() {
        return createJobDetail(DispenseAutoClose.class, "dispenseAutoCloseJobDetail");
    }

    @Bean
    public CronTriggerFactoryBean dispenseAutoCloseJobTrigger(@Qualifier("dispenseAutoCloseJobDetail") JobDetail jobDetail) {
        return createCronTrigger(jobDetail, cronExpDispenseAutoClose, "PHARMACY_DISPENSE_AUTOCLOSE");
    }

    @Bean
    public JobDetail dispenseReturnAutoCloseJobDetail() {
        return createJobDetail(DispenseReturnAutoClose.class, "dispenseReturnAutoCloseJobDetail");
    }

    @Bean
    public CronTriggerFactoryBean dispenseReturnAutoCloseJobTrigger(@Qualifier("dispenseReturnAutoCloseJobDetail") JobDetail jobDetail) {
        return createCronTrigger(jobDetail, cronExpDispenseAutoClose, "PHARMACY_DISPENSERETURN_AUTOCLOSE");
    }

    @Bean
    public JobDetail stockIndentJobDetail() {
        return createJobDetail(StockIndentAutoClose.class, "stockIndentJobDetail");
    }

    @Bean
    public CronTriggerFactoryBean stockIndentJobTrigger(@Qualifier("stockIndentJobDetail") JobDetail jobDetail) {
        return createCronTrigger(jobDetail, cronExpression, "PHARMACY_STOCKINDENT_AUTOCLOSE");
    }

    @Bean
    public JobDetail stockIndentAutoRejectJobDetail() {
        return createJobDetail(StockIndentAutoRejection.class, "stockIndentAutoRejectJobDetail");
    }

    @Bean
    public CronTriggerFactoryBean stockIndentAutoRejectJobTrigger(@Qualifier("stockIndentAutoRejectJobDetail") JobDetail jobDetail) {
        return createCronTrigger(jobDetail, cronExpression, "PHARMACY_STOCKINDENT_AUTOREJECT");
    }

    @Bean
    public JobDetail processInstanceRegenerateJobDetail() {
        return createJobDetail(ProcessInstanceRegenerateJob.class, "processInstanceRegenerateJobDetail");
    }

    @Bean
    public CronTriggerFactoryBean processInstanceRegenerateJobTrigger(@Qualifier("processInstanceRegenerateJobDetail") JobDetail jobDetail) {
        return createCronTrigger(jobDetail,"0 */10 * * * ? *", "PHARMACY_PROCESS_INSTANCE_REGENERATE");
    }

    private CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, String cronExpression, String triggerName) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        factoryBean.setName(triggerName);
        return factoryBean;
    }

    private JobDetail createJobDetail(Class jobClass, String jobName) {
        return JobBuilder.newJob(jobClass)
            .withIdentity(jobName)
            .storeDurably().build();
    }
}
