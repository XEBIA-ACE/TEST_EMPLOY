package com.example.employee.mapper;

import com.example.employee.dto.DepartmentRequest;
import com.example.employee.dto.DepartmentResponse;
import com.example.employee.model.Department;
import com.example.employee.model.EmployeeStatus;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link Department} entity ↔ DTOs.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DepartmentMapper {

    /**
     * Maps entity → response DTO.
     * Employee count is computed from the collection filtered by ACTIVE status.
     */
    @Mapping(target = "employeeCount", expression = "java(countActiveEmployees(department))")
    DepartmentResponse toResponse(Department department);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Department toEntity(DepartmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DepartmentRequest request, @MappingTarget Department department);

    /** Counts only ACTIVE employees to give a meaningful headcount. */
    default int countActiveEmployees(Department department) {
        if (department.getEmployees() == null) {
            return 0;
        }
        return (int) department.getEmployees().stream()
                .filter(e -> EmployeeStatus.ACTIVE.equals(e.getStatus()))
                .count();
    }
}
