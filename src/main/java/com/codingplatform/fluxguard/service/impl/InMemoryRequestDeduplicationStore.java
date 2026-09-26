package com.codingplatform.fluxguard.service.impl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.codingplatform.fluxguard.service.RequestDeduplicationStore;

@Component
public class InMemoryRequestDeduplicationStore implements RequestDeduplicationStore {

    private final Map<String, Long> claims = new ConcurrentHashMap<>();

    @Override
    public boolean claim(String key, Duration protectionWindow) {
        long now = System.nanoTime();
        long expiresAt = now + protectionWindow.toNanos();
        if (claims.size() > 5000) {
            claims.entrySet().removeIf(entry -> now - entry.getValue() >= 0);
        }
        Long previous = claims.putIfAbsent(key, expiresAt);
        if (previous == null) {
            return true;
        }
        if (now - previous >= 0) {
            return claims.replace(key, previous, expiresAt);
        }
        return false;
    }
}