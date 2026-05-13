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
        // 1. 단기 제한 (Burst 방어): 1분에 30회
        Bandwidth minuteLimit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        
        // 2. 중기 제한 (매크로 방어): 1시간에 100회
        Bandwidth hourLimit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofHours(1)));
        
        // 3. 장기 제한 (일일 과금 캡): 1일에 300회
        Bandwidth dayLimit = Bandwidth.classic(300, Refill.greedy(300, Duration.ofDays(1)));

        return Bucket.builder()
                .addLimit(minuteLimit)
                .addLimit(hourLimit)
                .addLimit(dayLimit)
                .build();
    }
}
