package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockAuditPlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the StockAuditPlan entity.
 */
@SuppressWarnings("unused")
public interface StockAuditPlanRepository extends JpaRepository<StockAuditPlan, DocumentId> {
    @Query("select stockAuditPlan from StockAuditPlan stockAuditPlan where stockAuditPlan.id=:id and stockAuditPlan.latest=true")
    StockAuditPlan findOne(@Param("id") Long id);

    @Query(value = "select nextval('stock_audit_plan_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockAuditPlan stockAuditPlan where stockAuditPlan.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockAuditPlan stockAuditPlan set stockAuditPlan.latest=false where stockAuditPlan.id=:id and stockAuditPlan.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockAuditPlan> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(sap.id) from stock_audit_plan sap where sap.latest=:latest AND sap.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_audit_plan sap where sap.latest = true AND sap.iu_datetime between :fromDate AND :toDate order by sap.iu_datetime", nativeQuery = true)
    List<StockAuditPlan> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
