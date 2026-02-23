package com.company.employee.service;

import com.company.employee.dto.request.CreateEmployeeRequest;
import com.company.employee.dto.request.UpdateEmployeeRequest;
import com.company.employee.dto.response.EmployeeResponse;
import com.company.employee.dto.response.PagedResponse;
import com.company.employee.model.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for employee business operations.
 *
 * <p>All methods throw {@link com.company.employee.exception.EmployeeNotFoundException}
 * when the requested employee does not exist.
 */
public interface EmployeeService {

    /**
     * Create a new employee record.
     *
     * @param request validated creation payload
     * @return the persisted employee
     * @throws com.company.employee.exception.DuplicateEmailException if email already in use
     */
    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    /**
     * Retrieve an employee by their unique identifier.
     *
     * @param id employee ID
     * @return the employee response DTO
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Return a paginated list of all employees.
     *
     * @param pageable pagination and sort parameters
     * @return page of employee responses
     */
    PagedResponse<EmployeeResponse> getAllEmployees(Pageable pageable);

    /**
     * Return a paginated list of employees in the given department.
     *
     * @param department department name (case-insensitive)
     * @param pageable   pagination and sort parameters
     * @return page of employee responses
     */
    PagedResponse<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable);

    /**
     * Return a paginated list of employees with the given status.
     *
     * @param status   employment status
     * @param pageable pagination and sort parameters
     * @return page of employee responses
     */
    PagedResponse<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * Full-text search across employee fields.
     *
     * @param query    search term
     * @param pageable pagination and sort parameters
     * @return page of matching employee responses
     */
    PagedResponse<EmployeeResponse> searchEmployees(String query, Pageable pageable);

    /**
     * Apply a partial update to an existing employee.
     *
     * @param id      employee ID
     * @param request update payload (null fields are ignored)
     * @return the updated employee response DTO
     * @throws com.company.employee.exception.DuplicateEmailException if updated email already in use
     */
    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    /**
     * Permanently remove an employee record.
     *
     * @param id employee ID
     */
    void deleteEmployee(Long id);
}
