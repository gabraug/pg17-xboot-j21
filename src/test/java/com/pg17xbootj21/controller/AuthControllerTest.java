package com.pg17xbootj21.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.dto.LoginRequest;
import com.pg17xbootj21.model.User;
import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.SessionService;
import com.pg17xbootj21.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = PasswordUtil.hash("password123");
        
        user = new User();
        user.setId("user1");
        user.setEmail("user@test.com");
        user.setPassword(hashedPassword);
        user.setName("Test User");
        user.setDepartment("TI");
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnToken() throws Exception {
        String token = "test-token-123";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("password123");

        when(authService.authenticate(eq("user@test.com"), eq("password123"))).thenReturn(user);
        when(authService.createSession(eq("user@test.com"))).thenReturn(token);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("user@test.com"));

        verify(authService, times(1)).authenticate(eq("user@test.com"), eq("password123"));
        verify(authService, times(1)).createSession(eq("user@test.com"));
    }

    @Test
    void login_WhenCredentialsAreInvalid_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("wrongpassword");

        when(authService.authenticate(eq("user@test.com"), eq("wrongpassword"))).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(authService, times(1)).authenticate(eq("user@test.com"), eq("wrongpassword"));
        verify(authService, never()).createSession(eq("user@test.com"));
    }

    @Test
    void login_WhenEmailIsMissing_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WhenPasswordIsMissing_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("password123");

        when(authService.authenticate(eq("user@test.com"), eq("password123")))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(authService, times(1)).authenticate(eq("user@test.com"), eq("password123"));
    }
}

