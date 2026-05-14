package com.simul.auth.application.service;

import com.simul.auth.application.dto.OAuth2UserInfo;
import com.simul.auth.application.dto.SocialLoginCommand;
import com.simul.auth.application.dto.TokenResponse;
import com.simul.auth.application.port.in.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.simul.auth.application.port.out.EmailVerificationPort;
import com.simul.auth.domain.model.EmailVerification;

/**
 * 인증 서비스 (UseCase 구현체)
 */
@Slf4j
@Service
@Transactional
public class AuthService implements SocialLoginUseCase, RefreshTokenUseCase, EmailAuthUseCase, LogoutUseCase, RestoreAccountUseCase {

    private final Map<String, OAuth2ProviderPort> providerMap;
    private final UserPersistencePort userPersistencePort;
    private final RegisterUserUseCase registerUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenPort refreshTokenPort;
    private final AccessTokenBlacklistPort accessTokenBlacklistPort;
    private final EmailVerificationPort emailVerificationPort;
    private final long refreshTokenValiditySeconds;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public AuthService(
        List<OAuth2ProviderPort> providers,
        UserPersistencePort userPersistencePort,
        RegisterUserUseCase registerUserUseCase,
        JwtTokenProvider jwtTokenProvider,
        PasswordEncoder passwordEncoder,
        RefreshTokenPort refreshTokenPort,
        AccessTokenBlacklistPort accessTokenBlacklistPort,
        EmailVerificationPort emailVerificationPort,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds
    ) {
        this.providerMap = providers.stream()
            .collect(Collectors.toMap(OAuth2ProviderPort::getProvider, Function.identity()));
        this.userPersistencePort = userPersistencePort;
        this.registerUserUseCase = registerUserUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenPort = refreshTokenPort;
        this.accessTokenBlacklistPort = accessTokenBlacklistPort;
        this.emailVerificationPort = emailVerificationPort;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    @Override
    public TokenResponse socialLogin(SocialLoginCommand command) {
        OAuth2ProviderPort provider = providerMap.get(command.provider());
        if (provider == null) {
            throw new BusinessException(ErrorCode.OAUTH2_FAILED, "지원하지 않는 소셜 로그인 제공자입니다: " + command.provider());
        }

        OAuth2UserInfo userInfo = provider.getUserInfo(command.code(), command.redirectUri());

        User user = userPersistencePort
            .findByProviderAndProviderIdIncludingDeleted(command.provider(), userInfo.providerId())
            .orElse(null);

        boolean isNewUser = false;

        if (user == null) {
            user = registerUserUseCase.registerSocialUser(
                command.provider(),
                userInfo.providerId(),
                userInfo.nickname(),
                userInfo.name(),
                userInfo.gender()
            );
            isNewUser = true;
        } else if (user.getDeletedAt() != null) {
            long daysSinceDeletion = ChronoUnit.DAYS.between(user.getDeletedAt(), LocalDateTime.now());
            if (daysSinceDeletion < 30) {
                throw new BusinessException(ErrorCode.USER_IN_GRACE_PERIOD, 
                    "최근 탈퇴한 " + command.provider() + " 계정입니다. 기존 계정의 데이터를 복구하시겠습니까?|ID:" + userInfo.providerId());
            } else {
                user.restore();
                user = userPersistencePort.save(user);
            }
        }

        AuthUser authUser = mapToAuthUser(user);
        if (!authUser.canLogin()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "정지된 계정입니다.");
        }

        return issueAndStoreTokens(authUser, isNewUser);
    }

    @Override
    public TokenResponse restoreAccount(String provider, String providerId) {
        User user = userPersistencePort.findByProviderAndProviderIdIncludingDeleted(provider, providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "복구할 계정을 찾을 수 없습니다."));

        if (user.getDeletedAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 활성화된 계정입니다.");
        }

        user.restore();
        User restoredUser = userPersistencePort.save(user);

