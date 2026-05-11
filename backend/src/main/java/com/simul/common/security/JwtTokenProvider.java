package com.simul.common.security;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증
 * - Access Token: 1시간 (application.yml 설정)
 * - Refresh Token: 14일 (application.yml 설정)
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidity,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidity
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMs = accessTokenValidity * 1000;
        this.refreshTokenValidityMs = refreshTokenValidity * 1000;
    }

    /**
     * Access Token 생성
     * Claims: sub=userId, role=USER/ADMIN
     */
    public String createAccessToken(UUID userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMs);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * 토큰에서 역할(role) 추출
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰에서 타입(type) 추출 — "access" 또는 "refresh"
     */
    public String getTypeFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * 토큰 유효성 검증
     * - 만료, 서명 위조, 형식 오류 시 BusinessException 발생
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "토큰이 만료되었습니다");
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다");
        }
    }

    /**
     * Access Token 전용 검증
     * - 서명/만료 + type="access" 여부까지 확인
     * - Refresh Token으로 일반 API 인증을 시도하는 것을 방지
     */
    public boolean validateAccessToken(String token) {
        validateToken(token);
        String type = getTypeFromToken(token);
        if (!"access".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Access Token이 아닙니다");
        }
        return true;
    }

    /**
     * Refresh Token 전용 검증
     * - 서명/만료 + type="refresh" 여부까지 확인
     */
    public boolean validateRefreshToken(String token) {
        validateToken(token);
        String type = getTypeFromToken(token);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh Token이 아닙니다");
        }
        return true;
    }

    /**
     * 토큰의 남은 유효 시간(초) 계산
     * - 블랙리스트 TTL 설정에 사용
     */
    public long getRemainingSeconds(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remainingMs / 1000, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 토큰 파싱 (내부 사용)
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
