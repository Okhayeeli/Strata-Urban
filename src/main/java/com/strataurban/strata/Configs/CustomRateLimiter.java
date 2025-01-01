package com.strataurban.strata.Configs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomRateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(CustomRateLimiter.class);
    private final int maxRequests;
    private final int timeWindowInSeconds;
    private final int cooldownPeriodSeconds;
    private final Map<String, RequestCount> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();

    public CustomRateLimiter(int maxRequests, int timeWindowInSeconds, int cooldownPeriodSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowInSeconds = timeWindowInSeconds;
        this.cooldownPeriodSeconds = cooldownPeriodSeconds;
        startCleanupThread();
    }

    public RateLimitResult tryAcquire(String key) {
        if (key == null || key.isEmpty()) {
            logger.warn("Attempt to acquire rate limit with null or empty key");
            return new RateLimitResult(false, 0);
        }

        // Check if user is in cooldown period
        Long blockedUntilTime = blockedUntil.get(key);
        if (blockedUntilTime != null) {
            long now = Instant.now().toEpochMilli();
            if (now < blockedUntilTime) {
                long waitTimeSeconds = (blockedUntilTime - now) / 1000;
                logger.debug("User {} is in cooldown period. Must wait {} seconds", key, waitTimeSeconds);
                return new RateLimitResult(false, waitTimeSeconds);
            } else {
                blockedUntil.remove(key);
                requestCounts.remove(key);
            }
        }

        long now = Instant.now().toEpochMilli();
        RequestCount count = requestCounts.compute(key, (k, v) -> {
            if (v == null || now - v.getStartTime() > timeWindowInSeconds * 1000) {
                return new RequestCount(now);
            }
            v.increment();
            return v;
        });

        boolean allowed = count.getCount() <= maxRequests;
        if (!allowed) {
            // Set cooldown period
            long blockedUntilTime1 = now + (cooldownPeriodSeconds * 1000);
            blockedUntil.put(key, blockedUntilTime1);
            logger.warn("Rate limit exceeded for key: {}. Blocked for {} seconds",
                    key, cooldownPeriodSeconds);
            return new RateLimitResult(false, cooldownPeriodSeconds);
        }
        return new RateLimitResult(true, 0);
    }

    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(timeWindowInSeconds * 1000);
                    cleanup();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private void cleanup() {
        long now = Instant.now().toEpochMilli();
        requestCounts.entrySet().removeIf(entry ->
                now - entry.getValue().getStartTime() > timeWindowInSeconds * 1000
        );
        blockedUntil.entrySet().removeIf(entry -> now > entry.getValue());
    }

    public static class RateLimitResult {
        private final boolean allowed;
        private final long waitTimeSeconds;

        public RateLimitResult(boolean allowed, long waitTimeSeconds) {
            this.allowed = allowed;
            this.waitTimeSeconds = waitTimeSeconds;
        }

        public boolean isAllowed() { return allowed; }
        public long getWaitTimeSeconds() { return waitTimeSeconds; }
    }

    private static class RequestCount {
        private final long startTime;
        private final AtomicInteger count;

        public RequestCount(long startTime) {
            this.startTime = startTime;
            this.count = new AtomicInteger(1);
        }

        public long getStartTime() { return startTime; }
        public int getCount() { return count.get(); }
        public void increment() { count.incrementAndGet(); }
    }
}