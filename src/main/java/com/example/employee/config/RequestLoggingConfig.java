package com.example.employee.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Registers a lightweight request/response logging filter.
 *
 * <p>Logs method, URI, status, and elapsed time for every HTTP request at
 * DEBUG level to avoid flooding production logs.</p>
 */
@Configuration
@Slf4j
public class RequestLoggingConfig {

    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {

            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                long start = System.currentTimeMillis();
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    long elapsed = System.currentTimeMillis() - start;
                    log.debug("{} {} → {} ({}ms)",
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus(),
                            elapsed);
                }
            }

            /** Skip logging for actuator endpoints to reduce noise. */
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                return request.getRequestURI().startsWith("/actuator");
            }
        };
    }
}
