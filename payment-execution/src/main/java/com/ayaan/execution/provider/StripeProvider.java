package com.ayaan.execution.provider;

import com.ayaan.execution.model.ExecutionRequest;
import com.ayaan.execution.model.ExecutionResult;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StripeProvider implements PaymentProvider {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Override
    public ExecutionResult processPayment(ExecutionRequest request) {
        try {
            Stripe.apiKey = stripeApiKey;

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long)(request.getAmount() * 100)) // In cents
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setDescription("Payment for transaction: " + request.getTransactionId())
                    .setReceiptEmail(request.getCustomerEmail())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Stripe PaymentIntent created: {}", intent.getId());

            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .providerTransactionId(intent.getId())
                    .status("SUCCESS")
                    .provider("stripe")
                    .message("Stripe PaymentIntent created: " + intent.getStatus())
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Stripe error: {}", e.getMessage());
            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .status("FAILED")
                    .provider("stripe")
                    .message("Stripe error: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    @Override
    public String getProviderName() {
        return "stripe";
    }
}