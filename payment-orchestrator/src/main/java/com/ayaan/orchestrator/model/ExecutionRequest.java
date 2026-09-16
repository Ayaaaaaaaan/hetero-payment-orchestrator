package com.ayaan.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {
    private String transactionId;
    private Double amount;
    private String currency;
    private String provider;
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;
    private String customerEmail;
    private String customerName;
}
