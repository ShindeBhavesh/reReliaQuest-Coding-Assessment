package com.challenge.api.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain contract for an employee exposed by the REST API.
 *
 * <p>Implementations must keep {@link #getFullName()} consistent with the first and last name: the full name is a
 * derived value and must never be stored as an independently mutable copy that can go stale. The
 * {@code contractTerminationDate} is {@code null} while an employee is still contracted.
 */
public interface Employee {

    UUID getUuid();

    void setUuid(UUID uuid);

    String getFirstName();

    void setFirstName(String name);

    String getLastName();

    void setLastName(String name);

    /**
     * @return the full name derived from the first and last name, or {@code null} when neither name is set
     */
    String getFullName();

    /**
     * Kept for contract completeness. The full name is always derived from the first and last name, therefore the
     * supplied value is ignored.
     */
    void setFullName(String name);

    Integer getSalary();

    void setSalary(Integer salary);

    Integer getAge();

    void setAge(Integer age);

    String getJobTitle();

    void setJobTitle(String jobTitle);

    String getEmail();

    void setEmail(String email);

    Instant getContractHireDate();

    void setContractHireDate(Instant date);

    /**
     * @return the contract termination date, or {@code null} for an employee who has not been terminated
     */
    Instant getContractTerminationDate();

    void setContractTerminationDate(Instant date);
}
