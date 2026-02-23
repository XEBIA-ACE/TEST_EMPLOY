package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.dto.PagedResponseDto;
import com.example.employeemanagement.model.Employee.EmploymentStatus;
import com.example.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employees", description = "CRUD operations for employee records")
public class EmployeeController {

    private final EmployeeService employeeService;

    // ------------------------------------------------------------------ CREATE

    @PostMapping
    @Operation(summary = "Create a new employee",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Employee created"),
                   @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                   @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
               })
    public ResponseEntity<EmployeeResponseDto> createEmployee(
            @Valid @RequestBody EmployeeRequestDto request) {

        log.info("POST /api/v1/employees — email={}", request.getEmail());
        EmployeeResponseDto created = employeeService.createEmployee(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    // ------------------------------------------------------------------- READ

    @GetMapping
    @Operation(summary = "List all employees (paginated)")
    public ResponseEntity<PagedResponseDto<EmployeeResponseDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by ID")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(
            @Parameter(description = "Employee database ID") @PathVariable Long id) {

        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/number/{employeeNumber}")
    @Operation(summary = "Get an employee by employee number")
    public ResponseEntity<EmployeeResponseDto> getEmployeeByNumber(
            @Parameter(description = "Employee number, e.g. EMP-000001")
            @PathVariable String employeeNumber) {

        return ResponseEntity.ok(employeeService.getEmployeeByEmployeeNumber(employeeNumber));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "List employees in a department (paginated)")
    public ResponseEntity<PagedResponseDto<EmployeeResponseDto>> getByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List employees by employment status (paginated)")
    public ResponseEntity<PagedResponseDto<EmployeeResponseDto>> getByStatus(
            @PathVariable EmploymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search across name, email, department, and job title")
    public ResponseEntity<PagedResponseDto<EmployeeResponseDto>> searchEmployees(
            @Parameter(description = "Search term") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeService.searchEmployees(query, pageable));
    }

    // ------------------------------------------------------------------ UPDATE

    @PutMapping("/{id}")
    @Operation(summary = "Fully update an employee",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Employee updated"),
                   @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content),
                   @ApiResponse(responseCode = "409", description = "Email conflict", content = @Content)
               })
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto request) {

        log.info("PUT /api/v1/employees/{}", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // ------------------------------------------------------------------ DELETE

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employee",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Employee deleted"),
                   @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
               })
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("DELETE /api/v1/employees/{}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------- Helpers

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
