package com.dhis2sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Configures a shared, thread-safe RestClient bean pre-set with
 * the DHIS2 base URL, Basic Authentication header, and JSON Content-Type.
 */
@Configuration
public class RestClientConfig {

    private final Dhis2Properties dhis2Properties;

    /**
     * Constructor injection of DHIS2 property bindings.
     */
    public RestClientConfig(Dhis2Properties dhis2Properties) {
        this.dhis2Properties = dhis2Properties;
    }

    /**
     * Creates a RestClient configured for all DHIS2 API interactions.
     * The instance is immutable and thread-safe once built.
     */
    @Bean
    public RestClient dhis2RestClient() {
        String authHeaderValue = buildBasicAuth(
                dhis2Properties.getUsername(),
                dhis2Properties.getPassword()
        );

        return RestClient.builder()
                .baseUrl(dhis2Properties.getBaseUrl())
                .defaultHeader("Authorization", authHeaderValue)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Builds a Basic Authentication header value.
     *
     * @param username the DHIS2 username
     * @param password the DHIS2 password
     * @return "Basic " followed by the Base64-encoded "username:password" string
     */
    private String buildBasicAuth(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}