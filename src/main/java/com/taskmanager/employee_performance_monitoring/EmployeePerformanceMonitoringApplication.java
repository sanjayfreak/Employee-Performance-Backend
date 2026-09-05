package com.taskmanager.employee_performance_monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CORS lives in config/CorsConfig only. There used to be three separate
 * CORS definitions (here, in CorsConfig, and @CrossOrigin on two
 * controllers), which could emit duplicate headers and break requests.
 */
@SpringBootApplication
public class EmployeePerformanceMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeePerformanceMonitoringApplication.class, args);
    }
}
