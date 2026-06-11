package com.circleguard.promotion.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter healthReportsSubmitted;
    public final Counter healthConfirmed;
    public final Counter healthRecoveries;
    public final Counter encountersReported;
    public final Counter circlesCreated;
    public final Counter circlesJoined;
    public final Counter circlesFenced;
    public final Counter locationSignalsIngested;
    public final Counter priorityAlertsTriggered;
    public final Counter statusChanges;

    public BusinessMetrics(MeterRegistry registry) {
        this.healthReportsSubmitted = Counter.builder("circleguard.promotion.health.report")
                .description("Health reports submitted")
                .register(registry);
        this.healthConfirmed = Counter.builder("circleguard.promotion.health.confirmed")
                .description("Cases confirmed")
                .register(registry);
        this.healthRecoveries = Counter.builder("circleguard.promotion.health.recovery")
                .description("Recoveries recorded")
                .register(registry);
        this.encountersReported = Counter.builder("circleguard.promotion.encounter.reported")
                .description("Proximity encounters reported")
                .register(registry);
        this.circlesCreated = Counter.builder("circleguard.promotion.circle.created")
                .description("Circles created")
                .register(registry);
        this.circlesJoined = Counter.builder("circleguard.promotion.circle.joined")
                .description("Users joined circles")
                .register(registry);
        this.circlesFenced = Counter.builder("circleguard.promotion.circle.fenced")
                .description("Circles fenced")
                .register(registry);
        this.locationSignalsIngested = Counter.builder("circleguard.promotion.location.signal")
                .description("WiFi location signals ingested")
                .register(registry);
        this.priorityAlertsTriggered = Counter.builder("circleguard.promotion.alert.priority")
                .description("Priority alerts triggered")
                .register(registry);
        this.statusChanges = Counter.builder("circleguard.promotion.status.changed")
                .description("Health status changes")
                .register(registry);
    }
}
