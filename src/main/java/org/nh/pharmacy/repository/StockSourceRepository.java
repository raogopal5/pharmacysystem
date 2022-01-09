package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.StockSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the StockSource entity.
 */
@SuppressWarnings("unused")
public interface StockSourceRepository extends JpaRepository<StockSource,Long> {


    @Query("select stockSource from StockSource stockSource where stockSource.sku =:uniqueId and stockSource.mfrBarcode is not null order by stockSource.id desc")
    List<StockSource> findOneByUniqueId(@Param("uniqueId") String uniqueId);

    @Query("select stockSource from StockSource stockSource where stockSource.sku = :sku and stockSource.barCode is not null order by stockSource.id desc")
    List<StockSource> findBarcode(@Param("sku") String sku);

    @Modifying
    @Query("update StockSource stockSource set stockSource.barCode = :barcode where stockSource.sku = :sku and stockSource.barCode is null")
    void updateBarcode(@Param("barcode") String barcode, @Param("sku") String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockSource from StockSource stockSource where stockSource.sku = :skuId and stockSource.availableQuantity > 0")
    List<StockSource> findBySkuIdByQuantity(@Param("skuId") String skuId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockSource from StockSource stockSource where stockSource.sku = :skuId and stockSource.availableQuantity = 0")
    List<StockSource> findBySkuIdByZeroQuantity(@Param("skuId") String skuId, Pageable pageable);

    @Modifying
    @Query("update StockSource stockSource set stockSource.availableQuantity = (stockSource.availableQuantity - :requestedQuantity), stockSource.lastStockOutDate = :lastStockOutDate where stockSource.id = :id")
    void reduceAvailableQuantity(@Param("id") Long id, @Param("requestedQuantity") Float requestedQuantity, @Param("lastStockOutDate") LocalDate lastStockOutDate);

    @Modifying
    @Query("update StockSource stockSource set stockSource.availableQuantity = (stockSource.availableQuantity + :requestedQuantity), stockSource.lastStockOutDate = :lastStockOutDate where stockSource.id = :id")
    void increaseAvailableQuantity(@Param("id") Long id, @Param("requestedQuantity") Float requestedQuantity, @Param("lastStockOutDate") LocalDate lastStockOutDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockSource from StockSource stockSource where stockSource.sku = :sku and stockSource.availableQuantity > 0")
    List<StockSource> findBySkuIdByQuantityByLastStockOutDate(@Param("sku") String sku,Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockSource from StockSource stockSource where stockSource.sku = :skuId")
    List<StockSource> findBySkuId(@Param("skuId") String skuId);

    @Query(value = "select * from stock_source ss where ss.sku = :skuId order by ss.first_stock_in_date desc limit 1", nativeQuery = true)
    StockSource findBySkuIdByQuantityAndOrderByDate(@Param("skuId") String skuId);

    @Query(value = "select * from stock_source ss where ss.owner = :owner and ss.item_id = :itemId and ss.batch_no = :batchNo and ss.expiry_date = :expiryDate order by ss.id desc limit 1", nativeQuery = true)
    StockSource findByOwnerItemIdBatchNoAndExpiry(@Param("owner") String owner, @Param("itemId") Long itemId, @Param("batchNo") String batchNo, @Param("expiryDate") LocalDate expiryDate);
}
