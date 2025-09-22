package com.fullstack.model;

/**
 * Enumeration for task priority levels
 * Demonstrates proper enum design for priority management
 */
public enum TaskPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");
    
    private final String displayName;
    
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
