package com.simul.backend.auth.adapter.in.web;

import com.simul.backend.auth.application.dto.SocialLoginCommand;
import com.simul.backend.auth.application.dto.TokenResponse;
import com.simul.backend.auth.application.port.in.RefreshTokenUseCase;
import com.simul.backend.auth.application.port.in.SocialLoginUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 API 컨트롤러
 *
 * 엔드포인트:
 * - POST /api/auth/social   → 소셜 로그인 (카카오/네이버/구글)
 * - POST /api/auth/refresh  → Access Token 갱신
 * - DELETE /api/auth/logout  → 로그아웃
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SocialLoginUseCase socialLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(
        SocialLoginUseCase socialLoginUseCase,
        RefreshTokenUseCase refreshTokenUseCase
    ) {
        this.socialLoginUseCase = socialLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    /**
     * 소셜 로그인
     * POST /api/auth/social
     *
     * Request Body:
     * {
     *   "provider": "kakao",       // kakao | naver | google
     *   "code": "인가코드",
     *   "redirectUri": "http://localhost:3000/callback"
     * }
     *
     * Response:
     * {
     *   "accessToken": "jwt...",
     *   "refreshToken": "jwt...",
     *   "isNewUser": false
     * }
     */
    @PostMapping("/social")
    public ResponseEntity<TokenResponse> socialLogin(
        @RequestBody SocialLoginCommand command
    ) {
        TokenResponse tokenResponse = socialLoginUseCase.socialLogin(command);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     *
     * Request Body:
     * { "refreshToken": "jwt..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");
        TokenResponse tokenResponse = refreshTokenUseCase.refreshToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 로그아웃
     * DELETE /api/auth/logout
     *
     * MVP에서는 Stateless JWT이므로 서버 측 별도 처리 없음
     * 프론트엔드에서 httpOnly 쿠키 삭제로 처리
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWT: 서버 측 별도 처리 없음
        // 향후 토큰 블랙리스트 구현 시 여기에 로직 추가
        return ResponseEntity.noContent().build();
    }
}
