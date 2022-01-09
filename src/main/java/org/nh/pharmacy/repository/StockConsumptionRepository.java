package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockConsumption;
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
 * Spring Data JPA repository for the StockConsumption entity.
 */
@SuppressWarnings("unused")
public interface StockConsumptionRepository extends JpaRepository<StockConsumption, DocumentId> {

    @Query("select stockConsumption from StockConsumption stockConsumption where stockConsumption.id=:id and stockConsumption.latest=true")
    StockConsumption findOne(@Param("id") Long id);

    @Query("select stockConsumption from StockConsumption stockConsumption where stockConsumption.documentNumber=:documentNumber and stockConsumption.latest=true")
    StockConsumption findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockConsumption from StockConsumption stockConsumption where stockConsumption.documentNumber=:documentNumber and stockConsumption.latest=true")
    StockConsumption findByDocumentNumberWithLock(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('stock_consumption_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockConsumption stockConsumption where stockConsumption.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockConsumption stockConsumption set stockConsumption.latest=false where stockConsumption.id=:id and stockConsumption.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockConsumption> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(stockcon.id) from stock_consumption stockcon where stockcon.latest=:latest AND stockcon.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_consumption stockcon where stockcon.latest = true AND stockcon.iu_datetime between :fromDate AND :toDate order by stockcon.iu_datetime", nativeQuery = true)
    List<StockConsumption> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
