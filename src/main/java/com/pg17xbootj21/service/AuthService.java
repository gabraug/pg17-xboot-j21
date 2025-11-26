package com.pg17xbootj21.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.model.User;
import com.pg17xbootj21.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final ObjectMapper objectMapper;
    private final SessionService sessionService;
    private static final String USERS_FILE = System.getProperty("user.dir") + "/data/users.json";

    public AuthService(SessionService sessionService) {
        this.objectMapper = new ObjectMapper();
        this.sessionService = sessionService;
    }

    public User authenticate(String email, String password) {
        try {
            List<User> users = loadUsers();
            return users.stream()
                    .filter(user -> user.getEmail().equals(email) 
                            && PasswordUtil.matches(password, user.getPassword()))
                    .findFirst()
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

    private List<User> loadUsers() throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            throw new RuntimeException("Users file not found: " + USERS_FILE);
        }
        return objectMapper.readValue(file, new TypeReference<List<User>>() {});
    }
}

