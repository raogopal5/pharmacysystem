package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.SystemAlert;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the SystemAlert entity.
 */
@SuppressWarnings("unused")
public interface SystemAlertRepository extends JpaRepository<SystemAlert,Long> {

}
