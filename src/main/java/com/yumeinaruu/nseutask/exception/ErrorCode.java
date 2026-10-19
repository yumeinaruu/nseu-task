package com.yumeinaruu.nseutask.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // проблемы с юзером 1xxx
    USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "User not found"),
    USERNAME_ALREADY_EXISTS(1002, HttpStatus.CONFLICT, "Username is already taken"),
    EMAIL_ALREADY_EXISTS(1003, HttpStatus.CONFLICT, "Email is already registered"),
    INVALID_CREDENTIALS(1004, HttpStatus.UNAUTHORIZED, "Invalid username or password"),

    // проблемы с таской 2xxx
    TASK_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "Task not found"),
    TASK_ACCESS_DENIED(2002, HttpStatus.FORBIDDEN, "Access to the task is denied"),

    // просто проблемы с 3xxx
    INTERNAL_ERROR(3001, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    VALIDATION_ERROR(3002, HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(3003, HttpStatus.UNAUTHORIZED, "Authentication is required"),
    ACCESS_DENIED(3004, HttpStatus.FORBIDDEN, "Access is denied");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}
