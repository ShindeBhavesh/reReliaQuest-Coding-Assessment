package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeInput;
import com.challenge.api.model.Employee;
import java.util.List;
import java.util.UUID;

/**
 * Business operations for employee management.
 *
 * <p>Implementations own the employee store; controllers only translate HTTP requests and responses.
 */
public interface EmployeeService {

    /**
     * @return every employee currently held by the service, unfiltered
     */
    List<Employee> getAllEmployees();

    /**
     * @return the employee for the given UUID
     * @throws com.challenge.api.exception.EmployeeNotFoundException if no employee exists for the given UUID
     */
    Employee getEmployeeByUuid(UUID uuid);

    /**
     * Creates an employee with a server-generated UUID. The contract termination date is always {@code null} because a
     * newly created employee has not been terminated yet.
     *
     * @return the created employee
     */
    Employee createEmployee(CreateEmployeeInput input);
}
