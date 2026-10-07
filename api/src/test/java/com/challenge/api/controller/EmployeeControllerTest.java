package com.challenge.api.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.model.CreateEmployeeInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end tests for the employee endpoints, exercising the real filter chain (API key), validation and error
 * handling. Each test either asserts against the seeded employees or against the employee it created itself, so no
 * test depends on the state left behind by another one.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String WRONG_API_KEY = "not-the-api-key";
    private static final UUID SEEDED_EMPLOYEE_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String EMPLOYEE_URL = "/api/v1/employee";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${employee.security.api-key}")
    private String apiKey;

    @Test
    void getAllEmployeesReturnsEveryEmployeeUnfiltered() throws Exception {
        mockMvc.perform(get(EMPLOYEE_URL).header(API_KEY_HEADER, apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].uuid", hasItem(SEEDED_EMPLOYEE_UUID.toString())))
                .andExpect(jsonPath("$[*].firstName", hasItem("John")))
                .andExpect(jsonPath("$[*].lastName", hasItem("Doe")))
                .andExpect(jsonPath("$[*].fullName", hasItem("John Doe")));
    }

    @Test
    void getAllEmployeesIsRejectedWithoutApiKey() throws Exception {
        mockMvc.perform(get(EMPLOYEE_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void getAllEmployeesIsRejectedWithWrongApiKey() throws Exception {
        mockMvc.perform(get(EMPLOYEE_URL).header(API_KEY_HEADER, WRONG_API_KEY)).andExpect(status().isUnauthorized());
    }

    @Test
    void getEmployeeByUuidReturnsTheMatchingEmployee() throws Exception {
        mockMvc.perform(get(EMPLOYEE_URL + "/{uuid}", SEEDED_EMPLOYEE_UUID).header(API_KEY_HEADER, apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(SEEDED_EMPLOYEE_UUID.toString())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.contractTerminationDate", nullValue()));
    }

    @Test
    void getEmployeeByUnknownUuidReturnsNotFound() throws Exception {
        mockMvc.perform(get(EMPLOYEE_URL + "/{uuid}", UUID.randomUUID()).header(API_KEY_HEADER, apiKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getEmployeeByMalformedUuidReturnsBadRequest() throws Exception {
        mockMvc.perform(get(EMPLOYEE_URL + "/not-a-uuid").header(API_KEY_HEADER, apiKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createEmployeeReturnsTheCreatedEmployeeWithServerGeneratedUuid() throws Exception {
        CreateEmployeeInput input = new CreateEmployeeInput(
                "Alice",
                "Wonderland",
                110000,
                28,
                "Product Manager",
                "alice@reliaquest.com",
                Instant.parse("2024-01-02T09:00:00Z"));

        String responseBody = mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Wonderland")))
                .andExpect(jsonPath("$.fullName", is("Alice Wonderland")))
                .andExpect(jsonPath("$.salary", is(110000)))
                .andExpect(jsonPath("$.age", is(28)))
                .andExpect(jsonPath("$.jobTitle", is("Product Manager")))
                .andExpect(jsonPath("$.email", is("alice@reliaquest.com")))
                .andExpect(jsonPath("$.contractHireDate", is("2024-01-02T09:00:00Z")))
                .andExpect(jsonPath("$.contractTerminationDate", nullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(responseBody);
        String createdUuid = created.get("uuid").asText();

        mockMvc.perform(get(EMPLOYEE_URL + "/{uuid}", createdUuid).header(API_KEY_HEADER, apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(createdUuid)))
                .andExpect(jsonPath("$.fullName", is("Alice Wonderland")));
    }

    @Test
    void createEmployeeUsesCurrentTimeWhenHireDateIsOmitted() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput("Bob", "Builder", 90000, 40, "Engineer", "bob@reliaquest.com", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractHireDate", notNullValue()));
    }

    @Test
    void createEmployeeIsRejectedWithoutApiKey() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput("Mallory", "Intruder", 90000, 40, "Engineer", "mallory@reliaquest.com", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEmployeeRejectsMissingNames() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput(null, null, 90000, 30, "Tester", "tester@reliaquest.com", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.firstName", notNullValue()))
                .andExpect(jsonPath("$.errors.lastName", notNullValue()));
    }

    @Test
    void createEmployeeRejectsBlankNames() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput("  ", "  ", 90000, 30, "Tester", "tester@reliaquest.com", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName", notNullValue()))
                .andExpect(jsonPath("$.errors.lastName", notNullValue()));
    }

    @Test
    void createEmployeeRejectsMalformedEmail() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput("Eve", "Attacker", 90000, 30, "Tester", "not-an-email", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", notNullValue()));
    }

    @Test
    void createEmployeeRejectsNegativeSalary() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput("Nina", "Negative", -1, 30, "Tester", "nina@reliaquest.com", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salary", notNullValue()));
    }

    @Test
    void createEmployeeRejectsUnrealisticAge() throws Exception {
        CreateEmployeeInput input =
                new CreateEmployeeInput("Tim", "Toddler", 90000, 5, "Tester", "tim@reliaquest.com", null);

        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age", notNullValue()));
    }

    @Test
    void createEmployeeRejectsMalformedJson() throws Exception {
        mockMvc.perform(post(EMPLOYEE_URL)
                        .header(API_KEY_HEADER, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Broken\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createEmployeeRejectsMissingBody() throws Exception {
        mockMvc.perform(post(EMPLOYEE_URL).header(API_KEY_HEADER, apiKey).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}
