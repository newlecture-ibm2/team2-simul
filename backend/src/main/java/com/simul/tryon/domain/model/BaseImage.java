package com.simul.tryon.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "base_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseImage extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "base_image_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "source_post_id")
    private UUID sourcePostId;

    public BaseImage(UUID userId, String imageUrl) {
        this.userId = userId;
        this.imageUrl = imageUrl;
    }

    public BaseImage(UUID userId, String imageUrl, UUID sourcePostId) {
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.sourcePostId = sourcePostId;
    }
}
