package com.ayaan.orchestrator.service;

import com.ayaan.orchestrator.model.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderSelectionService {

    private final CacheService cacheService;

    @Value("${payment.provider.default}")
    private String defaultProvider;

    @Value("${payment.rules.threshold-low}")
    private Double thresholdLow;

    @Value("${payment.rules.threshold-high}")
    private Double thresholdHigh;

    @Value("${payment.rules.provider-low}")
    private String providerLow;

    @Value("${payment.rules.provider-medium}")
    private String providerMedium;

    @Value("${payment.rules.provider-high}")
    private String providerHigh;

    public String selectProvider(PaymentRequest request) {
        String requested = request.getProvider();

        if (requested != null && !requested.isBlank()
                && !requested.equalsIgnoreCase("auto")) {
            String chosen = requested.toLowerCase();

            if ("paypal".equals(chosen) && "INR".equalsIgnoreCase(request.getCurrency())) {
                log.warn("PayPal does not support INR. Falling back to {}", providerMedium);
                return providerMedium.toLowerCase();
            }
            log.info("Provider explicitly chosen by client: {}", chosen);
            return chosen;
        }

        if ("auto".equalsIgnoreCase(requested)) {
            String auto = autoSelect(request.getAmount(), request.getCurrency());
            log.info("Auto-selected provider '{}' for amount {} {}", auto, request.getAmount(), request.getCurrency());
            return auto;
        }

        log.info("No provider specified, using default: {}", defaultProvider);
        return defaultProvider.toLowerCase();
    }

    private String autoSelect(Double amount, String currency) {
        if (amount == null) return defaultProvider;
        boolean isInr = "INR".equalsIgnoreCase(currency);

        if (amount < thresholdLow) return providerLow.toLowerCase();
        if (amount < thresholdHigh) return providerMedium.toLowerCase();
        return isInr ? providerMedium.toLowerCase() : providerHigh.toLowerCase();
    }

    public String getRuleSummary() {
        // Try cache first
        String cached = cacheService.getCachedRules();
        if (cached != null) {
            log.info("Rules summary served from CACHE");
            return cached;
        }

        // Cache miss → compute → cache it
        String summary = String.format(
            "Default: %s | INR: <%s→%s, <%s→%s, ≥%s→%s | Non-INR: <10k→%s, <1L→%s, ≥1L→%s",
            defaultProvider,
            thresholdLow, providerLow,
            thresholdHigh, providerMedium,
            thresholdHigh, providerMedium,
            providerLow, providerMedium, providerHigh
        );
        log.info("Rules summary computed and cached");
        cacheService.cacheRules(summary);
        return summary;
    }
}