package com.challenge.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for Swagger UI. The declared security scheme matches the API key that {@link SecurityConfig}
 * enforces, so the "Authorize" button in Swagger UI authenticates exactly like any other client.
 */
@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SCHEME = "apiKey";

    @Bean
    OpenAPI employeeApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee API")
                        .version("v1")
                        .description("Exposes employee information to Employees-R-US webhooks. "
                                + "Every /api/v1/employee endpoint requires the X-API-Key header."))
                .components(new Components()
                        .addSecuritySchemes(
                                API_KEY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("API key configured through the EMPLOYEE_API_KEY environment "
                                                + "variable (development default: local-dev-api-key)")));
    }
}
