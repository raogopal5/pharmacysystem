package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.MedicationRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the MedicationRequest entity.
 */
@SuppressWarnings("unused")
public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    MedicationRequest findByDocumentNumber(String documentNumber);


    @Query(value = "select count(medirequest.id) from medication_request medirequest where medirequest.latest=:latest AND medirequest.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalLatestRecord(@Param("latest") Boolean latest, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select * from medication_request medirequest where medirequest.latest = true AND medirequest.iu_datetime between :fromDate AND :toDate order by medirequest.iu_datetime", nativeQuery = true)
    List<MedicationRequest> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

    @Query("select medicationRequest from MedicationRequest medicationRequest where medicationRequest.documentNumber=:documentNumber and medicationRequest.version in (:versionFirst, :versionSecond)")
    List<MedicationRequest> findMedicationRequestByVersions(@Param("documentNumber") String documentNumber,
                                                      @Param("versionFirst") Integer versionFirst,
                                                      @Param("versionSecond") Integer versionSecond);

    @Query("select medicationRequest.version from MedicationRequest medicationRequest where medicationRequest.documentNumber=:documentNumber order by medicationRequest.version")
    List<Integer> filndALlVersion(@Param("documentNumber") String documentNumber);
}
