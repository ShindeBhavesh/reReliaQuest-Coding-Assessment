package com.challenge.api.controller;

import com.challenge.api.model.CreateEmployeeInput;
import com.challenge.api.model.Employee;
import com.challenge.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/employee")
@Tag(name = "Employee Controller", description = "Protected REST API endpoints for employee management")
@SecurityRequirement(name = "apiKey")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @GetMapping
    @Operation(
            summary = "Get all employees",
            description = "Returns an unfiltered list of all employees in the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of employees"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }


    @GetMapping("/{uuid}")
    @Operation(
            summary = "Get employee by UUID",
            description = "Returns a single employee record for the provided UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found and returned"),
        @ApiResponse(responseCode = "404", description = "Employee not found for given UUID"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Employee getEmployeeByUuid(
            @Parameter(description = "UUID of the employee to retrieve", required = true) @PathVariable UUID uuid) {
        Employee employee = employeeService.getEmployeeByUuid(uuid);
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found for UUID: " + uuid);
        }
        return employee;
    }


    @PostMapping
    @Operation(
            summary = "Create new employee",
            description = "Creates a new employee record and assigns a unique UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload (missing required attributes)"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Employee createEmployee(
            @Parameter(description = "Employee attributes to create", required = true) @RequestBody
                    CreateEmployeeInput requestBody) {
        if (requestBody == null
                || requestBody.getFirstName() == null
                || requestBody.getFirstName().isBlank()
                || requestBody.getLastName() == null
                || requestBody.getLastName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name and last name are required");
        }
        try {
            return employeeService.createEmployee(requestBody);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
