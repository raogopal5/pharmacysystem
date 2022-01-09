package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemStoreStockView;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the ItemStoreStockView entity.
 */
@SuppressWarnings("unused")
public interface ItemStoreStockViewRepository extends JpaRepository<ItemStoreStockView,Long> {


    @Query(value = "select count(issv.id) from item_store_stock_view issv where issv.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from item_store_stock_view issv where issv.iu_datetime between :fromDate AND :toDate order by issv.iu_datetime", nativeQuery = true)
    List<ItemStoreStockView> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

    @Query("Select item.id, item.name, item.code, item.description, stock.id, "+
        "stock.quantity, stock.batchNo, stock.storeId, stock.unitId from Item item, Stock stock, "+
        "ItemCategory category where stock.itemId = item.id and item.category = category.id "+
        "and stock.storeId = :storeId and category.code = :categoryCode  order by item.name")
    List<Object[]> findItemStockByStoreIdAndCategoryCode(@Param("storeId") Long storeId, @Param("categoryCode") String categoryCode);

    @Query(value = "select * from item_store_stock_view issv where issv.iu_datetime between :fromDate AND :toDate order by issv.iu_datetime", nativeQuery = true)
    List<ItemStoreStockView> findByLocalDateTimeRangeSortById(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, Pageable pageable);

    @Modifying
    @Query("update ItemStoreStockView issv set issv.name=:itemName where issv.code=:itemCode")
    Integer updateItemNameByCode(@Param("itemCode") String itemCode, @Param("itemName") String itemName);
}
