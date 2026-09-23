package com.ayaan.execution.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/circuit-breaker")
@RequiredArgsConstructor
public class CircuitBreakerController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/status")
    public Map<String, Map<String, Object>> allStatuses() {
        Map<String, Map<String, Object>> result = new HashMap<>();

        for (String name : new String[]{"razorpay", "stripe", "paypal"}) {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
            result.put(name, buildStatus(cb));
        }
        return result;
    }

    @GetMapping("/status/{name}")
    public Map<String, Object> oneStatus(@PathVariable String name) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
        return buildStatus(cb);
    }

    private Map<String, Object> buildStatus(CircuitBreaker cb) {
        Map<String, Object> status = new HashMap<>();
        status.put("state", cb.getState().toString());
        status.put("failureRate", cb.getMetrics().getFailureRate() + "%");
        status.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
        status.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
        status.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
        return status;
    }
}