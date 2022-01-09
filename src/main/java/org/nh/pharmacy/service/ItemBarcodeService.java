package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.ItemBarcode;
import org.nh.pharmacy.domain.ItemStoreStockView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service Interface for managing ItemBarcode.
 */
public interface ItemBarcodeService {

    /**
     * @param itemBarcode
     * @return
     */
    ItemBarcode save(ItemBarcode itemBarcode);

    /**
     * Get all the ItemBarcodes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemBarcode> findAll(Pageable pageable);

    /**
     * Get the "id" ItemBarcode.
     *
     * @param id the id of the entity
     * @return the entity
     */
    ItemBarcode findOne(Long id);

    ItemBarcode findByBarcode(String barcode);

    /**
     * Delete the "id" ItemBarcode.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the ItemBarcode corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ItemBarcode> search(String query, Pageable pageable);

    /**
     * @param barcode
     * @return
     */
    List<ItemStoreStockView> searchStoreItemForBarcode(String barcode, String storeId);

    /**
     * Delete all elastic index of ItemBarcode
     */
    void deleteIndex();

    /**
     * Do elastic index for ItemBarcode
     */
    void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate);
}
