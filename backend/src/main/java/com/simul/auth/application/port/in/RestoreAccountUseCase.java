package com.simul.auth.application.port.in;

import com.simul.auth.application.dto.TokenResponse;

/**
 * 계정 복구 유스케이스
 */
public interface RestoreAccountUseCase {
    
    /**
     * 탈퇴 유예 기간 내 계정 복구
     * @param provider 제공자 (email, kakao, ...)
     * @param providerId 제공자 고유 ID
     * @return 발급된 토큰 정보
     */
    TokenResponse restoreAccount(String provider, String providerId);
}
