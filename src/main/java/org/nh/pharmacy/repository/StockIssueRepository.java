package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockIssue;
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
 * Spring Data JPA repository for the StockIssue entity.
 */
@SuppressWarnings("unused")
public interface StockIssueRepository extends JpaRepository<StockIssue, DocumentId> {

    @Query("select stockIssue from StockIssue stockIssue where stockIssue.id=:id and stockIssue.latest=true")
    StockIssue findOne(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockIssue from StockIssue stockIssue where stockIssue.id=:id and stockIssue.latest=true")
    StockIssue findOneWithLock(@Param("id") Long id);

    @Query("select stockIssue from StockIssue stockIssue where stockIssue.documentNumber=:documentNumber and stockIssue.latest=true")
    StockIssue findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('stock_issue_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockIssue stockIssue where stockIssue.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockIssue stockIssue set stockIssue.latest=false where stockIssue.id=:id and stockIssue.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockIssue> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(stockissue.id) from stock_issue stockissue where stockissue.latest=:latest AND stockissue.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_issue stockissue where stockissue.latest = true AND stockissue.iu_datetime between :fromDate AND :toDate order by stockissue.iu_datetime", nativeQuery = true)
    List<StockIssue> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

}
