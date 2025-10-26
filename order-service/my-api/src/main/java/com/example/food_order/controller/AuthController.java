package com.example.food_order.controller;

import com.example.food_order.dto.request.LoginRequest;
import com.example.food_order.dto.response.LoginResponse;
import com.example.food_order.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // ตรวจสอบ username และ password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // โหลดข้อมูล user
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // สร้าง JWT token
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(LoginResponse.builder()
                    .token(token)
                    .username(userDetails.getUsername())
                    .message("Login successful")
                    .build());

        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage());

            return ResponseEntity.status(401)
                    .body(LoginResponse.builder()
                            .message("Invalid username or password")
                            .build());
        }
    }
}