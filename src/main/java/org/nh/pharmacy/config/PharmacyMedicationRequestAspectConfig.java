package org.nh.pharmacy.config;

import org.nh.pharmacy.aop.producer.PharmacyMedicationRequestAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class PharmacyMedicationRequestAspectConfig {

    @Bean
    public PharmacyMedicationRequestAspect publishMedicationRequestAspect() {
        return new PharmacyMedicationRequestAspect();
    }
}
