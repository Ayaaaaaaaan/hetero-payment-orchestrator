package com.ayaan.orchestrator.controller;

import com.ayaan.orchestrator.model.FraudRulesConfig;
import com.ayaan.orchestrator.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    @GetMapping("/rules")
    public FraudRulesConfig getRules() {
        return fraudDetectionService.getRules();
    }

    @PostMapping("/reload")
    public Map<String, Object> reload() {
        fraudDetectionService.reloadRules();
        return Map.of(
                "reloaded", true,
                "rulesCount", fraudDetectionService.getRules().getRules().size(),
                "globalEnabled", fraudDetectionService.getRules().isGlobalEnabled()
        );
    }
}