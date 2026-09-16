package com.ayaan.execution.provider;

import com.ayaan.execution.model.ExecutionRequest;
import com.ayaan.execution.model.ExecutionResult;

public interface PaymentProvider {
    ExecutionResult processPayment(ExecutionRequest request);

    String getProviderName();
}