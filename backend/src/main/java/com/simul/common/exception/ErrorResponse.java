package com.simul.common.exception;

/**
 * 공통 에러 응답 형식
 * { "error_code": "ERR-001", "message": "인증이 필요합니다", "detail": "..." }
 */
public record ErrorResponse(
    String error_code,
    String message,
    String detail
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), detail);
    }
}
