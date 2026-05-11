package com.simul.auth.application.dto;

import com.simul.user.domain.model.Gender;

public record EmailSignupCommand(
    String email,
    String password,
    String name,
    String nickname,
    Gender gender
) {}
