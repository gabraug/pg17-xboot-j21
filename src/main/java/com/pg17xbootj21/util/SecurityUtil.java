package com.pg17xbootj21.util;

import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;

public class SecurityUtil {

    public static String getUserIdFromRequest(HttpServletRequest request, AuthService authService, SessionService sessionService) {
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            return userId;
        }

        String authorization = request.getHeader("Authorization");
        String token = extractToken(authorization);
        
        if (token == null || !sessionService.isValidSession(token)) {
            return null;
        }

        return authService.getUserIdByToken(token);
    }

    private static String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}

