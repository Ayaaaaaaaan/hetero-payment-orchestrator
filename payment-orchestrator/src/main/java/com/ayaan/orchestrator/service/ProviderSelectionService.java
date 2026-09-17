package com.ayaan.orchestrator.service;

import com.ayaan.orchestrator.model.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProviderSelectionService {

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

    /**
     * Selects the payment provider based on the request.
     *
     * Priority:
     * 1. If client specified a real provider (razorpay/stripe/paypal) -> use it
     * 2. If client sent "auto" -> use rule-based selection
     * 3. If client sent nothing -> use config default
     */
    public String selectProvider(PaymentRequest request) {
        String requested = request.getProvider();

        // Mode 1a: Client explicitly chose a provider
        if (requested != null && !requested.isBlank() && !requested.equalsIgnoreCase("auto")) {
            String chosen = requested.toLowerCase();
            
            // Safety: PayPal + INR combination is invalid
            if ("paypal".equals(chosen) && "INR".equalsIgnoreCase(request.getCurrency())) {
                log.warn("PayPal does not support INR. Falling back to {}", providerMedium);
                return providerMedium.toLowerCase();
            }
            
            log.info("Provider explicitly chosen by client: {}", chosen);
            return chosen;
        }

        // Mode 2: Client wants auto-selection based on amount
        if ("auto".equalsIgnoreCase(requested)) {
            String auto = autoSelect(request.getAmount(), request.getCurrency());
            log.info("Auto-selected provider '{}' for amount {} {}", auto, request.getAmount(), request.getCurrency());
            return auto;
        }

        // Mode 1b: Client sent nothing -> use default
        log.info("No provider specified, using default: {}", defaultProvider);
        return defaultProvider.toLowerCase();
    }

    /**
     * Rule-based selection based on amount.
     *   amount < thresholdLow   -> providerLow      (razorpay)
     *   amount < thresholdHigh  -> providerMedium   (stripe)
     *   amount >= thresholdHigh -> providerHigh     (paypal)
     */
    private String autoSelect(Double amount, String currency) {
    if (amount == null) {
        return defaultProvider;
    }

    // PayPal doesn't support INR — skip it for INR transactions
    boolean isInr = "INR".equalsIgnoreCase(currency);

    if (amount < thresholdLow) {
        return providerLow.toLowerCase();          // razorpay
    }
    if (amount < thresholdHigh) {
        return providerMedium.toLowerCase();       // stripe
    }
    // For large amounts, PayPal is preferred ONLY for non-INR
    return isInr ? providerMedium.toLowerCase() : providerHigh.toLowerCase();
}

    // Expose rules for a config endpoint (used in demo)
    public String getRuleSummary() {
    return String.format(
        "Default: %s | INR: <%s→%s, <%s→%s, ≥%s→%s | Non-INR: <10k→%s, <1L→%s, ≥1L→%s",
        defaultProvider,
        thresholdLow, providerLow,
        thresholdHigh, providerMedium,
        thresholdHigh, providerMedium,     // INR + large → still stripe
        providerLow, providerMedium, providerHigh  // Non-INR → normal
    );
}
}