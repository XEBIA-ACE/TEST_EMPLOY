package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.dto.PagedResponseDto;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.model.Employee.EmploymentStatus;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    // Simple sequence counter for generating employee numbers.
    // In production, replace with a database sequence or UUID strategy.
    private static final AtomicLong EMP_COUNTER = new AtomicLong(0);

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto request) {
        log.info("Creating employee with email: {}", request.getEmail());

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("An employee with email '%s' already exists".formatted(request.getEmail()));
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setEmployeeNumber(generateEmployeeNumber());

        Employee saved = employeeRepository.save(employee);
        log.info("Created employee id={} employeeNumber={}", saved.getId(), saved.getEmployeeNumber());

        return employeeMapper.toResponseDto(saved);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = findByIdOrThrow(id);
        return employeeMapper.toResponseDto(employee);
    }

    @Override
    public EmployeeResponseDto getEmployeeByEmployeeNumber(String employeeNumber) {
        Employee employee = employeeRepository.findByEmployeeNumber(employeeNumber)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with employee number: " + employeeNumber));
        return employeeMapper.toResponseDto(employee);
    }

    @Override
    public PagedResponseDto<EmployeeResponseDto> getAllEmployees(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);
        return toPagedResponse(page);
    }

    @Override
    public PagedResponseDto<EmployeeResponseDto> getEmployeesByDepartment(String department, Pageable pageable) {
        Page<Employee> page = employeeRepository.findByDepartment(department, pageable);
        return toPagedResponse(page);
    }

    @Override
    public PagedResponseDto<EmployeeResponseDto> getEmployeesByStatus(EmploymentStatus status, Pageable pageable) {
        Page<Employee> page = employeeRepository.findByEmploymentStatus(status, pageable);
        return toPagedResponse(page);
    }

    @Override
    public PagedResponseDto<EmployeeResponseDto> searchEmployees(String query, Pageable pageable) {
        log.debug("Searching employees with query: '{}'", query);
        Page<Employee> page = employeeRepository.searchEmployees(query, pageable);
        return toPagedResponse(page);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto request) {
        log.info("Updating employee id={}", id);

        Employee employee = findByIdOrThrow(id);

        // Check for email conflict only if the email is being changed
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException(
                "An employee with email '%s' already exists".formatted(request.getEmail()));
        }

        employeeMapper.updateEntityFromDto(request, employee);
        Employee updated = employeeRepository.save(employee);

        log.info("Updated employee id={}", updated.getId());
        return employeeMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Deleting employee id={}", id);
        Employee employee = findByIdOrThrow(id);
        employeeRepository.delete(employee);
        log.info("Deleted employee id={}", id);
    }

    // --- Helpers ---

    private Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
    }

    private PagedResponseDto<EmployeeResponseDto> toPagedResponse(Page<Employee> page) {
        return PagedResponseDto.<EmployeeResponseDto>builder()
            .content(page.getContent().stream().map(employeeMapper::toResponseDto).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

    /**
     * Generates a zero-padded employee number, e.g. {@code EMP-000001}.
     * Thread-safe via {@link AtomicLong}; use a DB sequence in production.
     */
    private String generateEmployeeNumber() {
        long count = employeeRepository.count() + EMP_COUNTER.incrementAndGet();
        return "EMP-%06d".formatted(count);
    }
}
