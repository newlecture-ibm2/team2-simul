package com.simul.tag.application.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TagRateLimiterService {

    private final Map<UUID, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(UUID userId) {
        return cache.computeIfAbsent(userId, this::newBucket);
    }

    private Bucket newBucket(UUID userId) {
        // 최대 30개 토큰, 1분마다 30개씩 충전
        Refill refill = Refill.greedy(30, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(30, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
