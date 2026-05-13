package com.simul.post.adapter.in.web;

import com.simul.post.application.dto.CommentResponse;
import com.simul.post.application.dto.CreateCommentCommand;
import com.simul.post.application.port.in.CreateCommentUseCase;
import com.simul.post.application.port.in.DeleteCommentUseCase;
import com.simul.post.application.port.in.LoadCommentUseCase;
import com.simul.post.application.port.in.UpdateCommentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 관련 API")
public class CommentController {

    private final LoadCommentUseCase loadCommentUseCase;
    private final CreateCommentUseCase createCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;

    @Operation(summary = "댓글 목록 조회", description = "특정 게시물의 댓글 목록을 조회합니다. 대댓글은 replies로 포함됩니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(loadCommentUseCase.loadComments(postId, pageable));
    }

    @Operation(summary = "댓글 작성", description = "게시물에 댓글을 작성합니다. 대댓글인 경우 parentCommentId를 전달합니다.")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId,
            @RequestBody CreateCommentCommand command) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createCommentUseCase.createComment(postId, userId, command));
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제(소프트 딜리트)합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID commentId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }
        deleteCommentUseCase.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID commentId,
            @RequestBody Map<String, String> request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }
        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "error_code", "ERR-305-C",
                    "message", "댓글 내용을 입력해주세요."
            ));
        }
        if (content.length() > 200) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "error_code", "ERR-305-B",
                    "message", "댓글은 200자를 초과할 수 없습니다."
            ));
        }
        return ResponseEntity.ok(updateCommentUseCase.updateComment(commentId, userId, content));
    }
}
