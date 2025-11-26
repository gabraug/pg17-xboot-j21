package com.pg17xbootj21.security;

import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityInterceptorTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private SecurityInterceptor securityInterceptor;

    @BeforeEach
    void setUp() {
        securityInterceptor = new SecurityInterceptor(sessionService, authService);
    }

    @Test
    void preHandle_WhenPublicEndpoint_ShouldBypassSecurity() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/login");

        boolean result = securityInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
    }

    @Test
    void preHandle_WhenTokenIsInvalid_ShouldReturnUnauthorized() throws Exception {
        when(request.getRequestURI()).thenReturn("/requests");
        when(request.getHeader(eq("Authorization"))).thenReturn("Bearer invalid");
        when(sessionService.isValidSession(eq("invalid"))).thenReturn(false);

        StringWriter writerContent = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writerContent));

        boolean result = securityInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(sessionService, times(1)).isValidSession(eq("invalid"));
        verify(response, times(1)).setStatus(eq(401));
        assertTrue(writerContent.toString().contains("Invalid or expired token"));
    }

    @Test
    void preHandle_WhenUserNotFound_ShouldReturnUnauthorized() throws Exception {
        when(request.getRequestURI()).thenReturn("/requests");
        when(request.getHeader(eq("Authorization"))).thenReturn("Bearer valid");
        when(sessionService.isValidSession(eq("valid"))).thenReturn(true);
        when(authService.getUserIdByToken(eq("valid"))).thenReturn(null);

        StringWriter writerContent = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writerContent));

        boolean result = securityInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(sessionService, times(1)).isValidSession(eq("valid"));
        verify(authService, times(1)).getUserIdByToken(eq("valid"));
        assertTrue(writerContent.toString().contains("User not found"));
    }

    @Test
    void preHandle_WhenTokenIsValid_ShouldAttachUserId() throws Exception {
        when(request.getRequestURI()).thenReturn("/requests");
        when(request.getHeader(eq("Authorization"))).thenReturn("Bearer valid");
        when(sessionService.isValidSession(eq("valid"))).thenReturn(true);
        when(authService.getUserIdByToken(eq("valid"))).thenReturn("user123");

        boolean result = securityInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request, times(1)).setAttribute(eq("userId"), eq("user123"));
    }
}

