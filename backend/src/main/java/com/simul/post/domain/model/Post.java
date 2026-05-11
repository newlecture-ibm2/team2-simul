package com.simul.post.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Post extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "base_image_id")
    private UUID baseImageId;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status;

    @Column(name = "caption", length = 300)
    private String caption;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "is_blinded", nullable = false)
    private Boolean isBlinded = false;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @Builder
    public Post(UUID postId, UUID userId, UUID baseImageId, UUID itemId, String imageUrl, PostStatus status, String caption, Boolean isPublic, Boolean isBlinded, Integer reportCount, Integer likeCount, Integer viewCount) {
        this.postId = postId;
        this.userId = userId;
        this.baseImageId = baseImageId;
        this.itemId = itemId;
        this.imageUrl = imageUrl;
        this.status = status != null ? status : PostStatus.PROCESSING;
        this.caption = caption;
        this.isPublic = isPublic != null ? isPublic : false;
        this.isBlinded = isBlinded != null ? isBlinded : false;
        this.reportCount = reportCount != null ? reportCount : 0;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
    }

    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(String caption, Boolean isPublic) {
        if (caption != null) {
            this.caption = caption;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
