package com.example.employee.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uniform error envelope returned for all 4xx / 5xx responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response body")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error category", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Human-readable error message", example = "Employee not found with id=42")
    private String message;

    @Schema(description = "Request path that triggered the error", example = "/api/v1/employees/42")
    private String path;

    @Builder.Default
    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Field-level validation errors; present only for 400 Bad Request responses.
     */
    @Schema(description = "Per-field validation errors (present on 400 responses)")
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of a single field validation failure")
    public static class FieldError {

        @Schema(description = "Name of the offending field", example = "email")
        private String field;

        @Schema(description = "Rejected value", example = "not-an-email")
        private Object rejectedValue;

        @Schema(description = "Validation message", example = "Email must be a valid address")
        private String message;
    }
}
