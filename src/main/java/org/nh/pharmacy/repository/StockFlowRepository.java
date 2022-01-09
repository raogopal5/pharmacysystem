package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.StockFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the StockFlow entity.
 */
@SuppressWarnings("unused")
public interface StockFlowRepository extends JpaRepository<StockFlow,Long> {

    @Query("select stockFlow from StockFlow stockFlow where stockFlow.stockId=:stockId")
    Stock findOneByStockId(@Param("stockId") String stockId);

    @Query("select sum(quantity) from StockFlow stockFlow where stockFlow.itemId=:itemId and stockFlow.transactionDate>=:date1 and stockFlow.transactionDate<=:date2" +
        " and stockFlow.flowType='StockOut' and stockFlow.storeId=:storeId")
    Float findSumOfStockQuantityByItemIdAndTransactionDateBetween(@Param("itemId") Long itemId, @Param("date1") LocalDateTime date1, @Param("date2") LocalDateTime date2,
                                                                  @Param("storeId") Long storeId);

    @Query("select sum(quantity) from StockFlow stockFlow where stockFlow.itemId in (select id from Item where dispensableGenericName =:dispensableGenericName) " +
        "and stockFlow.transactionDate>=:date1 and stockFlow.transactionDate<=:date2 and stockFlow.flowType='StockOut' and stockFlow.storeId=:storeId")
    Float findSumOfStockQuantityByDispensableGenericNameAndTransactionDateBetween(@Param("dispensableGenericName") String dispensableGenericName,
                                                                                  @Param("date1") LocalDateTime date1, @Param("date2") LocalDateTime date2,
                                                                                  @Param("storeId") Long storeId);
}
