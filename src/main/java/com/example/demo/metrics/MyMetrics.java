package com.example.demo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class MyMetrics {
    private final MeterRegistry registry;

    public MyMetrics(MeterRegistry registry) { this.registry = registry; }

    @PostConstruct
    public void init() {
        registry.counter("demo.requests.total");
    }

    public void incrementRequests() {
        registry.counter("demo.requests.total").increment();
    }
}
