package com.company.employee.controller;

import com.company.employee.dto.request.CreateEmployeeRequest;
import com.company.employee.dto.request.UpdateEmployeeRequest;
import com.company.employee.dto.response.EmployeeResponse;
import com.company.employee.dto.response.PagedResponse;
import com.company.employee.model.entity.Employee.EmployeeStatus;
import com.company.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

/**
 * REST controller exposing CRUD operations for employee resources.
 *
 * <p>Base path: {@code /api/v1/employees}
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "CRUD operations for employee records")
public class EmployeeController {

    private final EmployeeService employeeService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new employee")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Employee created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<EmployeeResponse> createEmployee(
        @Valid @RequestBody CreateEmployeeRequest request
    ) {
        log.debug("POST /api/v1/employees - email={}", request.getEmail());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponse> getEmployee(
        @Parameter(description = "Employee ID", example = "1")
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping
    @Operation(summary = "List all employees (paginated)")
    @ApiResponse(responseCode = "200", description = "Paginated list of employees")
    public ResponseEntity<PagedResponse<EmployeeResponse>> getAllEmployees(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Items per page", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field", example = "lastName")
        @RequestParam(defaultValue = "lastName") String sortBy,
        @Parameter(description = "Sort direction", example = "asc")
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "List employees by department")
    public ResponseEntity<PagedResponse<EmployeeResponse>> getByDepartment(
        @Parameter(description = "Department name", example = "Engineering")
        @PathVariable String department,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "lastName") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List employees by employment status")
    public ResponseEntity<PagedResponse<EmployeeResponse>> getByStatus(
        @Parameter(description = "Employment status", example = "ACTIVE")
        @PathVariable EmployeeStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "lastName") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees by keyword (name, email, department, title)")
    public ResponseEntity<PagedResponse<EmployeeResponse>> searchEmployees(
        @Parameter(description = "Search term", example = "engineer")
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeService.searchEmployees(query, pageable));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee (partial update — null fields ignored)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Email already in use by another employee")
    })
    public ResponseEntity<EmployeeResponse> updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        log.debug("PUT /api/v1/employees/{}", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employee by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deleted"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.debug("DELETE /api/v1/employees/{}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
