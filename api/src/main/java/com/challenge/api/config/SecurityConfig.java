package com.challenge.api.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security setup for the employee API.
 *
 * <p>The API is stateless and protected by an API key ({@link ApiKeyAuthFilter}). The key is supplied through the
 * {@code employee.security.api-key} property, which resolves to the {@code EMPLOYEE_API_KEY} environment variable and
 * falls back to a documented development default so a fresh clone can be started and evaluated without any
 * undocumented setup.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PROTECTED_PATHS = {"/api/v1/employee", "/api/v1/employee/**"};
    private static final String[] PUBLIC_PATHS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    private final String apiKey;

    public SecurityConfig(@Value("${employee.security.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(PROTECTED_PATHS)
                        .authenticated()
                        .requestMatchers(PUBLIC_PATHS)
                        .permitAll()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(new ApiKeyAuthFilter(apiKey), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
