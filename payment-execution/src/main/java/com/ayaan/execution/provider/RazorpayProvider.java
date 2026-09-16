package com.ayaan.execution.provider;

import com.ayaan.execution.model.ExecutionRequest;
import com.ayaan.execution.model.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class RazorpayProvider implements PaymentProvider {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ExecutionResult processPayment(ExecutionRequest request) {
        try {
            JSONObject body = new JSONObject();
            body.put("amount", (int) (request.getAmount() * 100));
            body.put("currency", request.getCurrency());
            body.put("receipt", UUID.randomUUID().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes());
            headers.set("Authorization", "Basic " + auth);

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/orders", entity, String.class);

            JSONObject json = new JSONObject(response.getBody());
            String orderId = json.getString("id");

            log.info("Razorpay order created: {}", orderId);

            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .providerTransactionId(orderId)
                    .status("SUCCESS")
                    .provider("razorpay")
                    .message("Razorpay order created successfully")
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Razorpay error: {}", e.getMessage());
            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .status("FAILED")
                    .provider("razorpay")
                    .message("Razorpay error: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    @Override
    public String getProviderName() {
        return "razorpay";
    }
}