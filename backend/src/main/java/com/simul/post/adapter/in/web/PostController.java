package com.simul.post.adapter.in.web;

import com.simul.post.application.dto.CreatePostCommand;
import com.simul.post.application.dto.FeedPostResponse;
import com.simul.post.application.dto.ToggleLikeResponse;
import com.simul.post.application.port.in.CreatePostUseCase;
import com.simul.post.application.port.in.GetFeedPostsUseCase;
import com.simul.post.application.port.in.TogglePostLikeUseCase;
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

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final GetFeedPostsUseCase getFeedPostsUseCase;
    private final TogglePostLikeUseCase togglePostLikeUseCase;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal UUID userId,
            @RequestPart("images") List<MultipartFile> images,
            @RequestParam(value = "caption", required = false, defaultValue = "") String caption,
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "tags", required = false) List<String> tags,
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

        CreatePostCommand command = CreatePostCommand.builder()
                .userId(userId)
                .images(images)
                .caption(caption)
                .isPublic(isPublic)
                .tags(tags)
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
}

