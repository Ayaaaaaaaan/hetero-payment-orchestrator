package com.ayaan.orchestrator.controller;

import com.ayaan.orchestrator.entity.Transaction;
import com.ayaan.orchestrator.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getByTransactionId(@PathVariable String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Transaction> getByUser(@PathVariable String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/status/{status}")
    public List<Transaction> getByStatus(@PathVariable String status) {
        return transactionRepository.findByStatus(status.toUpperCase());
    }
}