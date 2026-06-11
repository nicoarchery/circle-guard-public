package com.circleguard.identity.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter identitiesMapped;
    public final Counter visitorsRegistered;
    public final Counter identityLookups;
    public final Counter lookupSuccesses;
    public final Counter lookupFailures;

    public BusinessMetrics(MeterRegistry registry) {
        this.identitiesMapped = Counter.builder("circleguard.identity.map")
                .description("Identities mapped to anonymous IDs")
                .register(registry);
        this.visitorsRegistered = Counter.builder("circleguard.identity.visitor.registered")
                .description("Visitors registered")
                .register(registry);
        this.identityLookups = Counter.builder("circleguard.identity.lookup.total")
                .description("Total identity lookups")
                .register(registry);
        this.lookupSuccesses = Counter.builder("circleguard.identity.lookup.success")
                .description("Successful identity lookups")
                .register(registry);
        this.lookupFailures = Counter.builder("circleguard.identity.lookup.failure")
                .description("Failed identity lookups")
                .register(registry);
    }
}
