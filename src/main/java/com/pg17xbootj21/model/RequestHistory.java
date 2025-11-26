package com.pg17xbootj21.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "request_history")
public class RequestHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "request_protocol", nullable = false)
    private Request request;
    
    @Column(nullable = false)
    private Instant date;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String action;

    public RequestHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

