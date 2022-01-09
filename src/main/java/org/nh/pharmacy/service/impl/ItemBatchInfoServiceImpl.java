package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.ItemBatchInfo;
import org.nh.pharmacy.repository.ItemBatchInfoRepository;
import org.nh.pharmacy.repository.search.ItemBatchInfoSearchRepository;
import org.nh.pharmacy.service.ItemBatchInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Created
 */
@Service
@Transactional
public class ItemBatchInfoServiceImpl implements ItemBatchInfoService {

    private final Logger log = LoggerFactory.getLogger(ItemBatchInfoServiceImpl.class);

    private final ItemBatchInfoRepository itemBatchInfoRepository;
    private final ItemBatchInfoSearchRepository itemBatchInfoSearchRepository;
    private final ApplicationProperties applicationProperties;

    public ItemBatchInfoServiceImpl(ItemBatchInfoRepository itemBatchInfoRepository,
                                    ItemBatchInfoSearchRepository itemBatchInfoSearchRepository,
                                    ApplicationProperties applicationProperties) {
        this.itemBatchInfoRepository = itemBatchInfoRepository;
        this.itemBatchInfoSearchRepository = itemBatchInfoSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Save a ItemBatchInfo.
     *
     * @param itemBatchInfo the entity to save
     * @return the persisted entity
     */
    @Override
    public ItemBatchInfo save(ItemBatchInfo itemBatchInfo) {
        log.debug("Request to save ItemBatchInfo : {}", itemBatchInfo);
        ItemBatchInfo result = itemBatchInfoRepository.save(itemBatchInfo);
        itemBatchInfoSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the itemBatchInfos.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemBatchInfo> findAll(Pageable pageable) {
        log.debug("Request to get all itemBatchInfos");
        return itemBatchInfoRepository.findAll(pageable);
    }

    /**
     * Get one ItemBatchInfo by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ItemBatchInfo findOne(Long id) {
        log.debug("Request to get ItemBatchInfo : {}", id);
        return itemBatchInfoRepository.findById(id).get();
    }

    /**
     * @param itemId
     * @param batchNo
     * @return
     */
    @Override
    public ItemBatchInfo findByItemAndBatchNo(Long itemId, String batchNo) {
        log.debug("Request to find By itemId:{} And BatchNo : {}", itemId, batchNo);
        return itemBatchInfoSearchRepository.findByItemAndBatch(itemId, batchNo);
    }

    public ItemBatchInfo createIfNotExists(ItemBatchInfo itemBatchInfo) {
        log.debug("Request to createIfNotExists itemBatchInfo: {}", itemBatchInfo);
        ItemBatchInfo queriedItemBatchInfo = findByItemAndBatchNo(itemBatchInfo.getItemId(), itemBatchInfo.getBatchNo());
        if (Objects.isNull(queriedItemBatchInfo)) {
            itemBatchInfo = save(itemBatchInfo);
        }
        return itemBatchInfo;
    }

    /**
     * Delete the  ItemBatchInfo by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ItemBatchInfo : {}", id);
        itemBatchInfoRepository.deleteById(id);
        itemBatchInfoSearchRepository.deleteById(id);
    }

    /**
     * Search for the ItemBatchInfo corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ItemBatchInfo> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of itemBatchInfos for query {}", query);
        Page<ItemBatchInfo> result = itemBatchInfoSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }

    /**
     *
     */
    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on ItemBatchInfo latest=true");
        List<ItemBatchInfo> data = itemBatchInfoRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            itemBatchInfoSearchRepository.saveAll(data);
        }
    }


    /**
     *
     */
    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of itemBatchInfos");
        itemBatchInfoSearchRepository.deleteAll();
    }
}
