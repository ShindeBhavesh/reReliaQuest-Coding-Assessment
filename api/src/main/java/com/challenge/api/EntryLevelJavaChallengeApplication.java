package com.challenge.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Entry point of the employee API.
 *
 * <p>{@link UserDetailsServiceAutoConfiguration} is excluded on purpose: every request is authenticated with the API
 * key filter, so the auto-configured development user and its generated password are unused and would only add
 * misleading noise to the logs.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class EntryLevelJavaChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntryLevelJavaChallengeApplication.class, args);
    }
}
