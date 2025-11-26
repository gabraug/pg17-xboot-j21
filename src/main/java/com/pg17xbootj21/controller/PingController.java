package com.pg17xbootj21.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {

    @Autowired
    private Instant applicationStartTime;

    @GetMapping("/uptime")
    public ResponseEntity<Map<String, Object>> uptime() {
        Duration uptime = Duration.between(applicationStartTime, Instant.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("uptimeSeconds", uptime.getSeconds());
        response.put("uptimeFormatted", formatDuration(uptime));
        response.put("startTime", applicationStartTime.toString());
        
        return ResponseEntity.ok(response);
    }
    
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}

