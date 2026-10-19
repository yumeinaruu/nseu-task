package com.yumeinaruu.nseutask.exception;

public class UsernameAlreadyExistsException extends ApiException {

    public UsernameAlreadyExistsException(String username) {
        super("Username " + username + " is already taken", ErrorCode.USERNAME_ALREADY_EXISTS);
    }
}
