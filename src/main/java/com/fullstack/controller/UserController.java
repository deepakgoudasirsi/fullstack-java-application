package com.fullstack.controller;

import com.fullstack.model.User;
import com.fullstack.model.UserRole;
import com.fullstack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for User operations
 * Demonstrates proper REST API design with error handling
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return ResponseEntity with the created user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while creating the user"));
        }
    }
    
    /**
     * Get user by ID
     * @param id the user ID
     * @return ResponseEntity with the user if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving the user"));
        }
    }
    
    /**
     * Get user by username
     * @param username the username
     * @return ResponseEntity with the user if found
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Optional<User> user = userService.getUserByUsername(username);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving the user"));
        }
    }
    
    /**
     * Get all active users
     * @return ResponseEntity with list of active users
     */
    @GetMapping
    public ResponseEntity<?> getAllActiveUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving users"));
        }
    }
    
    /**
     * Update user
     * @param id the user ID
     * @param user the updated user data
     * @return ResponseEntity with the updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the user"));
        }
    }
    
    /**
     * Deactivate user
     * @param id the user ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(new SuccessResponse("User deactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while deactivating the user"));
        }
    }
    
    /**
     * Activate user
     * @param id the user ID
     * @return ResponseEntity with success message
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(new SuccessResponse("User activated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while activating the user"));
        }
    }
    
    /**
     * Update user role
     * @param id the user ID
     * @param roleRequest the role update request
     * @return ResponseEntity with success message
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody RoleUpdateRequest roleRequest) {
        try {
            userService.updateUserRole(id, roleRequest.getRole());
            return ResponseEntity.ok(new SuccessResponse("User role updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the user role"));
        }
    }
    
    /**
     * Validate user credentials
     * @param credentials the login credentials
     * @return ResponseEntity with validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateCredentials(@RequestBody LoginRequest credentials) {
        try {
            boolean isValid = userService.validateCredentials(credentials.getUsername(), credentials.getPassword());
            if (isValid) {
                return ResponseEntity.ok(new SuccessResponse("Credentials are valid"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while validating credentials"));
        }
    }
    
    // Inner classes for request/response objects
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    public static class RoleUpdateRequest {
        private UserRole role;
        
        public UserRole getRole() {
            return role;
        }
        
        public void setRole(UserRole role) {
            this.role = role;
        }
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
