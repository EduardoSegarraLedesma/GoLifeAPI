package org.GoLIfeAPI.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of("message", "GoLifeAPI is ready!"));
    }

    @GetMapping("/_ah/start")
    public ResponseEntity<Map<String, String>> start() {
        return ResponseEntity.ok(Map.of("message", "App is warming up"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }
}
