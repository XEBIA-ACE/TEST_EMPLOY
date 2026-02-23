package com.example.employee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration.
 *
 * <p>The service is stateless (JWT-ready). In this scaffolded form all
 * authenticated endpoints require a valid Bearer token. CSRF is disabled
 * because we use stateless REST semantics.</p>
 *
 * <p>To wire a real JWT provider, inject a {@code JwtAuthenticationFilter}
 * before {@code UsernamePasswordAuthenticationFilter} and configure the
 * UserDetailsService / AuthenticationProvider beans as needed.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Public paths that do not require authentication. */
    private static final String[] PUBLIC_PATHS = {
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — APIs are stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session management (no HTTP session)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Health checks and API docs are always reachable
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // Allow Prometheus metrics endpoint for internal scraping
                        .requestMatchers("/actuator/prometheus").permitAll()
                        // Read operations are open to any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                        // Mutations require authenticated users (scope/role checks plugged in here)
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").authenticated()
                        .anyRequest().authenticated())

                // Enable HTTP Basic for quick local testing; replace with JWT filter in prod
                .httpBasic(basic -> {});

        return http.build();
    }
}
