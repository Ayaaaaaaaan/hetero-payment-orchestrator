package com.ayaan.orchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.ttl.rules}")
    private long rulesTtl;

    @Value("${cache.ttl.transaction}")
    private long transactionTtl;

    @Value("${cache.ttl.user-transactions}")
    private long userTransactionsTtl;

    // ============== GENERIC OPERATIONS ==============

    public void put(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached key={} ttl={}s", key, ttlSeconds);
        } catch (Exception e) {
            log.warn("Redis PUT failed for key={}: {}", key, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            return (T) value;
        } catch (Exception e) {
            log.warn("Redis GET failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cache key={}", key);
        } catch (Exception e) {
            log.warn("Redis DELETE failed for key={}: {}", key, e.getMessage());
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    // ============== TYPED HELPERS ==============

    public void cacheRules(String rules) {
        put("cache:provider:rules", rules, rulesTtl);
    }

    public String getCachedRules() {
        return get("cache:provider:rules", String.class);
    }

    public void cacheTransaction(String transactionId, Object transaction) {
        put("cache:txn:" + transactionId, transaction, transactionTtl);
    }

    public Object getCachedTransaction(String transactionId) {
        return get("cache:txn:" + transactionId, Object.class);
    }

    public void invalidateTransaction(String transactionId) {
        delete("cache:txn:" + transactionId);
    }

    public void cacheUserTransactions(String userId, Object transactions) {
        put("cache:user:" + userId + ":txns", transactions, userTransactionsTtl);
    }

    public Object getCachedUserTransactions(String userId) {
        return get("cache:user:" + userId + ":txns", Object.class);
    }

    public void invalidateUserTransactions(String userId) {
        delete("cache:user:" + userId + ":txns");
    }
}