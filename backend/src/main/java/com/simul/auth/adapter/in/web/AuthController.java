package com.simul.auth.adapter.in.web;

import com.simul.auth.application.dto.SocialLoginCommand;
import com.simul.auth.application.dto.TokenResponse;
import com.simul.auth.application.port.in.LogoutUseCase;
import com.simul.auth.application.port.in.RefreshTokenUseCase;
import com.simul.auth.application.port.in.SocialLoginUseCase;
import com.simul.auth.application.dto.EmailLoginCommand;
import com.simul.auth.application.dto.EmailSignupCommand;
import com.simul.auth.application.port.in.EmailAuthUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 API 컨트롤러
 *
 * 엔드포인트:
 * - POST /auth/social   → 소셜 로그인 (카카오/네이버/구글)
 * - POST /auth/refresh  → Access Token 갱신
 * - DELETE /auth/logout  → 로그아웃
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SocialLoginUseCase socialLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final EmailAuthUseCase emailAuthUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
        SocialLoginUseCase socialLoginUseCase,
        RefreshTokenUseCase refreshTokenUseCase,
        EmailAuthUseCase emailAuthUseCase,
        LogoutUseCase logoutUseCase
    ) {
        this.socialLoginUseCase = socialLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.emailAuthUseCase = emailAuthUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    /**
     * 소셜 로그인
     * POST /auth/social
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

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> emailSignup(@RequestBody EmailSignupCommand command) {
        TokenResponse response = emailAuthUseCase.emailSignup(
            command.email(), command.password(), command.name(), command.nickname(), command.gender()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/email")
    public ResponseEntity<TokenResponse> emailLogin(@RequestBody EmailLoginCommand command) {
        TokenResponse response = emailAuthUseCase.emailLogin(command.email(), command.password());
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 갱신
     * POST /auth/refresh
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
     * DELETE /auth/logout
     *
     * Redis에서 리프레시 토큰을 삭제하여 재사용을 차단
     * 프론트엔드에서는 추가로 httpOnly 쿠키(세션) 삭제 처리
     *
     * Request Body:
     * { "refreshToken": "jwt..." }
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null) {
            logoutUseCase.logout(refreshToken);
        }
        return ResponseEntity.noContent().build();
    }
}
