package com.simul.tag.application.service;

import com.simul.tag.application.port.in.AnalyzeImageTagsUseCase;
import com.simul.tag.application.port.out.VisionApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * [Hexagonal - Application Service]
 * 태그 분석 비즈니스 로직을 처리합니다.
 * 외부 Vision API는 VisionApiPort 인터페이스를 통해서만 접근합니다.
 */
@Service
@RequiredArgsConstructor
public class TagAnalysisService implements AnalyzeImageTagsUseCase {

    private final VisionApiPort visionApiPort;

    @Override
    public List<String> analyzeImageTags(MultipartFile image) {
        // 1. 이미지 유효성 기본 검증
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("ERR-101: 이미지 파일이 비어있습니다.");
        }

        // 2. Vision API를 통해 태그 추출 (Adapter가 필터링까지 수행)
        List<String> tags = visionApiPort.analyzeImage(image);

        // 3. 태그 최대 10개 제한 (ERR-307-A 정책)
        if (tags.size() > 10) {
            return tags.subList(0, 10);
        }

        return tags;
    }
}
