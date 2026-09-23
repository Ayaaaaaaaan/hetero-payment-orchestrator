package com.ayaan.execution.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    /**
     * Simulates provider failure for demo purposes.
     * Usage: GET /api/test/fail?provider=razorpay
     * This bypasses the circuit breaker so we can test the fallback.
     */
    @GetMapping("/fail")
    public String simulateFailure(@RequestParam String provider) {
        log.error("SIMULATED FAILURE for provider: {}", provider);
        throw new RuntimeException("Simulated failure for " + provider);
    }
}