package com.example.employeemanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error envelope returned on API failures")
public class ErrorResponse {

    @Schema(example = "2024-01-20T14:30:00")
    private LocalDateTime timestamp;

    @Schema(example = "404")
    private int status;

    @Schema(example = "NOT_FOUND")
    private String error;

    @Schema(example = "Employee not found with id: 42")
    private String message;

    @Schema(description = "Request path that triggered the error", example = "/api/v1/employees/42")
    private String path;

    @Schema(description = "Field-level validation errors (present only for 400 responses)")
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @Schema(description = "Per-field validation violation detail")
    public static class FieldError {
        @Schema(example = "email")
        private String field;

        @Schema(example = "must be a valid email address")
        private String message;

        @Schema(example = "not-an-email")
        private Object rejectedValue;
    }
}
