package com.example.employee.dto;

import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outbound employee representation returned by all read endpoints.
 * Never exposes internal entity fields directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee data returned by the API")
public class EmployeeResponse {

    @Schema(description = "Unique employee identifier", example = "42")
    private Long id;

    @Schema(description = "First name", example = "Jane")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name (first + last)", example = "Jane Doe")
    private String fullName;

    @Schema(description = "Work email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Contact phone", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String jobTitle;

    @Schema(description = "Annual gross salary", example = "95000.00")
    private BigDecimal salary;

    @Schema(description = "Hire date", example = "2023-03-15")
    private LocalDate hireDate;

    @Schema(description = "Date of birth", example = "1990-07-22")
    private LocalDate dateOfBirth;

    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status;

    @Schema(description = "Type of employment", example = "FULL_TIME")
    private EmploymentType employmentType;

    @Schema(description = "Department summary")
    private DepartmentSummary department;

    @Schema(description = "Direct manager summary")
    private ManagerSummary manager;

    @Schema(description = "Street address", example = "123 Main St")
    private String address;

    @Schema(description = "City", example = "San Francisco")
    private String city;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last-updated timestamp")
    private LocalDateTime updatedAt;

    /** Nested summary to avoid circular serialization. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Condensed department information embedded in employee responses")
    public static class DepartmentSummary {
        private Long id;
        private String name;
        private String location;
    }

    /** Condensed manager info to avoid deep nesting. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Condensed manager information embedded in employee responses")
    public static class ManagerSummary {
        private Long id;
        private String fullName;
        private String jobTitle;
        private String email;
    }
}
