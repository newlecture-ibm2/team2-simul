package com.simul.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.simul.auth.application.port.out.AccessTokenBlacklistPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.env.Environment;
import java.util.Arrays;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT 인증 필터
 * - 모든 요청에서 Authorization 헤더의 Bearer 토큰을 검사
 * - 유효한 토큰이면 SecurityContext에 인증 정보를 설정
 * - 인증 불필요 경로(permitAll)는 SecurityConfig에서 별도 관리
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenBlacklistPort accessTokenBlacklistPort;
    private final Environment env;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        AccessTokenBlacklistPort accessTokenBlacklistPort,
        Environment env
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accessTokenBlacklistPort = accessTokenBlacklistPort;
        this.env = env;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                // Access Token만 허용 (Refresh Token으로 API 인증 시도 차단)
                jwtTokenProvider.validateAccessToken(token);

                // 블랙리스트 체크 (로그아웃된 토큰 차단)
                if (accessTokenBlacklistPort.isBlacklisted(token)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // Spring Security에 인증 정보 설정
                // ROLE_ 접두사 사용 (Spring Security 규약)
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,      // principal: 사용자 ID
                        null,        // credentials: JWT에서는 불필요
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 토큰 검증 실패 시 인증 정보를 설정하지 않음
                // → SecurityConfig의 인가 설정에 따라 401 또는 허용
                SecurityContextHolder.clearContext();
                // [LOCAL DEV ONLY] 토큰이 있더라도(만료/깨짐) 로컬 환경이면 Mock User로 fallback
                if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
                    UUID mockUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            mockUserId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // [LOCAL DEV ONLY] 토큰이 없더라도 로컬 환경이면 편의를 위해 Mock User 주입
            if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
                UUID mockUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        mockUserId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization: Bearer {token} 에서 토큰 부분만 추출
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
