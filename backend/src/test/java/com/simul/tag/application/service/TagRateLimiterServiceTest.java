package com.simul.tag.application.service;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TagRateLimiterServiceTest {

    @Test
    @DisplayName("1분당 30회 요청 제한: 31번째 요청은 실패(false)해야 한다")
    void testRateLimit_30_Requests_Per_Minute() {
        // given
        TagRateLimiterService rateLimiterService = new TagRateLimiterService();
        UUID userId = UUID.randomUUID();
        Bucket bucket = rateLimiterService.resolveBucket(userId);

        // when & then
        // 1번부터 30번까지의 요청은 성공(true)해야 함
        for (int i = 1; i <= 30; i++) {
            assertThat(bucket.tryConsume(1))
                    .as(i + "번째 요청은 한도 내이므로 성공해야 합니다.")
                    .isTrue();
        }

        // 31번째 요청은 토큰 고갈로 실패(false)해야 함
        assertThat(bucket.tryConsume(1))
                .as("31번째 요청은 1분당 30회 제한에 걸려 실패해야 합니다.")
                .isFalse();
    }

    @Test
    @DisplayName("사용자별 버킷 격리: 다른 유저는 별도의 제한 한도를 가진다")
    void testRateLimit_Isolated_By_User() {
        // given
        TagRateLimiterService rateLimiterService = new TagRateLimiterService();
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        Bucket bucketA = rateLimiterService.resolveBucket(userA);
        Bucket bucketB = rateLimiterService.resolveBucket(userB);

        // when
        // User A가 30번 모두 소진
        for (int i = 1; i <= 30; i++) {
            bucketA.tryConsume(1);
        }

        // then
        // User A의 31번째 요청은 실패해야 함
        assertThat(bucketA.tryConsume(1)).isFalse();

        // 하지만 User B는 자신의 할당량이 그대로 남아있으므로 첫 요청이 성공해야 함
        assertThat(bucketB.tryConsume(1)).isTrue();
    }
}
