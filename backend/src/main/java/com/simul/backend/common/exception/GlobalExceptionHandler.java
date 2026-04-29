package com.simul.backend.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 * - BusinessException → ErrorCode 기반 응답
 * - 기타 예외 → ERR-000 Internal Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse response = ErrorResponse.of(
            ErrorCode.INTERNAL_ERROR,
            e.getMessage()
        );
        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
            .body(response);
    }
}
