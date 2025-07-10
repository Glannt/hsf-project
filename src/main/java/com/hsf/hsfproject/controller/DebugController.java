package com.hsf.hsfproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class DebugController {

    @GetMapping("/debug")
    public String debug(Principal principal) {
        return "Debug endpoint working! Principal: " + (principal != null ? principal.getName() : "null");
    }

    @GetMapping("/debug-user")
    public String debugUser(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }
        return "User logged in: " + principal.getName();
    }

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }
} 