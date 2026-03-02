package com.dhis2sync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Client for DHIS2 organisationUnit API operations.
 * Encapsulates HTTP interactions for querying organisation units.
 * All exceptions propagate to the service layer for centralized error handling.
 */
@Component
public class Dhis2OrgUnitClient {

    private static final Logger logger = LoggerFactory.getLogger(Dhis2OrgUnitClient.class);

    private final RestClient dhis2RestClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection of pre-configured RestClient and ObjectMapper.
     *
     * @param dhis2RestClient RestClient bean with DHIS2 base URL and Basic Auth
     * @param objectMapper    Spring-managed ObjectMapper for JSON parsing
     */
    public Dhis2OrgUnitClient(RestClient dhis2RestClient, ObjectMapper objectMapper) {
        this.dhis2RestClient = dhis2RestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Queries DHIS2 for an organisationUnit with the given code.
     * Returns the organisation unit's UID if found, or empty Optional if not found.
     *
     * @param code the organisation unit code to search for
     * @return Optional containing the organisation unit UID if found, empty otherwise
     * @throws Exception if HTTP request fails or JSON parsing fails
     */
    public Optional<String> findOrgUnitByCode(String code) throws Exception {
        String uri = "/api/42/organisationUnits?filter=code:eq:" + code + "&fields=id";

        String responseBody = dhis2RestClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode array = root.path("organisationUnits");

        Optional<String> result;
        if (array.isArray() && array.size() > 0) {
            String uid = array.get(0).path("id").asText();
            result = Optional.of(uid);
        } else {
            result = Optional.empty();
        }

        logger.debug("DHIS2 机构查询: code={}, found={}", code, result.isPresent());
        return result;
    }
}