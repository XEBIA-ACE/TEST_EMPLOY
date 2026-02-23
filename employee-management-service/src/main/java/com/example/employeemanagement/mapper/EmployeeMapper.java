package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.model.Employee;
import org.mapstruct.*;

/**
 * MapStruct mapper between the {@link Employee} domain entity and its DTOs.
 *
 * <p>The {@code componentModel = "spring"} setting causes MapStruct to generate
 * a Spring bean, which is injected wherever {@code EmployeeMapper} is declared.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmployeeMapper {

    /**
     * Maps an {@link EmployeeRequestDto} to a new {@link Employee} entity.
     * Fields managed by the service layer (employeeNumber, id) are deliberately excluded.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Employee toEntity(EmployeeRequestDto dto);

    /**
     * Maps an {@link Employee} entity to a response DTO, adding a computed {@code fullName}.
     */
    @Mapping(target = "fullName", expression = "java(employee.getFirstName() + \" \" + employee.getLastName())")
    EmployeeResponseDto toResponseDto(Employee employee);

    /**
     * Applies non-null fields from a request DTO onto an existing entity (for PATCH-style updates).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromDto(EmployeeRequestDto dto, @MappingTarget Employee employee);
}
