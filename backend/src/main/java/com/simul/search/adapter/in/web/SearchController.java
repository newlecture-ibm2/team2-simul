package com.simul.search.adapter.in.web;

import com.simul.post.application.dto.FeedPostResponse;
import com.simul.post.application.port.in.SearchPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * [Hexagonal - Inbound Adapter (Web)]
 * GET /search
 * 태그 및 캡션 기반의 통합 검색 기능을 오케스트레이션합니다.
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchPostUseCase searchPostUseCase;

    /**
     * 통합 검색
     * @param query 검색어 (필수)
     * @param type 검색 유형 (tag, caption, all) 기본값은 all
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 검색된 피드 포스트 목록 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<FeedPostResponse>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Page<FeedPostResponse> responses = searchPostUseCase.searchPosts(
                query.trim(),
                type,
                userId,
                PageRequest.of(page, size)
        );

        return ResponseEntity.ok(responses);
    }
}
