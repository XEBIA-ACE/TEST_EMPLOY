package com.company.employee.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Centralised exception handler for all controller-layer exceptions.
 *
 * <p>Maps domain exceptions and Spring MVC exceptions to consistent
 * {@link ErrorResponse} JSON payloads with appropriate HTTP status codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Domain exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        EmployeeNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("Employee not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
        DuplicateEmailException ex, HttpServletRequest request
    ) {
        log.warn("Duplicate email conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request.getRequestURI());
    }

    // -------------------------------------------------------------------------
    // Validation exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        BindingResult result = ex.getBindingResult();
        List<ErrorResponse.FieldError> fieldErrors = result.getFieldErrors().stream()
            .map(fe -> ErrorResponse.FieldError.builder()
                .field(fe.getField())
                .rejectedValue(fe.getRejectedValue() == null ? null : fe.getRejectedValue().toString())
                .message(fe.getDefaultMessage())
                .build())
            .toList();

        log.warn("Validation failed with {} error(s)", fieldErrors.size());

        ErrorResponse body = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message("Request validation failed")
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now())
            .fieldErrors(fieldErrors)
            .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex, HttpServletRequest request
    ) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        log.warn(message);
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
        HttpMessageNotReadableException ex, HttpServletRequest request
    ) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Malformed or unreadable request body", request.getRequestURI());
    }

    // -------------------------------------------------------------------------
    // Catch-all
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred", request.getRequestURI());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(
        HttpStatus status, String error, String message, String path
    ) {
        ErrorResponse body = ErrorResponse.builder()
            .status(status.value())
            .error(error)
            .message(message)
            .path(path)
            .timestamp(OffsetDateTime.now())
            .build();
        return ResponseEntity.status(status).body(body);
    }
}
