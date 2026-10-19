package com.yumeinaruu.nseutask.exception;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException() {
        super("Email is already registered", ErrorCode.EMAIL_ALREADY_EXISTS);
    }

}
