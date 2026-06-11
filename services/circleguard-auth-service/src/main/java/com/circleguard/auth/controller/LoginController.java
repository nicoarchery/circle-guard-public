package com.circleguard.auth.controller;

import com.circleguard.auth.service.JwtTokenService;
import com.circleguard.auth.client.IdentityClient;
import com.circleguard.auth.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
//ggggg
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtTokenService jwtService;
    private final IdentityClient identityClient;
    private final BusinessMetrics metrics;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        metrics.loginAttempts.increment();

        try {
            // 1. Authenticate (Dual-Chain)
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 2. Anonymize (Fetch/Create Anonymous ID from Identity Service)
            UUID anonymousId = identityClient.getAnonymousId(username);

            // 3. Issue Token
            String token = jwtService.generateToken(anonymousId, auth);
            metrics.loginSuccesses.increment();

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "type", "Bearer",
                    "anonymousId", anonymousId.toString()
            ));
        } catch (org.springframework.security.core.AuthenticationException e) {
            metrics.loginFailures.increment();
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        } catch (Exception e) {
            metrics.loginFailures.increment();
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/visitor/handoff")
    public ResponseEntity<Map<String, String>> generateVisitorHandoff(@RequestBody Map<String, String> request) {
        String anonymousIdStr = request.get("anonymousId");
        if (anonymousIdStr == null) {
            return ResponseEntity.badRequest().build();
        }
        
        UUID anonymousId = UUID.fromString(anonymousIdStr);
        
        // Create a dummy authentication for the visitor
        Authentication visitorAuth = new UsernamePasswordAuthenticationToken(
                anonymousIdStr, 
                null, 
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("VISITOR"))
        );
        
        String token = jwtService.generateToken(anonymousId, visitorAuth);
        metrics.visitorHandoffs.increment();
        
        return ResponseEntity.ok(Map.of(
                "token", token,
                "handoffPayload", "HANDOFF_TOKEN:" + anonymousId.toString() + ":" + token
        ));
    }
}
