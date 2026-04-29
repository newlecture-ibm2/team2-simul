package com.simul.closet.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
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
public class ClosetItem extends BaseJpaEntity {

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
    private ClosetCollection closetCollection;

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
    public ClosetItem(UUID userId, ClothingImage clothingImage, ClosetCollection closetCollection, Category category, String memo, Integer sortOrder) {
        this.userId = userId;
        this.clothingImage = clothingImage;
        this.closetCollection = closetCollection;
        this.category = category;
        this.memo = memo;
        this.sortOrder = sortOrder;
        this.tryCount = 0;
    }

    public void update(Category category, String memo) {
        this.category = category;
        this.memo = memo;
    }

    public void setClosetCollection(ClosetCollection closetCollection) {
        this.closetCollection = closetCollection;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void incrementTryCount() {
        this.tryCount++;
    }
}
