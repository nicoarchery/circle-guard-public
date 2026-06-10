package com.circleguard.form.e2e;

import com.circleguard.form.model.HealthSurvey;
import com.circleguard.form.service.HealthSurveyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthSurveyE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private HealthSurveyService surveyService;

    @Test
    void submitSurveyShouldReturnSavedSurvey() {
        UUID anonymousId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        HealthSurvey saved = new HealthSurvey();
        saved.setId(UUID.randomUUID());
        saved.setAnonymousId(anonymousId);
        saved.setHasCough(true);

        when(surveyService.submitSurvey(any(HealthSurvey.class))).thenReturn(saved);

        ResponseEntity<HealthSurvey> response = restTemplate.postForEntity(
                url("/api/v1/surveys"),
                Map.of("anonymousId", anonymousId.toString(), "symptoms", new String[]{"COUGH"}),
                HealthSurvey.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(anonymousId, response.getBody().getAnonymousId());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
