package com.simul.user.adapter.out.persistence;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import com.simul.user.domain.model.Gender;
import com.simul.user.domain.model.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String name;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(length = 200)
    private String bio;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    public UserJpaEntity(UUID userId, String provider, String providerId, String email, String name, String nickname,
                         String password, Gender gender, String bio, String profileImageUrl, String bannerImageUrl,
                         boolean isPublic, Role role, boolean isActive) {
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.gender = gender;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.isPublic = isPublic;
        this.role = role;
        this.isActive = isActive;
    }
}
