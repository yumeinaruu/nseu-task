package com.yumeinaruu.nseutask.exception;

public class TaskNotFoundException extends ApiException {

    public TaskNotFoundException(Long taskId) {
        super("Task " + taskId + " not found", ErrorCode.TASK_NOT_FOUND);
    }
}
