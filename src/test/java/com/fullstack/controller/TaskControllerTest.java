package com.fullstack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.model.Task;
import com.fullstack.model.TaskStatus;
import com.fullstack.model.TaskPriority;
import com.fullstack.model.User;
import com.fullstack.service.TaskService;
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
 * Integration tests for TaskController
 * Demonstrates comprehensive testing of REST API endpoints
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setDueDate(LocalDateTime.now().plusDays(1));
        testTask.setUser(testUser);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createTask_Success() throws Exception {
        // Given
        TaskController.TaskRequest taskRequest = new TaskController.TaskRequest();
        taskRequest.setUserId(1L);
        taskRequest.setTitle("New Task");
        taskRequest.setDescription("New Description");
        taskRequest.setPriority(TaskPriority.HIGH);
        taskRequest.setDueDate(LocalDateTime.now().plusDays(2));

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(userService).getUserById(1L);
        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void createTask_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        TaskController.TaskRequest taskRequest = new TaskController.TaskRequest();
        taskRequest.setUserId(999L);
        taskRequest.setTitle("New Task");

        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        verify(userService).getUserById(999L);
        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    void createTask_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        TaskController.TaskRequest taskRequest = new TaskController.TaskRequest();
        taskRequest.setUserId(1L);
        // Missing required title

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUserById(anyLong());
        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    void getTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(taskService).getTaskById(1L);
    }

    @Test
    void getTaskById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskService).getTaskById(999L);
    }

    @Test
    void getTasksByUser_Success() throws Exception {
        // Given
        List<Task> userTasks = Arrays.asList(testTask);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.getTasksByUser(testUser)).thenReturn(userTasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"));

        verify(userService).getUserById(1L);
        verify(taskService).getTasksByUser(testUser);
    }

    @Test
    void getTasksByUser_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tasks/user/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        verify(userService).getUserById(999L);
        verify(taskService, never()).getTasksByUser(any(User.class));
    }

    @Test
    void getTasksByUserAndStatus_Success() throws Exception {
        // Given
        List<Task> pendingTasks = Arrays.asList(testTask);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.getTasksByUserAndStatus(testUser, TaskStatus.PENDING)).thenReturn(pendingTasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/user/1/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(userService).getUserById(1L);
        verify(taskService).getTasksByUserAndStatus(testUser, TaskStatus.PENDING);
    }

    @Test
    void getTasksByUserAndPriority_Success() throws Exception {
        // Given
        List<Task> mediumPriorityTasks = Arrays.asList(testTask);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.getTasksByUserAndPriority(testUser, TaskPriority.MEDIUM)).thenReturn(mediumPriorityTasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/user/1/priority/MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));

        verify(userService).getUserById(1L);
        verify(taskService).getTasksByUserAndPriority(testUser, TaskPriority.MEDIUM);
    }

    @Test
    void updateTask_Success() throws Exception {
        // Given
        TaskController.TaskRequest taskRequest = new TaskController.TaskRequest();
        taskRequest.setTitle("Updated Task");
        taskRequest.setDescription("Updated Description");
        taskRequest.setPriority(TaskPriority.HIGH);

        when(taskService.getTaskById(1L)).thenReturn(Optional.of(testTask));
        when(taskService.updateTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService).getTaskById(1L);
        verify(taskService).updateTask(any(Task.class));
    }

    @Test
    void updateTask_TaskNotFound_ReturnsBadRequest() throws Exception {
        // Given
        TaskController.TaskRequest taskRequest = new TaskController.TaskRequest();
        taskRequest.setTitle("Updated Task");

        when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task not found with ID: 999"));

        verify(taskService).getTaskById(999L);
        verify(taskService, never()).updateTask(any(Task.class));
    }

    @Test
    void updateTaskStatus_Success() throws Exception {
        // Given
        TaskController.StatusUpdateRequest statusRequest = new TaskController.StatusUpdateRequest();
        statusRequest.setStatus(TaskStatus.COMPLETED);

        when(taskService.updateTaskStatus(1L, TaskStatus.COMPLETED)).thenReturn(testTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).updateTaskStatus(1L, TaskStatus.COMPLETED);
    }

    @Test
    void updateTaskStatus_TaskNotFound_ReturnsBadRequest() throws Exception {
        // Given
        TaskController.StatusUpdateRequest statusRequest = new TaskController.StatusUpdateRequest();
        statusRequest.setStatus(TaskStatus.COMPLETED);

        when(taskService.updateTaskStatus(999L, TaskStatus.COMPLETED))
                .thenThrow(new IllegalArgumentException("Task not found with ID: 999"));

        // When & Then
        mockMvc.perform(put("/api/tasks/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task not found with ID: 999"));

        verify(taskService).updateTaskStatus(999L, TaskStatus.COMPLETED);
    }

    @Test
    void deleteTask_Success() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));

        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteTask_TaskNotFound_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Task not found with ID: 999"))
                .when(taskService).deleteTask(999L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task not found with ID: 999"));

        verify(taskService).deleteTask(999L);
    }

    @Test
    void getOverdueTasks_Success() throws Exception {
        // Given
        List<Task> overdueTasks = Arrays.asList(testTask);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.getOverdueTasks(testUser)).thenReturn(overdueTasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/user/1/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getUserById(1L);
        verify(taskService).getOverdueTasks(testUser);
    }

    @Test
    void getTasksDueSoon_Success() throws Exception {
        // Given
        List<Task> dueSoonTasks = Arrays.asList(testTask);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.getTasksDueSoon(testUser, 7)).thenReturn(dueSoonTasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/user/1/due-soon/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getUserById(1L);
        verify(taskService).getTasksDueSoon(testUser, 7);
    }

    @Test
    void getTaskStatistics_Success() throws Exception {
        // Given
        long[] stats = {7L, 2L, 1L, 3L, 1L}; // total, pending, in_progress, completed, cancelled
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(taskService.getTaskStatistics(testUser)).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/tasks/user/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(7))
                .andExpect(jsonPath("$.pending").value(2))
                .andExpect(jsonPath("$.inProgress").value(1))
                .andExpect(jsonPath("$.completed").value(3))
                .andExpect(jsonPath("$.cancelled").value(1));

        verify(userService).getUserById(1L);
        verify(taskService).getTaskStatistics(testUser);
    }
}
