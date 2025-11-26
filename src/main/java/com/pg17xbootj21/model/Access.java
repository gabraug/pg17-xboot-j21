package com.pg17xbootj21.model;

public class Access {
    private String userId;
    private String moduleId;
    private String status;
    private String grantedAt;
    private String expiresAt;
    private String requestProtocol;

    public Access() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(String grantedAt) {
        this.grantedAt = grantedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }
}

