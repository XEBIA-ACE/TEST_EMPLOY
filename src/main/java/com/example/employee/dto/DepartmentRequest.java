package com.example.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for creating or updating a department.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating a department")
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Unique department name", example = "Engineering")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Description of the department's purpose", example = "Builds and maintains software products")
    private String description;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
    @Schema(description = "Annual budget (USD)", example = "1500000.00")
    private Double budget;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Physical or virtual location of the department", example = "San Francisco, CA")
    private String location;
}
