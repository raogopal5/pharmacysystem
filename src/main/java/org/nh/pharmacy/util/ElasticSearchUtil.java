package org.nh.pharmacy.util;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.nh.common.dto.OrganizationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticSearchUtil {

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchUtil.class);

    public static <T> T getRecord(String index, String query, ElasticsearchOperations elasticsearchTemplate, Class<T> cls) {
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery(query).defaultOperator(Operator.AND)).withPageable(PageRequest.of(0, 1)).build();
        SearchHit<T> searchHit = elasticsearchTemplate.searchOne(searchQuery, cls, IndexCoordinates.of(index));
        T record = searchHit == null ? null : searchHit.getContent();
        log.debug("getRecord() index := " + index + ", query := " + query + ", cls := " + cls + ", record := " + record);
        return record;
    }

    public static <T> List<T> getRecords(String index, String query, Pageable pageable, ElasticsearchOperations elasticsearchTemplate, Class<T> cls) {
        return getPageRecords(index, query, pageable, elasticsearchTemplate, cls).getContent();
    }

    private static <T> Page<T> getPageRecords(String index, String query, Pageable pageable, ElasticsearchOperations elasticsearchTemplate, Class<T> cls) {
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery(query).defaultOperator(Operator.AND)).withPageable(pageable).build();
        SearchHits<T> searchHit = elasticsearchTemplate.search(searchQuery, cls, IndexCoordinates.of(index));
        List<T> records = new ArrayList<>();
        searchHit.getSearchHits().forEach(obj -> records.add(obj.getContent()));

        Page<T> page = new PageImpl<>(records, pageable, searchHit.getTotalHits());
        log.debug("getPageRecords() index := " + index + ", query := " + query + ", cls := " + cls + ", records := " + records);
        return page;
    }


    public static <T> List<T> getRecords(Query query, Class<T> cls, ElasticsearchOperations elasticsearchOperations, String... indexNames) {
        List<T> records = new ArrayList<>();
        SearchHits<T> searchHits = elasticsearchOperations.search(query, cls, IndexCoordinates.of(indexNames));
        searchHits.getSearchHits().forEach(obj -> records.add(obj.getContent()));
        return records;
    }

    public static <T> Page<T> getPageRecords(Query query, Class<T> cls, ElasticsearchOperations elasticsearchOperations, String... indexNames) {
        List<T> records = new ArrayList<>();
        SearchHits<T> searchHits = elasticsearchOperations.search(query, cls, IndexCoordinates.of(indexNames));
        searchHits.getSearchHits().forEach(obj -> records.add(obj.getContent()));
        return new PageImpl<>(records, query.getPageable(), searchHits.getTotalHits());
    }

    public static Aggregations getAggregations(Query query, ElasticsearchOperations elasticsearchOperations, String... indexNames) {
        SearchHits<Map> searchHits = elasticsearchOperations.search(query, Map.class, IndexCoordinates.of(indexNames));
        return searchHits.getAggregations();
    }

    public static <T> T queryForObject(String index, CriteriaQuery criteriaQuery, ElasticsearchOperations elasticsearchTemplate, Class<T> cls) {
        List<T> result = elasticsearchTemplate.search(criteriaQuery, cls, IndexCoordinates.of(index)).get().map(SearchHit::getContent).collect(Collectors.toList());
        return result.size() != 0 ? result.get(0) : null;
    }

    public static <T> List<T> queryForList(String index, CriteriaQuery criteriaQuery, ElasticsearchOperations elasticsearchTemplate, Class<T> cls) {
        return elasticsearchTemplate.search(criteriaQuery, cls, IndexCoordinates.of(index)).get().map(SearchHit::getContent).collect(Collectors.toList());
    }

    public static <T> List<T> queryForList(String index, Query query, ElasticsearchOperations elasticsearchTemplate, Class<T> cls) {
        return elasticsearchTemplate.search(query, cls, IndexCoordinates.of(index)).get().map(SearchHit::getContent).collect(Collectors.toList());
    }
}
