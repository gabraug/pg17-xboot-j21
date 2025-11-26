package com.pg17xbootj21.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class UptimeConfig {

    @Bean
    public Instant applicationStartTime() {
        return Instant.now();
    }
}

