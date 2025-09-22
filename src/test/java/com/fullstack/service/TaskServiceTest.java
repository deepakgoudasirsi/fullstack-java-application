package com.fullstack.service;

import com.fullstack.model.Task;
import com.fullstack.model.TaskStatus;
import com.fullstack.model.TaskPriority;
import com.fullstack.model.User;
import com.fullstack.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 * Demonstrates comprehensive testing of service layer business logic
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

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
    void createTask_Success() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.createTask(testTask);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getTitle(), result.getTitle());
        assertEquals(testTask.getDescription(), result.getDescription());
        assertEquals(testTask.getStatus(), result.getStatus());
        assertEquals(testTask.getPriority(), result.getPriority());
        assertEquals(testTask.getUser(), result.getUser());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(taskRepository).save(testTask);
    }

    @Test
    void getTaskById_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        Optional<Task> result = taskService.getTaskById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTask.getId(), result.get().getId());
        assertEquals(testTask.getTitle(), result.get().getTitle());
        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_NotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(taskRepository).findById(999L);
    }

    @Test
    void getTasksByUser_Success() {
        // Given
        List<Task> userTasks = Arrays.asList(testTask);
        when(taskRepository.findByUser(testUser)).thenReturn(userTasks);

        // When
        List<Task> result = taskService.getTasksByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask.getId(), result.get(0).getId());
        verify(taskRepository).findByUser(testUser);
    }

    @Test
    void getTasksByUserAndStatus_Success() {
        // Given
        List<Task> pendingTasks = Arrays.asList(testTask);
        when(taskRepository.findByUserAndStatus(testUser, TaskStatus.PENDING)).thenReturn(pendingTasks);

        // When
        List<Task> result = taskService.getTasksByUserAndStatus(testUser, TaskStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskStatus.PENDING, result.get(0).getStatus());
        verify(taskRepository).findByUserAndStatus(testUser, TaskStatus.PENDING);
    }

    @Test
    void getTasksByUserAndPriority_Success() {
        // Given
        List<Task> mediumPriorityTasks = Arrays.asList(testTask);
        when(taskRepository.findByUserAndPriority(testUser, TaskPriority.MEDIUM)).thenReturn(mediumPriorityTasks);

        // When
        List<Task> result = taskService.getTasksByUserAndPriority(testUser, TaskPriority.MEDIUM);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskPriority.MEDIUM, result.get(0).getPriority());
        verify(taskRepository).findByUserAndPriority(testUser, TaskPriority.MEDIUM);
    }

    @Test
    void updateTask_Success() {
        // Given
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(TaskPriority.HIGH);
        updatedTask.setDueDate(LocalDateTime.now().plusDays(2));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        Task result = taskService.updateTask(updatedTask);

        // Then
        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_TaskNotFound_ThrowsException() {
        // Given
        Task updatedTask = new Task();
        updatedTask.setId(999L);
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(updatedTask);
        });

        assertEquals("Task not found with ID: 999", exception.getMessage());
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED);

        // Then
        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(testTask);
    }

    @Test
    void updateTaskStatus_TaskNotFound_ThrowsException() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTaskStatus(999L, TaskStatus.COMPLETED);
        });

        assertEquals("Task not found with ID: 999", exception.getMessage());
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_Success() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_TaskNotFound_ThrowsException() {
        // Given
        when(taskRepository.existsById(999L)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(999L);
        });

        assertEquals("Task not found with ID: 999", exception.getMessage());
        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void getOverdueTasks_Success() {
        // Given
        List<Task> overdueTasks = Arrays.asList(testTask);
        when(taskRepository.findOverdueTasksByUser(eq(testUser), any(LocalDateTime.class))).thenReturn(overdueTasks);

        // When
        List<Task> result = taskService.getOverdueTasks(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository).findOverdueTasksByUser(eq(testUser), any(LocalDateTime.class));
    }

    @Test
    void getTasksDueSoon_Success() {
        // Given
        List<Task> dueSoonTasks = Arrays.asList(testTask);
        when(taskRepository.findTasksDueSoon(eq(testUser), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(dueSoonTasks);

        // When
        List<Task> result = taskService.getTasksDueSoon(testUser, 7);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository).findTasksDueSoon(eq(testUser), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getTaskStatistics_Success() {
        // Given
        when(taskRepository.countByUserAndStatus(testUser, TaskStatus.PENDING)).thenReturn(2L);
        when(taskRepository.countByUserAndStatus(testUser, TaskStatus.IN_PROGRESS)).thenReturn(1L);
        when(taskRepository.countByUserAndStatus(testUser, TaskStatus.COMPLETED)).thenReturn(3L);
        when(taskRepository.countByUserAndStatus(testUser, TaskStatus.CANCELLED)).thenReturn(1L);

        // When
        long[] stats = taskService.getTaskStatistics(testUser);

        // Then
        assertNotNull(stats);
        assertEquals(5, stats.length);
        assertEquals(7L, stats[0]); // total
        assertEquals(2L, stats[1]); // pending
        assertEquals(1L, stats[2]); // in_progress
        assertEquals(3L, stats[3]); // completed
        assertEquals(1L, stats[4]); // cancelled

        verify(taskRepository).countByUserAndStatus(testUser, TaskStatus.PENDING);
        verify(taskRepository).countByUserAndStatus(testUser, TaskStatus.IN_PROGRESS);
        verify(taskRepository).countByUserAndStatus(testUser, TaskStatus.COMPLETED);
        verify(taskRepository).countByUserAndStatus(testUser, TaskStatus.CANCELLED);
    }

    @Test
    void getHighPriorityPendingTasks_Success() {
        // Given
        List<Task> highPriorityTasks = Arrays.asList(testTask);
        when(taskRepository.findHighPriorityPendingTasks(testUser)).thenReturn(highPriorityTasks);

        // When
        List<Task> result = taskService.getHighPriorityPendingTasks(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository).findHighPriorityPendingTasks(testUser);
    }

    @Test
    void getCompletedTasksByDateRange_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Task> completedTasks = Arrays.asList(testTask);
        when(taskRepository.findCompletedTasksByDateRange(eq(testUser), eq(startDate), eq(endDate)))
                .thenReturn(completedTasks);

        // When
        List<Task> result = taskService.getCompletedTasksByDateRange(testUser, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository).findCompletedTasksByDateRange(eq(testUser), eq(startDate), eq(endDate));
    }
}
