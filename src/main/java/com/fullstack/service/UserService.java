package com.fullstack.service;

import com.fullstack.model.User;
import com.fullstack.model.UserRole;
import com.fullstack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User operations
 * Demonstrates proper service layer design with business logic
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return the created user
     * @throws IllegalArgumentException if username or email already exists
     */
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values
        user.setRole(UserRole.USER);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Get user by ID
     * @param id the user ID
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by username
     * @param username the username
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     * @param email the email
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all active users
     * @return list of active users
     */
    @Transactional(readOnly = true)
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    /**
     * Update user information
     * @param user the user to update
     * @return the updated user
     */
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
        
        // Check if username is being changed and if it's available
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Check if email is being changed and if it's available
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Update fields
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        // Only update password if provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }
    
    /**
     * Deactivate user (soft delete)
     * @param id the user ID
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Activate user
     * @param id the user ID
     */
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Update user role
     * @param id the user ID
     * @param role the new role
     */
    public void updateUserRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Validate user credentials
     * @param username the username
     * @param password the password
     * @return true if credentials are valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateCredentials(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getIsActive() && passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
}
