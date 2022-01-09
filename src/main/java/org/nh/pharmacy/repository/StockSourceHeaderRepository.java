package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.StockSourceHeader;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


/**
 * Spring Data JPA repository for the StockSourceHeader entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StockSourceHeaderRepository extends JpaRepository<StockSourceHeader, Long> {

    @Query(value = "select count(ssh.id) from stock_source_header ssh where ssh.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query(value = "select * from stock_source_header ssh where ssh.iu_datetime between :fromDate AND :toDate order by ssh.iu_datetime", nativeQuery = true)
    List<StockSourceHeader> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

}
