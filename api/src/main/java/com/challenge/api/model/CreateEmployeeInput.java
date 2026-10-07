package com.challenge.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Request payload for {@code POST /api/v1/employee}, validated with Jakarta Bean Validation before the controller is
 * invoked.
 *
 * <p>The employee UUID is generated server-side and is therefore not accepted from the client. The contract hire date
 * is optional: when it is omitted the service uses the current time.
 */
public record CreateEmployeeInput(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @PositiveOrZero Integer salary,
        @Min(18) @Max(100) Integer age,
        @Size(max = 100) String jobTitle,
        @Email @Size(max = 254) String email,
        Instant contractHireDate) {}
