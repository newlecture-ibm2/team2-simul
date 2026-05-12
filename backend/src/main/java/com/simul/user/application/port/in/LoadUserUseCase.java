package com.simul.user.application.port.in;

import com.simul.user.application.dto.UserProfileResponse;
import com.simul.user.application.dto.UserResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 사용자 조회 유즈케이스 (Input Port)
 * - Auth, Post 등 타 도메인에서 이 인터페이스로 사용자 정보에 접근
 */
public interface LoadUserUseCase {

    /**
     * ID로 사용자 조회
     * @throws BusinessException USER_NOT_FOUND
     */
    UserResponse loadUser(UUID userId);

    /**
     * 여러 사용자 ID로 사용자 조회 맵 반환
     */
    Map<UUID, UserResponse> loadUsers(List<UUID> userIds);

    /**
     * @param targetUserId 조회할 사용자 ID
     * @param currentUserId 현재 로그인 사용자 ID (null이면 비로그인)
     * @param postCount 게시물 수
     */
    UserProfileResponse loadUserProfile(UUID targetUserId, UUID currentUserId, long postCount);
}
