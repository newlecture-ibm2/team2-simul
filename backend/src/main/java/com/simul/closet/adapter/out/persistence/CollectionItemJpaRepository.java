package com.simul.closet.adapter.out.persistence;

import com.simul.closet.domain.model.CollectionItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CollectionItemJpaRepository extends JpaRepository<CollectionItem, UUID> {

    @Query("SELECT ci FROM CollectionItem ci " +
           "WHERE ci.collection.id = :collectionId AND ci.deletedAt IS NULL")
    List<CollectionItem> findByCollectionIdAndDeletedAtIsNull(@Param("collectionId") UUID collectionId);

    @Query("SELECT ci FROM CollectionItem ci " +
           "WHERE ci.item.id = :itemId AND ci.deletedAt IS NULL")
    List<CollectionItem> findByItemIdAndDeletedAtIsNull(@Param("itemId") UUID itemId);

    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END " +
           "FROM CollectionItem ci " +
           "WHERE ci.collection.id = :collectionId AND ci.item.id = :itemId AND ci.deletedAt IS NULL")
    boolean existsByCollectionIdAndItemIdAndDeletedAtIsNull(
            @Param("collectionId") UUID collectionId,
            @Param("itemId") UUID itemId);

    @Query("SELECT COUNT(ci) FROM CollectionItem ci " +
           "WHERE ci.collection.id = :collectionId AND ci.deletedAt IS NULL")
    long countByCollectionIdAndDeletedAtIsNull(@Param("collectionId") UUID collectionId);

    @Query("SELECT ci.item.clothingImage.imageUrl FROM CollectionItem ci " +
           "WHERE ci.collection.id = :collectionId AND ci.deletedAt IS NULL AND ci.item.deletedAt IS NULL " +
           "ORDER BY ci.createdAt DESC")
    List<String> findTopImageUrlsByCollectionId(@Param("collectionId") UUID collectionId, Pageable pageable);

    @Modifying
    @Query("UPDATE CollectionItem ci SET ci.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE ci.collection.id = :collectionId AND ci.item.id = :itemId AND ci.deletedAt IS NULL")
    void softDeleteByCollectionIdAndItemId(
            @Param("collectionId") UUID collectionId,
            @Param("itemId") UUID itemId);

    @Modifying
    @Query("UPDATE CollectionItem ci SET ci.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE ci.collection.id = :collectionId AND ci.deletedAt IS NULL")
    void softDeleteAllByCollectionId(@Param("collectionId") UUID collectionId);

    @Modifying
    @Query("UPDATE CollectionItem ci SET ci.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE ci.item.id = :itemId AND ci.deletedAt IS NULL")
    void softDeleteAllByItemId(@Param("itemId") UUID itemId);
}
