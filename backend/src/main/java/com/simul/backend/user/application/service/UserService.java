package com.simul.backend.user.application.service;

import com.simul.backend.common.exception.BusinessException;
import com.simul.backend.common.exception.ErrorCode;
import com.simul.backend.user.application.dto.UserResponse;
import com.simul.backend.user.application.port.in.LoadUserUseCase;
import com.simul.backend.user.application.port.in.RegisterUserUseCase;
import com.simul.backend.user.application.port.out.UserPersistencePort;
import com.simul.backend.user.domain.model.Gender;
import com.simul.backend.user.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 사용자 서비스 (UseCase 구현체)
 * - LoadUserUseCase, RegisterUserUseCase 두 가지 유즈케이스를 구현
 * - UserPersistencePort(Output Port)를 통해 DB에 접근
 */
@Service
public class UserService implements LoadUserUseCase, RegisterUserUseCase {

    private final UserPersistencePort userPersistencePort;

    public UserService(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    /**
     * ID로 사용자 조회
     * - 없으면 USER_NOT_FOUND(ERR-003) 예외
     */
    @Override
    public UserResponse loadUser(UUID userId) {
        User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    /**
     * 소셜 로그인 기반 신규 사용자 등록
     * - Auth 도메인에서 호출
     */
    @Override
    public User registerSocialUser(String provider, String providerId, String nickname, String name, Gender gender) {
        User newUser = new User(provider, providerId, nickname, name, gender);
        return userPersistencePort.save(newUser);
    }
}
