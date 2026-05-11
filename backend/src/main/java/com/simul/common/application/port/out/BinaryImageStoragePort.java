package com.simul.common.application.port.out;

public interface BinaryImageStoragePort {
    /**
     * Raw image bytes를 저장하고 접근 가능한 상대 경로(URL)를 반환합니다.
     *
     * @param bytes 이미지 bytes
     * @param mimeType 예: image/png, image/jpeg
     * @param directoryPrefix 도메인별 디렉토리 접두사 (예: "tryon")
     * @return 저장된 이미지의 상대 경로 (예: "/uploads/images/tryon/2026/05/04/uuid.png")
     */
    String upload(byte[] bytes, String mimeType, String directoryPrefix);
}

