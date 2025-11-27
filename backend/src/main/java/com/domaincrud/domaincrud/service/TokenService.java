package com.domaincrud.domaincrud.service;

import com.domaincrud.domaincrud.entity.Employee;
import com.domaincrud.domaincrud.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final EmployeeRepository employeeRepository;

    public TokenService(EmployeeRepository employeeRepository) {
        this.restTemplate = new RestTemplate();
        this.employeeRepository = employeeRepository;
    }

    /**
     * Step 1: Exchange authorization code for tokens
     * Called from OAuthController.callback()
     */
    public TokenResponse exchangeCode(String authorizationCode) {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google client ID is not configured. Check secrets.properties or environment variables.");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("Google client secret is not configured. Check secrets.properties or environment variables.");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalStateException("OAuth redirect URI is not configured.");
        }

        log.info("Attempting token exchange with redirectUri={}", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tokenEndpoint,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            return new TokenResponse(
                    (String) responseBody.get("id_token"),
                    (String) responseBody.get("access_token"),
                    (String) responseBody.get("refresh_token")
            );
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Token exchange failed. Status: {}, Body: {}", e.getStatusCode(), responseBody);
            throw new RuntimeException("Failed to exchange authorization code: " + e.getStatusCode() +
                    " - " + e.getStatusText() +
                    (responseBody != null && !responseBody.isBlank() ? " | Response: " + responseBody : ""));
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage(), e);
        }
    }

    /**
     * Step 2: Validate ID token with Google
     * Returns user email if valid, null otherwise
     */
    public String validateIdToken(String idToken) {
        String validationUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(validationUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenInfo = response.getBody();

                // Verify token fields
                String aud = (String) tokenInfo.get("aud");
                String iss = (String) tokenInfo.get("iss");
                String email = (String) tokenInfo.get("email");

                // Check if token is valid
                boolean validAudience = clientId.equals(aud);
                boolean validIssuer = "https://accounts.google.com".equals(iss) ||
                        "accounts.google.com".equals(iss);

                if (validAudience && validIssuer) {
                    // Additional check: Verify user exists in database and is admin
                    return validateUserInDatabase(email) ? email : null;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Step 3: Validate access token
     */
    public boolean validateAccessToken(String accessToken) {
        String validationUrl = "https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(validationUrl, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate user exists in database and is an admin (department_id = 1)
     */
    private boolean validateUserInDatabase(String email) {
        return employeeRepository.findByEmail(email)
                .map(employee -> employee.getDepartmentId() != null &&
                        employee.getDepartmentId().equals(1L))
                .orElse(false);
    }

    /**
     * Get user email from ID token
     */
    public String getEmailFromIdToken(String idToken) {
        String validationUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(validationUrl, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenInfo = response.getBody();
                return (String) tokenInfo.get("email");
            }
        } catch (Exception e) {
            System.err.println("Failed to get email from token: " + e.getMessage());
        }
        return null;
    }

    // Token Response DTO
    public static class TokenResponse {
        private final String idToken;
        private final String accessToken;
        private final String refreshToken;

        public TokenResponse(String idToken, String accessToken, String refreshToken) {
            this.idToken = idToken;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getIdToken() { return idToken; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}