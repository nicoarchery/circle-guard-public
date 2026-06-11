package com.circleguard.file.controller;

import com.circleguard.file.service.FileStorageService;
import com.circleguard.file.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileStorageService storageService;
    private final BusinessMetrics metrics;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String filename = storageService.saveFile(file);
            metrics.filesUploaded.increment();
            return ResponseEntity.ok(Map.of("filename", filename));
        } catch (Exception e) {
            metrics.uploadErrors.increment();
            throw e;
        }
    }
}
