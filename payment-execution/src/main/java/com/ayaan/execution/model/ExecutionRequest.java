package com.ayaan.execution.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {
    private String transactionId;
    private Double amount;
    private String currency;      // "INR", "USD"
    private String provider;      // "razorpay", "stripe", "paypal"
    private String cardNumber;    // Test card number
    private String cvv;
    private String expiryMonth;
    private String expiryYear;
    private String customerEmail;
    private String customerName;
}