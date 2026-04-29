package com.simul.backend.auth.application.port.out;

import com.simul.backend.auth.application.dto.OAuth2UserInfo;

/**
 * OAuth2 제공자 포트 (Output Port)
 * - 각 소셜 로그인 제공자(카카오/네이버/구글) Adapter가 구현
 * - 인가 코드를 받아 사용자 정보를 반환하는 공통 인터페이스
 */
public interface OAuth2ProviderPort {

    /**
     * 이 Adapter가 담당하는 제공자 이름
     * @return "kakao", "naver", "google"
     */
    String getProvider();

    /**
     * OAuth2 인가 코드로 사용자 정보 조회
     * 내부적으로:
     * 1. 인가 코드 → Access Token 교환
     * 2. Access Token → 사용자 프로필 조회
     *
     * @param code 인가 코드 (프론트엔드에서 전달)
     * @param redirectUri 프론트엔드 콜백 URL
     * @return 정규화된 사용자 정보
     */
    OAuth2UserInfo getUserInfo(String code, String redirectUri);
}
