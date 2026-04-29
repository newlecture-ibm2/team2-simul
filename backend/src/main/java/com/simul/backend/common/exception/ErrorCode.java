package com.simul.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 체계
 * - ERR-0xx: 공통
 * - ERR-1xx: 시착
 * - ERR-2xx: 옷장
 * - ERR-3xx: 피드/태그
 * - ERR-4xx: 신고
 */
public enum ErrorCode {

    // 공통
    INTERNAL_ERROR("ERR-000", HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다"),
    UNAUTHORIZED("ERR-001", HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    INVALID_TOKEN("ERR-001", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED("ERR-001", HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    OAUTH2_FAILED("ERR-001", HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다"),
    FORBIDDEN("ERR-002", HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    RESOURCE_NOT_FOUND("ERR-003", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    USER_NOT_FOUND("ERR-003", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),

    // AI 시착
    CREDIT_EXHAUSTED("ERR-103-A", HttpStatus.UNPROCESSABLE_ENTITY, "오늘의 무료 시착을 모두 사용했습니다"),
    AI_GENERATION_FAILED("ERR-103-B", HttpStatus.INTERNAL_SERVER_ERROR, "AI 생성에 실패했습니다"),

    // 옷장
    CLOSET_LIMIT_EXCEEDED("ERR-201-A", HttpStatus.UNPROCESSABLE_ENTITY, "옷장이 가득 찼습니다 (최대 200개)"),
    CLOSET_IMAGE_TOO_LARGE("ERR-201-B", HttpStatus.UNPROCESSABLE_ENTITY, "이미지 크기가 10MB를 초과했습니다"),

    // 게시물
    POST_IMAGE_REQUIRED("ERR-301-A", HttpStatus.UNPROCESSABLE_ENTITY, "이미지를 1장 이상 첨부해주세요"),
    POST_IMAGE_TOO_LARGE("ERR-301-B", HttpStatus.UNPROCESSABLE_ENTITY, "이미지 크기가 20MB를 초과했습니다"),
    POST_IMAGE_LIMIT("ERR-301-D", HttpStatus.UNPROCESSABLE_ENTITY, "이미지는 최대 5장까지 첨부할 수 있습니다"),

    // 신고
    DUPLICATE_REPORT("ERR-401-A", HttpStatus.UNPROCESSABLE_ENTITY, "이미 신고한 게시물입니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getCode() { return code; }
    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getMessage() { return message; }
}
