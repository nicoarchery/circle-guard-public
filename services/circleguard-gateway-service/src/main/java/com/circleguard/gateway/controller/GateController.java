package com.circleguard.gateway.controller;

import com.circleguard.gateway.service.QrValidationService;
import com.circleguard.gateway.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gate")
@RequiredArgsConstructor
public class GateController {
    private final QrValidationService validationService;
    private final BusinessMetrics metrics;

    @PostMapping("/validate")
    public ResponseEntity<QrValidationService.ValidationResult> validate(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        metrics.validationsTotal.increment();
        QrValidationService.ValidationResult result = validationService.validateToken(token);
        if (result.valid()) {
            metrics.accessGranted.increment();
        } else {
            metrics.accessDenied.increment();
        }
        return ResponseEntity.ok(result);
    }
}
