package com.simul.user.application.port.in;

import com.simul.user.domain.model.Gender;
import com.simul.user.domain.model.User;

/**
 * 사용자 등록 유즈케이스 (Input Port)
 */
public interface RegisterUserUseCase {

    /**
     * 소셜 로그인 정보로 신규 사용자 등록
     */
    User registerSocialUser(String provider, String providerId, String nickname, String name, Gender gender, String email);

    /**
     * 이메일 회원가입으로 신규 사용자 등록
     */
    User registerEmailUser(String email, String password, String nickname, String name, Gender gender);
}
