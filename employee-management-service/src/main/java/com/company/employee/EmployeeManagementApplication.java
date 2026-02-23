package com.company.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Employee Management Service.
 *
 * <p>This service exposes a RESTful API for creating, reading, updating,
 * and deleting employee records. It uses Spring Data JPA for persistence,
 * Flyway for schema migrations, and SpringDoc for API documentation.
 */
@SpringBootApplication
@EnableJpaAuditing
public class EmployeeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}
