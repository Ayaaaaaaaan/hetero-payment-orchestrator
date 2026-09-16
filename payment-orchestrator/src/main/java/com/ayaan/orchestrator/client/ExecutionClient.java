package com.ayaan.orchestrator.client;

import com.ayaan.orchestrator.model.ExecutionRequest;
import com.ayaan.orchestrator.model.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionClient {

    private final RestTemplate restTemplate;

    @Value("${execution.service.url}")
    private String executionServiceUrl;

    public ExecutionResult execute(ExecutionRequest request) {
        String url = executionServiceUrl + "/api/execution/execute";
        log.info("Calling Execution Service: {}", url);

        try {
            ResponseEntity<ExecutionResult> response =
                    restTemplate.postForEntity(url, request, ExecutionResult.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Execution Service call failed: {}", e.getMessage());
            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .status("FAILED")
                    .provider(request.getProvider())
                    .message("Execution Service unreachable: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
}