package com.pg17xbootj21.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import com.pg17xbootj21.config.SecurityConfig;
import com.pg17xbootj21.security.SecurityInterceptor;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import(PingControllerTest.PingTestConfiguration.class)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityInterceptor securityInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(securityInterceptor.preHandle(
                argThat(request -> true),
                argThat(response -> true),
                argThat(handler -> true)
        )).thenReturn(true);
    }

    @Test
    void uptime_ShouldReturnStatusAndDurations() throws Exception {
        mockMvc.perform(get("/api/uptime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.uptimeSeconds").isNumber())
                .andExpect(jsonPath("$.uptimeFormatted").exists())
                .andExpect(jsonPath("$.startTime").value("2025-01-01T00:00:00Z"));
    }

    @TestConfiguration
    static class PingTestConfiguration {
        @Bean
        public Instant applicationStartTime() {
            return Instant.parse("2025-01-01T00:00:00Z");
        }
    }
}

