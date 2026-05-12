package com.simul.user.application.port.out;

import com.simul.user.domain.model.Follow;
import java.util.UUID;

/**
 * 팔로우 저장소 포트 (Output Port)
 * - 실제 구현은 FollowPersistenceAdapter가 담당
 * - Service 계층은 이 인터페이스에만 의존 (헥사고날 원칙)
 */
public interface FollowPersistencePort {

    /**
     * 팔로우 저장
     */
    Follow save(Follow follow);

    /**
     * 팔로우 삭제
     */
    void delete(UUID followerId, UUID followingId);

    /**
     * 팔로우 존재 여부 확인
     */
    boolean exists(UUID followerId, UUID followingId);

    /**
     * 팔로워 수 조회 (나를 팔로우하는 사람 수)
     */
    long countFollowers(UUID userId);

    /**
     * 팔로잉 수 조회 (내가 팔로우하는 사람 수)
     */
    long countFollowings(UUID userId);

    /**
     * 팔로워 ID 목록 조회 (나를 팔로우하는 사람들의 ID)
     */
    java.util.List<UUID> getFollowerIds(UUID userId);

    /**
     * 팔로잉 ID 목록 조회 (내가 팔로우하는 사람들의 ID)
     */
    java.util.List<UUID> getFollowingIds(UUID userId);
}
