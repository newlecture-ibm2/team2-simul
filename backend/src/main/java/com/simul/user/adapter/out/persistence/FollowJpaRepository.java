package com.simul.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Follow JPA Repository
 * - Spring Data JPA 기반 자동 구현
 */
public interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, UUID> {

    /**
     * 팔로우 관계 존재 여부 확인
     */
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * 팔로우 관계 조회
     */
    Optional<FollowJpaEntity> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * 팔로우 관계 삭제
     */
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * 팔로워 수 (나를 팔로우하는 사람 수)
     */
    long countByFollowingId(UUID followingId);

    /**
     * 팔로잉 수 (내가 팔로우하는 사람 수)
     */
    long countByFollowerId(UUID followerId);

    /**
     * 나를 팔로우하는 사람들의 팔로우 엔티티 목록 조회
     */
    java.util.List<FollowJpaEntity> findAllByFollowingId(UUID followingId);

    /**
     * 내가 팔로우하는 사람들의 팔로우 엔티티 목록 조회
     */
    java.util.List<FollowJpaEntity> findAllByFollowerId(UUID followerId);
}
