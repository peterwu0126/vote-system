package com.example.vote.common;

/**
 * 自訂業務例外 (共用層)
 * Service 層在業務規則不符時拋出此例外，由 GlobalExceptionHandler 統一處理。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
