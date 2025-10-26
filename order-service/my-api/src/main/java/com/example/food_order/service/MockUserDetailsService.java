package com.example.food_order.service;

import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MockUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void init() {
        initUsers();

        System.out.println("✅ Users initialized: " + users.keySet());
    }

    private void initUsers() {

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .authorities("ROLE_ADMIN", "ROLE_USER")
                .build();

        users.put("admin", admin);

        UserDetails normalUser = User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .authorities("ROLE_USER")
                .build();

        users.put("user", normalUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        System.out.println("🔍 Loading user: " + username);

        initUsers();

        UserDetails user = users.get(username);

        if (user == null) {
            System.out.println("❌ User not found!");
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }
}