package com.pg17xbootj21.dto;

public class LoginResponse {
    private String accessToken;
    private String name;
    private String email;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String name, String email) {
        this.accessToken = accessToken;
        this.name = name;
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

