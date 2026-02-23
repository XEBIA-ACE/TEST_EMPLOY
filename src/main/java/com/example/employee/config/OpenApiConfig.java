package com.example.employee.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * <p>Once the application is running, browse to
 * <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a>
 * for the interactive API documentation.</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Management Service API",
                version = "1.0.0",
                description = "RESTful API for managing employees and organizational departments",
                contact = @Contact(
                        name = "Platform Engineering",
                        email = "platform@example.com"),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT")),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development"),
                @Server(url = "https://api-staging.example.com", description = "Staging"),
                @Server(url = "https://api.example.com", description = "Production")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide a valid JWT Bearer token. Obtain one from the auth service.")
public class OpenApiConfig {
    // Annotation-driven — no bean definitions required.
}
