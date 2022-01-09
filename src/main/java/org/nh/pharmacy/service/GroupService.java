package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Service Interface for managing Group.
 */
public interface GroupService {

    /**
     * Save a group.
     *
     * @param group the entity to save
     * @return the persisted entity
     */
    Group save(Group group);

    /**
     * Get all the groups.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Group> findAll(Pageable pageable);

    /**
     * Get the "id" group.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Group findOne(Long id);

    /**
     * Check existence of group
     *
     * @param groupId
     * @return boolean
     */
    boolean groupExists(String groupId);

    /**
     * Get group(s) associated to user
     *
     * @param userId
     * @return groupList
     */
    List<String> groupsForUser(String userId);

    /**
     * Get group members
     *
     * @param groupId
     * @return userList
     */
    Set<String> getMembersForGroup(String groupId);

    /**
     * Delete the "id" group.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the group corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<Group> search(String query, Pageable pageable);

    /**
     * Group subscriber
     */
    void consume(String groupInput);

    /**
     * Do elastic index for Group
     */
    void doIndex();
}
