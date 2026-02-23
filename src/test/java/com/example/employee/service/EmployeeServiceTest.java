package com.example.employee.service;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.exception.DuplicateResourceException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.mapper.EmployeeMapper;
import com.example.employee.model.Employee;
import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmployeeServiceImpl}.
 * All collaborators are mocked; no database interaction.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock EmployeeMapper employeeMapper;

    @InjectMocks EmployeeServiceImpl employeeService;

    private Employee sampleEmployee;
    private EmployeeResponse sampleResponse;
    private EmployeeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .jobTitle("Senior Engineer")
                .salary(new BigDecimal("95000"))
                .hireDate(LocalDate.of(2022, 1, 10))
                .status(EmployeeStatus.ACTIVE)
                .employmentType(EmploymentType.FULL_TIME)
                .build();

        sampleResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .build();

        sampleRequest = EmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .jobTitle("Senior Engineer")
                .salary(new BigDecimal("95000"))
                .hireDate(LocalDate.of(2022, 1, 10))
                .build();
    }

    // -----------------------------------------------------------------------
    // getEmployees
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getEmployees")
    class GetEmployeesTests {

        @Test
        @DisplayName("returns paginated response when employees exist")
        void returnsPagedResponse() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("lastName"));
            Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), pageable, 1);

            when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            PagedResponse<EmployeeResponse> result = employeeService.getEmployees(
                    null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("returns empty page when no employees match filters")
        void returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> emptyPage = Page.empty(pageable);

            when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            PagedResponse<EmployeeResponse> result = employeeService.getEmployees(
                    null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // -----------------------------------------------------------------------
    // getEmployeeById
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getEmployeeById")
    class GetByIdTests {

        @Test
        @DisplayName("returns response when employee found")
        void returnsEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.getEmployeeById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when employee not found")
        void throwsWhenNotFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -----------------------------------------------------------------------
    // createEmployee
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployeeTests {

        @Test
        @DisplayName("creates and returns new employee")
        void createsEmployee() {
            when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(employeeMapper.toEntity(sampleRequest)).thenReturn(sampleEmployee);
            when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.createEmployee(sampleRequest);

            assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
            verify(employeeRepository).save(sampleEmployee);
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email already exists")
        void throwsOnDuplicateEmail() {
            when(employeeRepository.existsByEmailIgnoreCase(sampleRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(sampleRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(sampleRequest.getEmail());

            verify(employeeRepository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // updateEmployee
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("updates and returns modified employee")
        void updatesEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(false);
            when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.updateEmployee(1L, sampleRequest);

            assertThat(result).isNotNull();
            verify(employeeMapper).updateEntity(sampleRequest, sampleEmployee);
        }

        @Test
        @DisplayName("throws IllegalArgumentException if employee sets self as manager")
        void throwsOnSelfManager() {
            sampleRequest.setManagerId(1L);  // same as the employee's own id
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(false);

            assertThatThrownBy(() -> employeeService.updateEmployee(1L, sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // -----------------------------------------------------------------------
    // deleteEmployee (soft delete)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("soft-deletes employee by setting status to TERMINATED")
        void softDeletesEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any())).thenReturn(sampleEmployee);

            employeeService.deleteEmployee(1L);

            assertThat(sampleEmployee.getStatus()).isEqualTo(EmployeeStatus.TERMINATED);
            verify(employeeRepository).save(sampleEmployee);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when employee not found")
        void throwsWhenNotFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
