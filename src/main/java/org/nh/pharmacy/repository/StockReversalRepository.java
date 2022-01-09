package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockReversal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the StockReversal entity.
 */
@SuppressWarnings("unused")
public interface StockReversalRepository extends JpaRepository<StockReversal, DocumentId> {

    @Query("select stockReversal from StockReversal stockReversal where stockReversal.id=:id and stockReversal.latest=true")
    StockReversal findOne(@Param("id") Long id);

    @Query(value = "select nextval('stock_reversal_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockReversal stockReversal where stockReversal.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockReversal stockReversal set stockReversal.latest=false where stockReversal.id=:id and stockReversal.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockReversal> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(stockreversal.id) from stock_reversal stockreversal where stockreversal.latest=:latest AND stockreversal.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_reversal stockreversal where stockreversal.latest = true AND stockreversal.iu_datetime between :fromDate AND :toDate order by stockreversal.iu_datetime", nativeQuery = true)
    List<StockReversal> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
