package com.challenge.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.CreateEmployeeInput;
import com.challenge.api.model.Employee;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the in-memory service implementation. Each test gets its own service instance, so no state leaks
 * between tests.
 */
class EmployeeServiceImplTest {

    private static final UUID SEEDED_EMPLOYEE_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant HIRE_DATE = Instant.parse("2024-01-02T09:00:00Z");

    private final EmployeeService employeeService = new EmployeeServiceImpl();

    @Test
    void returnsAllSeededEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();

        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFullName).containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void findsSeededEmployeeByUuid() {
        Employee employee = employeeService.getEmployeeByUuid(SEEDED_EMPLOYEE_UUID);

        assertThat(employee.getUuid()).isEqualTo(SEEDED_EMPLOYEE_UUID);
        assertThat(employee.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void throwsWhenEmployeeDoesNotExist() {
        UUID unknownUuid = UUID.randomUUID();

        assertThatThrownBy(() -> employeeService.getEmployeeByUuid(unknownUuid))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining(unknownUuid.toString());
    }

    @Test
    void createsEmployeeWithServerGeneratedUuid() {
        Employee created = createEmployee("Ada", "Lovelace", HIRE_DATE);

        assertThat(created.getUuid()).isNotNull();
        assertThat(created.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(created.getContractHireDate()).isEqualTo(HIRE_DATE);
        assertThat(created.getContractTerminationDate()).isNull();
        assertThat(employeeService.getEmployeeByUuid(created.getUuid())).isSameAs(created);
    }

    @Test
    void usesCurrentTimeWhenHireDateIsOmitted() {
        Instant before = Instant.now();

        Employee created = createEmployee("Grace", "Hopper", null);

        assertThat(created.getContractHireDate()).isBetween(before, Instant.now());
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> employeeService.createEmployee(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void generatesUniqueUuidsForConcurrentCreations() throws Exception {
        int creations = 50;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<UUID>> futures = new ArrayList<>();
            for (int i = 0; i < creations; i++) {
                futures.add(executor.submit(
                        () -> createEmployee("Concurrent", "Employee", null).getUuid()));
            }

            Set<UUID> uuids = new HashSet<>();
            for (Future<UUID> future : futures) {
                assertThat(uuids.add(future.get())).isTrue();
            }

            assertThat(uuids).hasSize(creations);
            assertThat(employeeService.getAllEmployees()).hasSize(creations + 2);
        } finally {
            executor.shutdownNow();
        }
    }

    private Employee createEmployee(String firstName, String lastName, Instant hireDate) {
        return employeeService.createEmployee(new CreateEmployeeInput(
                firstName, lastName, 100000, 36, "Engineer", "engineer@reliaquest.com", hireDate));
    }
}
