package com.simul.tag.domain.model;

import com.simul.common.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class Tag extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tag_id")
    private UUID id;

    @Column(name = "name", length = 20, unique = true, nullable = false)
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Builder
    public Tag(String name, String category) {
        this.name = name;
        this.category = category;
        this.usageCount = 0;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
}
