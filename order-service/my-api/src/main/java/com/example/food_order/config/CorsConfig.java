package com.example.food_order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // อนุญาตให้รับ credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // อนุญาต origin จาก Angular (เปลี่ยน port ตามที่คุณใช้)
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://localhost:80");

        // อนุญาตทุก header
        config.addAllowedHeader("*");

        // อนุญาตทุก HTTP method (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*");

        // ใช้กับทุก endpoint
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}