package com.example.employee.dto;

import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inbound payload for creating or updating an employee.
 * All fields validated before reaching the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating an employee")
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Employee's first name", example = "Jane")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Employee's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 255)
    @Schema(description = "Unique work email address", example = "jane.doe@example.com")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,20}$", message = "Phone number format is invalid")
    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    private String phone;

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title must not exceed 150 characters")
    @Schema(description = "Official job title", example = "Senior Software Engineer")
    private String jobTitle;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Salary must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Annual gross salary", example = "95000.00")
    private BigDecimal salary;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    @Schema(description = "Date the employee was hired", example = "2023-03-15")
    private LocalDate hireDate;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Employee's date of birth", example = "1990-07-22")
    private LocalDate dateOfBirth;

    @Schema(description = "Employment status", example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"})
    private EmployeeStatus status;

    @Schema(description = "Type of employment", example = "FULL_TIME",
            allowableValues = {"FULL_TIME", "PART_TIME", "CONTRACT", "INTERN"})
    private EmploymentType employmentType;

    @Schema(description = "ID of the department this employee belongs to", example = "3")
    private Long departmentId;

    @Schema(description = "ID of the employee's direct manager", example = "12")
    private Long managerId;

    @Size(max = 500)
    @Schema(description = "Street address", example = "123 Main St, Suite 4")
    private String address;

    @Size(max = 100)
    @Schema(description = "City", example = "San Francisco")
    private String city;

    @Size(max = 100)
    @Schema(description = "Country", example = "USA")
    private String country;
}
