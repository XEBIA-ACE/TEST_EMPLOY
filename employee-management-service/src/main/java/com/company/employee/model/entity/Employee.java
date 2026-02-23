package com.company.employee.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity representing an employee.
 *
 * <p>Auditing fields ({@code createdAt} / {@code updatedAt}) are populated
 * automatically by Spring Data's auditing infrastructure.
 */
@Entity
@Table(
    name = "employees",
    indexes = {
        @Index(name = "idx_employees_email", columnList = "email", unique = true),
        @Index(name = "idx_employees_department", columnList = "department"),
        @Index(name = "idx_employees_status", columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
    @SequenceGenerator(name = "employee_seq", sequenceName = "employees_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Lifecycle statuses for an employee. */
    public enum EmployeeStatus {
        ACTIVE,
        INACTIVE,
        ON_LEAVE,
        TERMINATED
    }
}
