package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Access;
import com.pg17xbootj21.model.Module;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BusinessRuleService {

    private final ModuleService moduleService;
    private final AccessService accessService;

    public BusinessRuleService(ModuleService moduleService, AccessService accessService) {
        this.moduleService = moduleService;
        this.accessService = accessService;
    }

    public String validateBusinessRules(String userId, String department, List<String> requestedModuleIds) throws IOException {
        List<String> activeModuleIds = accessService.getActiveModuleIds(userId);
        int currentActiveModules = activeModuleIds.size();
        int maxModules = "TI".equals(department) ? 10 : 5;

        for (String moduleId : requestedModuleIds) {
            Module module = moduleService.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));

            if (!isDepartmentAllowed(department, module)) {
                return "Departamento sem permissão para acessar este módulo";
            }

            if (hasIncompatibleModule(activeModuleIds, module)) {
                return "Módulo incompatível com outro módulo já ativo em seu perfil";
            }
        }

        if (currentActiveModules + requestedModuleIds.size() > maxModules) {
            return "Limite de módulos ativos atingido";
        }

        for (int i = 0; i < requestedModuleIds.size(); i++) {
            String moduleId1 = requestedModuleIds.get(i);
            Module module1 = moduleService.findById(moduleId1).orElse(null);
            if (module1 != null) {
                for (int j = i + 1; j < requestedModuleIds.size(); j++) {
                    String moduleId2 = requestedModuleIds.get(j);
                    if (module1.getIncompatibleModules().contains(moduleId2)) {
                        return "Módulo incompatível com outro módulo já ativo em seu perfil";
                    }
                }
            }
        }

        return null;
    }

    private boolean isDepartmentAllowed(String department, Module module) {
        if ("TI".equals(department)) {
            return true;
        }
        return module.getAllowedDepartments().contains(department);
    }

    private boolean hasIncompatibleModule(List<String> activeModuleIds, Module requestedModule) throws IOException {
        for (String activeModuleId : activeModuleIds) {
            Module activeModule = moduleService.findById(activeModuleId).orElse(null);
            if (activeModule != null) {
                if (activeModule.getIncompatibleModules().contains(requestedModule.getId()) ||
                    requestedModule.getIncompatibleModules().contains(activeModuleId)) {
                    return true;
                }
            }
        }
        return false;
    }
}

