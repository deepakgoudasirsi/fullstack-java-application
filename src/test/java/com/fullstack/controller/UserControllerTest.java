package com.fullstack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.model.User;
import com.fullstack.model.UserRole;
import com.fullstack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 * Demonstrates comprehensive testing of REST API endpoints
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createUser_Success() throws Exception {
        // Given
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists_ReturnsBadRequest() throws Exception {
        // Given
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username already exists: testuser"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists: testuser"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void createUser_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        User invalidUser = new User();
        invalidUser.setUsername(""); // Invalid username

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    void getUserByUsername_Success() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void getUserByUsername_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/username/nonexistent"))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUsername("nonexistent");
    }

    @Test
    void getAllActiveUsers_Success() throws Exception {
        // Given
        List<User> activeUsers = Arrays.asList(testUser, testUser2);
        when(userService.getAllActiveUsers()).thenReturn(activeUsers);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].username").value("testuser2"));

        verify(userService).getAllActiveUsers();
    }

    @Test
    void updateUser_Success() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService).updateUser(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(999L);
        updatedUser.setUsername("updateduser");

        when(userService.updateUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("User not found with ID: 999"));

        // When & Then
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        verify(userService).updateUser(any(User.class));
    }

    @Test
    void deactivateUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));

        verify(userService).deactivateUser(1L);
    }

    @Test
    void deactivateUser_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("User not found with ID: 999"))
                .when(userService).deactivateUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        verify(userService).deactivateUser(999L);
    }

    @Test
    void activateUser_Success() throws Exception {
        // Given
        doNothing().when(userService).activateUser(1L);

        // When & Then
        mockMvc.perform(put("/api/users/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User activated successfully"));

        verify(userService).activateUser(1L);
    }

    @Test
    void updateUserRole_Success() throws Exception {
        // Given
        UserController.RoleUpdateRequest roleRequest = new UserController.RoleUpdateRequest();
        roleRequest.setRole(UserRole.ADMIN);

        doNothing().when(userService).updateUserRole(1L, UserRole.ADMIN);

        // When & Then
        mockMvc.perform(put("/api/users/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User role updated successfully"));

        verify(userService).updateUserRole(1L, UserRole.ADMIN);
    }

    @Test
    void validateCredentials_ValidCredentials_ReturnsOk() throws Exception {
        // Given
        UserController.LoginRequest loginRequest = new UserController.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(userService.validateCredentials("testuser", "password123")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/users/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Credentials are valid"));

        verify(userService).validateCredentials("testuser", "password123");
    }

    @Test
    void validateCredentials_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        UserController.LoginRequest loginRequest = new UserController.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(userService.validateCredentials("testuser", "wrongpassword")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/users/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(userService).validateCredentials("testuser", "wrongpassword");
    }
}
