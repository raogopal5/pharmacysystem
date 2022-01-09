package org.nh.pharmacy.config;

import io.github.jhipster.config.JHipsterConstants;
import org.nh.billing.service.PamIntegrationService;
import org.nh.pharmacy.aop.logging.LoggingAspect;
import org.nh.pharmacy.aop.pam.PamIntegrationAspect;
import org.nh.pharmacy.service.DispenseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@EnableAspectJAutoProxy
public class PamIntegrationAspectConfiguration {

    @Bean
    public PamIntegrationAspect pamIntegrationAspect(DispenseService dispenseService,
                                              PamIntegrationService pamIntegrationService) {
        return new PamIntegrationAspect(dispenseService,pamIntegrationService);
    }
}
