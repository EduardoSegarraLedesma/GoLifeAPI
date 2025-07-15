package org.GoLIfeAPI.security;

import org.GoLIfeAPI.infrastructure.FirebaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final FirebaseService firebaseService;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(FirebaseService firebaseService,
                          RateLimitingFilter rateLimitingFilter) {
        this.firebaseService = firebaseService;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/api/salud", "/api/salud/**",
                        "/swagger-ui.html", "/swagger-ui/**",
                        "/v3/api-docs/**", "/swagger-resources/**",
                        "/openapi.yaml", "/favicon.ico"
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        FirebaseAuthFilter firebaseAuthFilter = new FirebaseAuthFilter(firebaseService);
        http.securityMatcher("/**")
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, FirebaseAuthFilter.class);
        return http.build();
    }
}