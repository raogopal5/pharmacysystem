package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemUnitAverageCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;

/**
 * Spring Data JPA repository for the ItemUnitAverageCost entity.
 */
@SuppressWarnings("unused")
public interface ItemUnitAverageCostRepository extends JpaRepository<ItemUnitAverageCost,Long> {

    @Query("from ItemUnitAverageCost itemUnitAverageCost where itemUnitAverageCost.itemId =:itemId and itemUnitAverageCost.unitId =:unitId")
    ItemUnitAverageCost findByItemIdUnitId(@Param("itemId") Long itemId, @Param("unitId") Long unitId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("from ItemUnitAverageCost itemUnitAverageCost where itemUnitAverageCost.itemId =:itemId and itemUnitAverageCost.unitId =:unitId")
    ItemUnitAverageCost findByItemIdUnitIdByLock(@Param("itemId") Long itemId, @Param("unitId") Long unitId);
}
