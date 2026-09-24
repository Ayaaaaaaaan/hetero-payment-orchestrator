package com.ayaan.orchestrator.controller;

import com.ayaan.orchestrator.entity.Transaction;
import com.ayaan.orchestrator.repository.TransactionRepository;
import com.ayaan.orchestrator.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CacheService cacheService;

    @GetMapping
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getByTransactionId(@PathVariable String transactionId) {
        // Try cache first
        Object cached = cacheService.getCachedTransaction(transactionId);
        if (cached != null) {
            return ResponseEntity.ok((Transaction) cached);
        }

        // Cache miss → DB → cache it
        return transactionRepository.findByTransactionId(transactionId)
                .map(txn -> {
                    cacheService.cacheTransaction(transactionId, txn);
                    return ResponseEntity.ok(txn);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Transaction> getByUser(@PathVariable String userId) {
        // Try cache
        Object cached = cacheService.getCachedUserTransactions(userId);
        if (cached != null) {
            return (List<Transaction>) cached;
        }

        // Cache miss
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        cacheService.cacheUserTransactions(userId, transactions);
        return transactions;
    }

    @GetMapping("/status/{status}")
    public List<Transaction> getByStatus(@PathVariable String status) {
        return transactionRepository.findByStatus(status.toUpperCase());
    }
}