package org.nh.pharmacy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration implements DisposableBean {

    private final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    private final Environment env;

    private final ServerProperties serverProperties;

    private final DiscoveryClient discoveryClient;

    private Registration registration;

    private final ApplicationProperties applicationProperties;

    public CacheConfiguration(Environment env, ServerProperties serverProperties, DiscoveryClient discoveryClient, ApplicationProperties applicationProperties) {
        this.env = env;
        this.serverProperties = serverProperties;
        this.discoveryClient = discoveryClient;
        this.applicationProperties = applicationProperties;
    }

    @Autowired(required = false)
    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    @Override
    public void destroy() throws Exception {
        log.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }



    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        ApplicationProperties.RedisCacheProperties redisCache = applicationProperties.getRedisCache();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisCache.getRedisHost());
        redisStandaloneConfiguration.setPort(redisCache.getRedisPort());
        return new JedisConnectionFactory(redisStandaloneConfiguration);
        //factory.setUsePool(true);
    }

    /*@Bean
    @Primary
    RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }*/

    @Bean
    public CacheManager redisCacheManager() {
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        RedisSerializationContext.SerializationPair<Object> jsonSerializer =
            RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer);
        RedisCacheManager cacheManager = RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(jedisConnectionFactory())
            .cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofDays(3650))
                    .serializeValuesWith(jsonSerializer)
            )
            .build();
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }






}
