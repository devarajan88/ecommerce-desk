package com.appsdeveloperblog.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service that creates / manages users in Keycloak via the Admin REST API.
 * Uses the client-credentials flow (admin-cli) to get a management token,
 * then calls POST /admin/realms/{realm}/users.
 */
@Service
public class KeycloakAdminService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminService.class);

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;          // e.g. http://keycloak:8080

    @Value("${keycloak.admin.realm}")
    private String realm;              // saga-realm

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;      // admin-cli

    @Value("${keycloak.admin.username}")
    private String adminUsername;      // admin

    @Value("${keycloak.admin.password}")
    private String adminPassword;      // admin

    private final RestTemplate restTemplate = new RestTemplate();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Registers a new user in Keycloak and sets their initial password.
     *
     * @param username  login username (derived from email prefix or given)
     * @param email     user's email address
     * @param firstName first name
     * @param lastName  last name (can be empty string)
     * @param password  initial password (temporary=false by default)
     */
    public void createKeycloakUser(String username, String email,
                                   String firstName, String lastName,
                                   String password) {
        String adminToken = getAdminToken();

        // --- Build Keycloak user representation ---
        Map<String, Object> userRepresentation = Map.of(
            "username", username,
            "email", email,
            "firstName", firstName,
            "lastName", lastName,
            "enabled", true,
            "emailVerified", true,
            "credentials", List.of(
                Map.of(
                    "type", "password",
                    "value", password,
                    "temporary", false
                )
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userRepresentation, headers);
        String url = serverUrl + "/admin/realms/" + realm + "/users";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Keycloak user '{}' created successfully.", username);
            } else {
                log.warn("Unexpected Keycloak response: {} — {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to create Keycloak user '{}': {}", username, e.getMessage());
            throw new RuntimeException("Keycloak user creation failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Retrieves a short-lived admin access token via Resource Owner Password credentials. */
    private String getAdminToken() {
        String tokenUrl = serverUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", adminClientId);
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, request, Map.class);
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new RuntimeException("Empty token response from Keycloak master realm");
            }
            return (String) tokenResponse.get("access_token");
        } catch (Exception e) {
            throw new RuntimeException("Cannot obtain Keycloak admin token: " + e.getMessage(), e);
        }
    }
}
