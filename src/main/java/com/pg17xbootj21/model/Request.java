package com.pg17xbootj21.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requests")
public class Request {
    @Id
    private String protocol;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "user_department", nullable = false)
    private String userDepartment;
    
    @ElementCollection
    @CollectionTable(name = "request_modules", joinColumns = @JoinColumn(name = "request_protocol"))
    @Column(name = "module_id")
    private List<String> modules = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String justification;
    
    @Column(nullable = false)
    private boolean urgent;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "denial_reason", columnDefinition = "TEXT")
    private String denialReason;
    
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestHistory> history = new ArrayList<>();

    public Request() {
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDepartment() {
        return userDepartment;
    }

    public void setUserDepartment(String userDepartment) {
        this.userDepartment = userDepartment;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDenialReason() {
        return denialReason;
    }

    public void setDenialReason(String denialReason) {
        this.denialReason = denialReason;
    }

    public List<RequestHistory> getHistory() {
        return history;
    }

    public void setHistory(List<RequestHistory> history) {
        this.history = history;
    }
}

