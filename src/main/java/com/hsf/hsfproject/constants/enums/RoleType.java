package com.hsf.hsfproject.constants.enums;

public enum RoleType {
    USER("USER", "Regular user with basic access"),
    MANAGER("MANAGER", "Manager with order management access"), 
    ADMIN("ADMIN", "Administrator with full system access");
    
    private final String roleName;
    private final String description;
    
    RoleType(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public String getDescription() {
        return description;
    }
} 