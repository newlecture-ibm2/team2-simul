package com.simul.tag.adapter.in.web;

import com.simul.tag.application.port.in.AnalyzeImageTagsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import com.simul.tag.application.service.TagRateLimiterService;
import io.github.bucket4j.Bucket;

/**
 * [Hexagonal - Inbound Adapter (Web)]
 * POST /tags/analyze
 * 이미지를 받아 Google Vision API 기반 패션 태그 추천 목록을 반환합니다.
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final AnalyzeImageTagsUseCase analyzeImageTagsUseCase;
    private final TagRateLimiterService tagRateLimiterService;

    /**
     * 이미지를 분석하여 추천 태그를 반환합니다.
     * @param image multipart/form-data 이미지 파일
     * @return 추천 태그 리스트
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeImageTags(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UUID userId) {

        Bucket bucket = tagRateLimiterService.resolveBucket(userId);
        if (!bucket.tryConsume(1)) {
            // 429 Too Many Requests 반환
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "error_code", "ERR-307-C",
                    "message", "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.",
                    "detail", "Rate limit exceeded (max 30 requests per minute)"
            ));
        }

        try {
            List<String> tags = analyzeImageTagsUseCase.analyzeImageTags(image);
            return ResponseEntity.ok(Map.of(
                    "recommended_tags", tags,
                    "count", tags.size()
            ));
        } catch (RuntimeException e) {
            // ERR-307-B: Vision API 실패 시 Graceful Degradation
            return ResponseEntity.internalServerError().body(Map.of(
                    "error_code", "ERR-307-B",
                    "message", "자동 태그 추출에 실패했어요. 수동으로 입력해주세요.",
                    "detail", e.getMessage()
            ));
        }
    }
}
