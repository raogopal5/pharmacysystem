package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the ItemCategory entity.
 */
@SuppressWarnings("unused")
public interface ItemCategoryRepository extends JpaRepository<ItemCategory,Long> {

}
