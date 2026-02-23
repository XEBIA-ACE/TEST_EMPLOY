package com.example.employee.service;

import com.example.employee.dto.DepartmentRequest;
import com.example.employee.dto.DepartmentResponse;
import com.example.employee.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

/**
 * Business logic contract for department operations.
 */
public interface DepartmentService {

    PagedResponse<DepartmentResponse> getDepartments(Pageable pageable);

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    void deleteDepartment(Long id);
}
