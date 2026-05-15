package com.simul.user.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.user.application.port.in.WithdrawUserUseCase;
import com.simul.user.application.port.out.UserPersistencePort;
import com.simul.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원 탈퇴 서비스
 * @Primary를 사용하여 기존 UserService의 withdraw보다 우선순위를 가집니다.
 */
@Primary
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawUserService implements WithdrawUserUseCase {

    private final UserPersistencePort userPersistencePort;

    @Override
    public void withdraw(UUID userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 도메인 모델의 withdraw 호출 (isActive = false, deletedAt 설정)
        user.withdraw();
        
        userPersistencePort.save(user);
    }
}
