package com.simul.post.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "reporter_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private UUID reportId;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Report(UUID postId, UUID reporterId, String reason) {
        this.postId = postId;
        this.reporterId = reporterId;
        this.reason = reason;
    }
}
