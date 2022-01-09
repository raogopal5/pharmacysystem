package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.ItemBarcode;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the ItemBarcode entity.
 */
public interface ItemBarcodeSearchRepository extends ElasticsearchRepository<ItemBarcode, Long> {

    @Query("{\"bool\": {\"must\": [{\"match\": {\"itemCode.raw\": \"?0\"}}]}}")
    ItemBarcode findByItemCode(String itemCode);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"barcode.raw\": \"?0\"}}]}}")
    ItemBarcode findByBarcode(String barcode);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"itemId\": \"?0\"}}]}}")
    ItemBarcode findByItemId(Long itemId);
}
