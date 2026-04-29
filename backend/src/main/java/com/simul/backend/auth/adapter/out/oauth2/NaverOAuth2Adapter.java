package com.simul.backend.auth.adapter.out.oauth2;

import com.simul.backend.auth.application.dto.OAuth2UserInfo;
import com.simul.backend.auth.application.port.out.OAuth2ProviderPort;
import com.simul.backend.common.exception.BusinessException;
import com.simul.backend.common.exception.ErrorCode;
import com.simul.backend.user.domain.model.Gender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 네이버 OAuth2 Adapter
 *
 * 흐름:
 * 1. 인가 코드 → POST nid.naver.com/oauth2.0/token → Access Token
 * 2. Access Token → GET openapi.naver.com/v1/nid/me → 사용자 정보
 */
@Component
public class NaverOAuth2Adapter implements OAuth2ProviderPort {

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public NaverOAuth2Adapter(
        @Value("${oauth2.naver.client-id:}") String clientId,
        @Value("${oauth2.naver.client-secret:}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public OAuth2UserInfo getUserInfo(String code, String redirectUri) {
        try {
            String accessToken = getAccessToken(code, redirectUri);
            return fetchUserProfile(accessToken);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH2_FAILED,
                "네이버 로그인 처리 중 오류: " + e.getMessage());
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
            .uri("https://nid.naver.com/oauth2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        return (String) response.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo fetchUserProfile(String accessToken) {
        Map<String, Object> response = restClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        // 네이버는 response.response 안에 사용자 정보가 있음
        Map<String, Object> profile = (Map<String, Object>) response.get("response");

        String providerId = (String) profile.get("id");
        String nickname = (String) profile.get("nickname");
        String name = (String) profile.get("name");
        String profileImage = (String) profile.get("profile_image");
        String genderStr = (String) profile.get("gender");

        Gender gender = Gender.UNKNOWN;
        if ("M".equalsIgnoreCase(genderStr)) {
            gender = Gender.MALE;
        } else if ("F".equalsIgnoreCase(genderStr)) {
            gender = Gender.FEMALE;
        }

        return new OAuth2UserInfo(providerId, nickname, name, gender, profileImage);
    }
}
