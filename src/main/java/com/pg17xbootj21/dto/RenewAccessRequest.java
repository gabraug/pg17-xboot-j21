package com.pg17xbootj21.dto;

import jakarta.validation.constraints.NotBlank;

public class RenewAccessRequest {
    
    @NotBlank(message = "Request protocol is required")
    private String requestProtocol;

    public RenewAccessRequest() {
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }
}

