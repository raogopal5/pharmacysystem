package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.UOM;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the UOM entity.
 */
@SuppressWarnings("unused")
public interface UOMRepository extends JpaRepository<UOM,Long> {

}
