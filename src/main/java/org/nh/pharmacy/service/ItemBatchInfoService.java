package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ItemBatchInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service Interface for managing ItemBatchInfo.
 */
public interface ItemBatchInfoService {

    /**
     * @param itemBatchInfo
     * @return
     */
    ItemBatchInfo save(ItemBatchInfo itemBatchInfo);

    ItemBatchInfo createIfNotExists(ItemBatchInfo itemBatchInfo);

    /**
     * Get all the ItemBatchInfos.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemBatchInfo> findAll(Pageable pageable);

    /**
     * Get the "id" ItemBatchInfo.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemBatchInfo findOne(Long id);


    ItemBatchInfo findByItemAndBatchNo(Long itemId, String batchNo);

    /**
     * Delete the "id" ItemBatchInfo.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the ItemBatchInfo corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemBatchInfo> search(String query, Pageable pageable);

    /**
     * Delete all elastic index of ItemBatchInfo
     */
    void deleteIndex();

    /**
     * Do elastic index for ItemBatchInfo
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);
}
