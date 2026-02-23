package com.example.employee.service.impl;

import com.example.employee.dto.DepartmentRequest;
import com.example.employee.dto.DepartmentResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.exception.DuplicateResourceException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.mapper.DepartmentMapper;
import com.example.employee.model.Department;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of {@link DepartmentService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DepartmentResponse> getDepartments(Pageable pageable) {
        log.debug("Listing departments, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<DepartmentResponse> page = departmentRepository
                .findAll(pageable)
                .map(departmentMapper::toResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("Fetching department id={}", id);
        return departmentMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        log.info("Creating department name='{}'", request.getName());

        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "A department named '" + request.getName() + "' already exists");
        }

        Department saved = departmentRepository.save(departmentMapper.toEntity(request));
        log.info("Department created: id={}, name='{}'", saved.getId(), saved.getName());
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        log.info("Updating department id={}", id);
        Department department = findOrThrow(id);

        if (request.getName() != null &&
                departmentRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException(
                    "A department named '" + request.getName() + "' already exists");
        }

        departmentMapper.updateEntity(request, department);
        Department saved = departmentRepository.save(department);
        log.info("Department updated: id={}", saved.getId());
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department id={}", id);
        Department department = findOrThrow(id);

        if (!department.getEmployees().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete department id=" + id + " because it still has employees assigned.");
        }

        departmentRepository.delete(department);
        log.info("Department id={} deleted", id);
    }

    // -----------------------------------------------------------------------

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id=" + id));
    }
}
