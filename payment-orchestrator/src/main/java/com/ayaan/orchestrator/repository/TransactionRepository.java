package com.ayaan.orchestrator.repository;

import com.ayaan.orchestrator.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByUserId(String userId);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);
}