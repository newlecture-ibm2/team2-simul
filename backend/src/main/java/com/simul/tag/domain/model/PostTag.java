package com.simul.tag.domain.model;

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
@Table(
    name = "post_tags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_tag_id")
    private UUID id;

    @Column(name = "source_image_url", length = 500)
    private String sourceImageUrl;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PostTag(UUID postId, Tag tag, String sourceImageUrl) {
        this.postId = postId;
        this.tag = tag;
        this.sourceImageUrl = sourceImageUrl;
    }
}
