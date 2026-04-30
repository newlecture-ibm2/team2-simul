package com.simul.tag.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
    name = "post_tags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_tag_id")
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    public PostTag(UUID postId, Tag tag) {
        this.postId = postId;
        this.tag = tag;
    }
}
