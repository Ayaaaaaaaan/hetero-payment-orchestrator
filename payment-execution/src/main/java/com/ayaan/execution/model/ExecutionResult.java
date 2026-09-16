package com.ayaan.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    private String transactionId;
    private String providerTransactionId; // Provider's ID (e.g., "pay_xxx" for Razorpay)
    private String status; // "SUCCESS" or "FAILED"
    private String provider;
    private String message;
    private Long timestamp;
}