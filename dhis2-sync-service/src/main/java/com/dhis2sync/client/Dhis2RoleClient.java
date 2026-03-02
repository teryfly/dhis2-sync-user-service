package com.dhis2sync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client for DHIS2 userRole API operations.
 * Encapsulates HTTP interactions for querying and creating roles.
 * All exceptions propagate to the service layer for centralized error handling.
 */
@Component
public class Dhis2RoleClient {

    private static final Logger logger = LoggerFactory.getLogger(Dhis2RoleClient.class);

    private final RestClient dhis2RestClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection of pre-configured RestClient and ObjectMapper.
     *
     * @param dhis2RestClient RestClient bean with DHIS2 base URL and Basic Auth
     * @param objectMapper    Spring-managed ObjectMapper for JSON parsing
     */
    public Dhis2RoleClient(RestClient dhis2RestClient, ObjectMapper objectMapper) {
        this.dhis2RestClient = dhis2RestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Queries DHIS2 for a userRole with the given code.
     * Returns the role's UID if found, or empty Optional if not found.
     *
     * @param code the role code to search for
     * @return Optional containing the role UID if found, empty otherwise
     * @throws Exception if HTTP request fails or JSON parsing fails
     */
    public Optional<String> findRoleByCode(String code) throws Exception {
        String uri = "/api/42/userRoles?filter=code:eq:" + code + "&fields=id";

        String responseBody = dhis2RestClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode array = root.path("userRoles");

        Optional<String> result;
        if (array.isArray() && array.size() > 0) {
            String uid = array.get(0).path("id").asText();
            result = Optional.of(uid);
        } else {
            result = Optional.empty();
        }

        logger.debug("DHIS2 角色查询: code={}, found={}", code, result.isPresent());
        return result;
    }

    /**
     * Creates a new userRole in DHIS2.
     * Returns the raw response body string (contains UID in JSON structure).
     *
     * @param name        the role display name
     * @param description the role description
     * @param code        the unique role code
     * @return the HTTP response body as a String
     * @throws Exception if HTTP request fails
     */
    public String createRole(String name, String description, String code) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        body.put("code", code);

        String responseBody = dhis2RestClient.post()
                .uri("/api/42/userRoles")
                .body(body)
                .retrieve()
                .body(String.class);

        logger.debug("DHIS2 角色创建请求: code={}, response={}", code, responseBody);
        return responseBody;
    }
}