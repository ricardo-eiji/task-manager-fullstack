package com.taskmanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

// This is the file that is related to expecting admin:admin123
// The [@Value("${ADMIN_PASSWORD}")] is from .env
// The line [auth.anyRequest().authenticated()] means every API request must provide those credentials 
// These [.username("admin")] [.password(encoder.encode(adminPassword))] is where we define 


@Configuration
public class SecurityConfig
{
    @Value("${ADMIN_PASSWORD}") // This env var is from .env
    private String adminPassword;
    // then: .password(encoder.encode(adminPassword)) - below

    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    // PasswordEncoder — passwords are never stored in plain text, even in-memory

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder)
    {
        UserDetails user = User.builder()
            .username("admin")
            .password(encoder.encode(adminPassword))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
    // InMemoryUserDetailsManager — defines a fixed user (admin / admin123) 
        // instead of an auto-generated password

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception
    {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(basic -> {});
        return http.build();
    }
    // SecurityFilterChain — defines the rule: every request must be authenticated, 
        // using basic auth

}