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
 * 구글 OAuth2 Adapter
 *
 * 흐름:
 * 1. 인가 코드 → POST oauth2.googleapis.com/token → Access Token
 * 2. Access Token → GET googleapis.com/oauth2/v2/userinfo → 사용자 정보
 */
@Component
public class GoogleOAuth2Adapter implements OAuth2ProviderPort {

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public GoogleOAuth2Adapter(
        @Value("${oauth2.google.client-id:}") String clientId,
        @Value("${oauth2.google.client-secret:}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    @Override
    public String getProvider() {
        return "google";
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
                "구글 로그인 처리 중 오류: " + e.getMessage());
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
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        return (String) response.get("access_token");
    }

    private OAuth2UserInfo fetchUserProfile(String accessToken) {
        Map<String, Object> response = restClient.get()
            .uri("https://www.googleapis.com/oauth2/v2/userinfo")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        String providerId = (String) response.get("id");
        String name = (String) response.get("name");
        String profileImage = (String) response.get("picture");
        String email = (String) response.get("email");

        // 구글은 기본적으로 성별을 주지 않으므로 UNKNOWN 처리
        // 닉네임 필드가 따로 없으므로 이름을 닉네임으로 사용
        return new OAuth2UserInfo(providerId, name, name, Gender.UNKNOWN, profileImage, email);
    }
}
