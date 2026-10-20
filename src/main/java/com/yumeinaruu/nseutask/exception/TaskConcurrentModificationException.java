package com.yumeinaruu.nseutask.exception;

public class TaskConcurrentModificationException extends ApiException {

    public TaskConcurrentModificationException(Long taskId) {
        super("Task " + taskId + " was modified concurrently", ErrorCode.TASK_CONCURRENT_MODIFICATION);
    }
}
