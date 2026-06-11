package com.circleguard.promotion.controller;

import com.circleguard.promotion.service.LocationResolutionService;
import com.circleguard.promotion.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationSignalController {
    private final LocationResolutionService locationResolutionService;
    private final BusinessMetrics metrics;

    @PostMapping("/signal")
    public ResponseEntity<Void> receiveSignal(@RequestBody Map<String, Object> request) {
        String apMac = (String) request.get("apMac");
        String deviceMac = (String) request.get("deviceMac");
        Double rssi = Double.valueOf(request.get("rssi").toString());

        if (apMac == null || deviceMac == null || rssi == null) {
            return ResponseEntity.badRequest().build();
        }

        metrics.locationSignalsIngested.increment();
        locationResolutionService.processSignal(apMac, deviceMac, rssi);
        return ResponseEntity.ok().build();
    }
}
