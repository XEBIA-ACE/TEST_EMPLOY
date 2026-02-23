package com.example.employee.repository;

import com.example.employee.model.Department;
import com.example.employee.model.Employee;
import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA slice test for {@link EmployeeRepository}.
 * Uses an in-memory H2 database (configured in application-test.yml).
 */
@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired EmployeeRepository employeeRepository;
    @Autowired DepartmentRepository departmentRepository;

    private Department engineering;
    private Employee alice;
    private Employee bob;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        engineering = departmentRepository.save(Department.builder()
                .name("Engineering")
                .budget(1_000_000.0)
                .build());

        alice = employeeRepository.save(Employee.builder()
                .firstName("Alice").lastName("Smith")
                .email("alice@example.com")
                .jobTitle("VP Engineering")
                .salary(new BigDecimal("145000"))
                .hireDate(LocalDate.of(2018, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .employmentType(EmploymentType.FULL_TIME)
                .department(engineering)
                .build());

        bob = employeeRepository.save(Employee.builder()
                .firstName("Bob").lastName("Jones")
                .email("bob@example.com")
                .jobTitle("Software Engineer")
                .salary(new BigDecimal("95000"))
                .hireDate(LocalDate.of(2021, 6, 1))
                .status(EmployeeStatus.ACTIVE)
                .employmentType(EmploymentType.FULL_TIME)
                .department(engineering)
                .manager(alice)
                .build());
    }

    @Test
    @DisplayName("findByEmailIgnoreCase returns employee for matching email")
    void findByEmail() {
        assertThat(employeeRepository.findByEmailIgnoreCase("ALICE@EXAMPLE.COM"))
                .isPresent()
                .get()
                .extracting(Employee::getFirstName)
                .isEqualTo("Alice");
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase returns true for existing email")
    void existsByEmail() {
        assertThat(employeeRepository.existsByEmailIgnoreCase("bob@example.com")).isTrue();
        assertThat(employeeRepository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }

    @Test
    @DisplayName("findByDepartmentId returns only employees in that department")
    void findByDepartmentId() {
        Page<Employee> page = employeeRepository.findByDepartmentId(
                engineering.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByManagerId returns direct reports")
    void findByManagerId() {
        Page<Employee> reports = employeeRepository.findByManagerId(
                alice.getId(), PageRequest.of(0, 10));

        assertThat(reports.getTotalElements()).isEqualTo(1);
        assertThat(reports.getContent().get(0).getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    @DisplayName("searchEmployees matches on first name, last name, and email")
    void searchEmployees() {
        Page<Employee> nameResult = employeeRepository.searchEmployees("alice", PageRequest.of(0, 10));
        assertThat(nameResult.getTotalElements()).isEqualTo(1);

        Page<Employee> emailResult = employeeRepository.searchEmployees("example.com", PageRequest.of(0, 10));
        assertThat(emailResult.getTotalElements()).isEqualTo(2);

        Page<Employee> noResult = employeeRepository.searchEmployees("zzznomatch", PageRequest.of(0, 10));
        assertThat(noResult.getTotalElements()).isZero();
    }
}
