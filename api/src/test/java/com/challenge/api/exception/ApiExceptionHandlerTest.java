package com.challenge.api.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the unexpected-error contract: a failing dependency results in a generic {@code 500} problem response and
 * never exposes the underlying exception message to the client.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiExceptionHandlerTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String INTERNAL_DETAIL = "internal-detail-that-must-not-leak";

    @Autowired
    private MockMvc mockMvc;

    @Value("${employee.security.api-key}")
    private String apiKey;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void returnsGenericInternalServerErrorWithoutLeakingExceptionDetails() throws Exception {
        given(employeeService.getAllEmployees()).willThrow(new IllegalStateException(INTERNAL_DETAIL));

        mockMvc.perform(get("/api/v1/employee").header(API_KEY_HEADER, apiKey))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.detail", is("An unexpected error occurred")))
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))));
    }
}
