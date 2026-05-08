package com.simul.user.application.port.in;

import java.util.UUID;

/**
 * 회원 탈퇴 유즈케이스 (Input Port)
 */
public interface WithdrawUserUseCase {
    void withdraw(UUID userId);
}
