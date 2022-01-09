package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.StockTransit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the StockTransit entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StockTransitRepository extends JpaRepository<StockTransit, Long> {
    @Query("select stockTransit from StockTransit stockTransit where transactionNo=:transactionNo")
    List<StockTransit> findByTransactionNo(@Param("transactionNo") String transactionNo);

    @Query("select stockTransit from StockTransit stockTransit where transactionLineId=:id")
    StockTransit findByTransactionId(@Param("id") Long id);

    @Modifying (clearAutomatically = true)
    @Query("update StockTransit set pendingQuantity=(pendingQuantity - :quantityOut) where stockId = :stockId  AND transactionLineId=:lineId")
    void updateStockTransitQuantity(@Param("quantityOut") Float quantityOut, @Param("stockId") Long stockId, @Param("lineId") Long lineId);
}
