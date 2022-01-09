package org.nh.pharmacy.service;

import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

/**
 * Created by Nitesh on 6/23/17.
 */
public interface ElasticSearchQueryService {

    public <T> List<T> queryElasticSearch(String query, Class<T> clazz, String indices);
}
