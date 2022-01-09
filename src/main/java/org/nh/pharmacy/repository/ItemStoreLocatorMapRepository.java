package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.ItemStoreLocatorMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for the ItemStoreLocatorMap entity.
 */
@SuppressWarnings("unused")
public interface ItemStoreLocatorMapRepository extends JpaRepository<ItemStoreLocatorMap,Long> {

    @Query("select itemStoreLocatorMap from ItemStoreLocatorMap itemStoreLocatorMap order by itemStoreLocatorMap.id")
    Page<ItemStoreLocatorMap> findAllSortById(Pageable pageable);

}
