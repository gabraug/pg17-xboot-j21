package com.pg17xbootj21.security;

import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;
    private final AuthService authService;

    public SecurityInterceptor(SessionService sessionService, AuthService authService) {
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        if (isPublicEndpoint(path)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        String token = extractToken(authorization);

        if (token == null || !sessionService.isValidSession(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\",\"status\":401}");
            return false;
        }

        String userId = authService.getUserIdByToken(token);
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"User not found\",\"status\":401}");
            return false;
        }

        request.setAttribute("userId", userId);
        return true;
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/auth/login") || 
               path.startsWith("/api/uptime") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/swagger-ui.html") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/error");
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}

