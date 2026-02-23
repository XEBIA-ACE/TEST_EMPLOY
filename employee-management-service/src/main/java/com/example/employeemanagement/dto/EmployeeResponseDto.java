package com.example.employeemanagement.dto;

import com.example.employeemanagement.model.Employee.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee resource representation")
public class EmployeeResponseDto {

    @Schema(description = "Internal database ID", example = "1")
    private Long id;

    @Schema(description = "Human-readable employee number", example = "EMP-000042")
    private String employeeNumber;

    @Schema(example = "Jane")
    private String firstName;

    @Schema(example = "Doe")
    private String lastName;

    @Schema(description = "Full display name", example = "Jane Doe")
    private String fullName;

    @Schema(example = "jane.doe@example.com")
    private String email;

    @Schema(example = "+1-555-123-4567")
    private String phone;

    @Schema(example = "Engineering")
    private String department;

    @Schema(example = "Senior Software Engineer")
    private String jobTitle;

    @Schema(example = "ACTIVE")
    private EmploymentStatus employmentStatus;

    @Schema(example = "95000.00")
    private BigDecimal salary;

    @Schema(example = "2023-06-15")
    private LocalDate hireDate;

    @Schema(example = "1990-04-22")
    private LocalDate dateOfBirth;

    @Schema(example = "123 Main St, Springfield, IL 62701")
    private String address;

    @Schema(description = "Manager's employee ID", example = "42")
    private Long managerId;

    @Schema(example = "2023-06-15T09:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2024-01-20T14:30:00")
    private LocalDateTime updatedAt;

    @Schema(example = "admin")
    private String createdBy;

    @Schema(example = "hruser")
    private String updatedBy;
}
