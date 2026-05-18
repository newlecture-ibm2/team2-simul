package com.simul.auth.adapter.in.web;

import com.simul.auth.application.dto.SocialLoginCommand;
import com.simul.auth.application.dto.TokenResponse;
import com.simul.auth.application.port.in.LogoutUseCase;
import com.simul.auth.application.port.in.RefreshTokenUseCase;
import com.simul.auth.application.port.in.SocialLoginUseCase;
import com.simul.auth.application.dto.EmailLoginCommand;
import com.simul.auth.application.dto.EmailSignupCommand;
import com.simul.auth.application.port.in.EmailAuthUseCase;
import com.simul.auth.application.port.in.FindPasswordUseCase;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
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
    private final com.simul.auth.application.port.in.RestoreAccountUseCase restoreAccountUseCase;
    private final FindPasswordUseCase findPasswordUseCase;

    public AuthController(
        SocialLoginUseCase socialLoginUseCase,
        RefreshTokenUseCase refreshTokenUseCase,
        EmailAuthUseCase emailAuthUseCase,
        LogoutUseCase logoutUseCase,
        com.simul.auth.application.port.in.RestoreAccountUseCase restoreAccountUseCase,
        FindPasswordUseCase findPasswordUseCase
    ) {
        this.socialLoginUseCase = socialLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.emailAuthUseCase = emailAuthUseCase;
        this.logoutUseCase = logoutUseCase;
        this.restoreAccountUseCase = restoreAccountUseCase;
        this.findPasswordUseCase = findPasswordUseCase;
    }

    /**
     * 계정 복구
     * POST /auth/restore
     */
    @PostMapping("/restore")
    public ResponseEntity<TokenResponse> restore(
        @RequestBody Map<String, String> request
    ) {
        String provider = request.get("provider");
        String providerId = request.get("providerId");
        TokenResponse response = restoreAccountUseCase.restoreAccount(provider, providerId);
        return ResponseEntity.ok(response);
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

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        emailAuthUseCase.verifyEmail(token);
        return ResponseEntity.ok().build();
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
     * POST /auth/logout
     *
     * - Redis에서 리프레시 토큰을 삭제하여 재사용을 차단
     * - Authorization 헤더의 Access Token도 블랙리스트에 등록하여 즉시 무효화
     * - 프론트엔드에서는 추가로 httpOnly 쿠키(세션) 삭제 처리
     *
     * Request Body:
     * { "refreshToken": "jwt..." }
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");
        // Access Token 추출 (블랙리스트용)
        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        logoutUseCase.logout(refreshToken, accessToken);
        return ResponseEntity.noContent().build();
    }

    // ========== 비밀번호 찾기 API ==========

    /**
     * 1단계: 비밀번호 재설정 인증코드 메일 발송 요청
     * POST /auth/password/code/request
     */
    @PostMapping("/password/code/request")
    public ResponseEntity<Void> requestResetCode(
        @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일을 입력해 주세요.");
        }
        findPasswordUseCase.requestResetCode(email);
        return ResponseEntity.ok().build();
    }

    /**
     * 2단계: 인증코드 입력값 검증 API
     * POST /auth/password/code/verify
     */
    @PostMapping("/password/code/verify")
    public ResponseEntity<Void> verifyResetCode(
        @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일과 인증번호를 모두 입력해 주세요.");
        }
        findPasswordUseCase.verifyResetCode(email, code);
        return ResponseEntity.ok().build();
    }

    /**
     * 3단계: 비밀번호 실제 변경
     * POST /auth/password/code/reset
     */
    @PostMapping("/password/code/reset")
    public ResponseEntity<Void> resetPassword(
        @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        if (email == null || email.isBlank() || code == null || code.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "잘못된 요청 파라미터입니다.");
        }

        findPasswordUseCase.resetPassword(email, code, newPassword);
        return ResponseEntity.ok().build();
    }
}
