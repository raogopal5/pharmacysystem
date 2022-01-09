package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA repository for the Group entity.
 */
@SuppressWarnings("unused" )
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("select case when (count(group_master) > 0) then true else false end from Group group_master where group_master.code = ?1 and group_master.active = true" )
    boolean groupExists(String groupId);

    @Query(value = "select t.code from (select code, jsonb_array_elements(members) as member from group_master where active=true ) t where t.member ->'member' ->> 'code' = ?1 and t.member -> 'inactive' = 'false'", nativeQuery = true)
    List<String> getGroupsForUser(String userId);

    @Query(value ="select group_master from Group group_master where group_master.code = ?1")
    Group getGroupByCode(String groupId);

    @Query("select group_master from Group group_master order by group_master.id")
    Page<Group> findAllSortById(Pageable pageable);

}
