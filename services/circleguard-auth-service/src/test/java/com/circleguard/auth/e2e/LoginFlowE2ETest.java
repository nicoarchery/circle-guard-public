package com.circleguard.auth.e2e;

import com.circleguard.auth.client.IdentityClient;
import com.circleguard.auth.security.SecurityConfig;
import com.circleguard.auth.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class LoginFlowE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtTokenService jwtService;

    @MockBean
    private IdentityClient identityClient;

    @Test
    void loginShouldReturnJwtAndAnonymousId() {
        UUID anonymousId = UUID.randomUUID();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(identityClient.getAnonymousId("e2e-user")).thenReturn(anonymousId);
        when(jwtService.generateToken(eq(anonymousId), any(Authentication.class))).thenReturn("e2e-token");

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/auth/login"),
                requestBody("e2e-user", "password123"), Map.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("e2e-token", response.getBody().get("token"));
        assertEquals(anonymousId.toString(), response.getBody().get("anonymousId"));
        assertEquals("Bearer", response.getBody().get("type"));
    }

    @Test
    void visitorHandoffShouldReturnPayloadWithAnonymousId() {
        UUID anonymousId = UUID.randomUUID();
        when(jwtService.generateToken(eq(anonymousId), any(Authentication.class))).thenReturn("visitor-token");

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/auth/visitor/handoff"),
                Map.of("anonymousId", anonymousId.toString()), Map.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("visitor-token", response.getBody().get("token"));
        assertEquals("HANDOFF_TOKEN:" + anonymousId + ":visitor-token", response.getBody().get("handoffPayload"));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, String> requestBody(String username, String password) {
        return Map.of("username", username, "password", password);
    }
}
