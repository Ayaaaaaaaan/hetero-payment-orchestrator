package com.ayaan.orchestrator.service;

import com.ayaan.orchestrator.client.ExecutionClient;
import com.ayaan.orchestrator.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final ExecutionClient executionClient;
    private final ProviderSelectionService providerSelectionService;   // ← NEW

    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Generate transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Processing payment: {}", transactionId);

        // 2. Select provider (NEW LOGIC)
        String selectedProvider = providerSelectionService.selectProvider(request);
        log.info("Selected provider: {}", selectedProvider);

        // 3. Build ExecutionRequest
        ExecutionRequest execRequest = new ExecutionRequest(
                transactionId,
                request.getAmount(),
                request.getCurrency(),
                selectedProvider,                    // ← use selected, not requested
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear(),
                request.getCustomerEmail(),
                request.getCustomerName()
        );

        // 4. Call Execution Service
        ExecutionResult result = executionClient.execute(execRequest);

        // 5. Convert to PaymentResponse
        return PaymentResponse.builder()
                .transactionId(result.getTransactionId())
                .providerTransactionId(result.getProviderTransactionId())
                .status(result.getStatus())
                .provider(result.getProvider())
                .message(result.getMessage())
                .timestamp(result.getTimestamp())
                .build();
    }
}