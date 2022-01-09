package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockIndent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the StockIndent entity.
 */
@SuppressWarnings("unused")
public interface StockIndentRepository extends JpaRepository<StockIndent, DocumentId> {

    @Query("select stockIndent from StockIndent stockIndent where stockIndent.id=:id and stockIndent.latest=true")
    StockIndent findOne(@Param("id") Long id);

    @Query("select stockIndent from StockIndent stockIndent where stockIndent.documentNumber=:documentNumber and stockIndent.latest=true")
    StockIndent findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('stock_indent_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockIndent stockIndent where stockIndent.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockIndent stockIndent set stockIndent.latest=false where stockIndent.id=:id and stockIndent.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockIndent> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(stockindent.id) from stock_indent stockindent where stockindent.latest=:latest AND stockindent.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_indent stockindent where stockindent.latest = true AND stockindent.iu_datetime between :fromDate AND :toDate order by stockindent.iu_datetime", nativeQuery = true)
    List<StockIndent> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
