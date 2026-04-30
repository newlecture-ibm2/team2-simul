package com.simul.post.application.service;

import com.simul.common.application.service.FileStorageService;
import com.simul.post.application.dto.CreatePostCommand;
import com.simul.post.application.port.in.CreatePostUseCase;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostImage;
import com.simul.post.domain.model.PostStatus;
import com.simul.tag.application.port.in.AttachTagsToPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final FileStorageService fileStorageService;
    private final AttachTagsToPostUseCase attachTagsToPostUseCase;

    @Override
    @Transactional
    public Post createPost(CreatePostCommand command) {
        List<MultipartFile> images = command.getImages();

        // 1. 이미지 유효성 검사
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("ERR-301-A: 이미지가 첨부되지 않았습니다.");
        }
        if (images.size() > 5) {
            throw new IllegalArgumentException("ERR-301-D: 이미지는 최대 5장까지 업로드할 수 있습니다.");
        }

        // 2. 태그 유효성 검사
        List<String> tags = command.getTags();
        if (tags != null && tags.size() > 10) {
            throw new IllegalArgumentException("ERR-307-A: 태그는 최대 10개까지 등록 가능합니다.");
        }

        // 3. Post 엔티티 기본 정보 생성
        Post post = Post.builder()
                .userId(command.getUserId())
                .baseImageId(command.getBaseImageId())
                .itemId(command.getItemId())
                .status(PostStatus.COMPLETED)
                .caption(command.getCaption())
                .isPublic(command.getIsPublic())
                .build();

        // 4. 이미지 저장 및 PostImage 엔티티 연결
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            String imageUrl = fileStorageService.store(file);

            PostImage postImage = PostImage.builder()
                    .imageUrl(imageUrl)
                    .sortOrder(i)
                    .build();

            post.addImage(postImage);
        }

        // Post 엔티티 저장
        Post savedPost = postRepositoryPort.save(post);

        // 5. 태그 매핑 (N:M)
        if (tags != null && !tags.isEmpty()) {
            attachTagsToPostUseCase.attachTags(savedPost.getPostId(), tags);
        }

        return savedPost;
    }
}
