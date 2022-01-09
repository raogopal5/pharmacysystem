package org.nh.pharmacy.util;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.nh.common.util.CommonConstants;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.Group;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.dto.Configuration;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.nh.pharmacy.exception.constants.PharmacyErrorCodes.GROUP_NOT_FOUND;

/**
 * A ConfigurationUtil.
 */
public class ConfigurationUtil {

    /**
     * Get configuration value using input parameters
     *
     * @param key    configuration key
     * @param hscId  health care center id
     * @param unitId unit id
     * @return value configuration value
     * @Param elasticsearchTemplate search template
     */
    public static String getConfiguration(String key, Long hscId, Long unitId, Long unitPartOfId, ElasticsearchOperations elasticsearchTemplate) {

        StringBuilder builder = new StringBuilder().append("(").append("key:").append(key).append(" AND ((applicableType:system AND applicableTo:0)");
        if (nonNull(hscId)) {
            builder.append(" OR (applicableType:local AND applicableTo:").append(hscId).append(")");
        }
        if (nonNull(unitId)) {
            builder.append(" OR (applicableType:unit AND applicableTo:").append(unitId).append(")");
        }
        if (nonNull(unitPartOfId)) {
            builder.append(" OR (applicableType:global AND applicableTo:").append(unitPartOfId).append(")");
        } else {
            if (nonNull(unitId)) {
                Query searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(termQuery("id", unitId))
                    .build();
                List<Organization> organizationList = ElasticSearchUtil.getRecords(searchQuery, Organization.class, elasticsearchTemplate, "organization");
                if (isNotEmpty(organizationList)) {
                    Organization partOf = organizationList.get(0).getPartOf();
                    if (nonNull(partOf))
                        builder.append(" OR (applicableType:global AND applicableTo:").append(partOf.getId()).append(")");
                }
            }
        }
        builder.append("))");

        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery(builder.toString()))
            .withPageable(PageRequest.of(0, 1))
            .withSort(SortBuilders.fieldSort("level").order(SortOrder.DESC))
            .build();

        List<Configuration> configurationList = ElasticSearchUtil.getRecords(searchQuery, Configuration.class, elasticsearchTemplate, "configuration");

        if (isNotEmpty(configurationList)) {
            return configurationList.get(0).getValue();
        } else {
            throw new IllegalStateException("No configuration found for the key : " + key);
        }
    }

    /**
     * Get comma separated group codes for workflow
     *
     * @param context context to be searched
     * @param unitId  unit id
     * @return value configuration value
     * @Param elasticsearchTemplate search template
     */
    public static String getCommaSeparatedGroupCodes(Context context, Long unitId, ElasticsearchOperations elasticsearchTemplate) {

        QueryBuilder queryBuilder = boolQuery()
            .must(matchQuery("context", context.name()))
            .must(termQuery("active", true))
            .filter(boolQuery().should(termQuery("partOf.id", unitId))
                               .should(boolQuery().mustNot(existsQuery("partOf"))));

        Query searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();

        List<Group> groupList = ElasticSearchUtil.getRecords(searchQuery, Group.class, elasticsearchTemplate, "group");

        if (isNotEmpty(groupList)) {
            List<String> unitGroupCodeList = new ArrayList<>();
            List<String> globalGroupCodeList = new ArrayList<>();
            for (Group group : groupList) {
                if (nonNull(group.getPartOf())) {
                    unitGroupCodeList.add(group.getCode());
                } else {
                    globalGroupCodeList.add(group.getCode());
                }
            }
            if (isNotEmpty(unitGroupCodeList)) {
                return unitGroupCodeList.stream().map(groupCode -> groupCode.trim()).collect(Collectors.joining(","));
            } else {
                return globalGroupCodeList.stream().map(groupCode -> groupCode.trim()).collect(Collectors.joining(","));
            }
        } else {
            throw new CustomParameterizedException(GROUP_NOT_FOUND, new HashMap<String, Object>(){{put("context", context.getDisplayName());}});
        }
    }

    /**
     * This method is used for fetching organization data
     * @param key
     * @param hscId
     * @param unitId
     * @param unitPartOfId
     * @return
     */
    public static String getConfigurationData(String key, Long hscId, Long unitId, Long unitPartOfId, ElasticsearchOperations elasticsearchTemplate, ApplicationProperties applicationProperties, PharmacyRedisCacheService pharmacyRedisCacheService){
        if(applicationProperties.getRedisCache().isCacheEnabled())        {
            return ConfigurationUtil.getConfigurationWithCache(key, hscId, unitId, unitPartOfId, elasticsearchTemplate, pharmacyRedisCacheService);
        }else {
            return getConfiguration(key, hscId, unitId, unitPartOfId, elasticsearchTemplate);
        }
    }

    /**
     *
     * @param key
     * @param hscId
     * @param unitId
     * @param unitPartOfId
     * @param elasticsearchTemplate
     * @param pharmacyRedisCacheService
     * @return
     */
    public static String getConfigurationWithCache(String key, Long hscId, Long unitId, Long unitPartOfId, ElasticsearchOperations elasticsearchTemplate, PharmacyRedisCacheService pharmacyRedisCacheService) {
        StringBuilder builder = new StringBuilder().append("(").append("key:").append(key).append(" AND ((applicableType:system AND applicableTo:0)");
        StringBuilder cacheKey = new StringBuilder().append("PHR: ").append("key:").append(key).append(" AND (system:0");
        if (nonNull(hscId)) {
            builder.append(" OR (applicableType:local AND applicableTo:").append(hscId).append(")");
            cacheKey.append(" OR hscId:").append(hscId);
        }
        if (nonNull(unitId)) {
            builder.append(" OR (applicableType:unit AND applicableTo:").append(unitId).append(")");
            cacheKey.append(" OR unitId:").append(unitId);
        }
        if (nonNull(unitPartOfId)) {
            builder.append(" OR (applicableType:global AND applicableTo:").append(unitPartOfId).append(")");
            cacheKey.append(" OR unitPartOfId:").append(unitPartOfId);
        } else {
            if (nonNull(unitId)) {
                String orgCacheKey = "PHR: " + "unit.id:" + unitId;
                Organization organization = pharmacyRedisCacheService.fetchOrganization(elasticsearchTemplate,unitId, orgCacheKey);
                if(nonNull(organization) && nonNull(organization.getPartOf())) {
                    builder.append(" OR (applicableType:global AND applicableTo:").append(organization.getPartOf().getId()).append(")");
                    cacheKey.append(" OR partOfId:").append(organization.getPartOf().getId());
                }
            }
        }
        builder.append("))");
        cacheKey.append(")");
        Configuration configuration =  pharmacyRedisCacheService.getConfiguration(key, builder, elasticsearchTemplate, cacheKey.toString());
        return configuration.getValue();
    }
}
