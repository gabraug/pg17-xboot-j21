package com.pg17xbootj21.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.model.Access;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessService {

    private final ObjectMapper objectMapper;
    private static final String ACCESSES_FILE = System.getProperty("user.dir") + "/data/accesses.json";

    public AccessService() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Access> getAllAccesses() throws IOException {
        File file = new File(ACCESSES_FILE);
        if (!file.exists()) {
            return List.of();
        }
        return objectMapper.readValue(file, new TypeReference<List<Access>>() {});
    }

    public List<Access> getActiveAccessesByUserId(String userId) throws IOException {
        return getAllAccesses().stream()
                .filter(access -> access.getUserId().equals(userId) && "ATIVO".equals(access.getStatus()))
                .collect(Collectors.toList());
    }

    public boolean hasActiveAccess(String userId, String moduleId) throws IOException {
        return getActiveAccessesByUserId(userId).stream()
                .anyMatch(access -> access.getModuleId().equals(moduleId));
    }

    public List<String> getActiveModuleIds(String userId) throws IOException {
        return getActiveAccessesByUserId(userId).stream()
                .map(Access::getModuleId)
                .collect(Collectors.toList());
    }
}

