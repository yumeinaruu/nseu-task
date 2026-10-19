package com.yumeinaruu.nseutask.exception;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid username or password", ErrorCode.INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(Throwable cause) {
        super("Invalid username or password", ErrorCode.INVALID_CREDENTIALS, cause);
    }
}
