package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResponseCORS {
    @GetMapping("/cors")
    public Map<String, Object> response() {
        return Map.of("status", "UP", "time", Instant.now().toString());
    }
}
