package com.pg17xbootj21.controller;

import com.pg17xbootj21.dto.ErrorResponse;
import com.pg17xbootj21.dto.ModuleResponse;
import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.service.ModuleService;
import com.pg17xbootj21.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/modules")
@Tag(name = "Módulos", description = "Endpoints para consulta de módulos disponíveis")
@SecurityRequirement(name = "Bearer Authentication")
public class ModuleController {

    private final ModuleService moduleService;
    private final SessionService sessionService;

    public ModuleController(ModuleService moduleService, SessionService sessionService) {
        this.moduleService = moduleService;
        this.sessionService = sessionService;
    }

    @Operation(summary = "Listar módulos disponíveis", description = "Retorna a lista completa de módulos disponíveis no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de módulos retornada com sucesso",
                content = @Content(schema = @Schema(implementation = ModuleResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido ou expirado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

