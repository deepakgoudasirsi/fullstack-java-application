package com.fullstack.service;

import com.fullstack.model.User;
import com.fullstack.model.UserRole;
import com.fullstack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Demonstrates comprehensive testing of service layer business logic
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.USER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("password123");
        testUser2.setFirstName("Test2");
        testUser2.setLastName("User2");
        testUser2.setRole(UserRole.ADMIN);
        testUser2.setIsActive(true);
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(UserRole.USER, result.getRole());
        assertTrue(result.getIsActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(passwordEncoder).encode(testUser.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUser);
        });

        assertEquals("Username already exists: " + testUser.getUsername(), exception.getMessage());
        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUser);
        });

        assertEquals("Email already exists: " + testUser.getEmail(), exception.getMessage());
        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getAllActiveUsers_Success() {
        // Given
        List<User> activeUsers = Arrays.asList(testUser, testUser2);
        when(userRepository.findByIsActiveTrue()).thenReturn(activeUsers);

        // When
        List<User> result = userService.getAllActiveUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testUser));
        assertTrue(result.contains(testUser2));
        verify(userRepository).findByIsActiveTrue();
    }

    @Test
    void updateUser_Success() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(updatedUser);

        // Then
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(updatedUser);
        });

        assertEquals("User not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(1L);

        // Then
        assertFalse(testUser.getIsActive());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void activateUser_Success() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.activateUser(1L);

        // Then
        assertTrue(testUser.getIsActive());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserRole_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserRole(1L, UserRole.ADMIN);

        // Then
        assertEquals(UserRole.ADMIN, testUser.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void validateCredentials_ValidCredentials_ReturnsTrue() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // When
        boolean result = userService.validateCredentials("testuser", "password123");

        // Then
        assertTrue(result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }

    @Test
    void validateCredentials_InvalidPassword_ReturnsFalse() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // When
        boolean result = userService.validateCredentials("testuser", "wrongpassword");

        // Then
        assertFalse(result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
    }

    @Test
    void validateCredentials_UserNotFound_ReturnsFalse() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        boolean result = userService.validateCredentials("nonexistent", "password123");

        // Then
        assertFalse(result);
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void validateCredentials_InactiveUser_ReturnsFalse() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.validateCredentials("testuser", "password123");

        // Then
        assertFalse(result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
