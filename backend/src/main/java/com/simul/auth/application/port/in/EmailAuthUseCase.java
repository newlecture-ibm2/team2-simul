package com.simul.auth.application.port.in;

import com.simul.auth.application.dto.TokenResponse;
import com.simul.user.domain.model.Gender;

public interface EmailAuthUseCase {
    TokenResponse emailSignup(String email, String password, String name, String nickname, Gender gender);
    TokenResponse emailLogin(String email, String password);
}
