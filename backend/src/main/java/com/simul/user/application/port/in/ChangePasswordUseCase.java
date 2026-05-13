package com.simul.user.application.port.in;

import java.util.UUID;

/**
 * 비밀번호 변경 유스케이스
 */
public interface ChangePasswordUseCase {
    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param oldPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     */
    void changePassword(UUID userId, String oldPassword, String newPassword);
}
