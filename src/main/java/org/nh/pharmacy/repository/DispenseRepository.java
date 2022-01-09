package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DocumentId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the Dispense entity.
 */
@SuppressWarnings("unused")
public interface DispenseRepository extends JpaRepository<Dispense,DocumentId> {

    @Query("select dispense from Dispense dispense where dispense.id=:id and dispense.latest=true")
    Dispense findOne(@Param("id") Long id);

    @Query("select dispense from Dispense dispense where dispense.documentNumber=:documentNumber and dispense.latest=true")
    Dispense findOneByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Query(value = "select nextval('dispense_id_seq')", nativeQuery = true)
    Long getId();

    @Modifying
    @Query("update Dispense dispense set dispense.latest=false where dispense.id=:id and dispense.latest=true")
    void updateLatest(@Param("id") Long id);

    @Modifying
    @Query("delete from Dispense dispense where dispense.id=:id")
    void delete(@Param("id") Long id);

    List<Dispense> findAllByLatest(Boolean latest, Pageable pageable);

    @Query("select count(dispense) from Dispense dispense where dispense.latest=:latest")
    long getTotalLatestRecord(@Param("latest") Boolean latest);

    @Query(value = "select count(dispen.id) from dispense dispen where dispen.latest=:latest AND dispen.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from dispense dispen where dispen.latest = true AND dispen.iu_datetime between :fromDate AND :toDate order by dispen.id", nativeQuery = true)
    List<Dispense> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

    @Query("select dispense from Dispense dispense where dispense.documentNumber=:documentNumber and dispense.version in (:versionFirst, :versionSecond)")
    List<Dispense> findDispenseByVersions(@Param("documentNumber") String documentNumber,
                                          @Param("versionFirst") Integer versionFirst,
                                          @Param("versionSecond") Integer versionSecond);

    @Query("select dispense.version from Dispense dispense where dispense.documentNumber=:documentNumber order by dispense.version")
    List<Integer> filndALlVersion(@Param("documentNumber") String documentNumber);

    @Transactional(propagation = Propagation.REQUIRES_NEW)// loading in a new transaction to avoid errors due to version update
    @Query("select dispense from Dispense dispense where dispense.documentNumber=:documentNumber and dispense.latest=true")
    Dispense findReadOnlyOneByDocumentNumber(@Param("documentNumber") String documentNumber);

}
