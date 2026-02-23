package com.company.employee.exception;

/**
 * Thrown when an employee record cannot be found by the requested identifier.
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }
}
