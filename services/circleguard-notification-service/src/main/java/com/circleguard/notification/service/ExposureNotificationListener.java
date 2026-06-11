package com.circleguard.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.circleguard.notification.monitoring.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExposureNotificationListener {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper objectMapper;
    private final LmsService lmsService;
    private final BusinessMetrics metrics;

    /**
     * Listens for status changes and exposure events.
     */
    @KafkaListener(topics = "promotion.status.changed", groupId = "notification-group")
    public void handleStatusChange(String eventJson) {
        log.info("Received health status change event: {}", eventJson);
        try {
            JsonNode node = objectMapper.readTree(eventJson);
            String userId = node.path("anonymousId").asText("unknown");
            String status = node.path("status").asText("UNKNOWN");
            
            if (!"ACTIVE".equals(status) && !"UNKNOWN".equals(status)) {
                metrics.statusChangeNotifications.increment();
                dispatcher.dispatch(userId, status);
                metrics.lmsSyncs.increment();
                lmsService.syncRemoteAttendance(userId, status);
            }
        } catch (Exception e) {
            log.error("Failed to parse health status change event: {}", e.getMessage());
        }
    }
}
