package org.nh.pharmacy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.nh.common.elastic.converter.ElasticSearchCustomConverter;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchConfiguration {

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client) {
        ElasticsearchRestTemplate elasticsearchRestTemplate = new ElasticsearchRestTemplate(client);
        ElasticSearchCustomConverter.addElasticConverters(elasticsearchRestTemplate);
        return elasticsearchRestTemplate;
    }

    private ObjectMapper mapper;

    public ElasticsearchConfiguration(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /*@Bean
    public EntityMapper getEntityMapper() {
        return new CustomEntityMapper(mapper);
    }

    *//*@Bean
    public ElasticsearchTemplate elasticsearchTemplate(Client client, EntityMapper entityMapper) {
        return new ElasticsearchTemplate(client, entityMapper);
    }*//*

    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
    public ElasticsearchTemplate elasticsearchTemplate() throws UnknownHostException {
        return new ElasticsearchTemplate(elasticsearchClient(), entityMapper());
    }*/

    /*@Bean
    @Primary
    public ElasticsearchOperations elasticsearchTemplate(final JestClient jestClient,
                                                         final ElasticsearchConverter elasticsearchConverter,
                                                         final SimpleElasticsearchMappingContext simpleElasticsearchMappingContext,
                                                         EntityMapper mapper) {
        return new JestElasticsearchTemplate(
            jestClient,
            elasticsearchConverter,
            new DefaultJestResultsMapper(simpleElasticsearchMappingContext, mapper));
    }

    public class CustomEntityMapper implements EntityMapper {

        private ObjectMapper objectMapper;

        public CustomEntityMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
            objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
        }

        @Override
        public String mapToString(Object object) throws IOException {
            return objectMapper.writeValueAsString(object);
        }

        @Override
        public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
            return objectMapper.readValue(source, clazz);
        }

        @Override
        public Map<String, Object> mapObject(Object source) {
            try {
                return objectMapper.readValue(mapToString(source), HashMap.class);
            } catch (IOException e) {
                throw new MappingException(e.getMessage(), e);
            }
        }

        @Override
        public <T> T readObject (Map<String, Object> source, Class<T> targetType) {
            try {
                return mapToObject(mapToString(source), targetType);
            } catch (IOException e) {
                throw new MappingException(e.getMessage(), e);
            }
        }
    }*/

    @Bean
    public String moduleName() {
        return "phr";
    }

}
