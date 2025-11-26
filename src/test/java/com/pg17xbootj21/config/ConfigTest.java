package com.pg17xbootj21.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();
    private final UptimeConfig uptimeConfig = new UptimeConfig();

    @Test
    void customOpenApi_ShouldConfigureSecurityScheme() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        Info info = openAPI.getInfo();

        assertEquals("Sistema de Solicitação de Acesso a Módulos", info.getTitle());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
    }

    @Test
    void applicationStartTime_ShouldReturnInstant() {
        Instant startTime = uptimeConfig.applicationStartTime();

        assertNotNull(startTime);
        assertTrue(startTime.isBefore(Instant.now()) || startTime.equals(Instant.now()));
    }
}

