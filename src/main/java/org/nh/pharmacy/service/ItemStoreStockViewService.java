package org.nh.pharmacy.service;

import org.nh.common.dto.ItemStoreStockViewDTO;
import org.nh.pharmacy.domain.ItemStoreStockView;
import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.domain.dto.ItemStoreStockViewGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing ItemStoreStockView.
 */
public interface ItemStoreStockViewService {

    /**
     * Save a itemStoreStockView.
     *
     * @param itemStoreStockView the entity to save
     * @return the persisted entity
     */
    ItemStoreStockView save(ItemStoreStockView itemStoreStockView);

    /**
     * Get all the itemStoreStockViews.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemStoreStockView> findAll(Pageable pageable);

    /**
     * Get the "id" itemStoreStockView.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemStoreStockView findOne(Long id);

    /**
     * Delete the "id" itemStoreStockView.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the itemStoreStockView corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemStoreStockView> search(String query, Pageable pageable);

    List<ItemStoreStockViewGroup> searchItemAvailabilityItemStoreStockViews(String query, Pageable pageable);

    /***
     *
     * @param query
     * @param pageable
     * @return
     */
    Page<ItemStoreStockView> searchUniqueItems(String query, Pageable pageable);

    /***
     *
     * @param query
     * @param forStoreId
     * @param pageable
     * @return
     */
    Page<ItemStoreStockView> searchItems(String query, Long forStoreId, Pageable pageable);

    Page<ItemStoreStockView> searchBarcodeItems(String query,String barcode, Long forStoreId, Pageable pageable);

    Page<ItemStoreStockView> searchBatchItems(String batch, Long forStoreId, Pageable pageable);

    void doUpdate(Long storeId, Set<Long> itemIds);

    /**
     * To update ItemStoreStockView for set of ID's
     *
     * @param itemIds,storeId
     */
    public void updateItemStoreStockView(Set<Long> itemIds, Long storeId);

    /**
     * Updates Transit Quantity
     *
     * @param itemIds
     * @param indentStoreIds
     */
    void updateTransitQuantity(Set<Long> itemIds, String indentStoreIds);

    /**
     * Delete all elastic index of itemStoreStockView
     */
    void deleteIndex();

    /**
     * Do elastic index for itemStoreStockView
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);

    /**
     * Update Transit Qty and Available Qty during stock issue
     * @param map
     */
    void updateISSVTransitQty(Map<String, Object> map);
    List<ItemStoreStockViewDTO> searchGenericItem(Long genericId,Long storeId,Pageable pageable,Boolean availableStock);
    List<ItemStoreStockViewDTO> searchStockItem(Medication medication,Long storeId);

    /****
     *
     * @param storeId
     * @param categoryCode
     * @return
     */
    List<Map<String, Object>> getItemStockByStoreIdAndCategoryCode(Long storeId, String categoryCode);
    void updateItemName(Map<String, Object> map);
}
