package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemBarcode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the ItemBarcode entity.
 */
@SuppressWarnings("unused")
public interface ItemBarcodeRepository extends JpaRepository<ItemBarcode,Long> {

    Page<ItemBarcode> findAll(Pageable pageable);

    @Query(value = "select count(itembarcode.id) from item_barcode itembarcode where itembarcode.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query(value = "select * from item_barcode itembarcode where itembarcode.iu_datetime between :fromDate AND :toDate order by itembarcode.iu_datetime", nativeQuery = true)
    List<ItemBarcode> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
