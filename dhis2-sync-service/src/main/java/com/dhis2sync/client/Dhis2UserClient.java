package com.dhis2sync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client for DHIS2 user API operations.
 * Encapsulates HTTP interactions for querying and creating users.
 * All exceptions propagate to the service layer for centralized error handling.
 */
@Component
public class Dhis2UserClient {

    private static final Logger logger = LoggerFactory.getLogger(Dhis2UserClient.class);

    private final RestClient dhis2RestClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection of pre-configured RestClient and ObjectMapper.
     *
     * @param dhis2RestClient RestClient bean with DHIS2 base URL and Basic Auth
     * @param objectMapper    Spring-managed ObjectMapper for JSON parsing
     */
    public Dhis2UserClient(RestClient dhis2RestClient, ObjectMapper objectMapper) {
        this.dhis2RestClient = dhis2RestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Queries DHIS2 for a user with the given username.
     * Returns the user's UID if found, or empty Optional if not found.
     *
     * @param username the username to search for
     * @return Optional containing the user UID if found, empty otherwise
     * @throws Exception if HTTP request fails or JSON parsing fails
     */
    public Optional<String> findUserByUsername(String username) throws Exception {
        String uri = "/api/42/users?filter=username:eq:" + username + "&fields=id";

        String responseBody = dhis2RestClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode array = root.path("users");

        Optional<String> result;
        if (array.isArray() && array.size() > 0) {
            String uid = array.get(0).path("id").asText();
            result = Optional.of(uid);
        } else {
            result = Optional.empty();
        }

        logger.debug("DHIS2 用户查询: username={}, found={}", username, result.isPresent());
        return result;
    }

    /**
     * Creates a new user in DHIS2 with assigned roles and organisation units.
     * Returns the raw response body string (contains UID in JSON structure).
     *
     * @param username   the user login name
     * @param password   the user password
     * @param firstName  the user's first name (typically same as username)
     * @param surname    the user's surname (typically same as username)
     * @param roleIds    list of DHIS2 role UIDs to assign to the user
     * @param orgUnitIds list of DHIS2 organisation unit UIDs to assign to the user
     * @return the HTTP response body as a String
     * @throws Exception if HTTP request fails
     */
    public String createUser(String username, String password, String firstName, String surname,
                             List<String> roleIds, List<String> orgUnitIds) throws Exception {

        // Construct userRoles array: [{"id": "roleUid1"}, {"id": "roleUid2"}, ...]
        List<Map<String, String>> userRoles = new ArrayList<>();
        for (String roleId : roleIds) {
            Map<String, String> roleMap = new HashMap<>();
            roleMap.put("id", roleId);
            userRoles.add(roleMap);
        }

        // Construct organisationUnits array: [{"id": "orgUid1"}, {"id": "orgUid2"}, ...]
        List<Map<String, String>> organisationUnits = new ArrayList<>();
        for (String orgUnitId : orgUnitIds) {
            Map<String, String> orgMap = new HashMap<>();
            orgMap.put("id", orgUnitId);
            organisationUnits.add(orgMap);
        }

        // Construct request body
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("firstName", firstName);
        body.put("surname", surname);
        body.put("userRoles", userRoles);
        body.put("organisationUnits", organisationUnits);

        String responseBody = dhis2RestClient.post()
                .uri("/api/42/users")
                .body(body)
                .retrieve()
                .body(String.class);

        logger.debug("DHIS2 用户创建请求: username={}, response={}", username, responseBody);
        return responseBody;
    }
}