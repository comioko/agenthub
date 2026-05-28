package com.agenthub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "AgentHub Backend");
        return ResponseEntity.ok(result);
    }
}