        AuthUser authUser = mapToAuthUser(restoredUser);
        return issueAndStoreTokens(authUser, false);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        jwtTokenProvider.validateRefreshToken(refreshToken);
        refreshTokenPort.findByToken(refreshToken)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "무효화된 리프레시 토큰입니다"));

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthUser authUser = mapToAuthUser(user);
        if (!authUser.canLogin()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "정지된 계정입니다.");
        }

        refreshTokenPort.deleteByToken(refreshToken);
        return issueAndStoreTokens(authUser, false);
    }

    @Override
    public void logout(String refreshToken, String accessToken) {
        if (refreshToken != null) {
            refreshTokenPort.deleteByToken(refreshToken);
        }
        if (accessToken != null) {
            try {
                long remainingSeconds = jwtTokenProvider.getRemainingSeconds(accessToken);
                if (remainingSeconds > 0) {
                    accessTokenBlacklistPort.addToBlacklist(accessToken, remainingSeconds);
                }
            } catch (Exception e) {}
        }
    }

    @Override
    public TokenResponse emailSignup(String email, String password, String name, String nickname, com.simul.user.domain.model.Gender gender) {
        // 이미 탈퇴한 계정이 있는지 확인
        userPersistencePort.findByProviderAndProviderIdIncludingDeleted("email", email)
            .ifPresent(u -> {
                if (u.getDeletedAt() != null) {
                    long days = ChronoUnit.DAYS.between(u.getDeletedAt(), LocalDateTime.now());
                    if (days < 30) {
                        throw new BusinessException(ErrorCode.USER_IN_GRACE_PERIOD, "이미 사용 중인 이메일입니다. 기존 계정을 복구하시려면 로그인 화면에서 기존 비밀번호로 로그인해 주세요.");
                    }
                } else {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 가입된 이메일입니다.");
                }
            });

        String encodedPassword = passwordEncoder.encode(password);
        User user = registerUserUseCase.registerEmailUser(email, encodedPassword, nickname, name, gender);

        // 이메일 인증 토큰 생성 및 저장
        EmailVerification verification = EmailVerification.create(email);
        emailVerificationPort.save(verification);

        // 인증 링크 출력 (로그)
        String verificationLink = frontendUrl + "/auth/verify?token=" + verification.getToken();
        log.info("================================================================");
        log.info("이메일 인증 링크: {}", verificationLink);
        log.info("================================================================");

        throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Override
    public TokenResponse emailLogin(String email, String password) {
        User user = userPersistencePort.findByProviderAndProviderIdIncludingDeleted("email", email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "가입되지 않은 이메일입니다."));

        // 비밀번호 검증 (보안: 탈퇴 여부와 상관없이 먼저 체크)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호가 일치하지 않습니다.");
        }

        if (user.getDeletedAt() != null) {
            long days = ChronoUnit.DAYS.between(user.getDeletedAt(), LocalDateTime.now());
            if (days < 30) {
                // 비밀번호가 맞고 30일 이내인 경우만 복구 팝업을 띄울 수 있게 에러 발생
                throw new BusinessException(ErrorCode.USER_IN_GRACE_PERIOD, 
                    "최근 탈퇴한 계정입니다. 본인 확인이 완료되었습니다. 기존 계정의 데이터를 복구하시겠습니까?");
            } else {
                // 30일이 지났으면 자동 복구 (또는 정책에 따라 가입 불가 처리)
                user.restore();
                user = userPersistencePort.save(user);
            }
        }
        
        AuthUser authUser = mapToAuthUser(user);
        if (!authUser.canLogin()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        
        return issueAndStoreTokens(authUser, false);
    }

    @Override
    public void verifyEmail(String token) {
        EmailVerification verification = emailVerificationPort.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 인증 토큰입니다."));

        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "만료된 인증 토큰입니다.");
        }

        User user = userPersistencePort.findByProviderAndProviderId("email", verification.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.restore(); // isActive를 true로 변경하는 용도로 재사용
        userPersistencePort.save(user);

        emailVerificationPort.deleteByEmail(verification.getEmail());
    }

    private TokenResponse issueAndStoreTokens(AuthUser authUser, boolean isNewUser) {
        String accessToken = jwtTokenProvider.createAccessToken(authUser.getUserId(), authUser.getRole().name());
        String refreshTokenStr = jwtTokenProvider.createRefreshToken(authUser.getUserId());
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
