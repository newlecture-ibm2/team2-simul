package com.simul.backend.common.exception;

/**
 * 비즈니스 로직 예외
 * - 모든 도메인 예외의 기본 클래스
 * - ErrorCode를 가지고 있어 GlobalExceptionHandler에서 일관된 응답 생성
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
