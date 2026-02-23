package com.company.employee.mapper;

import com.company.employee.dto.request.CreateEmployeeRequest;
import com.company.employee.dto.request.UpdateEmployeeRequest;
import com.company.employee.dto.response.EmployeeResponse;
import com.company.employee.model.entity.Employee;
import org.mapstruct.*;

/**
 * MapStruct mapper between the {@link Employee} entity and its DTOs.
 *
 * <p>MapStruct generates the implementation at compile time. Null source
 * properties in {@link #updateEmployee} are ignored so that PATCH-style
 * partial updates work correctly.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    /**
     * Maps a {@link CreateEmployeeRequest} to a new {@link Employee} entity.
     * Audit timestamps and the generated ID are not set here.
     */
    Employee toEntity(CreateEmployeeRequest request);

    /**
     * Maps an {@link Employee} entity to an {@link EmployeeResponse} DTO.
     * The {@code fullName} field is derived from firstName + lastName.
     */
    @Mapping(target = "fullName", expression = "java(employee.getFirstName() + \" \" + employee.getLastName())")
    EmployeeResponse toResponse(Employee employee);

    /**
     * Applies non-null fields from {@link UpdateEmployeeRequest} to an existing entity.
     * Fields present in the request but null are left unchanged on the target.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployee(UpdateEmployeeRequest request, @MappingTarget Employee employee);
}
