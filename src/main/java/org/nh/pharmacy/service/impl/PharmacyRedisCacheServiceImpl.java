package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.nh.billing.domain.dto.ItemTaxMapping;
import org.nh.billing.util.BillingApiConstants;
import org.nh.common.dto.UserDTO;
import org.nh.common.util.CommonConstants;
import org.nh.pharmacy.domain.Group;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.User;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.dto.Configuration;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.nh.pharmacy.exception.constants.PharmacyErrorCodes.GROUP_NOT_FOUND;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForList;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForObject;

@Service
public class PharmacyRedisCacheServiceImpl implements PharmacyRedisCacheService {

    private final Logger log = LoggerFactory.getLogger(PharmacyRedisCacheServiceImpl.class);

    @Override
    @Cacheable( value = BillingApiConstants.ITEM_TAX_MAPPINGS_CACHE_NAME,key = "#cacheKey",cacheNames = BillingApiConstants.ITEM_TAX_MAPPINGS_CACHE_NAME)
    public List<ItemTaxMapping> fetchItemTaxMapping(String indexName, Query searchQuery, ElasticsearchOperations elasticsearchTemplate, Class<ItemTaxMapping> itemTaxMappingClass, String cacheKey) {
        log.info("Pharmacy: fetching item tax mapping from cache, cache key: {} ", cacheKey);
        return queryForList(indexName, searchQuery, elasticsearchTemplate, itemTaxMappingClass);
    }

    @Override
    @Cacheable(value = CommonConstants.GROUP_CACHE_NAME, key = "#cacheKey",cacheNames = CommonConstants.GROUP_CACHE_NAME)
    public String getCommaSeparatedGroupCodes(Context context, Long unitId, ElasticsearchOperations elasticsearchTemplate, String cacheKey) {
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

    @Override
    @Cacheable(value = CommonConstants.VALUESETCODE_CACHE_NAME, key = "#cacheKey",cacheNames = CommonConstants.VALUESETCODE_CACHE_NAME)
    public List<ValueSetCode> getValueSetCodes(String displayCode, String valueSetCode, ElasticsearchOperations elasticSearchTemplate, String cacheKey) {
        log.info("PHR: searching value set code with cache. Cache Key: {} ", cacheKey);
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery()
            .must(queryStringQuery("display.raw:\"" + displayCode + "\""))
            .must(queryStringQuery("valueSet.code.raw:" + valueSetCode))).build();
        return ElasticSearchUtil.getRecords(searchQuery, ValueSetCode.class, elasticSearchTemplate, "valuesetcode");
    }

    @Override
    @Cacheable(value = CommonConstants.ORGANIZATION_CACHE_NAME, key = "#cacheKey",cacheNames = CommonConstants.ORGANIZATION_CACHE_NAME)
    public Organization fetchOrganization(ElasticsearchOperations elasticsearchTemplate, Long unitId, String cacheKey) {
        log.info("PHR: organization search with cache. cache key: {} ", cacheKey);
        return queryForObject("organization", new CriteriaQuery(new Criteria("id").is(unitId)), elasticsearchTemplate, org.nh.pharmacy.domain.Organization.class);
    }

    @Override
    @Cacheable(value = CommonConstants.CONFIGURATION_CACHE_NAME, key = "#cacheKey",cacheNames = CommonConstants.CONFIGURATION_CACHE_NAME)
    public Configuration getConfiguration(String key, StringBuilder builder, ElasticsearchOperations elasticsearchTemplate, String cacheKey) {
        log.debug("PHR: configuration search with cache. cache key: {} \", cacheKey");
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery(builder.toString()))
            .withPageable(PageRequest.of(0, 1))
            .withSort(SortBuilders.fieldSort("level").order(SortOrder.DESC))
            .build();

        List<Configuration> configurationList = ElasticSearchUtil.getRecords(searchQuery, Configuration.class, elasticsearchTemplate, "configuration");
        if (isNotEmpty(configurationList)) {
            return configurationList.get(0);
        } else {
            throw new IllegalStateException("No configuration found for the key : " + key);
        }
    }

    @Override
    @Cacheable(value = CommonConstants.USER_CACHE_NAME, key = "#cacheKey",cacheNames = CommonConstants.USER_CACHE_NAME)
    public UserDTO getUserData(String cacheKey, ElasticsearchOperations elasticsearchTemplate) {
        log.info("querying user cache with key: {} ", cacheKey);
        return ElasticSearchUtil.queryForObject("user", new CriteriaQuery(new Criteria("login.raw").is(SecurityUtils.getCurrentUserLogin().get())), elasticsearchTemplate, UserDTO.class);
    }

    @Override
    @Cacheable(value = CommonConstants.USER_CACHE_NAME, key = "#cacheKey",cacheNames = CommonConstants.USER_CACHE_NAME)
    public User getUserEntityData(String cacheKey, String login, ElasticsearchOperations elasticsearchTemplate) {
        log.info("querying user entity cache with key: {} ", cacheKey);
        return ElasticSearchUtil.queryForObject("user", new CriteriaQuery(new Criteria("login.raw").is(login)), elasticsearchTemplate, User.class);
    }
}
