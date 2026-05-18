package com.simul.user.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.user.application.dto.FollowCountResponse;
import com.simul.user.application.port.in.FollowUserUseCase;
import com.simul.user.application.port.in.LoadFollowUseCase;
import com.simul.user.application.port.in.UnfollowUserUseCase;
import com.simul.user.application.port.out.FollowPersistencePort;
import com.simul.user.application.port.out.UserPersistencePort;
import com.simul.user.domain.model.Follow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 팔로우 서비스 (UseCase 구현체)
 * - FollowUserUseCase, UnfollowUserUseCase, LoadFollowUseCase 구현
 * - FollowPersistencePort(Output Port)를 통해 DB에 접근
 */
@Service
@Transactional
public class FollowService implements FollowUserUseCase, UnfollowUserUseCase, LoadFollowUseCase {

    private final FollowPersistencePort followPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public FollowService(FollowPersistencePort followPersistencePort, UserPersistencePort userPersistencePort) {
        this.followPersistencePort = followPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    /**
     * 팔로우
     * - 자기 자신 팔로우 불가
     * - 대상 사용자 존재 여부 확인
     * - 이미 팔로우 중이면 무시 (멱등성)
     */
    @Override
    public void follow(UUID followerId, UUID followingId) {
        // 자기 자신 팔로우 검증
        Follow.validateNotSelfFollow(followerId, followingId);

        // 대상 사용자 존재 여부 확인
        userPersistencePort.findById(followingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 팔로우 중이면 무시 (멱등성 보장)
        if (followPersistencePort.exists(followerId, followingId)) {
            return;
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

        followPersistencePort.save(follow);
    }

    /**
     * 언팔로우
     * - 팔로우 관계가 없으면 무시 (멱등성)
     */
    @Override
    public void unfollow(UUID followerId, UUID followingId) {
        // 자기 자신 언팔로우 시도 방지
        Follow.validateNotSelfFollow(followerId, followingId);

        // 팔로우 관계가 없어도 에러 없이 성공 (멱등성)
        if (!followPersistencePort.exists(followerId, followingId)) {
            return;
        }

        followPersistencePort.delete(followerId, followingId);
    }

    /**
     * 팔로워/팔로잉 수 조회
     */
    @Override
    @Transactional(readOnly = true)
    public FollowCountResponse getFollowCounts(UUID userId) {
        long followerCount = followPersistencePort.countFollowers(userId);
        long followingCount = followPersistencePort.countFollowings(userId);
        return new FollowCountResponse(followerCount, followingCount);
    }

    /**
     * 팔로우 여부 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followPersistencePort.exists(followerId, followingId);
    }

    /**
     * 특정 사용자의 팔로워 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.simul.user.application.dto.FollowUserResponse> getFollowers(UUID currentUserId, UUID targetUserId) {
        java.util.List<UUID> followerIds = followPersistencePort.getFollowerIds(targetUserId);
        return mapToFollowUserResponseList(currentUserId, followerIds);
    }

    /**
     * 특정 사용자의 팔로잉 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.simul.user.application.dto.FollowUserResponse> getFollowings(UUID currentUserId, UUID targetUserId) {
        java.util.List<UUID> followingIds = followPersistencePort.getFollowingIds(targetUserId);
        return mapToFollowUserResponseList(currentUserId, followingIds);
    }

    private java.util.List<com.simul.user.application.dto.FollowUserResponse> mapToFollowUserResponseList(UUID currentUserId, java.util.List<UUID> userIds) {
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.List<com.simul.user.domain.model.User> users = userPersistencePort.findByIds(userIds);

        return users.stream().map(user -> {
            boolean isFollowing = false;
            if (currentUserId != null) {
                // 내 자신의 팔로우 상태는 항상 false (또는 UI에서 알아서 처리하지만 보통 본인 계정엔 팔로우 버튼 안 보임)
                if (currentUserId.equals(user.getUserId())) {
                    isFollowing = false;
                } else {
                    isFollowing = followPersistencePort.exists(currentUserId, user.getUserId());
                }
            }
            return new com.simul.user.application.dto.FollowUserResponse(
                    user.getUserId(),
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    isFollowing
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UUID> getFollowerIds(UUID userId) {
        return followPersistencePort.getFollowerIds(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UUID> getFollowingIds(UUID userId) {
        return followPersistencePort.getFollowingIds(userId);
    }
}
