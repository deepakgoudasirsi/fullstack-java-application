package com.fullstack.repository;

import com.fullstack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Demonstrates proper repository pattern with custom queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find active users
     * @return list of active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find users by role
     * @param role the role to search for
     * @return list of users with the specified role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") String role);
    
    /**
     * Find users created after a specific date
     * @param date the date to search from
     * @return list of users created after the date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :date ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedAfter(@Param("date") java.time.LocalDateTime date);
}
