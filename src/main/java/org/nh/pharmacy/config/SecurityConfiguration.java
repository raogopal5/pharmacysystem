package org.nh.pharmacy.config;

import org.nh.pharmacy.config.oauth2.OAuth2JwtAccessTokenConverter;
import org.nh.pharmacy.config.oauth2.OAuth2Properties;
import org.nh.pharmacy.security.AuthoritiesConstants;
import org.nh.pharmacy.security.oauth2.OAuth2SignatureVerifierClient;
import org.nh.pharmacy.web.filter.MDCFilter;
import org.nh.pharmacy.web.filter.TimeZoneFilter;
import org.nh.security.oauth2.provider.token.CustomUserAuthenticationConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends ResourceServerConfigurerAdapter {
    private final OAuth2Properties oAuth2Properties;

    public SecurityConfiguration(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/**").authenticated()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN);
    }

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(OAuth2SignatureVerifierClient signatureVerifierClient) {
        JwtAccessTokenConverter converter = new OAuth2JwtAccessTokenConverter(oAuth2Properties, signatureVerifierClient);
        ((DefaultAccessTokenConverter)converter.getAccessTokenConverter()).setUserTokenConverter(new CustomUserAuthenticationConverter());
        return converter;
    }

    @Bean
    @Qualifier("loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate(RestTemplateCustomizer customizer) {
        RestTemplate restTemplate = new RestTemplate();
        customizer.customize(restTemplate);
        return restTemplate;
    }

    @Bean
    @Qualifier("vanillaRestTemplate")
    public RestTemplate vanillaRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public FilterRegistrationBean mdcFilterBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        MDCFilter mdcFilter = new MDCFilter();
        registrationBean.setFilter(mdcFilter);
        registrationBean.setOrder(10000);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean timeZoneFilterBean() {
        FilterRegistrationBean timeZoneBean = new FilterRegistrationBean();
        TimeZoneFilter timeZoneFilter = new TimeZoneFilter();
        timeZoneBean.setFilter(timeZoneFilter);
        timeZoneBean.setOrder(10001);
        return timeZoneBean;
    }
}
