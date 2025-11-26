package com.pg17xbootj21.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
public class Module {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "module_allowed_departments", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "department")
    private List<String> allowedDepartments = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "module_incompatible_modules", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "incompatible_module_id")
    private List<String> incompatibleModules = new ArrayList<>();
    
    @Column(nullable = false)
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

