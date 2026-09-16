package com.ayaan.execution.controller;

import com.ayaan.execution.factory.ProviderFactory;
import com.ayaan.execution.model.ExecutionRequest;
import com.ayaan.execution.model.ExecutionResult;
import com.ayaan.execution.provider.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
public class ExecutionController {

    private final ProviderFactory providerFactory;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResult> execute(@RequestBody ExecutionRequest request) {
        log.info("Executing payment via provider: {}", request.getProvider());

        try {
            PaymentProvider provider = providerFactory.getProvider(request.getProvider());
            ExecutionResult result = provider.processPayment(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ExecutionResult.builder()
                            .transactionId(request.getTransactionId())
                            .status("FAILED")
                            .provider(request.getProvider())
                            .message(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        } catch (RuntimeException e) {
            log.error("Payment execution failed for transaction {}", request.getTransactionId(), e);
            return ResponseEntity.internalServerError().body(
                    ExecutionResult.builder()
                            .transactionId(request.getTransactionId())
                            .status("FAILED")
                            .provider(request.getProvider())
                            .message("Payment execution failed: " + e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }
}