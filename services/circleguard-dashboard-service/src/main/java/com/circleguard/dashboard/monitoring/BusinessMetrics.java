package com.circleguard.dashboard.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter trendsQueried;
    public final Counter healthBoardQueries;
    public final Counter summaryQueries;
    public final Counter departmentQueries;
    public final Counter timeSeriesQueries;
    public final Counter dashboardViews;

    public BusinessMetrics(MeterRegistry registry) {
        this.trendsQueried = Counter.builder("circleguard.dashboard.trends")
                .description("Entry trend queries")
                .register(registry);
        this.healthBoardQueries = Counter.builder("circleguard.dashboard.healthboard")
                .description("Health board queries")
                .register(registry);
        this.summaryQueries = Counter.builder("circleguard.dashboard.summary")
                .description("Campus health summary queries")
                .register(registry);
        this.departmentQueries = Counter.builder("circleguard.dashboard.department")
                .description("Department-level queries")
                .register(registry);
        this.timeSeriesQueries = Counter.builder("circleguard.dashboard.timeseries")
                .description("Time series queries")
                .register(registry);
        this.dashboardViews = Counter.builder("circleguard.dashboard.views")
                .description("Total dashboard views")
                .register(registry);
    }
}
