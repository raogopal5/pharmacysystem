package org.nh.pharmacy.config;

import org.nh.pharmacy.aop.producer.StockServiceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created by Nitesh on 7/6/17.
 */
@Configuration
@EnableAspectJAutoProxy
public class StockServiceAspectConfiguration {

    @Bean
    public StockServiceAspect producerAspect() {
        return new StockServiceAspect();
    }
}
