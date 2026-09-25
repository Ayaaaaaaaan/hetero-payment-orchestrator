package com.ayaan.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {
    private String id;
    private String name;
    private String type;      // MAX_AMOUNT, DAILY_LIMIT, FREQUENCY, NEW_USER_HIGH_AMOUNT
    private boolean enabled;
    private int priority;
    private Map<String, Object> params;
    private String message;
}