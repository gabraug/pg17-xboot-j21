package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.repository.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    public Optional<Module> findById(String moduleId) {
        return moduleRepository.findById(moduleId);
    }

    public boolean isModuleActive(String moduleId) {
        return findById(moduleId)
                .map(Module::isActive)
                .orElse(false);
    }
}

