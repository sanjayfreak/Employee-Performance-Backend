package com.taskmanager.employee_performance_monitoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Prints, at startup, which Mongo URI the running jar actually resolved.
 * Credentials are masked. Remove once the deployment is stable.
 */
@Component
public class StartupConfigLogger {

    public StartupConfigLogger(@Value("${spring.data.mongodb.uri:NOT_SET}") String uri) {
        String safe = uri.replaceAll("://[^@/]+@", "://***:***@");
        System.out.println(">>>>> MONGO URI IN USE: " + safe);
    }
}
