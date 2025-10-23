package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> ok() {
        return Map.of(
                "status", "UP",
                "time", java.time.ZonedDateTime.now().toString()
        );
    }
}
