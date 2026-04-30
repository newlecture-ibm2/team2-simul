package com.simul.post.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_image_id")
    private UUID postImageId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public PostImage(UUID postImageId, Post post, String imageUrl, Integer sortOrder) {
        this.postImageId = postImageId;
        this.post = post;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
