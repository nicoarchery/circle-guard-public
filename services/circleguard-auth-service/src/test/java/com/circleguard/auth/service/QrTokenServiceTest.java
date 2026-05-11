package com.circleguard.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrTokenServiceTest {

    private static final String SECRET = "qr-secret-32-chars-long-1234567890";
    private static final long EXPIRATION_MS = 90_000L;

    private final QrTokenService tokenService = new QrTokenService(SECRET, EXPIRATION_MS);
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @Test
    void generateQrTokenShouldUseAnonymousIdAsSubject() {
        UUID anonymousId = UUID.randomUUID();

        String token = tokenService.generateQrToken(anonymousId);
        Claims claims = parse(token);

        assertEquals(anonymousId.toString(), claims.getSubject());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}