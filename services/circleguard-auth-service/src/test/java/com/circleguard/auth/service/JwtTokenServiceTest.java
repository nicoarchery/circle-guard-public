package com.circleguard.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-32-chars-long-123456";
    private static final long EXPIRATION_MS = 60_000L;

    private final JwtTokenService tokenService = new JwtTokenService(SECRET, EXPIRATION_MS);
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @Test
    void generateTokenShouldIncludeAnonymousIdAndPermissions() {
        UUID anonymousId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"), new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = tokenService.generateToken(anonymousId, authentication);
        Claims claims = parse(token);

        assertEquals(anonymousId.toString(), claims.getSubject());
        assertEquals(List.of("ROLE_STUDENT", "ROLE_USER"), claims.get("permissions", List.class));
    }

    @Test
    void generateTokenShouldSetFutureExpiration() {
        UUID anonymousId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = tokenService.generateToken(anonymousId, authentication);
        Claims claims = parse(token);

        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}