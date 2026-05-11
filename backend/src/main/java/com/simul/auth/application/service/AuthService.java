package com.simul.auth.application.service;

import com.simul.auth.application.dto.OAuth2UserInfo;
import com.simul.auth.application.dto.SocialLoginCommand;
import com.simul.auth.application.dto.TokenResponse;
import com.simul.auth.application.port.in.EmailAuthUseCase;
import com.simul.auth.application.port.in.LogoutUseCase;
import com.simul.auth.application.port.in.RefreshTokenUseCase;
import com.simul.auth.application.port.in.SocialLoginUseCase;
import com.simul.auth.application.port.out.AccessTokenBlacklistPort;
import com.simul.auth.application.port.out.OAuth2ProviderPort;
import com.simul.auth.application.port.out.RefreshTokenPort;
import com.simul.auth.domain.model.AuthRole;
import com.simul.auth.domain.model.AuthUser;
import com.simul.auth.domain.model.RefreshToken;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.common.security.JwtTokenProvider;
import com.simul.user.application.port.in.RegisterUserUseCase;
import com.simul.user.application.port.out.UserPersistencePort;
import com.simul.user.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 인증 서비스 (UseCase 구현체)
 *
 * 소셜 로그인 흐름:
 * 1. 프론트엔드가 소셜 로그인 후 인가 코드를 전달
 * 2. 해당 제공자의 OAuth2Adapter를 통해 사용자 정보 조회
 * 3. DB에서 기존 회원 확인 (provider + providerId)
 * 4. 신규면 자동 가입, 기존이면 기존 정보 사용
 * 5. JWT Access + Refresh Token 발급 → Redis에 저장
 */
@Service
public class AuthService implements SocialLoginUseCase, RefreshTokenUseCase, EmailAuthUseCase, LogoutUseCase {

    private final Map<String, OAuth2ProviderPort> providerMap;
    private final UserPersistencePort userPersistencePort;
    private final RegisterUserUseCase registerUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenPort refreshTokenPort;
    private final AccessTokenBlacklistPort accessTokenBlacklistPort;
    private final long refreshTokenValiditySeconds;

