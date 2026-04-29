package com.simul.backend.user.domain.model;

import com.simul.backend.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * 사용자 엔티티 (users 테이블)
 * - UUID PK, 소셜 로그인 기반
 * - UNIQUE(provider, provider_id) 제약조건으로 중복 가입 방지
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(length = 30)
    private String name;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender = Gender.UNKNOWN;

    @Column(length = 200)
    private String bio;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // JPA 기본 생성자
    protected User() {}

    /**
     * 소셜 로그인 시 신규 사용자 생성용 생성자
     */
    public User(String provider, String providerId, String nickname, String name, Gender gender) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.name = name;
        this.gender = (gender != null) ? gender : Gender.UNKNOWN;
        this.role = Role.USER;
        this.isPublic = true;
        this.isActive = true;
    }

    // === Getters ===
    public UUID getUserId() { return userId; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public String getName() { return name; }
    public String getNickname() { return nickname; }
    public Gender getGender() { return gender; }
    public String getBio() { return bio; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public boolean isPublic() { return isPublic; }
    public Role getRole() { return role; }
    public boolean isActive() { return isActive; }

    // === 비즈니스 메서드 ===

    /**
     * 프로필 수정 (null이 아닌 값만 업데이트)
     */
    public void updateProfile(String nickname, String name, Gender gender, String bio, String profileImageUrl) {
        if (nickname != null) this.nickname = nickname;
        if (name != null) this.name = name;
        if (gender != null) this.gender = gender;
        if (bio != null) this.bio = bio;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    /**
     * 회원 탈퇴 (소프트 딜리트)
     */
    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
