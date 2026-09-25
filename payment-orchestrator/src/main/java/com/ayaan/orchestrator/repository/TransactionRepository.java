package com.ayaan.orchestrator.repository;

import com.ayaan.orchestrator.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByStatus(String status);
    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserId(String userId);

    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.createdAt >= :since")
    long countByUserIdSince(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.createdAt >= :since " +
           "AND t.status IN ('SUCCESS', 'PROCESSING')")
    Double sumAmountSince(@Param("userId") String userId, @Param("since") LocalDateTime since);
}