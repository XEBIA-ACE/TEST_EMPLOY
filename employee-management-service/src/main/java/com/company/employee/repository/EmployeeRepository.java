package com.company.employee.repository;

import com.company.employee.model.entity.Employee;
import com.company.employee.model.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Employee} entities.
 *
 * <p>Extends {@link JpaSpecificationExecutor} to support dynamic filtering
 * via Specifications when more complex queries are needed.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    /** Find an employee by exact email (case-sensitive). */
    Optional<Employee> findByEmail(String email);

    /** Check whether an email is already registered (used for uniqueness validation). */
    boolean existsByEmail(String email);

    /**
     * Check whether another employee with a different ID uses the same email.
     * Used during updates to prevent duplicate email conflicts.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /** Paginated list of employees filtered by department. */
    Page<Employee> findByDepartmentIgnoreCase(String department, Pageable pageable);

    /** Paginated list of employees filtered by status. */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * Full-text style search across first name, last name, email, department, and job title.
     * The {@code LOWER} calls make the search case-insensitive without a dedicated text index.
     */
    @Query("""
        SELECT e FROM Employee e
        WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.email)     LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.department) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.jobTitle)  LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    Page<Employee> search(@Param("query") String query, Pageable pageable);
}
