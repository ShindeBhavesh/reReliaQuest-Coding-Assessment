package com.challenge.api.model;

import java.time.Instant;
import java.util.UUID;


public interface Employee {

    UUID getUuid();

    void setUuid(UUID uuid);

    String getFirstName();

    void setFirstName(String name);

    String getLastName();

    void setLastName(String name);

    String getFullName();

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

    Instant getContractTerminationDate();

    void setContractTerminationDate(Instant date);
}
