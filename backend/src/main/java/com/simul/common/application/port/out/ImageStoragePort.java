package com.simul.common.application.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStoragePort {
    /**
     * 이미지를 저장하고 접근 가능한 상대 경로(URL)를 반환합니다.
     *
     * @param file 저장할 이미지 파일
     * @param directoryPrefix 도메인별 디렉토리 접두사 (예: "tryon", "closet", "post")
     * @return 저장된 이미지의 상대 경로 (예: "/uploads/images/tryon/2026/05/04/uuid.jpg")
     */
    String uploadImage(MultipartFile file, String directoryPrefix);

    /**
     * 이미지를 삭제합니다.
     *
     * @param imageUrl 삭제할 이미지의 URL 경로
     */
    void deleteImage(String imageUrl);
}
