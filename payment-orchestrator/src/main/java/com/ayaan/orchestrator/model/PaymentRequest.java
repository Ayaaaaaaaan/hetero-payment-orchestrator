package com.ayaan.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String userId;
    private Double amount;
    private String currency;
    private String provider;      // razorpay, stripe, paypal
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;
    private String customerEmail;
    private String customerName;
}