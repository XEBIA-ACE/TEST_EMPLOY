package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.dto.PagedResponseDto;
import com.example.employeemanagement.model.Employee.EmploymentStatus;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponseDto createEmployee(EmployeeRequestDto request);

    EmployeeResponseDto getEmployeeById(Long id);

    EmployeeResponseDto getEmployeeByEmployeeNumber(String employeeNumber);

    PagedResponseDto<EmployeeResponseDto> getAllEmployees(Pageable pageable);

    PagedResponseDto<EmployeeResponseDto> getEmployeesByDepartment(String department, Pageable pageable);

    PagedResponseDto<EmployeeResponseDto> getEmployeesByStatus(EmploymentStatus status, Pageable pageable);

    PagedResponseDto<EmployeeResponseDto> searchEmployees(String query, Pageable pageable);

    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto request);

    void deleteEmployee(Long id);
}
