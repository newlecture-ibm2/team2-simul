package com.simul.user.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 변경 요청 DTO
 */
public record ChangePasswordRequest(
    @NotBlank(message = "현재 비밀번호를 입력해주세요")
    String oldPassword,

    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다")
    String newPassword
) {
}
