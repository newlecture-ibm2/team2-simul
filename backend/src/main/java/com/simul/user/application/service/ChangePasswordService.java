package com.simul.user.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.user.application.port.in.ChangePasswordUseCase;
import com.simul.user.application.port.out.UserPersistencePort;
import com.simul.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 현재 비밀번호 검증
        // 소셜 로그인 사용자는 비밀번호가 없을 수 있음
        if (user.getPassword() == null) {
            // 소셜 사용자가 비밀번호를 처음 설정하는 경우라면 로직이 다를 수 있지만,
            // 여기서는 "비밀번호 변경"이므로 기존 비밀번호가 있어야 함.
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        // 2. 새 비밀번호 암호화 및 업데이트
        user.changePassword(passwordEncoder.encode(newPassword));

        // 3. 저장
        userPersistencePort.save(user);
    }
}
