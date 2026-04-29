package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostPersistenceMapper {

    public Post mapToDomainEntity(PostJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        List<PostImage> images = jpaEntity.getImages().stream()
                .map(this::mapToDomainEntity)
                .toList();

        return Post.builder()
                .postId(jpaEntity.getPostId())
                .userId(jpaEntity.getUserId())
                .baseImageId(jpaEntity.getBaseImageId())
                .itemId(jpaEntity.getItemId())
                .imageUrl(jpaEntity.getImageUrl())
                .status(jpaEntity.getStatus())
                .caption(jpaEntity.getCaption())
                .isPublic(jpaEntity.getIsPublic())
                .isBlinded(jpaEntity.getIsBlinded())
                .reportCount(jpaEntity.getReportCount())
                .likeCount(jpaEntity.getLikeCount())
                .viewCount(jpaEntity.getViewCount())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .deletedAt(jpaEntity.getDeletedAt())
                .images(images)
                .build();
    }

    public PostImage mapToDomainEntity(PostImageJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return PostImage.builder()
                .postImageId(jpaEntity.getPostImageId())
                .postId(jpaEntity.getPost().getPostId())
                .imageUrl(jpaEntity.getImageUrl())
                .sortOrder(jpaEntity.getSortOrder())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }

    public PostJpaEntity mapToJpaEntity(Post domainEntity) {
        if (domainEntity == null) return null;

        PostJpaEntity postJpaEntity = PostJpaEntity.builder()
                .postId(domainEntity.getPostId())
                .userId(domainEntity.getUserId())
                .baseImageId(domainEntity.getBaseImageId())
                .itemId(domainEntity.getItemId())
                .imageUrl(domainEntity.getImageUrl())
                .status(domainEntity.getStatus())
                .caption(domainEntity.getCaption())
                .isPublic(domainEntity.getIsPublic())
                .isBlinded(domainEntity.getIsBlinded())
                .reportCount(domainEntity.getReportCount())
                .likeCount(domainEntity.getLikeCount())
                .viewCount(domainEntity.getViewCount())
                .build();

        if (domainEntity.getImages() != null) {
            domainEntity.getImages().forEach(img -> {
                PostImageJpaEntity imageJpaEntity = PostImageJpaEntity.builder()
                        .postImageId(img.getPostImageId())
                        .post(postJpaEntity)
                        .imageUrl(img.getImageUrl())
                        .sortOrder(img.getSortOrder())
                        .build();
                postJpaEntity.addImage(imageJpaEntity);
            });
        }

        return postJpaEntity;
    }
}
