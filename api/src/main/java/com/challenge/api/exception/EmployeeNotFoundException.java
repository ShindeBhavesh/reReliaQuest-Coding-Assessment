package com.challenge.api.exception;

import java.util.UUID;

/**
 * Thrown when an employee cannot be found for a given UUID. Mapped to {@code 404 Not Found} by
 * {@link ApiExceptionHandler}.
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(UUID uuid) {
        super("No employee found for UUID " + uuid);
    }
}
