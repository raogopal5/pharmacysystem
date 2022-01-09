package org.nh.pharmacy.repository;

import org.nh.pharmacy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for the User entity.
 */
@SuppressWarnings("unused")
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("select case when (count(user) > 0)  then true else false end from User user where user.login = ?1")
    boolean userExists(String userId);

    @Query("select user from User user where user.login = ?1")
    User findUserByUserId(String userId);

}
