package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.StockCorrection;
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
 * Spring Data JPA repository for the StockCorrection entity.
 */
@SuppressWarnings("unused")
public interface StockCorrectionRepository extends JpaRepository<StockCorrection, DocumentId> {

    @Query("select stockCorrection from StockCorrection stockCorrection where stockCorrection.id=:id and stockCorrection.latest=true")
    StockCorrection findOne(@Param("id") Long id);

    @Query("select stockCorrection from StockCorrection stockCorrection where stockCorrection.documentNumber=:documentNumber and stockCorrection.latest=true")
    StockCorrection findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('stock_correction_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("delete from StockCorrection stockCorrection where stockCorrection.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query("update StockCorrection stockCorrection set stockCorrection.latest=false where stockCorrection.id=:id and stockCorrection.latest=true")
    void updateLatest(@Param("id") Long id);

    List<StockCorrection> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(stockcorr.id) from stock_correction stockcorr where stockcorr.latest=:latest AND stockcorr.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from stock_correction stockcorr where stockcorr.latest = true AND stockcorr.iu_datetime between :fromDate AND :toDate order by stockcorr.iu_datetime", nativeQuery = true)
    List<StockCorrection> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stockCorrection from StockCorrection stockCorrection where stockCorrection.id=:id order by stockCorrection.version desc")
    List<StockCorrection> findAllByIdWithLock(@Param("id") Long id);
}
