package com.circleguard.gateway.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter validationsTotal;
    public final Counter accessGranted;
    public final Counter accessDenied;
    public final Counter invalidTokens;

    public BusinessMetrics(MeterRegistry registry) {
        this.validationsTotal = Counter.builder("circleguard.gateway.validation.total")
                .description("Total gate validations")
                .register(registry);
        this.accessGranted = Counter.builder("circleguard.gateway.access.granted")
                .description("Access granted to campus")
                .register(registry);
        this.accessDenied = Counter.builder("circleguard.gateway.access.denied")
                .description("Access denied to campus")
                .register(registry);
        this.invalidTokens = Counter.builder("circleguard.gateway.token.invalid")
                .description("Invalid tokens received")
                .register(registry);
    }
}
