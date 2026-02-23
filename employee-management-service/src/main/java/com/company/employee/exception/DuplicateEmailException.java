package com.company.employee.exception;

/**
 * Thrown when an attempt is made to use an email address that is already
 * registered to another employee.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("An employee with email '" + email + "' already exists");
    }
}
