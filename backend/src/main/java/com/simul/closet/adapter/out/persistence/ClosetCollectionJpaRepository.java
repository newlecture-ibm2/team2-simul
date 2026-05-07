package com.simul.closet.adapter.out.persistence;

import com.simul.closet.domain.model.ClosetCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface ClosetCollectionJpaRepository extends JpaRepository<ClosetCollection, UUID> {
    boolean existsByIdAndUserId(UUID id, UUID userId);
    java.util.List<ClosetCollection> findAllByUserId(UUID userId);

    @Query("SELECT new com.simul.closet.adapter.out.persistence.CollectionWithCountDto(c, COUNT(i.id)) " +
           "FROM ClosetCollection c " +
           "LEFT JOIN ClosetItem i ON c.id = i.closetCollection.id AND i.deletedAt IS NULL " +
           "WHERE c.userId = :userId AND c.deletedAt IS NULL " +
           "GROUP BY c.id")
    Page<CollectionWithCountDto> findCollectionsWithItemCount(@Param("userId") UUID userId, Pageable pageable);
}
