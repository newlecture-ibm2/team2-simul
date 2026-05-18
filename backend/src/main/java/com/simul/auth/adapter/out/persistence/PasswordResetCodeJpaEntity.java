package com.simul.auth.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetCodeJpaEntity {

    @Id
    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Builder
    public PasswordResetCodeJpaEntity(String email, String code, LocalDateTime expiryDate, boolean isVerified) {
        this.email = email;
        this.code = code;
        this.expiryDate = expiryDate;
        this.isVerified = isVerified;
    }
}
