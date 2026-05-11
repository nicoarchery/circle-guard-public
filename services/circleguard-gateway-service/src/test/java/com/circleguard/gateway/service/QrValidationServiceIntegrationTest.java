package com.circleguard.gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "qr.secret=my-super-secret-test-key-32-chars-long")
class QrValidationServiceIntegrationTest {

    @Autowired
    private QrValidationService qrValidationService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @Test
    void validateTokenShouldAllowClearUsers() {
        String anonymousId = UUID.randomUUID().toString();
        Key key = Keys.hmacShaKeyFor("my-super-secret-test-key-32-chars-long".getBytes());
        String token = Jwts.builder()
                .setSubject(anonymousId)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:status:" + anonymousId)).thenReturn("CLEAR");

        QrValidationService.ValidationResult result = qrValidationService.validateToken(token);

        assertTrue(result.valid());
    }

    @Test
    void validateTokenShouldRejectExpiredTokens() {
        String anonymousId = UUID.randomUUID().toString();
        Key key = Keys.hmacShaKeyFor("my-super-secret-test-key-32-chars-long".getBytes());
        String token = Jwts.builder()
                .setSubject(anonymousId)
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        QrValidationService.ValidationResult result = qrValidationService.validateToken(token);

        assertFalse(result.valid());
    }
}
