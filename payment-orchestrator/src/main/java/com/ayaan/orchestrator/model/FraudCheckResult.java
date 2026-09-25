package com.ayaan.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckResult {
    private boolean blocked;
    private String ruleId;       // Which rule blocked it (null if not blocked)
    private String ruleName;
    private String reason;       // The rule's message
}