package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.ItemBatchInfo;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the ItemBatchInfo entity.
 */
public interface ItemBatchInfoSearchRepository extends ElasticsearchRepository<ItemBatchInfo, Long> {

    @Query("{\"bool\": {\"must\": [{\"match\": {\"itemId\": \"?0\"}}, {\"match\": {\"batchNo.raw\": \"?1\"}}]}}")
    ItemBatchInfo findByItemAndBatch(Long itemId, String batchNo);
}
