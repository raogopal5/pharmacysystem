package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.Medication;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Medication entity.
 */
@SuppressWarnings("unused")
public interface MedicationRepository extends JpaRepository<Medication,Long> {

    @Query("select distinct medication from Medication medication left join fetch medication.ingredients")
    List<Medication> findAllWithEagerRelationships();

    @Query("select medication from Medication medication left join fetch medication.ingredients where medication.id =:id")
    Medication findOneWithEagerRelationships(@Param("id") Long id);

}
