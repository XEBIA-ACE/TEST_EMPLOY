package com.example.employee.service;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Business logic contract for employee operations.
 * The interface decouples the controller from the concrete implementation,
 * making it straightforward to swap or mock in tests.
 */
public interface EmployeeService {

    /**
     * Returns a paginated, optionally filtered list of employees.
     *
     * @param departmentId   optional department filter
     * @param status         optional status filter
     * @param employmentType optional employment-type filter
     * @param minSalary      optional lower salary bound
     * @param maxSalary      optional upper salary bound
     * @param search         optional free-text search
     * @param pageable       pagination and sort parameters
     */
    PagedResponse<EmployeeResponse> getEmployees(
            Long departmentId,
            EmployeeStatus status,
            EmploymentType employmentType,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String search,
            Pageable pageable);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id);

    /** Returns all direct reports of the given manager, paginated. */
    PagedResponse<EmployeeResponse> getDirectReports(Long managerId, Pageable pageable);
}
