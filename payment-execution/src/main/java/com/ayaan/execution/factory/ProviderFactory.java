package com.ayaan.execution.factory;

import com.ayaan.execution.provider.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProviderFactory {

    private final List<PaymentProvider> providers;

    private Map<String, PaymentProvider> providerMap;

    public PaymentProvider getProvider(String providerName) {
        if (providerMap == null) {
            providerMap = providers.stream()
                    .collect(Collectors.toMap(
                            PaymentProvider::getProviderName,
                            Function.identity()
                    ));
        }

        PaymentProvider provider = providerMap.get(providerName.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerName);
        }
        return provider;
    }
}