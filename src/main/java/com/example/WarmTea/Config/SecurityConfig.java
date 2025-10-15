package com.example.WarmTea.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 🔹 Основная настройка безопасности
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // отключаем CSRF
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // все запросы разрешены
        return http.build();
    }
}
