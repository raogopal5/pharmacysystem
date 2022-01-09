package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing User.
 */
public interface UserService {

    /**
     * Save a user.
     *
     * @param user the entity to save
     * @return the persisted entity
     */
    User save(User user);

    /**
     * Get all the users.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Get the "id" user.
     *
     * @param id the id of the entity
     * @return the entity
     */
    User findOne(Long id);

    /**
     * Check existence of user
     *
     * @param login
     * @return boolean
     */
    boolean userExists(String login);

    /**
     * Get user by login
     *
     * @param login
     * @return user entity
     */
    User findUserByLogin(String login);

    /**
     * Delete the "id" user.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the user corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<User> search(String query, Pageable pageable);

    /**
     * User subscriber
     */
    void consume(User user);

    User findUserByEmail(String s);
}
