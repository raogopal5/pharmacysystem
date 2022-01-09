package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the StockAudit entity.
 */
@SuppressWarnings("unused")
public interface StockAuditRepository extends JpaRepository<StockAudit, DocumentId> {

    @Query("select stockAudit from StockAudit stockAudit where stockAudit.id=:id and stockAudit.latest=true")
    StockAudit findOne(@Param("id") Long id);

    @Query("select stockAudit from StockAudit stockAudit where stockAudit.documentNumber=:documentNumber and stockAudit.latest=true")
    StockAudit findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('stock_audit_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockAudit stockAudit where stockAudit.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockAudit stockAudit set stockAudit.latest=false where stockAudit.id=:id and stockAudit.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockAudit> findAllByLatest(Boolean latest, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select stockAudit from StockAudit stockAudit where stockAudit.id=:id and stockAudit.latest=true")
    StockAudit findOneWithLock(@Param("id") Long id);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Query("select stockAudit from StockAudit stockAudit where stockAudit.id=:id and stockAudit.latest=true")
    StockAudit findDetachedOne(@Param("id") Long id);

    @Query(value = "select count(stockaudit.id) from stock_audit stockaudit where stockaudit.latest=:latest AND stockaudit.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_audit stockaudit where stockaudit.latest = true AND stockaudit.iu_datetime between :fromDate AND :toDate order by stockaudit.iu_datetime", nativeQuery = true)
    List<StockAudit> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

}
