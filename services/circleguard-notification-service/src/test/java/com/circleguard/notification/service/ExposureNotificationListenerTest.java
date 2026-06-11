package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "management.health.mail.enabled=false")
class ExposureNotificationListenerTest {

    @Autowired
    private ExposureNotificationListener listener;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private NotificationDispatcher dispatcher;

    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @MockBean
    private org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SmsService smsService;

    @MockBean
    private PushService pushService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void shouldHandleStatusChangeEventWithoutError() {
        String mockEvent = "{\"userId\": \"user-123\", \"newStatus\": \"EXPOSED\"}";
        assertDoesNotThrow(() -> listener.handleStatusChange(mockEvent));
    }
}
