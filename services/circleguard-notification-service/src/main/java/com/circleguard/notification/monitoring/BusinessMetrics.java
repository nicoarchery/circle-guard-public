package com.circleguard.notification.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    public final Counter emailsSent;
    public final Counter smsSent;
    public final Counter pushNotificationsSent;
    public final Counter statusChangeNotifications;
    public final Counter circleFenceNotifications;
    public final Counter priorityAlerts;
    public final Counter lmsSyncs;

    public BusinessMetrics(MeterRegistry registry) {
        this.emailsSent = Counter.builder("circleguard.notification.email.sent")
                .description("Emails sent")
                .register(registry);
        this.smsSent = Counter.builder("circleguard.notification.sms.sent")
                .description("SMS messages sent")
                .register(registry);
        this.pushNotificationsSent = Counter.builder("circleguard.notification.push.sent")
                .description("Push notifications sent")
                .register(registry);
        this.statusChangeNotifications = Counter.builder("circleguard.notification.statuschange")
                .description("Status change notifications")
                .register(registry);
        this.circleFenceNotifications = Counter.builder("circleguard.notification.circlefence")
                .description("Circle fence notifications")
                .register(registry);
        this.priorityAlerts = Counter.builder("circleguard.notification.priority.alert")
                .description("Priority alerts dispatched")
                .register(registry);
        this.lmsSyncs = Counter.builder("circleguard.notification.lms.sync")
                .description("LMS attendance syncs")
                .register(registry);
    }
}
