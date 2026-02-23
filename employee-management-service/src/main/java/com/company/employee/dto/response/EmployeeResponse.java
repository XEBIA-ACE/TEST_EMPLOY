package com.company.employee.dto.response;

import com.company.employee.model.entity.Employee.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Response body containing employee data returned by the API.
 */
@Data
@Schema(description = "Employee record returned by the API")
public class EmployeeResponse {

    @Schema(description = "Unique identifier", example = "42")
    private Long id;

    @Schema(description = "First name", example = "Jane")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name (firstName + lastName)", example = "Jane Doe")
    private String fullName;

    @Schema(description = "Corporate email address", example = "jane.doe@company.com")
    private String email;

    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Department", example = "Engineering")
    private String department;

    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String jobTitle;

    @Schema(description = "Annual gross salary", example = "95000.00")
    private BigDecimal salary;

    @Schema(description = "Hire date", example = "2022-03-15")
    private LocalDate hireDate;

    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status;

    @Schema(description = "Record creation timestamp (UTC)")
    private OffsetDateTime createdAt;

    @Schema(description = "Record last-modified timestamp (UTC)")
    private OffsetDateTime updatedAt;
}
