package com.example.employee.repository;

import com.example.employee.model.Employee;
import com.example.employee.model.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Employee}.
 *
 * <p>Extends {@link JpaSpecificationExecutor} to support dynamic filtering
 * (department, status, salary range, free-text search) via the Criteria API.</p>
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    /** Looks up an employee by unique email (case-insensitive). */
    Optional<Employee> findByEmailIgnoreCase(String email);

    /** Checks existence without loading the full entity. */
    boolean existsByEmailIgnoreCase(String email);

    /** Checks uniqueness while excluding the current employee (used on updates). */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /** All employees belonging to a given department, paginated. */
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    /** All direct reports of a given manager, paginated. */
    Page<Employee> findByManagerId(Long managerId, Pageable pageable);

    /** Employees filtered by status, paginated. */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * Free-text search across first name, last name, email, and job title.
     * LOWER() calls make the comparison case-insensitive.
     */
    @Query("""
            SELECT e FROM Employee e
            WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(e.email)     LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(e.jobTitle)  LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);

    /**
     * Aggregate: average salary per department.
     * Returns an Object array [departmentName, avgSalary].
     */
    @Query("""
            SELECT d.name, AVG(e.salary)
            FROM Employee e JOIN e.department d
            WHERE e.status = 'ACTIVE'
            GROUP BY d.name
            ORDER BY AVG(e.salary) DESC
            """)
    java.util.List<Object[]> findAverageSalaryByDepartment();

    /** Count employees within a salary band. */
    long countBySalaryBetween(BigDecimal minSalary, BigDecimal maxSalary);
}
