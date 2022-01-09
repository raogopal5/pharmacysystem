package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.dto.DispenseReturnDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the DispenseReturn entity.
 */
@SuppressWarnings("unused")
public interface DispenseReturnRepository extends JpaRepository<DispenseReturn, DocumentId> {

    @Query("select dispenseReturn from DispenseReturn dispenseReturn where dispenseReturn.id=:id and dispenseReturn.latest=true")
    DispenseReturn findOne(@Param("id") Long id);

    @Query("select dispenseReturn from DispenseReturn dispenseReturn where dispenseReturn.documentNumber=:documentNumber and dispenseReturn.latest=true")
    DispenseReturn findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('dispense_return_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("update DispenseReturn dispenseReturn set dispenseReturn.latest=false where dispenseReturn.id=:id and dispenseReturn.latest=true")
    void updateLatest(@Param("id") Long id);

    @Modifying
    @Query("delete from DispenseReturn dispenseReturn where dispenseReturn.id=:id")
    void delete(@Param("id") Long id);

    List<DispenseReturn> findAllByLatest(Boolean latest, Pageable pageable);

    @Query(value = "select count(disreturn.id) from dispense_return disreturn where disreturn.latest=:latest AND disreturn.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from dispense_return disreturn where disreturn.latest = true AND disreturn.iu_datetime between :fromDate AND :toDate order by disreturn.iu_datetime", nativeQuery = true)
    List<DispenseReturn> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

    @Query("select dispenseReturn from DispenseReturn dispenseReturn where dispenseReturn.documentNumber=:documentNumber and dispenseReturn.version in (:versionFirst, :versionSecond)")
    List<DispenseReturn> findDispenseReturnByVersions(@Param("documentNumber") String documentNumber,
                                          @Param("versionFirst") Integer versionFirst,
                                          @Param("versionSecond") Integer versionSecond);

    @Query("select dispenseReturn.version from DispenseReturn dispenseReturn where dispenseReturn.documentNumber=:documentNumber order by dispenseReturn.version")
    List<Integer> filndALlVersion(@Param("documentNumber") String documentNumber);
}
