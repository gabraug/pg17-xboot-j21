package com.pg17xbootj21.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.model.Module;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    private final ObjectMapper objectMapper;
    private static final String MODULES_FILE = System.getProperty("user.dir") + "/data/modules.json";

    public ModuleService() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Module> getAllModules() throws IOException {
        File file = new File(MODULES_FILE);
        if (!file.exists()) {
            throw new RuntimeException("Modules file not found: " + MODULES_FILE);
        }
        return objectMapper.readValue(file, new TypeReference<List<Module>>() {});
    }

    public Optional<Module> findById(String moduleId) throws IOException {
        return getAllModules().stream()
                .filter(module -> module.getId().equals(moduleId))
                .findFirst();
    }

    public boolean isModuleActive(String moduleId) throws IOException {
        return findById(moduleId)
                .map(Module::isActive)
                .orElse(false);
    }
}

