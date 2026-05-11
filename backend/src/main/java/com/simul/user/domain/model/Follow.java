package com.simul.user.domain.model;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 팔로우 도메인 모델
 * - 순수 Java 객체 (JPA 종속성 제거)
 * - follower가 following을 팔로우
 */
@Getter
@Builder
public class Follow {

    private UUID followId;
    private UUID followerId;
    private UUID followingId;
    private LocalDateTime createdAt;

    /**
     * 자기 자신을 팔로우하는지 검증
     */
    public static void validateNotSelfFollow(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.SELF_FOLLOW);
        }
    }
}

