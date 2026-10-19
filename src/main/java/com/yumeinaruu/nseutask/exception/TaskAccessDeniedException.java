package com.yumeinaruu.nseutask.exception;

public class TaskAccessDeniedException extends ApiException {

    public TaskAccessDeniedException(Long taskId) {
        super("Only the author or an admin can modify task " + taskId, ErrorCode.TASK_ACCESS_DENIED);
    }
}
