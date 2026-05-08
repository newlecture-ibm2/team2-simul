package com.simul.user.adapter.in.web;

import com.simul.user.domain.model.Gender;

/**
 * 프로필 수정 요청 DTO
 */
public record UpdateUserRequest(
    String nickname,
    String name,
    Gender gender,
    String bio,
    String profileImageUrl
) {}
