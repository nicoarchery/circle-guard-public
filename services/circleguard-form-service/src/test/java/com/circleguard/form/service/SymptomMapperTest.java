package com.circleguard.form.service;

import com.circleguard.form.model.HealthSurvey;
import com.circleguard.form.model.Question;
import com.circleguard.form.model.QuestionType;
import com.circleguard.form.model.Questionnaire;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SymptomMapperTest {

    private final SymptomMapper mapper = new SymptomMapper();

    @Test
    void shouldDetectSymptomsFromFever() {
        UUID questionId = UUID.randomUUID();
        Question q = Question.builder()
                .id(questionId)
                .text("Do you have a fever?")
                .type(QuestionType.YES_NO)
                .build();
        
        Questionnaire questionnaire = Questionnaire.builder()
                .questions(List.of(q))
                .build();
        
        HealthSurvey survey = HealthSurvey.builder()
                .responses(Map.of(questionId.toString(), (Object)"YES"))
                .build();
        
        assertTrue(mapper.hasSymptoms(survey, questionnaire));
    }

    @Test
    void shouldNotDetectSymptomsWhenNo() {
        UUID questionId = UUID.randomUUID();
        Question q = Question.builder()
                .id(questionId)
                .text("Do you have a fever?")
                .type(QuestionType.YES_NO)
                .build();
        
        Questionnaire questionnaire = Questionnaire.builder()
                .questions(List.of(q))
                .build();
        
        HealthSurvey survey = HealthSurvey.builder()
                .responses(Map.of(questionId.toString(), (Object)"NO"))
                .build();
        
        assertFalse(mapper.hasSymptoms(survey, questionnaire));
    }

    @Test
    void shouldReturnFalseWhenSurveyResponsesAreMissing() {
        Question q = Question.builder()
                .id(UUID.randomUUID())
                .text("Do you have a fever?")
                .type(QuestionType.YES_NO)
                .build();

        Questionnaire questionnaire = Questionnaire.builder()
                .questions(List.of(q))
                .build();

        HealthSurvey survey = HealthSurvey.builder()
                .responses(null)
                .build();

        assertFalse(mapper.hasSymptoms(survey, questionnaire));
    }

    @Test
    void shouldDetectSymptomsFromChoiceQuestionWithSelection() {
        UUID questionId = UUID.randomUUID();
        Question q = Question.builder()
                .id(questionId)
                .text("Select any symptoms you have experienced")
                .type(QuestionType.MULTI_CHOICE)
                .build();

        Questionnaire questionnaire = Questionnaire.builder()
                .questions(List.of(q))
                .build();

        HealthSurvey survey = HealthSurvey.builder()
                .responses(Map.of(questionId.toString(), (Object)"[COUGH,FEVER]"))
                .build();

        assertTrue(mapper.hasSymptoms(survey, questionnaire));
    }
}
