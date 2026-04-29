package com.simul.backend.closet.domain.model;

import com.simul.backend.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "clothing_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id")
    private UUID id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "uploader_id", nullable = false)
    private UUID uploaderId;

    public ClothingImage(String imageUrl, UUID uploaderId) {
        this.imageUrl = imageUrl;
        this.uploaderId = uploaderId;
    }
}
