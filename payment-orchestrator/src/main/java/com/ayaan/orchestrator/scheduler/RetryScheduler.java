package com.ayaan.orchestrator.scheduler;

import com.ayaan.orchestrator.client.ExecutionClient;
import com.ayaan.orchestrator.entity.Transaction;
import com.ayaan.orchestrator.model.ExecutionRequest;
import com.ayaan.orchestrator.model.ExecutionResult;
import com.ayaan.orchestrator.repository.TransactionRepository;
import com.ayaan.orchestrator.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryScheduler {

    private final TransactionRepository transactionRepository;
    private final ExecutionClient executionClient;
    private final CacheService cacheService;

    @Value("${retry.batch-size}")
    private int batchSize;

    @Value("${retry.max-attempts}")
    private int maxAttempts;

    /**
     * Runs every 10s (or configured interval).
     * 1. Expire old PENDING transactions
     * 2. Retry remaining PENDING transactions
     */
    @Scheduled(fixedDelayString = "${retry.scheduler.interval-ms}",
               initialDelayString = "${retry.scheduler.interval-ms}")
    public void retryPendingTransactions() {
        LocalDateTime now = LocalDateTime.now();

        // -------- Step A: Expire stale transactions --------
        List<Transaction> expired = transactionRepository
                .findByStatusAndExpiryTimeBefore("PENDING", now);

        for (Transaction t : expired) {
            t.setStatus("EXPIRED");
            t.setMessage("Payment expired: not completed within " +
                    "the allowed time window");
            transactionRepository.save(t);
            cacheService.invalidateTransaction(t.getTransactionId());
            cacheService.invalidateUserTransactions(t.getUserId());
            log.warn("Transaction {} EXPIRED", t.getTransactionId());
        }

        // -------- Step B: Retry remaining PENDING --------
        List<Transaction> pending = transactionRepository.findRetryablePending(now);

        int count = 0;
        for (Transaction t : pending) {
            if (count >= batchSize) break;
            count++;

            // Max retries reached → mark FAILED
            if (t.getRetryCount() >= maxAttempts) {
                t.setStatus("FAILED");
                t.setMessage("Max retry attempts (" + maxAttempts + ") reached");
                transactionRepository.save(t);
                cacheService.invalidateTransaction(t.getTransactionId());
                cacheService.invalidateUserTransactions(t.getUserId());
                log.warn("Transaction {} FAILED after {} retries",
                        t.getTransactionId(), t.getRetryCount());
                continue;
            }

            // Attempt retry
            retryOne(t);
        }

        if (!pending.isEmpty() || !expired.isEmpty()) {
            log.info("Retry cycle: {} expired, {} pending processed",
                    expired.size(), count);
        }
    }

    private void retryOne(Transaction t) {
        t.setRetryCount(t.getRetryCount() + 1);
        log.info("Retrying transaction {} (attempt {}/{})",
                t.getTransactionId(), t.getRetryCount(), maxAttempts);

        ExecutionRequest req = new ExecutionRequest(
                t.getTransactionId(),
                t.getAmount(),
                t.getCurrency(),
                t.getProvider(),
                null, null, null, null,  // card details not stored — see note below
                null, null
        );

        ExecutionResult result = executionClient.execute(req);

        t.setStatus(result.getStatus());
        t.setProviderTransactionId(result.getProviderTransactionId());
        t.setMessage(result.getMessage());
        transactionRepository.save(t);

        cacheService.invalidateTransaction(t.getTransactionId());
        cacheService.invalidateUserTransactions(t.getUserId());

        log.info("Retry result for {}: {}", t.getTransactionId(), result.getStatus());
    }
}