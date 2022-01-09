package org.nh.pharmacy.repository;

import org.nh.billing.domain.FinanceClearance;
import org.nh.pharmacy.domain.DocumentId;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the IPDispenseReturnRequest entity.
 */
@SuppressWarnings("unused")
public interface IPDispenseReturnRequestRepository extends JpaRepository<IPDispenseReturnRequest, DocumentId> {

    @Query("select ipDispenseReturnRequest from IPDispenseReturnRequest ipDispenseReturnRequest where ipDispenseReturnRequest.id=:id and ipDispenseReturnRequest.latest=true")
    IPDispenseReturnRequest findOne(@Param("id") Long id);

    @Modifying
    @Query("delete from IPDispenseReturnRequest ipDispenseReturnRequest where ipDispenseReturnRequest.id=:id")
    void delete(@Param("id") Long id);

    @Modifying
    @Query(value = "update IPDispenseReturnRequest ipDispenseReturnRequest set ipDispenseReturnRequest.latest=false where ipDispenseReturnRequest.id=:id and ipDispenseReturnRequest.latest=true")
    void updateLatest(@Param("id") Long id);

    @Query(value = "select nextval('ipdispense_return_request_id_seq')", nativeQuery = true)
    Long getId();

    @Query(value = "select count(ipdrr.id) from ipdispense_return_request ipdrr where ipdrr.iu_datetime between :fromDate AND :toDate", nativeQuery = true)
    long getTotalRecord(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query(value = "select * from ipdispense_return_request ipdrr where ipdrr.iu_datetime between :fromDate AND :toDate order by ipdrr.iu_datetime", nativeQuery = true)
    List<IPDispenseReturnRequest> findByDateRangeSortById(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
