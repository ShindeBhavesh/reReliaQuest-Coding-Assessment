package com.challenge.api.controller;

import com.challenge.api.config.OpenApiConfig;
import com.challenge.api.model.CreateEmployeeInput;
import com.challenge.api.model.Employee;
import com.challenge.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints that expose employee information to Employees-R-US webhooks.
 *
 * <p>The controller is deliberately thin: it maps HTTP requests onto {@link EmployeeService} calls and leaves error
 * translation to {@code ApiExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/employee")
@Tag(name = "Employees", description = "Protected employee endpoints consumed by Employees-R-US webhooks")
@SecurityRequirement(name = OpenApiConfig.API_KEY_SCHEME)
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Returns all employees, unfiltered.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employees returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get employee by UUID", description = "Returns the employee that matches the given UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee returned"),
        @ApiResponse(responseCode = "400", description = "Path variable is not a valid UUID"),
        @ApiResponse(responseCode = "404", description = "No employee exists for the given UUID"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Employee getEmployeeByUuid(
            @Parameter(description = "UUID of the employee", example = "11111111-1111-1111-1111-111111111111")
                    @PathVariable
                    UUID uuid) {
        return employeeService.getEmployeeByUuid(uuid);
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Creates an employee and assigns a server-generated UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee created"),
        @ApiResponse(responseCode = "400", description = "Request body is missing or invalid"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Employee createEmployee(
            @Parameter(description = "Attributes of the employee to create", required = true) @Valid @RequestBody
                    CreateEmployeeInput input) {
        return employeeService.createEmployee(input);
    }
}
