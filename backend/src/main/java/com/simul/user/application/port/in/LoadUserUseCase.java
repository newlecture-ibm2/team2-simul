package com.simul.user.application.port.in;

import com.simul.user.application.dto.UserResponse;
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
}
