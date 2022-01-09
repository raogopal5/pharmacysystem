package org.nh.pharmacy.config.oauth2;

import org.nh.pharmacy.security.oauth2.CookieTokenExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

/**
 * Configures the RefreshFilter refreshing expired OAuth2 token Cookies.
 */
@Configuration
public class OAuth2AuthenticationConfiguration extends ResourceServerConfigurerAdapter {

    /**
     * Configure the ResourceServer security by installing a new TokenExtractor.
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenExtractor(tokenExtractor());
    }

    /**
     * The new TokenExtractor can extract tokens from Cookies and Authorization headers.
     *
     * @return the CookieTokenExtractor bean.
     */
    @Bean
    public TokenExtractor tokenExtractor() {
        return new CookieTokenExtractor();
    }

}
