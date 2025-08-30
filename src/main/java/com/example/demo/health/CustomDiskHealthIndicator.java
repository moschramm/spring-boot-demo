package com.example.demo.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomDiskHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        long free = Runtime.getRuntime().freeMemory(); // only Demo
        if (free > 20_000_000) {
            return Health.up().withDetail("freeMemory", free).build();
        } else {
            return Health.down().withDetail("freeMemory", free).build();
        }
    }
}
