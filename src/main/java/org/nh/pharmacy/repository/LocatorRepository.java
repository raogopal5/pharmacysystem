package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.Locator;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Locator entity.
 */
@SuppressWarnings("unused")
public interface LocatorRepository extends JpaRepository<Locator,Long> {

}
