package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.HealthcareServiceCenter;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the HealthcareServiceCenter entity.
 */
@SuppressWarnings("unused")
public interface HealthcareServiceCenterRepository extends JpaRepository<HealthcareServiceCenter,Long> {

}
