package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemBatchInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the ItemBatchInfo entity.
 */
@SuppressWarnings("unused")
public interface ItemBatchInfoRepository extends JpaRepository<ItemBatchInfo,Long> {

    @Query(value = "select count(itemBatchInfo.id) from item_batch_info itemBatchInfo where itemBatchInfo.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from item_batch_info itemBatchInfo where itemBatchInfo.iu_datetime between :fromDate AND :toDate order by itemBatchInfo.iu_datetime", nativeQuery = true)
    List<ItemBatchInfo> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
