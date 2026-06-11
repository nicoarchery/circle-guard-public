package com.circleguard.form.controller;

import com.circleguard.form.service.StorageService;
import com.circleguard.form.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttachmentController {

    private final StorageService storageService;
    private final BusinessMetrics metrics;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        metrics.attachmentsUploaded.increment();
        String filename = storageService.store(file);
        return ResponseEntity.ok(Map.of("filename", filename));
    }
}
