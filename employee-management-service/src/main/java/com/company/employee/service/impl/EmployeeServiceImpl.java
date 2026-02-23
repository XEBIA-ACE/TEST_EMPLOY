package com.company.employee.service.impl;

import com.company.employee.dto.request.CreateEmployeeRequest;
import com.company.employee.dto.request.UpdateEmployeeRequest;
import com.company.employee.dto.response.EmployeeResponse;
import com.company.employee.dto.response.PagedResponse;
import com.company.employee.exception.DuplicateEmailException;
import com.company.employee.exception.EmployeeNotFoundException;
import com.company.employee.mapper.EmployeeMapper;
import com.company.employee.model.entity.Employee;
import com.company.employee.model.entity.Employee.EmployeeStatus;
import com.company.employee.repository.EmployeeRepository;
import com.company.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link EmployeeService}.
 *
 * <p>All write operations run inside a transaction. Read-only operations use
 * {@code @Transactional(readOnly = true)} to allow Hibernate to apply
 * read-only optimisations and prevent accidental dirty writes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating employee with email: {}", request.getEmail());

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Employee employee = employeeMapper.toEntity(request);

        // Default status to ACTIVE when not explicitly provided
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created with id: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee with id: {}", id);
        Employee employee = findOrThrow(id);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.debug("Fetching all employees, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<EmployeeResponse> page = employeeRepository.findAll(pageable)
            .map(employeeMapper::toResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable) {
        log.debug("Fetching employees in department: {}", department);
        Page<EmployeeResponse> page = employeeRepository
            .findByDepartmentIgnoreCase(department, pageable)
            .map(employeeMapper::toResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status, Pageable pageable) {
        log.debug("Fetching employees with status: {}", status);
        Page<EmployeeResponse> page = employeeRepository
            .findByStatus(status, pageable)
            .map(employeeMapper::toResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> searchEmployees(String query, Pageable pageable) {
        log.debug("Searching employees with query: '{}'", query);
        Page<EmployeeResponse> page = employeeRepository
            .search(query.trim(), pageable)
            .map(employeeMapper::toResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("Updating employee with id: {}", id);
        Employee employee = findOrThrow(id);

        // Guard against email conflicts with other records
        if (request.getEmail() != null &&
            employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException(request.getEmail());
        }

        employeeMapper.updateEmployee(request, employee);
        Employee saved = employeeRepository.save(employee);
        log.info("Employee {} updated successfully", id);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
        Employee employee = findOrThrow(id);
        employeeRepository.delete(employee);
        log.info("Employee {} deleted successfully", id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}
