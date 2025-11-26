package com.pg17xbootj21.model;

import java.time.Instant;

public class SessionInfo {
    private String email;
    private Instant expiresAt;

    public SessionInfo() {
    }

    public SessionInfo(String email, Instant expiresAt) {
        this.email = email;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}

