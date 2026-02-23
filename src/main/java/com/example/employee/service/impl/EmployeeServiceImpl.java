package com.example.employee.service.impl;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.exception.DuplicateResourceException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.mapper.EmployeeMapper;
import com.example.employee.model.Department;
import com.example.employee.model.Employee;
import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.repository.EmployeeSpecification;
import com.example.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Concrete implementation of {@link EmployeeService}.
 *
 * <p>All public mutating methods are transactional. Read-only methods use
 * {@code readOnly = true} for a small performance benefit and to prevent
 * accidental writes.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> getEmployees(
            Long departmentId,
            EmployeeStatus status,
            EmploymentType employmentType,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String search,
            Pageable pageable) {

        log.debug("Listing employees: dept={}, status={}, type={}, salary={}-{}, search='{}'",
                departmentId, status, employmentType, minSalary, maxSalary, search);

        Specification<Employee> spec = EmployeeSpecification.withFilters(
                departmentId, status, employmentType, minSalary, maxSalary, search);

        Page<EmployeeResponse> page = employeeRepository
                .findAll(spec, pageable)
                .map(employeeMapper::toResponse);

        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee id={}", id);
        Employee employee = findEmployeeOrThrow(id);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating employee with email={}", request.getEmail());

        // Uniqueness guard: email must not already be in use
        if (employeeRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An employee with email '" + request.getEmail() + "' already exists");
        }

        Employee employee = employeeMapper.toEntity(request);

        // Resolve optional FK references
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id=" + request.getDepartmentId()));
            employee.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            Employee manager = findEmployeeOrThrow(request.getManagerId());
            employee.setManager(manager);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created: id={}, email={}", saved.getId(), saved.getEmail());
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Updating employee id={}", id);

        Employee employee = findEmployeeOrThrow(id);

        // Uniqueness guard: new email must not collide with another employee
        if (request.getEmail() != null &&
                employeeRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already in use by another employee");
        }

        // Apply non-null fields from the request onto the managed entity
        employeeMapper.updateEntity(request, employee);

        // Re-resolve FK references if they changed
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id=" + request.getDepartmentId()));
            employee.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(id)) {
                throw new IllegalArgumentException("An employee cannot be their own manager");
            }
            Employee manager = findEmployeeOrThrow(request.getManagerId());
            employee.setManager(manager);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee updated: id={}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Deleting employee id={}", id);
        Employee employee = findEmployeeOrThrow(id);

        // Soft-delete: mark TERMINATED instead of physically removing the row.
        // This preserves referential integrity for audit trails.
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        log.info("Employee id={} marked as TERMINATED", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> getDirectReports(Long managerId, Pageable pageable) {
        log.debug("Fetching direct reports for manager id={}", managerId);
        // Validate that the manager exists
        findEmployeeOrThrow(managerId);

        Page<EmployeeResponse> page = employeeRepository
                .findByManagerId(managerId, pageable)
                .map(employeeMapper::toResponse);

        return PagedResponse.of(page);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id=" + id));
    }
}
