package com.simul.post.adapter.in.web;

import com.simul.post.application.dto.CreatePostCommand;
import com.simul.post.application.port.in.CreatePostUseCase;
import com.simul.post.domain.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;

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
}
