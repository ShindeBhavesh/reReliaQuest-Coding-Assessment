package com.challenge.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.model.CreateEmployeeInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeControllerTest {

    private static final String API_KEY = "test-api-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEmployeeApiRequiresApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/v1/employee").header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].firstName", hasItem("John")))
                .andExpect(jsonPath("$[*].firstName", hasItem("Jane")));
    }

    @Test
    void testGetEmployeeByUuidSuccess() throws Exception {
        UUID validUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        mockMvc.perform(get("/api/v1/employee/" + validUuid).header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(validUuid.toString())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.fullName", is("John Doe")));
    }

    @Test
    void testGetEmployeeByUuidNotFound() throws Exception {
        UUID randomUuid = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/employee/" + randomUuid)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateEmployeeSuccess() throws Exception {
        CreateEmployeeInput input = new CreateEmployeeInput(
                "Alice", "Wonderland", 110000, 28, "Product Manager", "alice@reliaquest.com", null);

        mockMvc.perform(post("/api/v1/employee")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Wonderland")))
                .andExpect(jsonPath("$.fullName", is("Alice Wonderland")))
                .andExpect(jsonPath("$.salary", is(110000)))
                .andExpect(jsonPath("$.jobTitle", is("Product Manager")));
    }

    @Test
    void testCreateEmployeeBadRequestBlankNames() throws Exception {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setFirstName(" ");
        input.setLastName(" ");

        mockMvc.perform(post("/api/v1/employee")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployeeBadRequestMissingNames() throws Exception {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setSalary(50000);

        mockMvc.perform(post("/api/v1/employee")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }
}
