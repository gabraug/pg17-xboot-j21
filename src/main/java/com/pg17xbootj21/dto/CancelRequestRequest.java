package com.pg17xbootj21.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelRequestRequest {
    
    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 10, max = 200, message = "Cancellation reason must be between 10 and 200 characters")
    private String reason;

    public CancelRequestRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

