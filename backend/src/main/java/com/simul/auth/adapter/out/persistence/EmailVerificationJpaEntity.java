package com.simul.auth.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationJpaEntity {

    @Id
    @Column(length = 50)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Builder
    public EmailVerificationJpaEntity(String email, String token, LocalDateTime expiryDate, boolean isVerified) {
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
        this.isVerified = isVerified;
    }
}
