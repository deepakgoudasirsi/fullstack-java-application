package com.fullstack.service;

import com.fullstack.model.Task;
import com.fullstack.model.TaskStatus;
import com.fullstack.model.TaskPriority;
import com.fullstack.model.User;
import com.fullstack.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Task operations
 * Demonstrates proper service layer design with business logic
 */
@Service
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Create a new task
     * @param task the task to create
     * @return the created task
     */
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
    
    /**
     * Get task by ID
     * @param id the task ID
     * @return Optional containing the task if found
     */
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    /**
     * Get all tasks for a user
     * @param user the user
     * @return list of tasks for the user
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByUser(user);
    }
    
    /**
     * Get tasks by user and status
     * @param user the user
     * @param status the status
     * @return list of tasks matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByUserAndStatus(User user, TaskStatus status) {
        return taskRepository.findByUserAndStatus(user, status);
    }
    
    /**
     * Get tasks by user and priority
     * @param user the user
     * @param priority the priority
     * @return list of tasks matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByUserAndPriority(User user, TaskPriority priority) {
        return taskRepository.findByUserAndPriority(user, priority);
    }
    
    /**
     * Update task
     * @param task the task to update
     * @return the updated task
     */
    public Task updateTask(Task task) {
        Task existingTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + task.getId()));
        
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());
        existingTask.setUpdatedAt(LocalDateTime.now());
        
        // Set completion date if status is changed to completed
        if (task.getStatus() == TaskStatus.COMPLETED && existingTask.getStatus() != TaskStatus.COMPLETED) {
            existingTask.setCompletedAt(LocalDateTime.now());
        }
        
        return taskRepository.save(existingTask);
    }
    
    /**
     * Update task status
     * @param id the task ID
     * @param status the new status
     * @return the updated task
     */
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));
        
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        
        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        
        return taskRepository.save(task);
    }
    
    /**
     * Delete task
     * @param id the task ID
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("Task not found with ID: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    /**
     * Get overdue tasks for a user
     * @param user the user
     * @return list of overdue tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks(User user) {
        return taskRepository.findOverdueTasksByUser(user, LocalDateTime.now());
    }
    
    /**
     * Get tasks due soon for a user
     * @param user the user
     * @param days the number of days to look ahead
     * @return list of tasks due soon
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksDueSoon(User user, int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(days);
        return taskRepository.findTasksDueSoon(user, startDate, endDate);
    }
    
    /**
     * Get task statistics for a user
     * @param user the user
     * @return array with counts: [total, pending, in_progress, completed, cancelled]
     */
    @Transactional(readOnly = true)
    public long[] getTaskStatistics(User user) {
        long total = taskRepository.countByUserAndStatus(user, TaskStatus.PENDING) +
                    taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS) +
                    taskRepository.countByUserAndStatus(user, TaskStatus.COMPLETED) +
                    taskRepository.countByUserAndStatus(user, TaskStatus.CANCELLED);
        
        long pending = taskRepository.countByUserAndStatus(user, TaskStatus.PENDING);
        long inProgress = taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS);
        long completed = taskRepository.countByUserAndStatus(user, TaskStatus.COMPLETED);
        long cancelled = taskRepository.countByUserAndStatus(user, TaskStatus.CANCELLED);
        
        return new long[]{total, pending, inProgress, completed, cancelled};
    }
    
    /**
     * Get high priority pending tasks for a user
     * @param user the user
     * @return list of high priority pending tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getHighPriorityPendingTasks(User user) {
        return taskRepository.findHighPriorityPendingTasks(user);
    }
    
    /**
     * Get completed tasks for a user within date range
     * @param user the user
     * @param startDate the start date
     * @param endDate the end date
     * @return list of completed tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getCompletedTasksByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepository.findCompletedTasksByDateRange(user, startDate, endDate);
    }
}
