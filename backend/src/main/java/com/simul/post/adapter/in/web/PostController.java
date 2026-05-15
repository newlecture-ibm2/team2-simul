package com.simul.post.adapter.in.web;

import com.simul.post.application.dto.*;
import com.simul.post.application.port.in.*;
import com.simul.post.domain.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final GetFeedPostsUseCase getFeedPostsUseCase;
    private final TogglePostLikeUseCase togglePostLikeUseCase;
    private final GetPostDetailUseCase getPostDetailUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final ReportPostUseCase reportPostUseCase;
    private final GetPostLikesUseCase getPostLikesUseCase;
    private final GetUserPostsUseCase getUserPostsUseCase;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal UUID userId,
            @RequestPart("images") List<MultipartFile> images,
            @RequestParam(value = "caption", required = false, defaultValue = "") String caption,
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "newImageTagsMapJson", required = false) String newImageTagsMapJson,
            @RequestParam(value = "manualTags", required = false) List<String> manualTags,
            @RequestParam(value = "baseImageId", required = false) UUID baseImageId,
            @RequestParam(value = "itemId", required = false) UUID itemId
    ) {
        // userId가 null인 경우는 Spring Security 설정상 로그인하지 않은 사용자 접근 차단으로 막혀야 함
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        Map<Integer, List<String>> newImageTagsMap = null;
        if (newImageTagsMapJson != null && !newImageTagsMapJson.isEmpty()) {
            try {
                newImageTagsMap = new ObjectMapper().readValue(newImageTagsMapJson, new TypeReference<Map<Integer, List<String>>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CreatePostCommand command = CreatePostCommand.builder()
                .userId(userId)
                .images(images)
                .caption(caption)
                .isPublic(isPublic)
                .newImageTagsMap(newImageTagsMap)
                .manualTags(manualTags)
                .baseImageId(baseImageId)
                .itemId(itemId)
                .build();

        Post post = createPostUseCase.createPost(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Post created successfully",
                "postId", post.getPostId()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getFeedPosts(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(value = "tab", defaultValue = "all") String tab,
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<FeedPostResponse> posts = getFeedPostsUseCase.getFeedPosts(userId, tab, sort, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 좋아요 토글 (POST /posts/{postId}/likes)
     * - 로그인 필수 (비로그인 시 ERR-304-A)
     * - 좋아요 ↔ 취소 토글 동작
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> toggleLike(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId
    ) {
        // 비로그인 사용자 차단 (ERR-304-A)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-304-A",
                    "message", "좋아요를 누르려면 로그인이 필요합니다."
            ));
        }

        ToggleLikeResponse response = togglePostLikeUseCase.toggleLike(postId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시물 좋아요 목록 조회 (GET /posts/{postId}/likes)
     * - 누구나 열람 가능
     */
    @GetMapping("/{postId}/likes")
    public ResponseEntity<Page<LikeUserResponse>> getPostLikes(
            @PathVariable UUID postId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<LikeUserResponse> likes = getPostLikesUseCase.getPostLikes(postId, pageable);
        return ResponseEntity.ok(likes);
    }

    @PostMapping("/{postId}/report")
    public ResponseEntity<?> reportPost(
            @AuthenticationPrincipal UUID reporterId,
            @PathVariable UUID postId,
            @RequestBody ReportPostCommand command) {
        if (reporterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        reportPostUseCase.reportPost(postId, reporterId, command.getReason());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "게시물 신고가 접수되었습니다."
        ));
    }

    /**
     * 게시물 상세 조회 (GET /posts/{postId})
     * - 공개 게시물: 누구나 열람 가능 (비로그인 포함)
     * - 비공개 게시물: 작성자 본인만 열람 가능
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId
    ) {
        try {
            com.simul.post.application.dto.PostDetailResponse response = 
                getPostDetailUseCase.getPostDetail(postId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ERR-002")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error_code", "ERR-002",
                        "message", e.getMessage()
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error_code", "ERR-003",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 게시물 삭제 (DELETE /posts/{postId})
     * - 로그인 필수
     * - 작성자 본인만 삭제 가능
     * - 물리 삭제가 아닌 Soft Delete 처리 (deleted_at 업데이트)
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        try {
            deletePostUseCase.deletePost(postId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ERR-002")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error_code", "ERR-002",
                        "message", e.getMessage()
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error_code", "ERR-003",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 게시물 수정 (PATCH /posts/{postId})
     * - 로그인 필수
     * - 작성자 본인만 수정 가능
     * - 이미지 제외 내용/공개여부/태그만 수정 지원
     */
    @PatchMapping(value = "/{postId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId,
            @RequestParam(value = "caption", required = false, defaultValue = "") String caption,
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "existingImageTagsMapJson", required = false) String existingImageTagsMapJson,
            @RequestParam(value = "newImageTagsMapJson", required = false) String newImageTagsMapJson,
            @RequestParam(value = "manualTags", required = false) List<String> manualTags,
            @RequestParam(value = "existingImageUrls", required = false) List<String> existingImageUrls,
            @RequestParam(value = "newImages", required = false) List<org.springframework.web.multipart.MultipartFile> newImages,
            @RequestParam(value = "imageOrderJson", required = false) String imageOrderJson
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        try {
            System.out.println("[POST UPDATE] postId=" + postId + ", userId=" + userId);
            System.out.println("[POST UPDATE] caption=" + caption + ", isPublic=" + isPublic);
            System.out.println("[POST UPDATE] manualTags=" + manualTags);
            System.out.println("[POST UPDATE] existingImageUrls=" + existingImageUrls);
            System.out.println("[POST UPDATE] newImages count=" + (newImages != null ? newImages.size() : "null"));

            Map<String, List<String>> existingImageTagsMap = null;
            Map<Integer, List<String>> newImageTagsMap = null;
            ObjectMapper mapper = new ObjectMapper();
            if (existingImageTagsMapJson != null && !existingImageTagsMapJson.isEmpty()) {
                existingImageTagsMap = mapper.readValue(existingImageTagsMapJson, new TypeReference<Map<String, List<String>>>() {});
            }
            if (newImageTagsMapJson != null && !newImageTagsMapJson.isEmpty()) {
                newImageTagsMap = mapper.readValue(newImageTagsMapJson, new TypeReference<Map<Integer, List<String>>>() {});
            }
            
            List<String> imageOrder = null;
            if (imageOrderJson != null && !imageOrderJson.isEmpty()) {
                imageOrder = mapper.readValue(imageOrderJson, new TypeReference<List<String>>() {});
            }

            UpdatePostCommand command = UpdatePostCommand.builder()
                    .postId(postId)
                    .userId(userId)
                    .caption(caption)
                    .isPublic(isPublic)
                    .existingImageTagsMap(existingImageTagsMap)
                    .newImageTagsMap(newImageTagsMap)
                    .manualTags(manualTags)
                    .existingImageUrls(existingImageUrls)
                    .newImages(newImages)
                    .imageOrder(imageOrder)
                    .build();
            updatePostUseCase.updatePost(command);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            System.err.println("[POST UPDATE ERROR - IllegalArgument] " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("ERR-002")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error_code", "ERR-002",
                        "message", e.getMessage()
                ));
            } else if (e.getMessage().contains("ERR-307")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "error_code", "ERR-307-A",
                        "message", e.getMessage()
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error_code", "ERR-003",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[POST UPDATE ERROR - Unexpected] " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error_code", "ERR-000",
                    "message", "게시물 수정 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 특정 사용자의 게시물 목록 조회 (GET /posts/users/{userId})
     * - 본인: 공개 + 비공개 모두 조회
     * - 타인: 공개 게시물만 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<FeedPostResponse>> getUserPosts(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<FeedPostResponse> posts = getUserPostsUseCase.getUserPosts(userId, currentUserId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 내가 좋아요한 게시물 목록 조회 (GET /posts/liked)
     * - 로그인 필수
     */
    @GetMapping("/liked")
    public ResponseEntity<?> getLikedPosts(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        Page<FeedPostResponse> posts = getUserPostsUseCase.getLikedPosts(userId, pageable);
        return ResponseEntity.ok(posts);
    }
}

