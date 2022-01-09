package org.nh.pharmacy.service;

import org.nh.billing.domain.dto.ItemTaxMapping;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.User;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.dto.Configuration;
import org.nh.pharmacy.domain.enumeration.Context;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

public interface PharmacyRedisCacheService {

    List<ItemTaxMapping> fetchItemTaxMapping(String indexName, Query searchQuery, ElasticsearchOperations elasticsearchTemplate, Class<ItemTaxMapping> itemTaxMappingClass, String queryString);

    String getCommaSeparatedGroupCodes(Context context, Long unitId, ElasticsearchOperations elasticsearchTemplate, String cacheKey);

    List<ValueSetCode> getValueSetCodes(String displayCode, String valueSetCode, ElasticsearchOperations elasticsearchTemplate, String cacheKey);

    Organization fetchOrganization(ElasticsearchOperations elasticsearchTemplate, Long unitId, String cacheKey);

    Configuration getConfiguration(String key, StringBuilder builder, ElasticsearchOperations elasticsearchOperations, String cacheKey);

    UserDTO getUserData(String cacheKey, ElasticsearchOperations elasticsearchTemplate);

    User getUserEntityData(String cacheKey, String login, ElasticsearchOperations elasticsearchTemplate);
}
