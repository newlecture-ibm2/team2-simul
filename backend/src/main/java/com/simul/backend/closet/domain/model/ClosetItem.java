package com.simul.backend.closet.domain.model;

import com.simul.backend.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "closet_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClosetItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private ClothingImage clothingImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Column(name = "memo", length = 100)
    private String memo;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "try_count", nullable = false)
    private Integer tryCount = 0;

    @Builder
    public ClosetItem(UUID userId, ClothingImage clothingImage, Collection collection, Category category, String memo, Integer sortOrder) {
        this.userId = userId;
        this.clothingImage = clothingImage;
        this.collection = collection;
        this.category = category;
        this.memo = memo;
        this.sortOrder = sortOrder;
        this.tryCount = 0;
    }

    public void update(Category category, String memo) {
        this.category = category;
        this.memo = memo;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void incrementTryCount() {
        this.tryCount++;
    }
}
