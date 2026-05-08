package com.simul.post.application.service;

import com.simul.common.application.service.FileStorageService;
import com.simul.post.application.dto.CreatePostCommand;
import com.simul.post.application.dto.FeedPostResponse;
import com.simul.post.application.port.in.CreatePostUseCase;
import com.simul.post.application.port.in.GetFeedPostsUseCase;
import com.simul.post.application.port.out.PostLikePersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostImage;
import com.simul.post.domain.model.PostStatus;
import com.simul.tag.application.port.in.AttachTagsToPostUseCase;
import com.simul.tag.application.port.in.LoadTagsUseCase;
import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import com.simul.post.application.port.in.GetPostDetailUseCase;
import com.simul.post.application.port.in.DeletePostUseCase;
import com.simul.post.application.port.in.UpdatePostUseCase;
import com.simul.post.application.dto.PostDetailResponse;
import com.simul.post.application.dto.UpdatePostCommand;
// ... imports handled below by inserting at class declaration

@Service
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase, GetFeedPostsUseCase, GetPostDetailUseCase, DeletePostUseCase, UpdatePostUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final PostLikePersistencePort postLikePersistencePort;
    private final FileStorageService fileStorageService;
    private final AttachTagsToPostUseCase attachTagsToPostUseCase;
    private final LoadUserUseCase loadUserUseCase;
    private final LoadTagsUseCase loadTagsUseCase;

    // ... existing createPost method ...
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

    @Override
    @Transactional(readOnly = true)
    public Page<FeedPostResponse> getFeedPosts(UUID currentUserId, String tab, String sort, Pageable pageable) {
        
        // 정렬 기준 설정
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("popular".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
        
        Page<Post> postsPage;
        if ("following".equalsIgnoreCase(tab)) {
            // TODO: 팔로우 기능 구현 후 연동 필요
            postsPage = postRepositoryPort.findFollowingPosts(Collections.emptyList(), sortedPageable);
        } else {
            postsPage = postRepositoryPort.findAllPublicPosts(sortedPageable);
        }

        if (postsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> postIds = postsPage.getContent().stream().map(Post::getPostId).toList();
        List<UUID> userIds = postsPage.getContent().stream().map(Post::getUserId).distinct().toList();

        Map<UUID, UserResponse> userMap = loadUserUseCase.loadUsers(userIds);
        Map<UUID, List<String>> tagMap = loadTagsUseCase.loadTagsByPostIds(postIds);

        // 로그인한 유저의 좋아요 여부를 일괄 조회
        Set<UUID> likedPostIds = postLikePersistencePort.findLikedPostIdsByUserIdAndPostIds(currentUserId, postIds);

        return postsPage.map(post -> {
            UserResponse user = userMap.get(post.getUserId());
            List<String> postTags = tagMap.getOrDefault(post.getPostId(), Collections.emptyList());
            
            String imageUrl = post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl();
            
            return new FeedPostResponse(
                    post.getPostId(),
                    post.getUserId(),
                    user != null ? user.nickname() : "Unknown",
                    user != null ? user.profileImageUrl() : null,
                    imageUrl,
                    postTags,
                    post.getCaption(),
                    post.getLikeCount(),
                    likedPostIds.contains(post.getPostId()),
                    post.getCreatedAt()
            );
        });
    }

    @Override
    @Transactional
    public PostDetailResponse getPostDetail(UUID postId, UUID currentUserId) {
        Post post = postRepositoryPort.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ERR-003: 찾을 수 없는 콘텐츠입니다."));

        // 1. 블라인드된 게시물 접근 차단
        if (Boolean.TRUE.equals(post.getIsBlinded())) {
            throw new IllegalArgumentException("ERR-003: 찾을 수 없는 콘텐츠입니다."); // 보안상 404와 동일하게 처리
        }

        // 2. 비공개 게시물 접근 권한 검증
        if (Boolean.FALSE.equals(post.getIsPublic())) {
            if (currentUserId == null || !currentUserId.equals(post.getUserId())) {
                throw new IllegalArgumentException("ERR-002: 비공개 게시물은 작성자만 볼 수 있습니다.");
            }
        }

        // 3. 조회수 증가 (Transaction 안에서 관리됨)
        post.incrementViewCount();
        postRepositoryPort.save(post); // JPA 더티 체킹으로 업데이트 되지만 명시적 호출

        // 4. 작성자 정보 조회 (존재하지 않는 사용자일 경우 예외 처리하여 알 수 없음으로 표시)
        String nickname = "알 수 없음";
        String profileImageUrl = null;
        try {
            UserResponse author = loadUserUseCase.loadUser(post.getUserId());
            nickname = author.nickname();
            profileImageUrl = author.profileImageUrl();
        } catch (com.simul.common.exception.BusinessException e) {
            // 사용자가 삭제되었거나 더미 데이터인 경우
        }

        // 5. 이미지 URL 목록 추출 (sortOrder 기준으로 정렬)
        List<String> imageUrls = post.getImages().stream()
                .sorted(Comparator.comparing(PostImage::getSortOrder))
                .map(PostImage::getImageUrl)
                .toList();

        // 6. 태그 조회
        List<String> tags = loadTagsUseCase.loadTagsByPostIds(List.of(postId))
                .getOrDefault(postId, Collections.emptyList());

        // 7. 좋아요 여부 확인
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = postLikePersistencePort.findByPostIdAndUserId(postId, currentUserId).isPresent();
        }

        return new PostDetailResponse(
                post.getPostId(),
                post.getUserId(),
                nickname,
                profileImageUrl,
                imageUrls,
                tags,
                post.getCaption(),
                post.getLikeCount(),
                post.getViewCount(),
                isLiked,
                post.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, UUID currentUserId) {
        Post post = postRepositoryPort.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ERR-003: 찾을 수 없는 콘텐츠입니다."));

        if (!post.getUserId().equals(currentUserId)) {
            // Error Code ERR-002: 권한 없음
            throw new IllegalArgumentException("ERR-002: 게시물 삭제 권한이 없습니다.");
        }

        post.softDelete();
        postRepositoryPort.save(post);
    }

    @Override
    @Transactional
    public void updatePost(UpdatePostCommand command) {
        Post post = postRepositoryPort.findById(command.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        if (!post.getUserId().equals(command.getUserId())) {
            throw new IllegalArgumentException("ERR-002: 본인의 게시물만 수정할 수 있습니다.");
        }

        if (command.getTags() != null && command.getTags().size() > 10) {
            throw new IllegalArgumentException("ERR-307-A: 태그는 최대 10개까지 등록 가능합니다.");
        }

        // 엔티티 업데이트 (caption, isPublic)
        post.update(command.getCaption(), command.getIsPublic());
        postRepositoryPort.save(post);

        // 태그 업데이트 기능은 타 도메인 분리 정책으로 인해 임시 비활성화됨
    }
}
