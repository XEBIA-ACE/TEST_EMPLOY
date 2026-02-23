package com.company.employee.service;

import com.company.employee.dto.request.CreateEmployeeRequest;
import com.company.employee.dto.request.UpdateEmployeeRequest;
import com.company.employee.dto.response.EmployeeResponse;
import com.company.employee.dto.response.PagedResponse;
import com.company.employee.exception.DuplicateEmailException;
import com.company.employee.exception.EmployeeNotFoundException;
import com.company.employee.mapper.EmployeeMapper;
import com.company.employee.model.entity.Employee;
import com.company.employee.model.entity.Employee.EmployeeStatus;
import com.company.employee.repository.EmployeeRepository;
import com.company.employee.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee sampleEmployee;
    private EmployeeResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
            .id(1L)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@company.com")
            .department("Engineering")
            .jobTitle("Software Engineer")
            .salary(new BigDecimal("90000.00"))
            .hireDate(LocalDate.of(2022, 3, 15))
            .status(EmployeeStatus.ACTIVE)
            .build();

        sampleResponse = new EmployeeResponse();
        sampleResponse.setId(1L);
        sampleResponse.setFirstName("Jane");
        sampleResponse.setLastName("Doe");
        sampleResponse.setFullName("Jane Doe");
        sampleResponse.setEmail("jane.doe@company.com");
        sampleResponse.setDepartment("Engineering");
        sampleResponse.setStatus(EmployeeStatus.ACTIVE);
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("should create and return employee when email is unique")
        void createEmployee_success() {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setEmail("jane.doe@company.com");

            when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(employeeMapper.toEntity(request)).thenReturn(sampleEmployee);
            when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.createEmployee(request);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("jane.doe@company.com");
            verify(employeeRepository).save(sampleEmployee);
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email already exists")
        void createEmployee_duplicateEmail() {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setEmail("jane.doe@company.com");

            when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jane.doe@company.com");

            verify(employeeRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        @DisplayName("should return employee when found")
        void getById_found() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.getEmployeeById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when not found")
        void getById_notFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getAllEmployees")
    class GetAllEmployees {

        @Test
        @DisplayName("should return paginated employees")
        void getAllEmployees_returnsPage() {
            Page<Employee> employeePage = new PageImpl<>(List.of(sampleEmployee), PageRequest.of(0, 20), 1);
            when(employeeRepository.findAll(any(PageRequest.class))).thenReturn(employeePage);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            PagedResponse<EmployeeResponse> result = employeeService.getAllEmployees(PageRequest.of(0, 20));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("should update and return employee when valid")
        void updateEmployee_success() {
            UpdateEmployeeRequest request = new UpdateEmployeeRequest();
            request.setJobTitle("Staff Engineer");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.updateEmployee(1L, request);

            assertThat(result).isNotNull();
            verify(employeeMapper).updateEmployee(request, sampleEmployee);
            verify(employeeRepository).save(sampleEmployee);
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email conflicts with another employee")
        void updateEmployee_emailConflict() {
            UpdateEmployeeRequest request = new UpdateEmployeeRequest();
            request.setEmail("other@company.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.existsByEmailAndIdNot("other@company.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(DuplicateEmailException.class);

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when employee does not exist")
        void updateEmployee_notFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(99L, new UpdateEmployeeRequest()))
                .isInstanceOf(EmployeeNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("should delete employee when found")
        void deleteEmployee_success() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

            assertThatCode(() -> employeeService.deleteEmployee(1L)).doesNotThrowAnyException();

            verify(employeeRepository).delete(sampleEmployee);
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when employee does not exist")
        void deleteEmployee_notFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class);

            verify(employeeRepository, never()).delete(any());
        }
    }
}
