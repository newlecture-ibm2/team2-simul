package com.simul.tag.application.port.out;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * [Hexagonal - Outbound Port]
 * Tag 도메인이 외부 이미지 분석 서비스에 의존하기 위한 인터페이스.
 * 구현체가 Google이든 Naver든, 핵심 로직은 이 인터페이스만 알면 됩니다.
 */
public interface VisionApiPort {
    /**
     * 이미지를 분석하여 패션 관련 추천 태그 목록을 반환합니다.
     * @param image 업로드된 이미지 파일
     * @return 필터링된 태그 목록 (예: ["Jeans", "Denim", "Streetwear"])
     */
    List<String> analyzeImage(MultipartFile image);
}
