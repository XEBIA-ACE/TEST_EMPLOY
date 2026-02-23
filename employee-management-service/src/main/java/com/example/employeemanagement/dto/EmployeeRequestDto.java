package com.example.employeemanagement.dto;

import com.example.employeemanagement.model.Employee.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for creating or updating an employee")
public class EmployeeRequestDto {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "Employee's first name", example = "Jane")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Employee's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "Unique corporate email address", example = "jane.doe@example.com")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,30}$", message = "Phone number is invalid")
    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    private String phone;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @NotBlank(message = "Job title is required")
    @Size(max = 150)
    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String jobTitle;

    @NotNull(message = "Employment status is required")
    @Schema(description = "Current employment status", example = "ACTIVE")
    private EmploymentStatus employmentStatus;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Salary must have at most 13 integer digits and 2 decimal places")
    @Schema(description = "Annual salary in USD", example = "95000.00")
    private BigDecimal salary;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    @Schema(description = "Date the employee was hired", example = "2023-06-15")
    private LocalDate hireDate;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Employee's date of birth", example = "1990-04-22")
    private LocalDate dateOfBirth;

    @Size(max = 500)
    @Schema(description = "Home or work address", example = "123 Main St, Springfield, IL 62701")
    private String address;

    @Schema(description = "ID of the employee's direct manager", example = "42")
    private Long managerId;
}
