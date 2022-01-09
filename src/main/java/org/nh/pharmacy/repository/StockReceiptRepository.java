package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the StockReceipt entity.
 */
@SuppressWarnings("unused")
public interface StockReceiptRepository extends JpaRepository<StockReceipt, DocumentId> {

    @Query("select stockReceipt from StockReceipt stockReceipt where stockReceipt.id=:id and stockReceipt.latest=true")
    StockReceipt findOne(@Param("id") Long id);

    @Query("select stockReceipt from StockReceipt stockReceipt where stockReceipt.documentNumber=:documentNumber and stockReceipt.latest=true")
    StockReceipt findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('stock_receipt_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockReceipt stockReceipt where stockReceipt.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockReceipt stockReceipt set stockReceipt.latest=false where stockReceipt.id=:id and stockReceipt.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockReceipt> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(stockreceipt.id) from stock_receipt stockreceipt where stockreceipt.latest=:latest AND stockreceipt.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_receipt stockreceipt where stockreceipt.latest = true AND stockreceipt.iu_datetime between :fromDate AND :toDate order by stockreceipt.iu_datetime", nativeQuery = true)
    List<StockReceipt> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

}
