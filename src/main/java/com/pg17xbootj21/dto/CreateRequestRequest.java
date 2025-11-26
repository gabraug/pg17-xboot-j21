package com.pg17xbootj21.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateRequestRequest {
    
    @NotEmpty(message = "At least one module is required")
    @Size(min = 1, max = 3, message = "Must select between 1 and 3 modules")
    private List<String> modules;
    
    @NotBlank(message = "Justification is required")
    @Size(min = 20, max = 500, message = "Justification must be between 20 and 500 characters")
    private String justification;
    
    private boolean urgent;

    public CreateRequestRequest() {
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }
}

