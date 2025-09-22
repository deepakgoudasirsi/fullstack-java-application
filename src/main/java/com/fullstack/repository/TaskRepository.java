package com.fullstack.repository;

import com.fullstack.model.Task;
import com.fullstack.model.TaskStatus;
import com.fullstack.model.TaskPriority;
import com.fullstack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Task entity
 * Demonstrates proper repository pattern with complex queries
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find tasks by user
     * @param user the user to search for
     * @return list of tasks for the user
     */
    List<Task> findByUser(User user);
    
    /**
     * Find tasks by user and status
     * @param user the user to search for
     * @param status the status to search for
     * @return list of tasks matching the criteria
     */
    List<Task> findByUserAndStatus(User user, TaskStatus status);
    
    /**
     * Find tasks by user and priority
     * @param user the user to search for
     * @param priority the priority to search for
     * @return list of tasks matching the criteria
     */
    List<Task> findByUserAndPriority(User user, TaskPriority priority);
    
    /**
     * Find overdue tasks for a user
     * @param user the user to search for
     * @param currentDate the current date
     * @return list of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksByUser(@Param("user") User user, @Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find tasks due soon (within specified days)
     * @param user the user to search for
     * @param startDate the start date
     * @param endDate the end date
     * @return list of tasks due soon
     */
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED'")
    List<Task> findTasksDueSoon(@Param("user") User user, 
                               @Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count tasks by status for a user
     * @param user the user to search for
     * @param status the status to count
     * @return count of tasks with the specified status
     */
    long countByUserAndStatus(User user, TaskStatus status);
    
    /**
     * Find completed tasks for a user within date range
     * @param user the user to search for
     * @param startDate the start date
     * @param endDate the end date
     * @return list of completed tasks
     */
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status = 'COMPLETED' AND t.completedAt BETWEEN :startDate AND :endDate")
    List<Task> findCompletedTasksByDateRange(@Param("user") User user, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find high priority pending tasks
     * @param user the user to search for
     * @return list of high priority pending tasks
     */
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.priority IN ('HIGH', 'URGENT') AND t.status = 'PENDING' ORDER BY t.priority DESC, t.dueDate ASC")
    List<Task> findHighPriorityPendingTasks(@Param("user") User user);
}
