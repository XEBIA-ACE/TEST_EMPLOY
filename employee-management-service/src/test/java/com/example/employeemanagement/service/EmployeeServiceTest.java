package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.model.Employee.EmploymentStatus;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequestDto validRequest;
    private Employee sampleEmployee;
    private EmployeeResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = EmployeeRequestDto.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .department("Engineering")
            .jobTitle("Software Engineer")
            .employmentStatus(EmploymentStatus.ACTIVE)
            .hireDate(LocalDate.of(2023, 6, 15))
            .salary(new BigDecimal("90000.00"))
            .build();

        sampleEmployee = Employee.builder()
            .id(1L)
            .employeeNumber("EMP-000001")
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .department("Engineering")
            .jobTitle("Software Engineer")
            .employmentStatus(EmploymentStatus.ACTIVE)
            .hireDate(LocalDate.of(2023, 6, 15))
            .salary(new BigDecimal("90000.00"))
            .build();

        sampleResponse = EmployeeResponseDto.builder()
            .id(1L)
            .employeeNumber("EMP-000001")
            .firstName("Jane")
            .lastName("Doe")
            .fullName("Jane Doe")
            .email("jane.doe@example.com")
            .department("Engineering")
            .jobTitle("Software Engineer")
            .employmentStatus(EmploymentStatus.ACTIVE)
            .hireDate(LocalDate.of(2023, 6, 15))
            .salary(new BigDecimal("90000.00"))
            .build();
    }

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("should create employee successfully when email is unique")
        void shouldCreateEmployeeSuccessfully() {
            when(employeeRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(employeeMapper.toEntity(validRequest)).thenReturn(sampleEmployee);
            when(employeeRepository.count()).thenReturn(0L);
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);
            when(employeeMapper.toResponseDto(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponseDto result = employeeService.createEmployee(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
            assertThat(result.getFullName()).isEqualTo("Jane Doe");
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email already exists")
        void shouldThrowWhenEmailExists() {
            when(employeeRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(validRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jane.doe@example.com");

            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        @DisplayName("should return employee when found")
        void shouldReturnEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeMapper.toResponseDto(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponseDto result = employeeService.getEmployeeById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("should update employee when request is valid")
        void shouldUpdateEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.existsByEmailAndIdNot(validRequest.getEmail(), 1L)).thenReturn(false);
            when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
            when(employeeMapper.toResponseDto(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponseDto result = employeeService.updateEmployee(1L, validRequest);

            assertThat(result).isNotNull();
            verify(employeeMapper).updateEntityFromDto(validRequest, sampleEmployee);
        }

        @Test
        @DisplayName("should throw DuplicateEmailException on email conflict")
        void shouldThrowOnEmailConflict() {
            EmployeeRequestDto conflictRequest = EmployeeRequestDto.builder()
                .email("taken@example.com")
                .firstName("Jane").lastName("Doe")
                .department("Engineering").jobTitle("Engineer")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .hireDate(LocalDate.now())
                .build();

            // Make the existing employee have a different email so the conflict check runs
            sampleEmployee.setEmail("jane.doe@example.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(1L, conflictRequest))
                .isInstanceOf(DuplicateEmailException.class);
        }
    }

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("should delete employee when found")
        void shouldDeleteEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

            employeeService.deleteEmployee(1L);

            verify(employeeRepository).delete(sampleEmployee);
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class);

            verify(employeeRepository, never()).delete(any());
        }
    }
}
