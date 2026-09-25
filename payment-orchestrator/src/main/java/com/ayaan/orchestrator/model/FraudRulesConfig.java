package com.ayaan.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudRulesConfig {
    private boolean globalEnabled;
    private List<FraudRule> rules;
}