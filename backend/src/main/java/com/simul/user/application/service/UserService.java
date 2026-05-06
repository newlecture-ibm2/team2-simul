package com.simul.user.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import com.simul.user.application.port.in.RegisterUserUseCase;
import com.simul.user.application.port.out.UserPersistencePort;
import com.simul.user.domain.model.Gender;
import com.simul.user.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public Map<UUID, UserResponse> loadUsers(List<UUID> userIds) {
        List<User> users = userPersistencePort.findByIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getUserId, UserResponse::from));
    }

    /**
     * 소셜 로그인 기반 신규 사용자 등록
     * - Auth 도메인에서 호출
     */
    @Override
    public User registerSocialUser(String provider, String providerId, String nickname, String name, Gender gender) {
        User newUser = User.builder()
                .provider(provider)
                .providerId(providerId)
                .nickname(nickname)
                .name(name)
                .gender((gender != null) ? gender : Gender.UNKNOWN)
                .build();
        return userPersistencePort.save(newUser);
    }
}
