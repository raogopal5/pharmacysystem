package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Item entity.
 */
@SuppressWarnings("unused")
public interface ItemRepository extends JpaRepository<Item,Long> {

    @Query(value = "select * from item it where it.dispensable_generic_name in(:genericItemNames)", nativeQuery = true)
    List<Item> findItemsWithGenericName(@Param("genericItemNames") List<String> genericItemNames);

}
