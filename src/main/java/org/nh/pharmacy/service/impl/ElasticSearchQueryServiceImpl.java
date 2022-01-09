package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.service.ElasticSearchQueryService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForList;

/**
 * Created by Nitesh on 6/23/17.
 */
@Service
public class ElasticSearchQueryServiceImpl implements ElasticSearchQueryService {

    private ElasticsearchOperations elasticsearchTemplate;

    public ElasticSearchQueryServiceImpl(ElasticsearchOperations elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public <T> List<T> queryElasticSearch(String query, Class<T> clazz, String indices) {
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery(query).defaultOperator(Operator.AND)).build();
        return ElasticSearchUtil.getRecords(searchQuery, clazz, elasticsearchTemplate, indices);
    }
}
