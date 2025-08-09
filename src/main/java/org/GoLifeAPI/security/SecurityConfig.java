package org.GoLifeAPI.security;

import org.GoLifeAPI.infrastructure.FirebaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .securityMatcher("/**")
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, FirebaseAuthFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.setAllowedOrigins(List.of(
                "https://golife-deployment.web.app",
                "https://golife-462914.oa.r.appspot.com",
                "http://localhost:3000"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }
}