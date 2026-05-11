package com.circleguard.gateway.e2e;

import com.circleguard.gateway.service.QrValidationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;

import java.security.Key;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "qr.secret=my-super-secret-test-key-32-chars-long")
class GateValidationE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @Test
    void validateShouldReturnGreenForClearUser() {
        String anonymousId = UUID.randomUUID().toString();
        String token = createToken(anonymousId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:status:" + anonymousId)).thenReturn("CLEAR");

        ResponseEntity<QrValidationService.ValidationResult> response = restTemplate.postForEntity(
                url("/api/v1/gate/validate"),
                Map.of("token", token),
                QrValidationService.ValidationResult.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().valid());
        assertEquals("GREEN", response.getBody().status());
    }

    @Test
    void validateShouldReturnRedForContagiedUser() {
        String anonymousId = UUID.randomUUID().toString();
        String token = createToken(anonymousId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:status:" + anonymousId)).thenReturn("CONTAGIED");

        ResponseEntity<QrValidationService.ValidationResult> response = restTemplate.postForEntity(
                url("/api/v1/gate/validate"),
                Map.of("token", token),
                QrValidationService.ValidationResult.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().valid());
        assertEquals("RED", response.getBody().status());
    }

    private String createToken(String anonymousId) {
        Key key = Keys.hmacShaKeyFor("my-super-secret-test-key-32-chars-long".getBytes());
        return Jwts.builder().setSubject(anonymousId).signWith(key, SignatureAlgorithm.HS256).compact();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
