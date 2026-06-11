package com.circleguard.form.controller;

import com.circleguard.form.model.HealthSurvey;
import com.circleguard.form.service.HealthSurveyService;
import com.circleguard.form.monitoring.BusinessMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthSurveyController.class)
@Import(HealthSurveyControllerTest.TestConfig.class)
class HealthSurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthSurveyService surveyService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BusinessMetrics businessMetrics() {
            return new BusinessMetrics(new SimpleMeterRegistry());
        }
    }

    @Test
    void shouldSubmitSurveySuccessfully() throws Exception {
        UUID surveyId = UUID.randomUUID();
        HealthSurvey surveyResponse = new HealthSurvey();
        surveyResponse.setId(surveyId);
        surveyResponse.setAnonymousId(UUID.randomUUID());

        Mockito.when(surveyService.submitSurvey(Mockito.any(HealthSurvey.class))).thenReturn(surveyResponse);

        mockMvc.perform(post("/api/v1/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"anonymousId\": \"550e8400-e29b-41d4-a716-446655440000\", \"symptoms\": [\"COUGH\", \"FEVER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnSubmittedAnonymousId() throws Exception {
        UUID anonymousId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        HealthSurvey surveyResponse = new HealthSurvey();
        surveyResponse.setId(UUID.randomUUID());
        surveyResponse.setAnonymousId(anonymousId);

        Mockito.when(surveyService.submitSurvey(Mockito.any(HealthSurvey.class))).thenReturn(surveyResponse);

        mockMvc.perform(post("/api/v1/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"anonymousId\": \"550e8400-e29b-41d4-a716-446655440000\", \"symptoms\": [\"COUGH\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(anonymousId.toString()));
    }
}
