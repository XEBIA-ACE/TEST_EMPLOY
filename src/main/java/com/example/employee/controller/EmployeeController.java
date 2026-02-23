package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import com.example.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller exposing all employee endpoints.
 *
 * <p>Base path: {@code /api/v1/employees}</p>
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employees", description = "CRUD and search operations for employee records")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    // -----------------------------------------------------------------------
    // List / Search
    // -----------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "List employees",
            description = "Returns a paginated, optionally filtered list of employees. "
                    + "Combine any number of query parameters to narrow results.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Successfully retrieved employees"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid query parameter",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeResponse>>> getEmployees(
            @Parameter(description = "Filter by department ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filter by employment status") @RequestParam(required = false) EmployeeStatus status,
            @Parameter(description = "Filter by employment type") @RequestParam(required = false) EmploymentType employmentType,
            @Parameter(description = "Minimum salary (inclusive)") @RequestParam(required = false) BigDecimal minSalary,
            @Parameter(description = "Maximum salary (inclusive)") @RequestParam(required = false) BigDecimal maxSalary,
            @Parameter(description = "Free-text search across name, email, and job title") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "lastName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<EmployeeResponse> result = employeeService.getEmployees(
                departmentId, status, employmentType, minSalary, maxSalary, search, pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // -----------------------------------------------------------------------
    // Single resource
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Employee found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {

        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Create a new employee")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Employee created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Email already in use")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        log.info("POST /api/v1/employees - creating employee email={}", request.getEmail());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    // -----------------------------------------------------------------------
    // Update (full replace)
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee (full replace)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Employee updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Employee not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Email already in use by another employee")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        log.info("PUT /api/v1/employees/{}", id);
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    // -----------------------------------------------------------------------
    // Delete (soft)
    // -----------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an employee (marks as TERMINATED)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Employee terminated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {

        log.info("DELETE /api/v1/employees/{}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee terminated successfully", null));
    }

    // -----------------------------------------------------------------------
    // Direct reports
    // -----------------------------------------------------------------------

    @GetMapping("/{id}/direct-reports")
    @Operation(summary = "Get direct reports for a manager")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Direct reports retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Manager not found")
    })
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeResponse>>> getDirectReports(
            @Parameter(description = "Manager employee ID") @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        PagedResponse<EmployeeResponse> result = employeeService.getDirectReports(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
