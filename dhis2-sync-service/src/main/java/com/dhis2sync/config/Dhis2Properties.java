package com.dhis2sync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe binding for all DHIS2-related configuration values
 * defined under the "dhis2" prefix in application.yml.
 *
 * Spring Boot relaxed binding maps:
 *   dhis2.base-url           -> baseUrl
 *   dhis2.username           -> username
 *   dhis2.password           -> password
 *   dhis2.default-user-password -> defaultUserPassword
 */
@Component
@ConfigurationProperties(prefix = "dhis2")
public class Dhis2Properties {

    private String baseUrl;
    private String username;
    private String password;
    private String defaultUserPassword;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultUserPassword() {
        return defaultUserPassword;
    }

    public void setDefaultUserPassword(String defaultUserPassword) {
        this.defaultUserPassword = defaultUserPassword;
    }
}