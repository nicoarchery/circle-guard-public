package com.circleguard.auth.controller;

import com.circleguard.auth.service.QrTokenService;
import com.circleguard.auth.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/qr")
@RequiredArgsConstructor
public class QrTokenController {
    private final QrTokenService qrService;
    private final BusinessMetrics metrics;

    /**
     * Generates a short-lived QR token for campus entry.
     * The UID is extracted from the JWT authentication context.
     */
    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateToken(Authentication auth) {
        // In a real app, the anonymousId is stored in the JWT principal/claims
        UUID anonymousId = UUID.fromString(auth.getName());
        String token = qrService.generateQrToken(anonymousId);
        metrics.qrTokensGenerated.increment();
        
        return ResponseEntity.ok(Map.of(
            "qrToken", token,
            "expiresIn", "60"
        ));
    }
}
