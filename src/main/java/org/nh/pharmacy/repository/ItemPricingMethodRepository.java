package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemPricingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the ItemPricingMethod entity.
 */
@SuppressWarnings("unused")
public interface ItemPricingMethodRepository extends JpaRepository<ItemPricingMethod,Long> {

}
