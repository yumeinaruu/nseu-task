package com.yumeinaruu.nseutask.exception;

import com.yumeinaruu.nseutask.entity.enums.TaskStatus;

public class InvalidStatusTransitionException extends ApiException {

    public InvalidStatusTransitionException(Long taskId, TaskStatus from, TaskStatus to) {
        super("Task " + taskId + " cannot move from " + from + " to " + to, ErrorCode.INVALID_STATUS_TRANSITION);
    }
}
