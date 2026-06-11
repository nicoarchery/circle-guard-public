package com.circleguard.form.controller;

import com.circleguard.form.model.ValidationStatus;
import com.circleguard.form.service.HealthSurveyService;
import com.circleguard.form.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CertificateValidationController {

    private final HealthSurveyService surveyService;
    private final BusinessMetrics metrics;

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(surveyService.getPendingSurveys());
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validate(
            @PathVariable UUID id,
            @RequestParam ValidationStatus status,
            @RequestParam UUID adminId) {
        metrics.surveysValidated.increment();
        if (status == ValidationStatus.APPROVED) {
            metrics.surveysApproved.increment();
        } else {
            metrics.surveysRejected.increment();
        }
        surveyService.validateSurvey(id, status, adminId);
        return ResponseEntity.ok().build();
    }
}
