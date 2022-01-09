package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.InventoryAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the InventoryAdjustment entity.
 */
@SuppressWarnings("unused")
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment,DocumentId> {

    @Query("select inventoryAdjustment from InventoryAdjustment inventoryAdjustment where inventoryAdjustment.id=:id and inventoryAdjustment.latest=true")
    InventoryAdjustment findOne(@Param("id") Long id);

    @Query("select inventoryAdjustment from InventoryAdjustment inventoryAdjustment where inventoryAdjustment.documentNumber=:documentNumber and inventoryAdjustment.latest=true")
    InventoryAdjustment findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('inventory_adjustment_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from InventoryAdjustment inventoryAdjustment where inventoryAdjustment.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update InventoryAdjustment inventoryAdjustment set inventoryAdjustment.latest=false where inventoryAdjustment.id=:id and inventoryAdjustment.latest=true")
    void updateLatest(@Param("id") Long id);

    List<InventoryAdjustment> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(invenadjust.id) from inventory_adjustment invenadjust where invenadjust.latest=:latest AND invenadjust.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from inventory_adjustment invenadjust where invenadjust.latest = true AND invenadjust.iu_datetime between :fromDate AND :toDate order by invenadjust.iu_datetime", nativeQuery = true)
    List<InventoryAdjustment> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}



