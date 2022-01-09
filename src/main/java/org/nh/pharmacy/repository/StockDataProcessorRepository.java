package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.StockDataProcessor;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the StockDataProcessor entity.
 */
@SuppressWarnings("unused")
public interface StockDataProcessorRepository extends JpaRepository<StockDataProcessor,Long> {

}
