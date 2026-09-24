package com.ayaan.orchestrator.controller;

import com.ayaan.orchestrator.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @GetMapping("/exists/{key}")
    public Map<String, Object> exists(@PathVariable String key) {
        return Map.of("key", key, "exists", cacheService.exists(key));
    }

    @DeleteMapping("/{key}")
    public Map<String, Object> delete(@PathVariable String key) {
        cacheService.delete(key);
        return Map.of("key", key, "deleted", true);
    }
}