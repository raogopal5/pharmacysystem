package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionAuditRequestRepository extends JpaRepository<PrescriptionAuditRequest, Long> {

    @Query(value = "select count(pa.id) from prescription_audit_request pa where pa.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord( @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from prescription_audit_request pa where pa.iu_datetime between :fromDate AND :toDate order by pa.iu_datetime,pa.id", nativeQuery = true)
    List<PrescriptionAuditRequest> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

}
