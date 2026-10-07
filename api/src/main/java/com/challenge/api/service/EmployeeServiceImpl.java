package com.challenge.api.service;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.CreateEmployeeInput;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeModel;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * In-memory {@link EmployeeService} implementation.
 *
 * <p>The assessment explicitly does not require a persistence layer, so employees are held in a
 * {@link ConcurrentHashMap} keyed by UUID: lookups are constant time, and the map is safe for concurrent web requests.
 * Sample employees are seeded on start-up so the API is usable immediately.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final UUID JOHN_DOE_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID JANE_SMITH_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public EmployeeServiceImpl() {
        seedSampleEmployees();
    }

    @Override
    public List<Employee> getAllEmployees() {
        return List.copyOf(employees.values());
    }

    @Override
    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employees.get(uuid);
        if (employee == null) {
            throw new EmployeeNotFoundException(uuid);
        }
        return employee;
    }

    @Override
    public Employee createEmployee(CreateEmployeeInput input) {
        Objects.requireNonNull(input, "input must not be null");

        UUID uuid = UUID.randomUUID();
        Instant hireDate = input.contractHireDate() == null ? Instant.now() : input.contractHireDate();

        Employee employee = new EmployeeModel(
                uuid,
                input.firstName(),
                input.lastName(),
                input.salary(),
                input.age(),
                input.jobTitle(),
                input.email(),
                hireDate,
                null);

        employees.put(uuid, employee);
        return employee;
    }

    private void seedSampleEmployees() {
        employees.put(
                JOHN_DOE_UUID,
                new EmployeeModel(
                        JOHN_DOE_UUID,
                        "John",
                        "Doe",
                        95000,
                        30,
                        "Software Engineer",
                        "john.doe@reliaquest.com",
                        Instant.parse("2021-03-15T09:00:00Z"),
                        null));
        employees.put(
                JANE_SMITH_UUID,
                new EmployeeModel(
                        JANE_SMITH_UUID,
                        "Jane",
                        "Smith",
                        120000,
                        34,
                        "Senior Security Architect",
                        "jane.smith@reliaquest.com",
                        Instant.parse("2019-07-01T09:00:00Z"),
                        null));
    }
}
