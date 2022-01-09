package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.HSCGroupMapping;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the HSCGroupMapping entity.
 */
@SuppressWarnings("unused")
public interface HSCGroupMappingRepository extends JpaRepository<HSCGroupMapping,Long> {

}
