package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the ReserveStock entity.
 */
@SuppressWarnings("unused")
public interface ReserveStockRepository extends JpaRepository<ReserveStock,Long> {

    @Query("select reserveStock from ReserveStock reserveStock where reserveStock.stockId=:stockId")
    List<ReserveStock> findAllReservedStockByStockId(@Param("stockId") Long stockId);

    @Modifying
    @Query("delete from ReserveStock reserveStock where reserveStock.transactionId=:transactionId and reserveStock.transactionType=:transactionType")
    void deleteReservedStock(@Param("transactionId") Long transactionId, @Param("transactionType") TransactionType transactionType);

    @Modifying
    @Query("delete from ReserveStock reserveStock where reserveStock.transactionId=:transactionId and reserveStock.transactionType=:transactionType and reserveStock.stockId=:stockId")
    void deleteReservedStockByStockId(@Param("transactionId") Long transactionId, @Param("transactionType") TransactionType transactionType, @Param("stockId") Long stockId);

    @Query("select reserveStock from ReserveStock reserveStock where reserveStock.transactionNo =:transactionNumber")
    List<ReserveStock> findByTransactionNumber(@Param("transactionNumber") String transactionNumber);

    @Modifying
    @Query("delete from ReserveStock reserveStock where reserveStock.transactionLineId=:transactionLineId and reserveStock.transactionNo=:transactionNumber")
    void deleteByTransactionNoAndTransactionLineId(@Param("transactionNumber") String transactionNumber, @Param("transactionLineId") Long transactionLineId);

}
