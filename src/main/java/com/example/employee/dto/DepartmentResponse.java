package com.example.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbound department representation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Department data returned by the API")
public class DepartmentResponse {

    @Schema(description = "Unique department identifier", example = "3")
    private Long id;

    @Schema(description = "Department name", example = "Engineering")
    private String name;

    @Schema(description = "Department description", example = "Builds and maintains software products")
    private String description;

    @Schema(description = "Annual budget (USD)", example = "1500000.00")
    private Double budget;

    @Schema(description = "Location", example = "San Francisco, CA")
    private String location;

    @Schema(description = "Total number of active employees in this department", example = "25")
    private int employeeCount;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last-updated timestamp")
    private LocalDateTime updatedAt;
}
