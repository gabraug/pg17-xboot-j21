package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessRuleServiceTest {

    @Mock
    private ModuleService moduleService;

    @Mock
    private AccessService accessService;

    @InjectMocks
    private BusinessRuleService businessRuleService;

    private Module module1;
    private Module module2;
    private Module module3;

    @BeforeEach
    void setUp() {
        module1 = new Module();
        module1.setId("module1");
        module1.setName("Module One");
        module1.setActive(true);
        module1.setAllowedDepartments(Arrays.asList("TI", "RH"));
        module1.setIncompatibleModules(Arrays.asList("module2"));

        module2 = new Module();
        module2.setId("module2");
        module2.setName("Module Two");
        module2.setActive(true);
        module2.setAllowedDepartments(Arrays.asList("RH"));
        module2.setIncompatibleModules(Arrays.asList("module1", "module3"));

        module3 = new Module();
        module3.setId("module3");
        module3.setName("Module Three");
        module3.setActive(true);
        module3.setAllowedDepartments(Arrays.asList("TI", "RH"));
        module3.setIncompatibleModules(Collections.emptyList());

        lenient().when(moduleService.findById(argThat(id -> id != null)))
                .thenAnswer(invocation -> {
                    String moduleId = invocation.getArgument(0);
                    if ("module1".equals(moduleId)) {
                        return Optional.of(module1);
                    }
                    if ("module2".equals(moduleId)) {
                        return Optional.of(module2);
                    }
                    if ("module3".equals(moduleId)) {
                        return Optional.of(module3);
                    }
                    return Optional.empty();
                });
    }

    @Test
    void validateBusinessRules_WhenAllRulesPass_ShouldReturnNull() {
        String userId = "user1";
        String department = "TI";
        List<String> requestedModuleIds = Arrays.asList("module1");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertNull(result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module1"));
    }

    @Test
    void validateBusinessRules_WhenDepartmentNotAllowed_ShouldReturnError() {
        String userId = "user1";
        String department = "VENDAS";
        List<String> requestedModuleIds = Arrays.asList("module2");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertEquals("Departamento sem permissão para acessar este módulo", result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module2"));
    }

    @Test
    void validateBusinessRules_WhenDepartmentIsTI_ShouldAllowAnyModule() {
        String userId = "user1";
        String department = "TI";
        List<String> requestedModuleIds = Arrays.asList("module2");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module2"))).thenReturn(Optional.of(module2));

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertNull(result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module2"));
    }

    @Test
    void validateBusinessRules_WhenIncompatibleWithActiveModule_ShouldReturnError() {
        String userId = "user1";
        String department = "TI";
        List<String> requestedModuleIds = Arrays.asList("module2");
        List<String> activeModuleIds = Arrays.asList("module1");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(activeModuleIds);

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertEquals("Módulo incompatível com outro módulo já ativo em seu perfil", result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module1"));
        verify(moduleService, atLeastOnce()).findById(eq("module2"));
    }

    @Test
    void validateBusinessRules_WhenIncompatibleModulesInRequest_ShouldReturnError() {
        String userId = "user1";
        String department = "TI";
        List<String> requestedModuleIds = Arrays.asList("module1", "module2");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertEquals("Módulo incompatível com outro módulo já ativo em seu perfil", result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module1"));
        verify(moduleService, atLeastOnce()).findById(eq("module2"));
    }

    @Test
    void validateBusinessRules_WhenMaxModulesExceededForNonTI_ShouldReturnError() {
        String userId = "user1";
        String department = "RH";
        List<String> requestedModuleIds = Arrays.asList("module1", "module3");
        List<String> activeModuleIds = Arrays.asList("module2", "module1", "module3", "module1", "module2");

        module1.setIncompatibleModules(Collections.emptyList());
        module2.setIncompatibleModules(Collections.emptyList());

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(activeModuleIds);

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertEquals("Limite de módulos ativos atingido", result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module1"));
        verify(moduleService, atLeastOnce()).findById(eq("module3"));
    }

    @Test
    void validateBusinessRules_WhenMaxModulesExceededForTI_ShouldReturnError() {
        String userId = "user1";
        String department = "TI";
        List<String> requestedModuleIds = Arrays.asList("module1");
        List<String> activeModuleIds = Arrays.asList("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(activeModuleIds);

        String result = businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);

        assertEquals("Limite de módulos ativos atingido", result);
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, atLeastOnce()).findById(eq("module1"));
    }

    @Test
    void validateBusinessRules_WhenModuleNotFound_ShouldThrowException() {
        String userId = "user1";
        String department = "TI";
        List<String> requestedModuleIds = Arrays.asList("nonexistent");

        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> {
            businessRuleService.validateBusinessRules(userId, department, requestedModuleIds);
        });
        
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(moduleService, times(1)).findById(eq("nonexistent"));
    }
}

