package com.ayaan.orchestrator.controller;

import com.ayaan.orchestrator.service.ProviderSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ProviderSelectionService providerSelectionService;

    @GetMapping("/rules")
    public Map<String, String> rules() {
        return Map.of("provider-rules", providerSelectionService.getRuleSummary());
    }
}