package com.simul.tag.application.port.in;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * [Hexagonal - Inbound Port]
 * 태그 분석 기능을 외부(Controller)에 노출하는 유스케이스 인터페이스.
 */
public interface AnalyzeImageTagsUseCase {
    /**
     * 이미지를 분석하여 패션 관련 추천 태그 목록을 반환합니다.
     * @param image 업로드된 이미지 파일
     * @return 추천 태그 리스트
     */
    List<String> analyzeImageTags(MultipartFile image);
}
