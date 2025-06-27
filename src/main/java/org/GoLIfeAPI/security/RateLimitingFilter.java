package org.GoLIfeAPI.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> userBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(30)
                        .refillGreedy(30, Duration.ofMinutes(1))
                )
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uid = (String) request.getAttribute("uid");

        Bucket bucket = userBuckets.get(uid, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("text/plain");
            response.getWriter().write("Demasiadas peticiones. Espera un momento por favor.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.equals("/docs/openapi.yaml");
    }

}