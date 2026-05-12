package com.simul.closet.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "collection_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"collection_id", "item_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionItem extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private ClosetCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ClosetItem item;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public CollectionItem(ClosetCollection collection, ClosetItem item, Integer sortOrder) {
        this.collection = collection;
        this.item = item;
        this.sortOrder = sortOrder;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
