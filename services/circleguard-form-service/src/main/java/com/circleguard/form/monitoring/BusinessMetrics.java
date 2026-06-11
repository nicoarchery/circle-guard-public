package com.circleguard.form.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter surveysSubmitted;
    public final Counter surveysValidated;
    public final Counter surveysApproved;
    public final Counter surveysRejected;
    public final Counter questionnairesCreated;
    public final Counter questionnairesActivated;
    public final Counter attachmentsUploaded;

    public BusinessMetrics(MeterRegistry registry) {
        this.surveysSubmitted = Counter.builder("circleguard.form.survey.submitted")
                .description("Health surveys submitted")
                .register(registry);
        this.surveysValidated = Counter.builder("circleguard.form.survey.validated")
                .description("Surveys validated")
                .register(registry);
        this.surveysApproved = Counter.builder("circleguard.form.survey.approved")
                .description("Surveys approved")
                .register(registry);
        this.surveysRejected = Counter.builder("circleguard.form.survey.rejected")
                .description("Surveys rejected")
                .register(registry);
        this.questionnairesCreated = Counter.builder("circleguard.form.questionnaire.created")
                .description("Questionnaires created")
                .register(registry);
        this.questionnairesActivated = Counter.builder("circleguard.form.questionnaire.activated")
                .description("Questionnaires activated")
                .register(registry);
        this.attachmentsUploaded = Counter.builder("circleguard.form.attachment.uploaded")
                .description("Attachments uploaded")
                .register(registry);
    }
}
