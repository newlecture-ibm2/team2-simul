package com.simul.auth.adapter.out.oauth2;

import com.simul.auth.application.dto.OAuth2UserInfo;
import com.simul.auth.application.port.out.OAuth2ProviderPort;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.user.domain.model.Gender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 카카오 OAuth2 Adapter
 *
 * 흐름:
 * 1. 인가 코드 → POST kauth.kakao.com/oauth/token → Access Token
 * 2. Access Token → GET kapi.kakao.com/v2/user/me → 사용자 정보
 */
@Component
public class KakaoOAuth2Adapter implements OAuth2ProviderPort {

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public KakaoOAuth2Adapter(
        @Value("${oauth2.kakao.client-id:}") String clientId,
        @Value("${oauth2.kakao.client-secret:}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public OAuth2UserInfo getUserInfo(String code, String redirectUri) {
        try {
            // 1단계: 인가 코드 → Access Token
            String accessToken = getAccessToken(code, redirectUri);

            // 2단계: Access Token → 사용자 정보
            return fetchUserProfile(accessToken);
        } catch (org.springframework.web.client.RestClientResponseException e) {
            System.err.println("[카카오 에러 응답] " + e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.OAUTH2_FAILED,
                "카카오 로그인 처리 중 오류: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OAUTH2_FAILED,
                "카카오 로그인 처리 중 오류: " + e.getMessage());
        }
    }

    private String getAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        Map<String, Object> response = restClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        return (String) response.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo fetchUserProfile(String accessToken) {
        Map<String, Object> response = restClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        String providerId = String.valueOf(response.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");
        String genderStr = (String) kakaoAccount.get("gender");

        Gender gender = Gender.UNKNOWN;
        if ("male".equalsIgnoreCase(genderStr)) {
            gender = Gender.MALE;
        } else if ("female".equalsIgnoreCase(genderStr)) {
            gender = Gender.FEMALE;
        }

        // 카카오는 실명이 안 올 수 있으므로 닉네임을 이름으로 함께 사용
        // 사용자의 요청에 따라 카카오 로그인 이메일 정보는 의도적으로 수집하지 않고 null을 넘겨 빈 상태로 유지합니다.
        return new OAuth2UserInfo(providerId, nickname, nickname, gender, profileImage, null);
    }
}
