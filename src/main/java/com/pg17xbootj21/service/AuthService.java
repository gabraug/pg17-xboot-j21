package com.pg17xbootj21.service;

import com.pg17xbootj21.model.User;
import com.pg17xbootj21.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class AuthService {

    private final SessionService sessionService;
    private final UserService userService;

    public AuthService(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    public User authenticate(String email, String password) {
        try {
            return userService.findByEmail(email)
                    .filter(user -> PasswordUtil.matches(password, user.getPassword()))
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("Error loading users", e);
        }
    }

    public String createSession(String email) {
        String token = UUID.randomUUID().toString();
        sessionService.createSession(token, email);
        return token;
    }

    public String getUserIdByToken(String token) {
        try {
            String email = sessionService.getEmailByToken(token);
            if (email == null) {
                return null;
            }
            return userService.findByEmail(email)
                    .map(User::getId)
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("Error getting user", e);
        }
    }
}

