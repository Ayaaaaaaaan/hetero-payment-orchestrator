package com.ayaan.orchestrator.controller;

import com.ayaan.orchestrator.model.PaymentRequest;
import com.ayaan.orchestrator.model.PaymentResponse;
import com.ayaan.orchestrator.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final OrchestratorService orchestratorService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        log.info("Received payment request for amount: {}", request.getAmount());
        PaymentResponse response = orchestratorService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}