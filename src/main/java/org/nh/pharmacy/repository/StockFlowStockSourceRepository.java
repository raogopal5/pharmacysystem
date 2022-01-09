package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.StockFlowStockSource;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the StockFlowStockSource entity.
 */
@SuppressWarnings("unused")
public interface StockFlowStockSourceRepository extends JpaRepository<StockFlowStockSource,Long> {

}
