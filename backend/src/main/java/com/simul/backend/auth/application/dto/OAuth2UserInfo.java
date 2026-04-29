package com.simul.backend.auth.application.dto;

import com.simul.backend.user.domain.model.Gender;

/**
 * OAuth2 제공자로부터 받은 사용자 정보
 * - 성별, 이름 필드 추가
 */
public record OAuth2UserInfo(
    String providerId,
    String nickname,
    String name,
    Gender gender,
    String profileImageUrl
) {}
