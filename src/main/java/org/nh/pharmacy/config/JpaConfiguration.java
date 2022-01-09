package org.nh.pharmacy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

import static org.apache.commons.lang.ArrayUtils.addAll;

/**
 * A JpaConfiguration.
 */

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(JpaProperties.class)
public class JpaConfiguration {

    private final Logger log = LoggerFactory.getLogger(JpaConfiguration.class);

    private final DataSource dataSource;

    private final JpaProperties jpaProperties;

    private static final String[] JBPM_ENTITIES_TO_SCAN = { "org.jbpm.persistence.processinstance", "org.drools.persistence.info", "org.jbpm.process.audit", "org.jbpm.persistence.correlation", "org.jbpm.runtime.manager.impl.jpa", "org.jbpm.services.task.impl.model", "org.jbpm.services.task.audit.impl.model", "org.jbpm.kie.services.impl.store", "org.jbpm.kie.services.impl.query.persistence"};
    private static final String[] JBPM_MAPPING_RESOURCES = {"META-INF/JBPMorm.xml", "META-INF/Taskorm.xml", "META-INF/TaskAuditorm.xml", "META-INF/Servicesorm.xml"};
    private static final String[] ENTITIES_TO_SCAN = (String[]) addAll(new String[]{"org.nh.pharmacy", "org.nh.seqgen","org.nh.ehr","org.nh.billing","org.nh.jbpm"}, JBPM_ENTITIES_TO_SCAN);
    private static final String[] MAPPING_RESOURCES = JBPM_MAPPING_RESOURCES;

    protected JpaConfiguration(DataSource dataSource, JpaProperties jpaProperties) {
        this.dataSource = dataSource;
        this.jpaProperties = jpaProperties;
    }

    @Bean
    @ConditionalOnMissingBean({LocalContainerEntityManagerFactoryBean.class, EntityManagerFactory.class})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.debug("Building entity manager factory");
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setJpaVendorAdapter(hibernateJpaVendorAdapter());
        entityManagerFactory.setPackagesToScan(ENTITIES_TO_SCAN);
        entityManagerFactory.setJpaPropertyMap(hibernateProperties());
        entityManagerFactory.setMappingResources(MAPPING_RESOURCES);
        return entityManagerFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) throws NamingException {
        log.debug("Creating transaction manager");
        return new JpaTransactionManager(entityManagerFactory);
    }

    private HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(jpaProperties.isShowSql());
        hibernateJpaVendorAdapter.setDatabase(jpaProperties.determineDatabase(dataSource));
        hibernateJpaVendorAdapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        hibernateJpaVendorAdapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        return hibernateJpaVendorAdapter;
    }

    private Map<String, Object> hibernateProperties() {
        return new HibernateProperties().determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
    }
}
