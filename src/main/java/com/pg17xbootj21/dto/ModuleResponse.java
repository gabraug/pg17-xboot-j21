package com.pg17xbootj21.dto;

import java.util.List;

public class ModuleResponse {
    private String name;
    private String description;
    private List<String> allowedDepartments;
    private boolean active;
    private List<String> incompatibleModules;

    public ModuleResponse() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAllowedDepartments() {
        return allowedDepartments;
    }

    public void setAllowedDepartments(List<String> allowedDepartments) {
        this.allowedDepartments = allowedDepartments;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getIncompatibleModules() {
        return incompatibleModules;
    }

    public void setIncompatibleModules(List<String> incompatibleModules) {
        this.incompatibleModules = incompatibleModules;
    }
}

