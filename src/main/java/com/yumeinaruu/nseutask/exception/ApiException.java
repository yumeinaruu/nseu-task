package com.yumeinaruu.nseutask.exception;

import lombok.Getter;

/**
 * Базовая ошибка, от нее должны наследоваться все исключения. Каждое исключение в системе знает о своем
 * статус коде и просто коде внутреннем, который должен клиенту отображаться в случае проблем
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    protected ApiException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ApiException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
