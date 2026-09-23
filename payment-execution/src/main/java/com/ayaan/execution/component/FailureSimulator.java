package com.ayaan.execution.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FailureSimulator {

    private final Set<String> failedProviders = ConcurrentHashMap.newKeySet();

    public void enable(String provider) {
        failedProviders.add(provider.toLowerCase());
        log.warn("Failure simulation ENABLED for: {}", provider);
    }

    public void disable(String provider) {
        failedProviders.remove(provider.toLowerCase());
        log.warn("Failure simulation DISABLED for: {}", provider);
    }

    public boolean shouldFail(String provider) {
        return failedProviders.contains(provider.toLowerCase());
    }

    public Set<String> getActive() {
        return Set.copyOf(failedProviders);
    }
}