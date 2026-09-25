package com.ayaan.orchestrator.service;

import com.ayaan.orchestrator.client.ExecutionClient;
import com.ayaan.orchestrator.entity.Transaction;
import com.ayaan.orchestrator.model.*;
import com.ayaan.orchestrator.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final ExecutionClient executionClient;
    private final ProviderSelectionService providerSelectionService;
    private final TransactionRepository transactionRepository;
    private final CacheService cacheService;
    private final FraudDetectionService fraudDetectionService;

    public PaymentResponse processPayment(PaymentRequest request) {

        // Step 1: Generate transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Processing payment: {}", transactionId);

        // Step 2: Select provider
        String selectedProvider = providerSelectionService.selectProvider(request);
        log.info("Selected provider: {}", selectedProvider);

        // Step 2.5: Fraud check
        FraudCheckResult fraudResult = fraudDetectionService.check(request);
        if (fraudResult.isBlocked()) {
        log.warn("Payment blocked by fraud rule: {} - {}", fraudResult.getRuleId(), fraudResult.getReason());
        
        Transaction blocked = Transaction.builder()
                .transactionId(transactionId)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .provider(selectedProvider)
                .status("BLOCKED")
                .message(fraudResult.getReason())
                .blockedReason(fraudResult.getRuleName())
                .build();
        transactionRepository.save(blocked);
        cacheService.invalidateUserTransactions(request.getUserId());

        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status("BLOCKED")
                .provider(selectedProvider)
                .message("Payment blocked: " + fraudResult.getReason())
                .timestamp(System.currentTimeMillis())
                .build();
        }

        // Step 3: Save transaction to DB with status=PROCESSING
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .provider(selectedProvider)
                .status("PROCESSING")
                .build();
        transactionRepository.save(transaction);
        cacheService.invalidateTransaction(transactionId);
        cacheService.invalidateUserTransactions(request.getUserId());
        log.info("Saved transaction {} with status PROCESSING", transactionId);

        // Step 4: Build ExecutionRequest
        ExecutionRequest execRequest = new ExecutionRequest(
                transactionId,
                request.getAmount(),
                request.getCurrency(),
                selectedProvider,
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear(),
                request.getCustomerEmail(),
                request.getCustomerName()
        );

        // Step 5: Call Execution Service
        ExecutionResult result = executionClient.execute(execRequest);

        // Step 6: Update transaction status in DB
        transaction.setStatus(result.getStatus());
        transaction.setProviderTransactionId(result.getProviderTransactionId());
        transaction.setMessage(result.getMessage());
        transactionRepository.save(transaction);
        log.info("Updated transaction {} → {}", transactionId, result.getStatus());
        cacheService.invalidateTransaction(transactionId);
        cacheService.invalidateUserTransactions(request.getUserId());

        // Step 7: Return response to client
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