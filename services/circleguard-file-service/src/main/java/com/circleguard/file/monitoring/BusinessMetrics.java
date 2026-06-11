package com.circleguard.file.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter filesUploaded;
    public final Counter uploadErrors;

    public BusinessMetrics(MeterRegistry registry) {
        this.filesUploaded = Counter.builder("circleguard.file.uploaded")
                .description("Files uploaded")
                .register(registry);
        this.uploadErrors = Counter.builder("circleguard.file.upload.errors")
                .description("File upload errors")
                .register(registry);
    }
}
