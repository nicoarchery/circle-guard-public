package com.circleguard.auth.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter loginAttempts;
    public final Counter loginSuccesses;
    public final Counter loginFailures;
    public final Counter visitorHandoffs;
    public final Counter qrTokensGenerated;

    public BusinessMetrics(MeterRegistry registry) {
        this.loginAttempts = Counter.builder("circleguard.auth.login.attempts")
                .description("Total login attempts")
                .register(registry);
        this.loginSuccesses = Counter.builder("circleguard.auth.login.success")
                .description("Successful logins")
                .register(registry);
        this.loginFailures = Counter.builder("circleguard.auth.login.failures")
                .description("Failed logins")
                .register(registry);
        this.visitorHandoffs = Counter.builder("circleguard.auth.visitor.handoffs")
                .description("Visitor handoff tokens generated")
                .register(registry);
        this.qrTokensGenerated = Counter.builder("circleguard.auth.qr.generated")
                .description("QR tokens generated")
                .register(registry);
    }
}
