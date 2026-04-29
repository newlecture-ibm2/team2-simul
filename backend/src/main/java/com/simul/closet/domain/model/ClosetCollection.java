package com.simul.closet.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "collections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClosetCollection extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "collection_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public ClosetCollection(UUID userId, String name, String coverImageUrl, Integer sortOrder) {
        this.userId = userId;
        this.name = name;
        this.coverImageUrl = coverImageUrl;
        this.sortOrder = sortOrder;
    }

    public void update(String name, String coverImageUrl) {
        this.name = name;
        this.coverImageUrl = coverImageUrl;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
