package org.nh.pharmacy.config.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ElasticsearchClientConfiguration {

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    @Value("${spring.data.elasticsearch.properties.path.home}")
    private String pathHome;

    @Value("${spring.data.elasticsearch.properties.path.logs}")
    private String pathLog;

    @Bean
    public Client elasticsearchClient() {
        Settings.Builder settings = Settings.builder();
        settings.put("node.local", true);
        settings.put("cluster.name", clusterName);
        settings.put("http.enabled", false);
        settings.put("path.home", pathHome);
        settings.put("path.log", pathLog);
        settings.put("script.engine.expression.inline.aggs", true);
//        Node node = new LocalNode(settings.build()).start();
        Node node = null;
        return node.client();
    }

    /*private static class LocalNode extends Node {
        LocalNode(Settings settings) {

            super(InternalSettingsPreparer.prepareEnvironment(settings, null),
                Version.CURRENT,
                Collections.singleton(AnalysisICUPlugin.class));
        }
    }*/

}
