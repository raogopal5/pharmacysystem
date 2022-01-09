package org.nh.pharmacy.config;

import org.nh.pharmacy.aop.producer.PharmacyChargeRecordAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class PharmacyChargeRecordAspectConfig {
    @Bean
    public PharmacyChargeRecordAspect publishToChargeRecord() {
        return new PharmacyChargeRecordAspect();
    }
}
