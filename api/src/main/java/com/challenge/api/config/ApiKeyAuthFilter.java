package com.challenge.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that carry the configured API key in the {@code X-API-Key} header.
 *
 * <p>The filter only authenticates; deciding which endpoints need an authenticated caller is the responsibility of
 * {@link SecurityConfig}. An invalid or missing key therefore results in an unauthenticated request, which Spring
 * Security rejects with {@code 401 Unauthorized} on the protected endpoints.
 */
final class ApiKeyAuthFilter extends OncePerRequestFilter {

    static final String API_KEY_HEADER = "X-API-Key";

    private static final String PRINCIPAL_NAME = "employees-r-us";

    private final String expectedApiKey;

    ApiKeyAuthFilter(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (matches(apiKey)) {
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(PRINCIPAL_NAME, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private boolean matches(String apiKey) {
        return apiKey != null
                && MessageDigest.isEqual(
                        apiKey.getBytes(StandardCharsets.UTF_8), expectedApiKey.getBytes(StandardCharsets.UTF_8));
    }
}
