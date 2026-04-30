package com.simul.common.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 도메인 기본 엔티티 (BaseEntity)
 * - 모든 엔티티가 상속
 * - created_at, updated_at, deleted_at + softDelete() 제공
 * - Soft Delete 정책: DELETE 쿼리 물리 삭제 금지, softDelete() 호출 + save() 명시
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 소프트 딜리트 - deleted_at에 현재 시간 기록
     * 반드시 softDelete() 호출 후 save()를 명시적으로 호출할 것
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
