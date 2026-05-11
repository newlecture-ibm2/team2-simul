package com.simul.tryon.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tryon_credits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TryonCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "credit_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    public TryonCredit(UUID userId, LocalDateTime usedAt, UUID jobId) {
        this.userId = userId;
        this.usedAt = usedAt;
        this.jobId = jobId;
    }
}

