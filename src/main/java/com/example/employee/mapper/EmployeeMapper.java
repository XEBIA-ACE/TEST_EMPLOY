package com.example.employee.mapper;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.model.Employee;
import org.mapstruct.*;

/**
 * MapStruct mapper that converts between the {@link Employee} JPA entity
 * and its API-facing DTOs.
 *
 * <p>MapStruct generates the implementation at compile time, so there is
 * no reflection overhead at runtime.</p>
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmployeeMapper {

    /**
     * Maps an entity to the full response DTO.
     * Nested department and manager summaries are assembled here.
     */
    @Mapping(target = "fullName", expression = "java(employee.getFullName())")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "manager", source = "manager")
    EmployeeResponse toResponse(Employee employee);

    /** Maps the inbound request to a new entity (id and audit fields left unmapped). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)   // resolved by service
    @Mapping(target = "manager", ignore = true)       // resolved by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Employee toEntity(EmployeeRequest request);

    /**
     * Applies non-null fields from the request onto an existing entity instance.
     * Used for partial (PATCH) as well as full (PUT) updates.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(EmployeeRequest request, @MappingTarget Employee employee);

    /** Converts the Department entity reference to a condensed summary. */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "location", source = "location")
    EmployeeResponse.DepartmentSummary toDepartmentSummary(
            com.example.employee.model.Department department);

    /** Converts the manager entity reference to a condensed summary. */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", expression = "java(manager.getFullName())")
    @Mapping(target = "jobTitle", source = "jobTitle")
    @Mapping(target = "email", source = "email")
    EmployeeResponse.ManagerSummary toManagerSummary(Employee manager);
}
