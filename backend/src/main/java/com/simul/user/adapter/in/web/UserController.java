package com.simul.user.adapter.in.web;

import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import com.simul.user.application.port.in.UpdateUserUseCase;
import com.simul.user.application.port.in.WithdrawUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 사용자 관리 API 컨트롤러
 *
 * 엔드포인트:
 * - GET /users/me      → 내 정보 조회
 * - PATCH /users/me    → 프로필 수정
 * - DELETE /users/me   → 회원 탈퇴
 * - GET /users/{id}    → 타인 프로필 조회
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final LoadUserUseCase loadUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final WithdrawUserUseCase withdrawUserUseCase;

    public UserController(
        LoadUserUseCase loadUserUseCase,
        UpdateUserUseCase updateUserUseCase,
        WithdrawUserUseCase withdrawUserUseCase
    ) {
        this.loadUserUseCase = loadUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.withdrawUserUseCase = withdrawUserUseCase;
    }

    /**
     * 내 정보 조회
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UUID userId) {
        UserResponse response = loadUserUseCase.loadUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 수정
     * PATCH /users/me
     */
    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(
        @AuthenticationPrincipal UUID userId,
        @RequestBody UpdateUserRequest request
    ) {
        updateUserUseCase.updateProfile(
            userId,
            request.nickname(),
            request.name(),
            request.gender(),
            request.bio(),
            request.profileImageUrl()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 탈퇴 (소프트 딜리트)
     * DELETE /users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal UUID userId) {
        withdrawUserUseCase.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 타인 프로필 조회
     * GET /users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable UUID userId) {
        UserResponse response = loadUserUseCase.loadUser(userId);
        return ResponseEntity.ok(response);
    }
}
