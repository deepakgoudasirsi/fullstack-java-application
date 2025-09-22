package com.fullstack.controller;

import com.fullstack.model.Task;
import com.fullstack.model.TaskStatus;
import com.fullstack.model.TaskPriority;
import com.fullstack.model.User;
import com.fullstack.service.TaskService;
import com.fullstack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Task operations
 * Demonstrates proper REST API design with error handling
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskService taskService;
    private final UserService userService;
    
    @Autowired
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }
    
    /**
     * Create a new task
     * @param taskRequest the task creation request
     * @return ResponseEntity with the created task
     */
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        try {
            User user = userService.getUserById(taskRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + taskRequest.getUserId()));
            
            Task task = new Task();
            task.setTitle(taskRequest.getTitle());
            task.setDescription(taskRequest.getDescription());
            task.setPriority(taskRequest.getPriority());
            task.setDueDate(taskRequest.getDueDate());
            task.setUser(user);
            
            Task createdTask = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while creating the task"));
        }
    }
    
    /**
     * Get task by ID
     * @param id the task ID
     * @return ResponseEntity with the task if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            Optional<Task> task = taskService.getTaskById(id);
            if (task.isPresent()) {
                return ResponseEntity.ok(task.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving the task"));
        }
    }
    
    /**
     * Get all tasks for a user
     * @param userId the user ID
     * @return ResponseEntity with list of tasks
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTasksByUser(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            List<Task> tasks = taskService.getTasksByUser(user);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving tasks"));
        }
    }
    
    /**
     * Get tasks by user and status
     * @param userId the user ID
     * @param status the task status
     * @return ResponseEntity with list of tasks
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getTasksByUserAndStatus(@PathVariable Long userId, @PathVariable TaskStatus status) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            List<Task> tasks = taskService.getTasksByUserAndStatus(user, status);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving tasks"));
        }
    }
    
    /**
     * Get tasks by user and priority
     * @param userId the user ID
     * @param priority the task priority
     * @return ResponseEntity with list of tasks
     */
    @GetMapping("/user/{userId}/priority/{priority}")
    public ResponseEntity<?> getTasksByUserAndPriority(@PathVariable Long userId, @PathVariable TaskPriority priority) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            List<Task> tasks = taskService.getTasksByUserAndPriority(user, priority);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving tasks"));
        }
    }
    
    /**
     * Update task
     * @param id the task ID
     * @param taskRequest the task update request
     * @return ResponseEntity with the updated task
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest taskRequest) {
        try {
            Task existingTask = taskService.getTaskById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));
            
            existingTask.setTitle(taskRequest.getTitle());
            existingTask.setDescription(taskRequest.getDescription());
            existingTask.setPriority(taskRequest.getPriority());
            existingTask.setDueDate(taskRequest.getDueDate());
            
            Task updatedTask = taskService.updateTask(existingTask);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the task"));
        }
    }
    
    /**
     * Update task status
     * @param id the task ID
     * @param statusRequest the status update request
     * @return ResponseEntity with the updated task
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest statusRequest) {
        try {
            Task updatedTask = taskService.updateTaskStatus(id, statusRequest.getStatus());
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the task status"));
        }
    }
    
    /**
     * Delete task
     * @param id the task ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(new SuccessResponse("Task deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while deleting the task"));
        }
    }
    
    /**
     * Get overdue tasks for a user
     * @param userId the user ID
     * @return ResponseEntity with list of overdue tasks
     */
    @GetMapping("/user/{userId}/overdue")
    public ResponseEntity<?> getOverdueTasks(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            List<Task> tasks = taskService.getOverdueTasks(user);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving overdue tasks"));
        }
    }
    
    /**
     * Get tasks due soon for a user
     * @param userId the user ID
     * @param days the number of days to look ahead
     * @return ResponseEntity with list of tasks due soon
     */
    @GetMapping("/user/{userId}/due-soon/{days}")
    public ResponseEntity<?> getTasksDueSoon(@PathVariable Long userId, @PathVariable int days) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            List<Task> tasks = taskService.getTasksDueSoon(user, days);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving tasks due soon"));
        }
    }
    
    /**
     * Get task statistics for a user
     * @param userId the user ID
     * @return ResponseEntity with task statistics
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<?> getTaskStatistics(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            long[] stats = taskService.getTaskStatistics(user);
            TaskStatisticsResponse response = new TaskStatisticsResponse(
                    stats[0], stats[1], stats[2], stats[3], stats[4]
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving task statistics"));
        }
    }
    
    // Inner classes for request/response objects
    public static class TaskRequest {
        private Long userId;
        private String title;
        private String description;
        private TaskPriority priority;
        private LocalDateTime dueDate;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    }
    
    public static class StatusUpdateRequest {
        private TaskStatus status;
        
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
    }
    
    public static class TaskStatisticsResponse {
        private long total;
        private long pending;
        private long inProgress;
        private long completed;
        private long cancelled;
        
        public TaskStatisticsResponse(long total, long pending, long inProgress, long completed, long cancelled) {
            this.total = total;
            this.pending = pending;
            this.inProgress = inProgress;
            this.completed = completed;
            this.cancelled = cancelled;
        }
        
        // Getters
        public long getTotal() { return total; }
        public long getPending() { return pending; }
        public long getInProgress() { return inProgress; }
        public long getCompleted() { return completed; }
        public long getCancelled() { return cancelled; }
    }
    
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
