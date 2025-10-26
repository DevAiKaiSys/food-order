package com.example.food_order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User endpoint accessed successfully");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("description", "This endpoint can be accessed by USER and ADMIN roles");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin endpoint accessed successfully");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("description", "This endpoint can only be accessed by ADMIN role");
        response.put("adminData", "Sensitive admin information");

        return ResponseEntity.ok(response);
    }
}