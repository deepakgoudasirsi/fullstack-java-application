package com.fullstack.model;

/**
 * Enumeration for task status
 * Demonstrates proper enum design for task lifecycle management
 */
public enum TaskStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
