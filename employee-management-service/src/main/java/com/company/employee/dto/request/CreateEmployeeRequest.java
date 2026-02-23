package com.company.employee.dto.request;

import com.company.employee.model.entity.Employee.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating a new employee.
 */
@Data
@Schema(description = "Payload for creating a new employee record")
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "Employee first name", example = "Jane")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Employee last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Unique corporate email address", example = "jane.doe@company.com")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,30}$", message = "Phone number format is invalid")
    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    private String phone;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Schema(description = "Department the employee belongs to", example = "Engineering")
    private String department;

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title must not exceed 150 characters")
    @Schema(description = "Employee job title", example = "Senior Software Engineer")
    private String jobTitle;

    @DecimalMin(value = "0.00", message = "Salary must be a positive value")
    @Digits(integer = 10, fraction = 2, message = "Salary must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Annual gross salary", example = "95000.00")
    private BigDecimal salary;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Schema(description = "Date when the employee was hired (ISO-8601)", example = "2022-03-15")
    private LocalDate hireDate;

    @Schema(description = "Employment status", example = "ACTIVE", defaultValue = "ACTIVE")
    private EmployeeStatus status;
}
