package com.fullstack.model;

/**
 * Enumeration for user roles in the system
 * Demonstrates proper enum design for role-based access control
 */
public enum UserRole {
    USER("User"),
    ADMIN("Administrator"),
    MODERATOR("Moderator");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
