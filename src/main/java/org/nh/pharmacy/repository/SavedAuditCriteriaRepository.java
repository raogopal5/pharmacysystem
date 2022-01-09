package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.SavedAuditCriteria;

import org.springframework.data.jpa.repository.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the SavedAuditCriteria entity.
 */
@SuppressWarnings("unused")
public interface SavedAuditCriteriaRepository extends JpaRepository<SavedAuditCriteria,Long> {

    @Query(value = "select count(sac.id) from saved_audit_criterias sac where sac.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query(value = "select * from saved_audit_criterias sac where sac.iu_datetime between :fromDate AND :toDate order by sac.iu_datetime", nativeQuery = true)
    List<SavedAuditCriteria> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

}
