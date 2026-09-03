package com.challenge.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/employee/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/employee")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    ApiKeyFilter apiKeyFilter(@Value("${employee.api-key}") String apiKey) {
        return new ApiKeyFilter(apiKey);
    }

    static class ApiKeyFilter extends OncePerRequestFilter {

        private static final String API_KEY_HEADER = "X-API-Key";
        private final String expectedApiKey;

        ApiKeyFilter(String expectedApiKey) {
            this.expectedApiKey = expectedApiKey;
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String providedApiKey = request.getHeader(API_KEY_HEADER);

            if (providedApiKey == null || !providedApiKey.equals(expectedApiKey)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing API key");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            "employees-r-us", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.startsWith("/swagger-ui")
                    || path.startsWith("/v3/api-docs")
                    || path.equals("/swagger-ui.html")
                    || !path.startsWith("/api/v1/employee");
        }
    }
}
