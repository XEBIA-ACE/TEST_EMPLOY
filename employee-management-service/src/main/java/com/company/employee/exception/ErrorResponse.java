package com.company.employee.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard error response envelope returned by the API for all error cases.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error category", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Human-readable error message", example = "Employee not found with id: 99")
    private String message;

    @Schema(description = "Request path that triggered the error", example = "/api/v1/employees/99")
    private String path;

    @Schema(description = "Timestamp of the error (UTC)")
    private OffsetDateTime timestamp;

    /** Per-field validation errors; present only for 400 Bad Request responses. */
    @Schema(description = "Field-level validation errors (only present on 400 responses)")
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @Schema(description = "Validation error for a specific request field")
    public static class FieldError {

        @Schema(description = "Field name", example = "email")
        private String field;

        @Schema(description = "Rejected value", example = "not-an-email")
        private String rejectedValue;

        @Schema(description = "Validation message", example = "Email must be a valid address")
        private String message;
    }
}
