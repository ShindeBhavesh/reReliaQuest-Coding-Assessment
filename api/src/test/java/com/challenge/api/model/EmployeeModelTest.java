package com.challenge.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the employee domain model, with focus on the derived full name.
 */
class EmployeeModelTest {

    @Test
    void derivesFullNameFromFirstAndLastName() {
        EmployeeModel employee = new EmployeeModel();
        employee.setFirstName("John");
        employee.setLastName("Doe");

        assertThat(employee.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void keepsFullNameConsistentAfterRenaming() {
        EmployeeModel employee = new EmployeeModel(
                UUID.randomUUID(),
                "John",
                "Doe",
                95000,
                30,
                "Software Engineer",
                "john.doe@reliaquest.com",
                Instant.parse("2021-03-15T09:00:00Z"),
                null);

        employee.setFirstName("Jonathan");
        employee.setLastName("Doe-Smith");

        assertThat(employee.getFullName()).isEqualTo("Jonathan Doe-Smith");
    }

    @Test
    void usesTheAvailableNameWhenOnlyOneIsSet() {
        EmployeeModel employee = new EmployeeModel();
        employee.setFirstName("Cher");

        assertThat(employee.getFullName()).isEqualTo("Cher");

        employee.setFirstName(null);
        employee.setLastName("Prince");

        assertThat(employee.getFullName()).isEqualTo("Prince");
    }

    @Test
    void returnsNullFullNameWhenNoNameIsSet() {
        assertThat(new EmployeeModel().getFullName()).isNull();
    }

    @Test
    void ignoresExplicitFullNameBecauseItIsDerived() {
        EmployeeModel employee = new EmployeeModel();
        employee.setFirstName("John");
        employee.setLastName("Doe");

        employee.setFullName("Someone Else");

        assertThat(employee.getFullName()).isEqualTo("John Doe");
    }
}
