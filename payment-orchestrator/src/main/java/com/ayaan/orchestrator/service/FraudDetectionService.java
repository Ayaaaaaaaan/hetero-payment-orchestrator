package com.ayaan.orchestrator.service;

import com.ayaan.orchestrator.model.*;
import com.ayaan.orchestrator.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fraud.rules.file}")
    private String rulesFilePath;

    private FraudRulesConfig rulesConfig;

    @PostConstruct
    public void loadRulesOnStartup() {
        reloadRules();
    }

    public void reloadRules() {
        try {
            File file = new File(rulesFilePath);
            if (!file.exists()) {
                log.error("Fraud rules file not found at: {}", file.getAbsolutePath());
                rulesConfig = new FraudRulesConfig(true, List.of());
                return;
            }
            rulesConfig = objectMapper.readValue(file, FraudRulesConfig.class);
            log.info("Loaded {} fraud rules (globalEnabled={})",
                    rulesConfig.getRules().size(), rulesConfig.isGlobalEnabled());
        } catch (Exception e) {
            log.error("Failed to load fraud rules: {}", e.getMessage());
            rulesConfig = new FraudRulesConfig(true, List.of());
        }
    }

    public FraudRulesConfig getRules() {
        return rulesConfig;
    }

    /**
     * Evaluates all rules in priority order.
     * Returns first matching rule as a block decision.
     */
    public FraudCheckResult check(PaymentRequest request) {
        if (rulesConfig == null || !rulesConfig.isGlobalEnabled()) {
            return FraudCheckResult.builder().blocked(false).build();
        }

        List<FraudRule> enabledRules = rulesConfig.getRules().stream()
                .filter(FraudRule::isEnabled)
                .sorted(Comparator.comparingInt(FraudRule::getPriority))
                .toList();

        for (FraudRule rule : enabledRules) {
            if (matches(rule, request)) {
                log.warn("Fraud detected by rule '{}' for user {}: {}",
                        rule.getId(), request.getUserId(), rule.getMessage());
                return FraudCheckResult.builder()
                        .blocked(true)
                        .ruleId(rule.getId())
                        .ruleName(rule.getName())
                        .reason(rule.getMessage())
                        .build();
            }
        }
        return FraudCheckResult.builder().blocked(false).build();
    }

    private boolean matches(FraudRule rule, PaymentRequest request) {
        return switch (rule.getType()) {
            case "MAX_AMOUNT" -> matchMaxAmount(rule, request);
            case "DAILY_LIMIT" -> matchDailyLimit(rule, request);
            case "FREQUENCY" -> matchFrequency(rule, request);
            case "NEW_USER_HIGH_AMOUNT" -> matchNewUserHighAmount(rule, request);
            default -> false;
        };
    }

    // ============ RULE IMPLEMENTATIONS ============

    private boolean matchMaxAmount(FraudRule rule, PaymentRequest request) {
        Double max = getDouble(rule, "maxAmount");
        return max != null && request.getAmount() > max;
    }

    private boolean matchDailyLimit(FraudRule rule, PaymentRequest request) {
        Double limit = getDouble(rule, "dailyLimit");
        if (limit == null) return false;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        Double todaySum = transactionRepository.sumAmountSince(request.getUserId(), startOfDay);
        if (todaySum == null) todaySum = 0.0;

        return (todaySum + request.getAmount()) > limit;
    }

    private boolean matchFrequency(FraudRule rule, PaymentRequest request) {
        Integer maxTxns = getInt(rule, "maxTransactions");
        Integer windowMinutes = getInt(rule, "windowMinutes");
        if (maxTxns == null || windowMinutes == null) return false;

        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);
        long count = transactionRepository.countByUserIdSince(request.getUserId(), since);
        return count >= maxTxns;
    }

    private boolean matchNewUserHighAmount(FraudRule rule, PaymentRequest request) {
        Double threshold = getDouble(rule, "amountThreshold");
        if (threshold == null) return false;

        long userTxns = transactionRepository.countByUserId(request.getUserId());
        return userTxns == 0 && request.getAmount() > threshold;
    }

    // ============ PARAM HELPERS ============

    private Double getDouble(FraudRule rule, String key) {
        Object v = rule.getParams().get(key);
        if (v == null) return null;
        return ((Number) v).doubleValue();
    }

    private Integer getInt(FraudRule rule, String key) {
        Object v = rule.getParams().get(key);
        if (v == null) return null;
        return ((Number) v).intValue();
    }
}