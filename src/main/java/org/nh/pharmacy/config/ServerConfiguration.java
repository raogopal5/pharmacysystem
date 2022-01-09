package org.nh.pharmacy.config;

import io.undertow.UndertowOptions;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.undertow.ConfigurableUndertowWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class ServerConfiguration implements WebServerFactoryCustomizer<ConfigurableUndertowWebServerFactory> {

    private final ServerProperties serverProperties;

    public ServerConfiguration(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void customize(ConfigurableUndertowWebServerFactory factory) {
        String pattern = serverProperties.getUndertow().getAccesslog().getPattern();

        if (StringUtils.hasLength(pattern) && (pattern.contains("%D") || pattern.contains("%T"))) {
            factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true));
        }

    }

}
