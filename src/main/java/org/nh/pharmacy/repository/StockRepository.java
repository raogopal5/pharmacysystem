package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemUnitAverageCost;
import org.nh.pharmacy.domain.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Spring Data JPA repository for the Stock entity.
 */
@SuppressWarnings("unused")
public interface StockRepository extends JpaRepository<Stock,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from Stock stock where stock.id =:stockId")
    Stock getStockWithLock(@Param("stockId") Long stockId);

    @Query("select stock from Stock stock where stock.itemId =:itemId and stock.batchNo =:batchNo and stock.storeId=:storeId")
    List<Stock> getStockWithFields(@Param("itemId") Long itemId, @Param("batchNo") String batchNo, @Param("storeId") Long storeId);

    @Query("select new org.nh.pharmacy.domain.ItemUnitAverageCost(stock.itemId,stock.unitId, sum(stock.stockValue),sum(stock.quantity)) from Stock stock where stock.itemId =:itemId and stock.unitId =:unitId group by stock.itemId, stock.unitId " )
    ItemUnitAverageCost findStockValueQuantityByItemIdUnitId(@Param("itemId") Long itemId, @Param("unitId") Long unitId);

    @Query("select stock from Stock stock where stock.sku =:uniqueId and stock.storeId =:storeId")
    Stock findOneByUniqueIdStoreId(@Param("uniqueId") String uniqueId, @Param("storeId") Long storeId);

    @Query("select stock from Stock stock where stock.sku =:uniqueId")
    Page<Stock> findOneByUniqueId(@Param("uniqueId") String uniqueId, Pageable pageable);

    @Modifying (clearAutomatically = true)
    @Query("update Stock stock set stock.quantity = (stock.quantity - :quantityOut), stock.stockValue = ((stock.quantity - :quantityOut) * stock.cost) where stock.id = :stockId")
    void reduceStockQuantity(@Param("quantityOut") Float quantityOut, @Param("stockId") Long stockId);

    @Modifying (clearAutomatically = true)
    @Query("update Stock stock set stock.quantity = (stock.quantity - :quantityOut),stock.transitQuantity= (stock.transitQuantity + :quantityOut), stock.stockValue = ((stock.quantity - :quantityOut) * stock.cost) where stock.id = :stockId")
    void updateStockQuantity(@Param("quantityOut") Float quantityOut, @Param("stockId") Long stockId);

    @Modifying (clearAutomatically = true)
    @Query("update Stock stock set stock.transitQuantity= (stock.transitQuantity - :quantityOut) where stock.id = :stockId")
    void reduceStockTransitQuantity(@Param("quantityOut") Float quantityOut, @Param("stockId") Long stockId);

    @Modifying (clearAutomatically = true)
    @Query("update Stock stock set stock.quantity = (stock.quantity + :quantityIn), stock.stockValue = ((stock.quantity + :quantityIn) * stock.cost) where stock.id = :stockId")
    void increaseStockQuantity(@Param("quantityIn") Float quantityIn, @Param("stockId") Long stockId);

    @Query("Select item.id, item.code, item.name, store.id, store.code, store.name, item.dispensableGenericName, sum(stock.quantity) from Stock as stock JOIN Item as item " +
        " on stock.itemId = item.id JOIN HealthcareServiceCenter as store on stock.storeId = store.id where stock.itemId = :itemId and stock.storeId = :storeId" +
        " group by item.id, item.code, item.name, store.id, store.code, store.name, item.dispensableGenericName")
    List<Object[]> findItemStoreDetailsByItemIdAndStoreId(@Param("itemId") Long itemId, @Param("storeId") Long storeId);

    Stock findByItemIdAndStoreId(Long itemId, Long storeId);

    @Query("Select store.id, store.code, store.name, item.dispensableGenericName, sum(stock.quantity) from Stock as stock JOIN Item as item " +
        " on stock.itemId = item.id JOIN HealthcareServiceCenter as store on stock.storeId = store.id where stock.itemId in (select id from Item where " +
        " dispensableGenericName =:dispensableGenericName) and stock.storeId = :storeId group by store.id, store.code, store.name, item.dispensableGenericName")
    List<Object[]> findItemStoreDetailsByDispensableGenericNameAndStoreId(@Param("dispensableGenericName") String dispensableGenericName, @Param("storeId") Long storeId);

    @Query("select item.id, item.code, item.name, cast(sum((s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id),0)))) AS float) AS quantity" +
        " from Stock AS s INNER JOIN Item AS item on s.itemId = item.id where s.storeId = :storeId AND " +
        "item.dispensableGenericName = (select item.dispensableGenericName from item AS item where item.id = :itemId) "+
        "AND (s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id),0))) > 0 AND item.id NOT IN (:itemId) "+
        "group by item.id, item.code, item.name")
    List<Object[]> findItemDetailsByStoreIdAndItemId(@Param("storeId") Long storeId, @Param("itemId") Long itemId);

    @Query("select stock from Stock stock where stock.sku=:sku and stock.itemId=:itemId and stock.storeId=:storeId order by stock.id desc")
    List<Stock> findAllByStoreIdAndItemIdAndSku(@Param("storeId") Long storeId, @Param("itemId") Long itemId, @Param("sku") String sku);

    @Query("select stock from Stock stock where stock.itemId=:itemId and storeId=:storeId and stock.quantity>0")
    List<Stock> findByItemIdAndStoreIdWithPositiveQty(@Param("itemId") Long itemId, @Param("storeId") Long storeId);

    @Query("select stock from Stock stock where stock.itemId=:itemId and storeId=:storeId and stock.quantity = 0")
    List<Stock> findByItemIdAndStoreIdWithZeroQty(@Param("itemId") Long itemId, @Param("storeId") Long storeId);

    @Query("select new org.nh.pharmacy.domain.Stock(s.id, s.blocked, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, "+
        "cast((s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id" +
        " ),0)))as float), s.stockValue, " +
        " s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId))" +
        " from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.storeId = :storeId AND item.code = :itemCode " +
        "AND (s.quantity - coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id" +
        " ),0)) > 0 ")
    List<Stock> findAllBatchDetailsByStoreIdAndItemCodeAndNonZeroQty(@Param("storeId") Long storeId, @Param("itemCode") String itemCode);

    @Query("select new org.nh.pharmacy.domain.Stock(s.id, s.blocked, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, "+
        "cast((s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id" +
        " ),0)))as float), s.stockValue, " +
        " s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId))" +
        " from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.storeId = :storeId AND item.code = :itemCode ")
    List<Stock> findAllBatchDetailsByStoreIdAndItemCode(@Param("storeId") Long storeId, @Param("itemCode") String itemCode);

    @Query("select new org.nh.pharmacy.domain.Stock(s.id, s.blocked, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, "+
        "cast((s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id" +
        " ),0)))as float), s.stockValue, " +
        " s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId))" +
        " from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.storeId = :storeId AND s.blocked = false AND item.code = :itemCode " +
        "AND (s.quantity - coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id" +
        " ),0)) > 0 ")
    List<Stock> findAllUnblockedBatchDetailsByStoreIdAndItemCodeAndNonZeroQty(@Param("storeId") Long storeId, @Param("itemCode") String itemCode);

    @Query("select new org.nh.pharmacy.domain.Stock(s.id, s.blocked, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, "+
        "cast((s.quantity - (coalesce((select sum(rs.quantity) from ReserveStock as rs where rs.stockId = s.id" +
        " ),0)))as float), s.stockValue, " +
        " s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId))" +
        " from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.storeId = :storeId AND s.blocked = false AND item.code = :itemCode ")
    List<Stock> findAllUnblockedBatchDetailsByStoreIdAndItemCode(@Param("storeId") Long storeId, @Param("itemCode") String itemCode);


    @Query("select new org.nh.pharmacy.domain.Stock(s.id, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, "+
        "s.quantity, s.stockValue, " +
        " s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId))" +
        " from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.batchNo = :batchNo AND item.code = :itemCode")
    List<Stock> getStockWithItemCodeAndBatch(@Param("itemCode") String itemCode, @Param("batchNo") String batchNo);

    @Modifying
    @Query("update Stock stock set stock.barcode = :barcode where stock.sku = :sku")
    void updateStockBarcode(@Param("barcode") String barcode, @Param("sku") String sku);

    @Query("select new org.nh.pharmacy.domain.Stock(s.id, s.itemId, s.batchNo, s.expiryDate, s.owner, s.cost, s.mrp, "+
        "s.quantity, s.stockValue, " +
        " s.storeId, s.locatorId, s.supplier, s.uomId, s.sku, s.unitId, s.consignment, s.barcode, (select avgCost.averageCost from ItemUnitAverageCost avgCost where avgCost.unitId = s.unitId and avgCost.itemId = s.itemId))" +
        " from Stock as s INNER JOIN Item as item on s.itemId = item.id where s.storeId = :storeId AND item.code = :itemCode ")
    List<Stock> findStocksByStoreIdAndItemCode(@Param("storeId") Long storeId, @Param("itemCode") String itemCode);

    @Query("select stock.barcode from Stock stock where stock.itemId =:itemId")
    List<String> findBarcodeByItemId(@Param("itemId") Long itemId);

    List<Stock> findByStoreId(@Param("storeId") Long storeId);
}
