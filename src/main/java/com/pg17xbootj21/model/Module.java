package com.pg17xbootj21.model;

import java.util.List;

public class Module {
    private String id;
    private String name;
    private List<String> allowedDepartments;
    private List<String> incompatibleModules;
    private boolean active;

    public Module() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAllowedDepartments() {
        return allowedDepartments;
    }

    public void setAllowedDepartments(List<String> allowedDepartments) {
        this.allowedDepartments = allowedDepartments;
    }

    public List<String> getIncompatibleModules() {
        return incompatibleModules;
    }

    public void setIncompatibleModules(List<String> incompatibleModules) {
        this.incompatibleModules = incompatibleModules;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

