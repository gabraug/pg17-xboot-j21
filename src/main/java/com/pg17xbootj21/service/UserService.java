package com.pg17xbootj21.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.model.User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final ObjectMapper objectMapper;
    private static final String USERS_FILE = System.getProperty("user.dir") + "/data/users.json";

    public UserService() {
        this.objectMapper = new ObjectMapper();
    }

    public Optional<User> findById(String userId) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    private List<User> getAllUsers() throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            throw new RuntimeException("Users file not found: " + USERS_FILE);
        }
        return objectMapper.readValue(file, new TypeReference<List<User>>() {});
    }
}

