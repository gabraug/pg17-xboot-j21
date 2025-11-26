package com.pg17xbootj21.controller;

import com.pg17xbootj21.config.SecurityConfig;
import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.security.SecurityInterceptor;
import com.pg17xbootj21.service.ModuleService;
import com.pg17xbootj21.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ModuleController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class ModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModuleService moduleService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private SecurityInterceptor securityInterceptor;

    private Module module1;
    private Module module2;
    private List<Module> modules;

    @BeforeEach
    void setUp() throws Exception {
        when(securityInterceptor.preHandle(
                argThat(request -> true),
                argThat(response -> true),
                argThat(handler -> true)
        )).thenReturn(true);
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
    void listModules_WhenValidToken_ShouldReturnModules() throws Exception {
        String token = "valid-token-123";
        String authorization = "Bearer " + token;

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(moduleService.getAllModules()).thenReturn(modules);

        mockMvc.perform(get("/modules")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Module One"))
                .andExpect(jsonPath("$[0].description").value("Description One"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].name").value("Module Two"))
                .andExpect(jsonPath("$[1].active").value(false));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(moduleService, times(1)).getAllModules();
    }

    @Test
    void listModules_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        String token = "invalid-token";
        String authorization = "Bearer " + token;

        when(sessionService.isValidSession(eq(token))).thenReturn(false);

        mockMvc.perform(get("/modules")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(moduleService, never()).getAllModules();
    }

    @Test
    void listModules_WhenNoToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/modules")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(moduleService, never()).getAllModules();
    }

    @Test
    void listModules_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        String token = "valid-token-123";
        String authorization = "Bearer " + token;

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(moduleService.getAllModules()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/modules")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(moduleService, times(1)).getAllModules();
    }
}

