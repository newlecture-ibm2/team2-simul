package com.simul.closet.adapter.out.persistence;

import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClosetItemJpaRepository extends JpaRepository<ClosetItem, UUID> {
    
    @Query("SELECT ci FROM ClosetItem ci " +
           "JOIN FETCH ci.clothingImage " +
           "WHERE ci.userId = :userId AND ci.deletedAt IS NULL")
    List<ClosetItem> findAllByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId);

    @Query("SELECT ci FROM ClosetItem ci " +
           "JOIN FETCH ci.clothingImage " +
           "WHERE ci.id = :id AND ci.deletedAt IS NULL")
    Optional<ClosetItem> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    @Query("SELECT ci FROM ClosetItem ci " +
           "JOIN FETCH ci.clothingImage " +
           "WHERE ci.userId = :userId AND ci.deletedAt IS NULL " +
           "AND (:category IS NULL OR ci.category = :category)")
    Page<ClosetItem> findByUserIdAndCategoryWithPaging(
        @Param("userId") UUID userId,
        @Param("category") Category category,
        Pageable pageable
    );

    @Query("SELECT COUNT(ci) FROM ClosetItem ci WHERE ci.userId = :userId AND ci.deletedAt IS NULL")
    long countByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId);
}
