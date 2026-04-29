package com.simul.tag.adapter.in.web;

import com.simul.tag.application.port.in.AnalyzeImageTagsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * [Hexagonal - Inbound Adapter (Web)]
 * POST /api/tags/analyze
 * 이미지를 받아 Google Vision API 기반 패션 태그 추천 목록을 반환합니다.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final AnalyzeImageTagsUseCase analyzeImageTagsUseCase;

    /**
     * 이미지를 분석하여 추천 태그를 반환합니다.
     * @param image multipart/form-data 이미지 파일
     * @return 추천 태그 리스트
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeImageTags(
            @RequestParam("image") MultipartFile image) {

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
