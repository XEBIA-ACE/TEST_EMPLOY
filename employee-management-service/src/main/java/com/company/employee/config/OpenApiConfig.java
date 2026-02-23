package com.company.employee.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc / OpenAPI 3 configuration.
 *
 * <p>The generated specification is served at {@code /v3/api-docs} and the
 * interactive UI is available at {@code /swagger-ui/index.html}.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI employeeManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Employee Management Service API")
                .description("RESTful API for managing employee records including creation, retrieval, update, and deletion.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Engineering Team")
                    .email("engineering@company.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url(serverUrl).description("Current environment")
            ));
    }
}