    public AuthService(
        List<OAuth2ProviderPort> providers,
        UserPersistencePort userPersistencePort,
        RegisterUserUseCase registerUserUseCase,
        JwtTokenProvider jwtTokenProvider,
        PasswordEncoder passwordEncoder,
        RefreshTokenPort refreshTokenPort,
        AccessTokenBlacklistPort accessTokenBlacklistPort,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds
    ) {
        // 제공자 이름으로 빠르게 찾을 수 있도록 Map으로 변환
        // { "kakao": KakaoAdapter, "naver": NaverAdapter, "google": GoogleAdapter }
        this.providerMap = providers.stream()
            .collect(Collectors.toMap(OAuth2ProviderPort::getProvider, Function.identity()));
        this.userPersistencePort = userPersistencePort;
        this.registerUserUseCase = registerUserUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenPort = refreshTokenPort;
        this.accessTokenBlacklistPort = accessTokenBlacklistPort;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    @Override
    public TokenResponse socialLogin(SocialLoginCommand command) {
        // 1. 해당 제공자의 Adapter 찾기
        OAuth2ProviderPort provider = providerMap.get(command.provider());
        if (provider == null) {
            throw new BusinessException(ErrorCode.OAUTH2_FAILED,
                "지원하지 않는 소셜 로그인 제공자입니다: " + command.provider());
        }

        // 2. 인가 코드로 사용자 정보 조회
        OAuth2UserInfo userInfo = provider.getUserInfo(command.code(), command.redirectUri());

        // 3. 기존 회원 확인
        boolean isNewUser = false;
        User user = userPersistencePort
            .findByProviderAndProviderId(command.provider(), userInfo.providerId())
            .orElse(null);

        // 4. 신규 회원이면 자동 가입
        if (user == null) {
            user = registerUserUseCase.registerSocialUser(
                command.provider(),
                userInfo.providerId(),
                userInfo.nickname(),
                userInfo.name(),
                userInfo.gender()
            );
            isNewUser = true;
        }

        // 5. Auth 도메인 모델로 매핑 및 검증
        AuthUser authUser = mapToAuthUser(user);
        if (!authUser.canLogin()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "정지 또는 탈퇴된 계정입니다.");
        }

        // 6. JWT 토큰 발급 + Redis에 리프레시 토큰 저장
        return issueAndStoreTokens(authUser, isNewUser);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 1. Refresh Token JWT 서명 + 타입 검증 (Access Token으로 갱신 시도 차단)
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // 2. Redis에 해당 토큰이 존재하는지 확인 (강제 로그아웃 여부 체크)
        refreshTokenPort.findByToken(refreshToken)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN,
                "만료되었거나 무효화된 리프레시 토큰입니다"));

        // 3. 사용자 ID 추출 및 사용자 조회
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4. Auth 도메인 모델로 매핑 및 검증
        AuthUser authUser = mapToAuthUser(user);
        if (!authUser.canLogin()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "정지 또는 탈퇴된 계정입니다.");
        }

        // 5. 기존 리프레시 토큰 삭제 후 새 토큰 발급 (Rotation)
        refreshTokenPort.deleteByToken(refreshToken);
        return issueAndStoreTokens(authUser, false);
    }

    @Override
    public void logout(String refreshToken, String accessToken) {
        // 1. Redis에서 리프레시 토큰 삭제 → 해당 토큰으로 재발급 불가
        if (refreshToken != null) {
            refreshTokenPort.deleteByToken(refreshToken);
        }

        // 2. Access Token 블랙리스트 등록 → 남은 유효기간 동안 사용 차단
        if (accessToken != null) {
            try {
                long remainingSeconds = jwtTokenProvider.getRemainingSeconds(accessToken);
                if (remainingSeconds > 0) {
                    accessTokenBlacklistPort.addToBlacklist(accessToken, remainingSeconds);
                }
            } catch (Exception e) {
                // 이미 만료된 토큰은 블랙리스트에 넣을 필요 없음
            }
        }
    }

    @Override
    public TokenResponse emailSignup(String email, String password, String name, String nickname, com.simul.user.domain.model.Gender gender) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        
        // 회원가입
        User user = registerUserUseCase.registerEmailUser(email, encodedPassword, nickname, name, gender);
        
        // JWT 발급 + Redis 저장
        AuthUser authUser = mapToAuthUser(user);
        return issueAndStoreTokens(authUser, true);
    }

    @Override
    public TokenResponse emailLogin(String email, String password) {
        User user = userPersistencePort.findByProviderAndProviderId("email", email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "가입되지 않은 이메일입니다."));
                
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호가 일치하지 않습니다.");
        }
        
        AuthUser authUser = mapToAuthUser(user);
        if (!authUser.canLogin()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "정지 또는 탈퇴된 계정입니다.");
        }
        
        // JWT 발급 + Redis 저장
        return issueAndStoreTokens(authUser, false);
    }

    // ========== Private Helper Methods ==========

    /**
     * JWT 토큰 쌍(Access + Refresh) 발급 및 Redis에 리프레시 토큰 저장
     */
    private TokenResponse issueAndStoreTokens(AuthUser authUser, boolean isNewUser) {
        String accessToken = jwtTokenProvider.createAccessToken(
            authUser.getUserId(), authUser.getRole().name()
        );
        String refreshTokenStr = jwtTokenProvider.createRefreshToken(authUser.getUserId());

        // Redis에 리프레시 토큰 저장 (TTL 설정으로 자동 만료)
        RefreshToken refreshTokenDomain = RefreshToken.builder()
            .token(refreshTokenStr)
            .userId(authUser.getUserId())
            .timeToLive(refreshTokenValiditySeconds)
            .build();
        refreshTokenPort.save(refreshTokenDomain);

        return new TokenResponse(accessToken, refreshTokenStr, isNewUser);
    }

    private AuthUser mapToAuthUser(User user) {
        return AuthUser.builder()
                .userId(user.getUserId())
                .role(AuthRole.valueOf(user.getRole().name()))
                .isActive(user.isActive())
                .build();
    }
}
