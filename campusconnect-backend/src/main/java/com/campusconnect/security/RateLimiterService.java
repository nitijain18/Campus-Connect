package com.campusconnect.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> attempts = new ConcurrentHashMap<>();

    /**
     * Sliding-window rate limiter. Returns true if the action is allowed,
     * false if the caller has exceeded maxAttempts within windowSeconds.
     */
    public boolean tryConsume(String key, int maxAttempts, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        long windowStart = now - windowSeconds;

        CopyOnWriteArrayList<Long> timestamps = attempts.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        timestamps.removeIf(ts -> ts < windowStart);

        if (timestamps.size() >= maxAttempts) {
            return false;
        }

        timestamps.add(now);
        return true;
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}