package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    private ModuleService moduleService;

    private Module module1;
    private Module module2;
    private List<Module> modules;

    @BeforeEach
    void setUp() {
        moduleService = new ModuleService(moduleRepository);

        module1 = new Module();
        module1.setId("module1");
        module1.setName("Module One");
        module1.setDescription("Description One");
        module1.setActive(true);
        module1.setAllowedDepartments(Arrays.asList("TI", "RH"));
        module1.setIncompatibleModules(Arrays.asList("module2"));

        module2 = new Module();
        module2.setId("module2");
        module2.setName("Module Two");
        module2.setDescription("Description Two");
        module2.setActive(false);
        module2.setAllowedDepartments(Arrays.asList("RH"));
        module2.setIncompatibleModules(Arrays.asList("module1"));

        modules = Arrays.asList(module1, module2);
    }

    @Test
    void getAllModules_WhenModulesExist_ShouldReturnModules() {
        when(moduleRepository.findAll()).thenReturn(modules);

        List<Module> result = moduleService.getAllModules();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("module1", result.get(0).getId());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    void getAllModules_WhenNoModulesExist_ShouldReturnEmptyList() {
        when(moduleRepository.findAll()).thenReturn(Collections.emptyList());

        List<Module> result = moduleService.getAllModules();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenModuleExists_ShouldReturnModule() {
        when(moduleRepository.findById(eq("module1"))).thenReturn(Optional.of(module1));

        Optional<Module> result = moduleService.findById("module1");

        assertTrue(result.isPresent());
        assertEquals("module1", result.get().getId());
        assertEquals("Module One", result.get().getName());
        verify(moduleRepository, times(1)).findById(eq("module1"));
    }

    @Test
    void findById_WhenModuleDoesNotExist_ShouldReturnEmpty() {
        when(moduleRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

        Optional<Module> result = moduleService.findById("nonexistent");

        assertFalse(result.isPresent());
        verify(moduleRepository, times(1)).findById(eq("nonexistent"));
    }

    @Test
    void isModuleActive_WhenModuleIsActive_ShouldReturnTrue() {
        when(moduleRepository.findById(eq("module1"))).thenReturn(Optional.of(module1));

        boolean result = moduleService.isModuleActive("module1");

        assertTrue(result);
        verify(moduleRepository, times(1)).findById(eq("module1"));
    }

    @Test
    void isModuleActive_WhenModuleIsNotActive_ShouldReturnFalse() {
        when(moduleRepository.findById(eq("module2"))).thenReturn(Optional.of(module2));

        boolean result = moduleService.isModuleActive("module2");

        assertFalse(result);
        verify(moduleRepository, times(1)).findById(eq("module2"));
    }

    @Test
    void isModuleActive_WhenModuleDoesNotExist_ShouldReturnFalse() {
        when(moduleRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

        boolean result = moduleService.isModuleActive("nonexistent");

        assertFalse(result);
        verify(moduleRepository, times(1)).findById(eq("nonexistent"));
    }
}
