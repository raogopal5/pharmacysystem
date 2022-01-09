package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.ItemBarcode;
import org.nh.pharmacy.domain.ItemStoreStockView;
import org.nh.pharmacy.repository.ItemBarcodeRepository;
import org.nh.pharmacy.repository.search.ItemBarcodeSearchRepository;
import org.nh.pharmacy.repository.search.ItemStoreStockViewSearchRepository;
import org.nh.pharmacy.service.ItemBarcodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Created
 */
@Service
@Transactional
public class ItemBarcodeServiceImpl implements ItemBarcodeService {

    private final Logger log = LoggerFactory.getLogger(ItemBarcodeServiceImpl.class);

    private final ItemBarcodeRepository itemBarcodeRepository;
    private final ItemBarcodeSearchRepository itemBarcodeSearchRepository;
    private final ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository;
    private final ApplicationProperties applicationProperties;

    public ItemBarcodeServiceImpl(ItemBarcodeRepository itemBarcodeRepository,
                                  ItemBarcodeSearchRepository itemBarcodeSearchRepository,
                                  ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository,
                                  ApplicationProperties applicationProperties) {
        this.itemBarcodeRepository = itemBarcodeRepository;
        this.itemBarcodeSearchRepository = itemBarcodeSearchRepository;
        this.itemStoreStockViewSearchRepository = itemStoreStockViewSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Save a itemBarcode.
     *
     * @param itemBarcode the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemBarcode save(ItemBarcode itemBarcode) {
        log.debug("Request to save ItemBarcode : {}", itemBarcode);
        ItemBarcode result = itemBarcodeRepository.save(itemBarcode);
        itemBarcodeSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the itemBarcodes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemBarcode> findAll(Pageable pageable) {
        log.debug("Request to get all ItemBarcodes");
        return itemBarcodeRepository.findAll(pageable);
    }

    /**
     * Get one itemBarcode by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemBarcode findOne(Long id) {
        log.debug("Request to get ItemBarcode : {}", id);
        return itemBarcodeRepository.findById(id).get();
    }

    /**
     *
     * @param barcode
     * @return
     */
    @Override
    public ItemBarcode findByBarcode(String barcode) {
        log.debug("Request to get findByBarcode : {}", barcode);
        return itemBarcodeSearchRepository.findByBarcode(barcode);
    }

    /**
     * Delete the  itemBarcode by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemBarcode : {}", id);
        itemBarcodeRepository.deleteById(id);
        itemBarcodeSearchRepository.deleteById(id);
    }

    /**
     * Search for the itemBarcode corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemBarcode> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ItemBarcodes for query {}", query);
        Page<ItemBarcode> result = itemBarcodeSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ItemStoreStockView> searchStoreItemForBarcode(String barcode, String storeId) {
        List<ItemStoreStockView> allItems = new ArrayList<>();
        String itemCodes = "";
        Iterable<ItemBarcode> itemBarcodeItr = itemBarcodeSearchRepository.search(queryStringQuery("barcode:" + barcode.trim()));

        for (ItemBarcode itemBarcode : itemBarcodeItr) {
            itemCodes = itemCodes + itemBarcode.getItemCode() + " OR ";
        }
        if (itemCodes.length() > 0)
            itemCodes = itemCodes.substring(0, itemCodes.length() - 4);

        if (itemCodes.length() > 0) {
            Iterable<ItemStoreStockView> itemStoreStockViewItr = itemStoreStockViewSearchRepository.search(queryStringQuery(
                "code.raw:\"" + itemCodes + "\" store.id:" + storeId).defaultOperator(Operator.AND));
            itemStoreStockViewItr.forEach(allItems::add);
        }
        return allItems;
    }

    /**
     *
     */
    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on itemBarcode latest=true");
        List<ItemBarcode> data = itemBarcodeRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            itemBarcodeSearchRepository.saveAll(data);
        }
    }


    /**
     *
     */
    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Item Barcodes");
        itemBarcodeSearchRepository.deleteAll();
    }
}
