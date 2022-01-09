package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.MedicationOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the MedicationOrder entity.
 */
@SuppressWarnings("unused")
public interface MedicationOrderRepository extends JpaRepository<MedicationOrder,Long> {

    @Query(value = "select nextval('medication_order_id_seq')", nativeQuery = true)
    Long getId();

    @Query(value = "select count(medicationorder.id) from medication_order medicationorder where medicationorder.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query(value = "select * from medication_order medicationorder where medicationorder.iu_datetime between :fromDate AND :toDate order by medicationorder.iu_datetime", nativeQuery = true)
    List<MedicationOrder> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);


}
