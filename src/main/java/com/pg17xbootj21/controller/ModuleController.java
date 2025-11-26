package com.pg17xbootj21.controller;

import com.pg17xbootj21.dto.ErrorResponse;
import com.pg17xbootj21.dto.ModuleResponse;
import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.service.ModuleService;
import com.pg17xbootj21.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/modules")
public class ModuleController {

    private final ModuleService moduleService;
    private final SessionService sessionService;

    public ModuleController(ModuleService moduleService, SessionService sessionService) {
        this.moduleService = moduleService;
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<?> listModules(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (token == null || !sessionService.isValidSession(token)) {
            ErrorResponse error = new ErrorResponse(
                "Unauthorized",
                "Invalid or expired token",
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            List<Module> modules = moduleService.getAllModules();
            List<ModuleResponse> responses = modules.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private ModuleResponse toResponse(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setName(module.getName());
        response.setDescription(module.getDescription());
        response.setAllowedDepartments(module.getAllowedDepartments());
        response.setActive(module.isActive());
        response.setIncompatibleModules(module.getIncompatibleModules());
        return response;
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}

