package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeInput;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final Map<UUID, Employee> employeeMap = new ConcurrentHashMap<>();

    public EmployeeServiceImpl() {
        initMockData();
    }

    private void initMockData() {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Employee emp1 = new EmployeeModel(
                id1, "John", "Doe", 95000, 30, "Software Engineer", "john.doe@reliaquest.com", Instant.now(), null);
        employeeMap.put(id1, emp1);

        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Employee emp2 = new EmployeeModel(
                id2,
                "Jane",
                "Smith",
                120000,
                34,
                "Senior Security Architect",
                "jane.smith@reliaquest.com",
                Instant.now(),
                null);
        employeeMap.put(id2, emp2);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeMap.values());
    }

    @Override
    public Employee getEmployeeByUuid(UUID uuid) {
        return employeeMap.get(uuid);
    }

    @Override
    public Employee createEmployee(CreateEmployeeInput input) {
        if (input == null) {
            throw new IllegalArgumentException("CreateEmployeeInput request body cannot be null");
        }

        UUID uuid = UUID.randomUUID();
        Instant hireDate = input.getContractHireDate() != null ? input.getContractHireDate() : Instant.now();

        Employee employee = new EmployeeModel(
                uuid,
                input.getFirstName(),
                input.getLastName(),
                input.getSalary(),
                input.getAge(),
                input.getJobTitle(),
                input.getEmail(),
                hireDate,
                null);

        employeeMap.put(uuid, employee);
        return employee;
    }
}
