package com.example.employee.repository;

import com.example.employee.model.Employee;
import com.example.employee.model.EmployeeStatus;
import com.example.employee.model.EmploymentType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for building JPA {@link Specification}s used in dynamic
 * employee filtering. Each filter criterion is optional; only non-null
 * values contribute predicates to the final query.
 */
public final class EmployeeSpecification {

    private EmployeeSpecification() { /* utility class */ }

    /**
     * Builds a composite specification from optional filter parameters.
     *
     * @param departmentId  filter by department (optional)
     * @param status        filter by employment status (optional)
     * @param employmentType filter by employment type (optional)
     * @param minSalary     lower salary bound (optional)
     * @param maxSalary     upper salary bound (optional)
     * @param search        free-text search across name / email / title (optional)
     */
    public static Specification<Employee> withFilters(
            Long departmentId,
            EmployeeStatus status,
            EmploymentType employmentType,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String search) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("jobTitle")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
