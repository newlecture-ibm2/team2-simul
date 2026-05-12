package com.simul.user.application.dto;

/**
 * 팔로워/팔로잉 수 응답 DTO
 */
public record FollowCountResponse(
    long followerCount,
    long followingCount
) {}
