package com.circleguard.form.controller;

import com.circleguard.form.model.Questionnaire;
import com.circleguard.form.service.QuestionnaireService;
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

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionnaireController.class)
@Import(QuestionnaireControllerTest.TestConfig.class)
class QuestionnaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionnaireService questionnaireService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BusinessMetrics businessMetrics() {
            return new BusinessMetrics(new SimpleMeterRegistry());
        }
    }

    @Test
    void shouldReturnActiveQuestionnaire() throws Exception {
        UUID id = UUID.randomUUID();
        Questionnaire q = Questionnaire.builder()
                .id(id)
                .title("Daily Health Check")
                .isActive(true)
                .version(1)
                .build();

        Mockito.when(questionnaireService.getActiveQuestionnaire()).thenReturn(Optional.of(q));

        mockMvc.perform(get("/api/v1/questionnaires/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Daily Health Check"));
    }

    @Test
    void shouldCreateQuestionnaire() throws Exception {
        UUID id = UUID.randomUUID();
        Questionnaire q = Questionnaire.builder()
                .id(id)
                .title("New Survey")
                .version(1)
                .build();

        Mockito.when(questionnaireService.saveQuestionnaire(Mockito.any(Questionnaire.class))).thenReturn(q);

        mockMvc.perform(post("/api/v1/questionnaires")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"New Survey\", \"version\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Survey"));
    }
}
