package com.simul.user.adapter.in.web;

import com.simul.user.application.dto.UserProfileResponse;
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
 * - GET /users/me      → 내 프로필 조회 (팔로우 수 포함)
 * - PATCH /users/me    → 프로필 수정
 * - DELETE /users/me   → 회원 탈퇴
 * - GET /users/{id}    → 타인 프로필 조회 (팔로우 수 + 팔로우 여부 포함)
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final LoadUserUseCase loadUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final WithdrawUserUseCase withdrawUserUseCase;
    private final com.simul.post.application.port.in.GetUserPostsUseCase getUserPostsUseCase;

    public UserController(
        LoadUserUseCase loadUserUseCase,
        UpdateUserUseCase updateUserUseCase,
        WithdrawUserUseCase withdrawUserUseCase,
        com.simul.post.application.port.in.GetUserPostsUseCase getUserPostsUseCase
    ) {
        this.loadUserUseCase = loadUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.withdrawUserUseCase = withdrawUserUseCase;
        this.getUserPostsUseCase = getUserPostsUseCase;
    }

    /**
     * 내 프로필 조회 (팔로우 수 포함)
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyInfo(@AuthenticationPrincipal UUID userId) {
        long postCount = getUserPostsUseCase.countUserPosts(userId);
        UserProfileResponse response = loadUserUseCase.loadUserProfile(userId, userId, postCount);
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
     * 타인 프로필 조회 (팔로우 수 + 팔로우 여부 포함)
     * GET /users/{userId}
     * - 비로그인 사용자는 currentUserId가 null로 전달됨
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserInfo(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        long postCount = getUserPostsUseCase.countUserPosts(userId);
        UserProfileResponse response = loadUserUseCase.loadUserProfile(userId, currentUserId, postCount);
        return ResponseEntity.ok(response);
    }
}
