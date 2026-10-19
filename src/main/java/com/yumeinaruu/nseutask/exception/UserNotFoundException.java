package com.yumeinaruu.nseutask.exception;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(Long userId) {
        super("User " + userId + " not found", ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String username) {
        super("User " + username + " not found", ErrorCode.USER_NOT_FOUND);
    }
}
